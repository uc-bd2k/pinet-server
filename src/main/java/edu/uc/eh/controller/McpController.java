package edu.uc.eh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.uc.eh.service.*;
import edu.uc.eh.uniprot.Uniprot;
import edu.uc.eh.uniprot.UniprotRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class McpController {

    private static final String PROTOCOL_VERSION = "2024-11-05";
    private static final String SERVER_NAME = "pinet-mcp";
    private static final String SERVER_VERSION = "0.1.0";

    private final PeptideSearchService peptideSearchService;
    private final PeptideWithValueService peptideWithValueService;
    private final UniprotRepository uniprotRepository;
    private final UniprotService2 uniprotService2;
    private final PCGService pcgService;
    private final PrideService prideService;
    private final PhosphoService phosphoService;
    private final PhosphoServiceV2 phosphoServiceV2;
    private final FastaService fastaService;
    private final String pinetApiBaseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public McpController(PeptideSearchService peptideSearchService,
                         PeptideWithValueService peptideWithValueService,
                         UniprotRepository uniprotRepository,
                         UniprotService2 uniprotService2,
                         PCGService pcgService,
                         PrideService prideService,
                         PhosphoService phosphoService,
                         PhosphoServiceV2 phosphoServiceV2,
                         FastaService fastaService,
                         @Value("${pinet.apiBaseUrl:https://www.pinet-server.org/pinet/api}") String pinetApiBaseUrl) {
        this.peptideSearchService = peptideSearchService;
        this.peptideWithValueService = peptideWithValueService;
        this.uniprotRepository = uniprotRepository;
        this.uniprotService2 = uniprotService2;
        this.pcgService = pcgService;
        this.prideService = prideService;
        this.phosphoService = phosphoService;
        this.phosphoServiceV2 = phosphoServiceV2;
        this.fastaService = fastaService;
        this.pinetApiBaseUrl = pinetApiBaseUrl;
    }

    @GetMapping(value = "api/mcp")
    public ResponseEntity<String> mcpInfo(@RequestHeader(value = "Accept", required = false) String acceptHeader) throws Exception {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("name", SERVER_NAME);
        info.put("version", SERVER_VERSION);
        info.put("protocolVersion", PROTOCOL_VERSION);
        info.put("transport", "json-rpc over HTTP POST");
        info.put("toolsEndpoint", pinetApiBaseUrl + "/mcp");
        info.put("tools", buildTools());
        return mcpResponse(info, acceptHeader);
    }

    @PostMapping(value = "api/mcp")
    public ResponseEntity<?> handleMcp(@RequestBody Map<String, Object> request,
                                       @RequestHeader(value = "Accept", required = false) String acceptHeader) {
        Object id = request.get("id");
        String method = asString(request.get("method"));
        Map<String, Object> params = asMap(request.get("params"));

        try {
            if ("initialize".equals(method)) {
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("protocolVersion", PROTOCOL_VERSION);
                result.put("serverInfo", mapOf("name", SERVER_NAME, "version", SERVER_VERSION));
                result.put("capabilities", mapOf("tools", Collections.emptyMap()));
                return mcpResponse(success(id, result), acceptHeader);
            }

            if ("notifications/initialized".equals(method)) {
                return ResponseEntity.noContent().build();
            }

            if ("tools/list".equals(method)) {
                return mcpResponse(success(id, mapOf("tools", buildTools())), acceptHeader);
            }

            if ("tools/call".equals(method)) {
                String name = asString(params.get("name"));
                Map<String, Object> arguments = asMap(params.get("arguments"));
                return mcpResponse(success(id, callTool(name, arguments)), acceptHeader);
            }

            return mcpResponse(error(id, -32601, "Method not found: " + method), acceptHeader);
        } catch (Exception e) {
            try {
                return mcpResponse(error(id, -32000, e.getMessage()), acceptHeader);
            } catch (Exception serializationError) {
                return ResponseEntity.internalServerError()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"jsonrpc\":\"2.0\",\"id\":" + String.valueOf(id) + ",\"error\":{\"code\":-32000,\"message\":\"" +
                                escapeJson(e.getMessage()) + "\"}}");
            }
        }
    }

    private ResponseEntity<String> mcpResponse(Object payload, String acceptHeader) throws Exception {
        String json = objectMapper.writeValueAsString(payload);
        if (wantsEventStream(acceptHeader)) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_EVENT_STREAM)
                    .body("data: " + json + "\n\n");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }

    private boolean wantsEventStream(String acceptHeader) {
        return acceptHeader != null && acceptHeader.contains(MediaType.TEXT_EVENT_STREAM_VALUE);
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private Map<String, Object> callTool(String name, Map<String, Object> arguments) throws Exception {
        Object payload;
        switch (name) {
            case "runtime_heap":
                payload = runtimeHeapStats();
                break;
            case "peptide_to_protein":
                payload = peptideSearchService.getTable(
                        asString(arguments.get("organism")),
                        csvToArray(arguments.get("peptides"))
                );
                break;
            case "peptide_with_values":
                payload = peptideWithValueService.getTable(
                        asString(arguments.get("organism")),
                        csvToArray(arguments.get("peptides_with_values"))
                );
                break;
            case "uniprot_accession":
                payload = getUniprotAccession(
                        asString(arguments.get("organism")),
                        asString(arguments.get("accession"))
                );
                break;
            case "protein_ptm_by_description":
                payload = prideService.findPTMByDescription(asString(arguments.get("description")));
                break;
            case "protein_ptm_by_mass":
                payload = prideService.findPTMByMassAndDelta(
                        asDouble(arguments.get("mass")),
                        asDouble(arguments.get("delta"))
                );
                break;
            case "check_genes":
                payload = pcgService.checkGenes(csvToArray(arguments.get("gene_list")));
                break;
            case "gene_info":
                payload = pcgService.getTable(csvToIntegerList(arguments.get("gene_positions")));
                break;
            case "phosphoandptm_network":
                payload = phosphoService.computePtmNetwork(
                        asString(arguments.get("organism")),
                        csvToArray(arguments.get("ptm_protein_list"))
                );
                break;
            case "phospho2_network":
                payload = phosphoServiceV2.computePhosphoNetwork(
                        asString(arguments.get("organism")),
                        csvToArray(arguments.get("ptm_protein_list"))
                );
                break;
            case "deepphos_network":
                payload = phosphoService.computeDeepPhosOnlyNetwork(
                        asString(arguments.get("organism")),
                        csvToArray(arguments.get("ptm_protein_list"))
                );
                break;
            default:
                throw new IllegalArgumentException("Unknown tool: " + name);
        }

        return mapOf("content", Collections.singletonList(mapOf(
                "type", "text",
                "text", toPrettyJson(payload)
        )));
    }

    private JSONObject runtimeHeapStats() {
        Runtime runtime = Runtime.getRuntime();
        long committedBytes = runtime.totalMemory();
        long freeBytes = runtime.freeMemory();
        long usedBytes = committedBytes - freeBytes;
        long maxBytes = runtime.maxMemory();
        long mib = 1024L * 1024L;

        JSONObject heap = new JSONObject();
        heap.put("usedBytes", usedBytes);
        heap.put("committedBytes", committedBytes);
        heap.put("maxBytes", maxBytes);
        heap.put("usedMiB", usedBytes / mib);
        heap.put("committedMiB", committedBytes / mib);
        heap.put("maxMiB", maxBytes / mib);
        return heap;
    }

    private JSONObject getUniprotAccession(String organism, String accession) {
        Uniprot response = new Uniprot();
        JSONObject responseUniprot = new JSONObject();
        String canonicalAccession = accession.split("-")[0];

        try {
            response = uniprotRepository.findByAccession(canonicalAccession);
            if (response != null) {
                responseUniprot = response.toJson();
            }
        } catch (Exception ignored) {
        }

        if (responseUniprot.isEmpty()) {
            try {
                responseUniprot = uniprotService2.getTable(organism, canonicalAccession);
            } catch (Exception ignored) {
            }
        }

        if (!canonicalAccession.equals(accession)) {
            try {
                JSONObject fastaResult = fastaService.getTable(accession);
                String fastaSeq = (String) fastaResult.get("sequence");
                responseUniprot.put("sequence", fastaSeq);
                responseUniprot.put("length", fastaSeq.length());
            } catch (Exception ignored) {
            }
        }

        return responseUniprot;
    }

    private List<Map<String, Object>> buildTools() {
        List<Map<String, Object>> tools = new ArrayList<>();
        tools.add(tool("runtime_heap", "Get live JVM heap statistics from the PiNET server.",
                schema(Collections.emptyMap(), Collections.emptyList())));
        tools.add(tool("peptide_to_protein", "Map peptides to protein accessions for an organism.",
                schema(mapOf(
                                "organism", integerSchema("NCBI taxonomy id, for example 9606."),
                                "peptides", stringSchema("Peptide string or comma-separated peptide list.")
                        ),
                        Arrays.asList("organism", "peptides"))));
        tools.add(tool("peptide_with_values", "Map peptides with associated numeric values for an organism.",
                schema(mapOf(
                                "organism", integerSchema("NCBI taxonomy id, for example 9606."),
                                "peptides_with_values", stringSchema("Input exactly as expected by /api/peptidewithvalue.")
                        ),
                        Arrays.asList("organism", "peptides_with_values"))));
        tools.add(tool("uniprot_accession", "Fetch UniProt annotation for a protein accession and organism.",
                schema(mapOf(
                                "organism", integerSchema("NCBI taxonomy id, for example 9606."),
                                "accession", stringSchema("Canonical or isoform UniProt accession.")
                        ),
                        Arrays.asList("organism", "accession"))));
        tools.add(tool("protein_ptm_by_description", "Look up PTM ontology entries by description, for example phospho.",
                schema(mapOf("description", stringSchema("PTM description, for example phospho.")),
                        Collections.singletonList("description"))));
        tools.add(tool("protein_ptm_by_mass", "Look up PTM ontology entries by mass and delta.",
                schema(mapOf(
                                "mass", mapOf("type", "number", "description", "Observed PTM mass."),
                                "delta", mapOf("type", "number", "description", "Mass tolerance delta.")
                        ),
                        Arrays.asList("mass", "delta"))));
        tools.add(tool("check_genes", "Map gene symbols to PCG gene positions.",
                schema(mapOf("gene_list", stringSchema("Comma-separated gene symbols.")),
                        Collections.singletonList("gene_list"))));
        tools.add(tool("gene_info", "Fetch PCG gene metadata by gene positions returned from check_genes.",
                schema(mapOf("gene_positions", stringSchema("Comma-separated gene positions.")),
                        Collections.singletonList("gene_positions"))));
        tools.add(tool("phosphoandptm_network", "Build phospho and PTM network results for an organism and PTM protein list.",
                schema(mapOf(
                                "organism", integerSchema("NCBI taxonomy id, for example 9606."),
                                "ptm_protein_list", stringSchema("Comma-separated PTM protein entries like P51812{[pS]@369}.")
                        ),
                        Arrays.asList("organism", "ptm_protein_list"))));
        tools.add(tool("phospho2_network", "Build phospho2 network results for an organism and PTM protein list.",
                schema(mapOf(
                                "organism", integerSchema("NCBI taxonomy id, for example 9606."),
                                "ptm_protein_list", stringSchema("Comma-separated PTM protein entries like P51812{[pS]@369}.")
                        ),
                        Arrays.asList("organism", "ptm_protein_list"))));
        tools.add(tool("deepphos_network", "Build DeepPhos-only network results for an organism and PTM protein list.",
                schema(mapOf(
                                "organism", integerSchema("NCBI taxonomy id, for example 9606."),
                                "ptm_protein_list", stringSchema("Comma-separated PTM protein entries like P51812{[pS]@369}.")
                        ),
                        Arrays.asList("organism", "ptm_protein_list"))));
        return tools;
    }

    private Map<String, Object> tool(String name, String description, Map<String, Object> inputSchema) {
        return mapOf("name", name, "description", description, "inputSchema", inputSchema);
    }

    private Map<String, Object> schema(Map<String, Object> properties, List<String> required) {
        return mapOf(
                "type", "object",
                "properties", properties,
                "required", required,
                "additionalProperties", Boolean.FALSE
        );
    }

    private Map<String, Object> stringSchema(String description) {
        return mapOf("type", "string", "description", description);
    }

    private Map<String, Object> integerSchema(String description) {
        return mapOf("type", "integer", "description", description);
    }

    private Map<String, Object> success(Object id, Object result) {
        return mapOf("jsonrpc", "2.0", "id", id, "result", result);
    }

    private Map<String, Object> error(Object id, int code, String message) {
        return mapOf("jsonrpc", "2.0", "id", id, "error", mapOf("code", code, "message", message));
    }

    private String[] csvToArray(Object value) {
        String input = asString(value);
        if (input.isEmpty()) {
            return new String[0];
        }
        return input.split(",");
    }

    private ArrayList<Integer> csvToIntegerList(Object value) {
        ArrayList<Integer> out = new ArrayList<>();
        for (String part : csvToArray(value)) {
            if (!part.trim().isEmpty()) {
                out.add(Integer.parseInt(part.trim()));
            }
        }
        return out;
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Double asDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.valueOf(asString(value));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return Collections.emptyMap();
    }

    private String toPrettyJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof JSONObject || value instanceof JSONArray) {
            return value.toString();
        }
        return String.valueOf(value);
    }

    private Map<String, Object> mapOf(Object... kv) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            map.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        return map;
    }
}
