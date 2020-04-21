package edu.uc.eh.service;

import edu.uc.eh.utils.UtilsNetwork;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
//import org.apache.tomcat.jni.File;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


//import org.apache.http.HttpResponse;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//        import org.apache.http.client.methods.HttpGet;
//        import org.apache.http.client.methods.HttpPost;
//        import org.apache.http.impl.client.DefaultHttpClient;
//        import org.apache.http.message.BasicNameValuePair;

@Service
public class EnrichrServiceV2 {

    private static final Logger log = LoggerFactory.getLogger(EnrichrServiceV2.class);

    @Value("${urls.enrichrGetListId}")
    String enrichrGetListId;

    @Value("${urls.enrichrNetwork}")
    String enrichrNetwork;

    private final String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) throws Exception {

        EnrichrServiceV2 http = new EnrichrServiceV2();

        System.out.println("Testing 1 - Send Http GET request");
        http.sendGet();

//        System.out.println("\nTesting 2 - Send Http POST request");
//        http.getListId(String[] geneList);

        System.out.println("\nTesting 3 - Send Http View request");
        //http.view();


    }

    // HTTP GET request
    public void sendGet() throws Exception {

        String url = "http://www.google.com/search?q=developer";

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);


        // add request header
        request.addHeader("User-Agent", USER_AGENT);

        HttpResponse response = client.execute(request);

        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " +
                response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        System.out.println(result.toString());

    }
    private static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    // HTTP POST request
    public int getListId(String[] geneList) throws Exception {
        String url = enrichrGetListId;
        //String url = "https://selfsolve.apple.com/wcResults.do";
        HttpClient client = HttpClientBuilder.create().build();
        int listId;
        //JSONObject EnrichrJSON = new JSONObject();
        listId = 1;
        JSONObject geneResultJSON = new JSONObject();
        HttpPost post = new HttpPost(url);

        // add header

        post.setHeader("User-Agent", USER_AGENT);

//        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
//        urlParameters.add(new BasicNameValuePair("sn", "C02G8416DRJM"));
//        urlParameters.add(new BasicNameValuePair("cn", ""));
//        urlParameters.add(new BasicNameValuePair("locale", ""));
//        urlParameters.add(new BasicNameValuePair("caller", ""));
//        urlParameters.add(new BasicNameValuePair("num", "12345"));





        //--------
        //File payload = new File("/Users/shamsabz/Dropbox/PLNconverter-v2/genes.txt");
        String randomNumber = String.valueOf(getRandomNumberInRange(10000, 1000000));

        //File payload5 = new File("genesToIlincs" + randomNumber + ".txt");
        File payload = new File("genesToEnrichr" + randomNumber + ".txt");

        try{
            PrintWriter writer = new PrintWriter(payload, "UTF-8");
            for (int i = 0; i < geneList.length; i++) {
                writer.println(geneList[i]);
                }
//            writer.println("BRAF");
//            writer.println("TP53");
//            writer.println("OCIAD1");
//            writer.println("NCOR2");
            writer.close();
        } catch (IOException e) {
            // do something
        }

        ContentType plainAsciiContentType = ContentType.create("text/plain", Consts.ASCII);
        HttpEntity entity = MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .addPart("list", new FileBody(payload))
//                .addPart("username", new StringBody("bond", plainAsciiContentType))
//                .addPart("password", new StringBody("vesper", plainAsciiContentType))
                .build();
        HttpPost httpPost = new HttpPost(enrichrGetListId);
        httpPost.setEntity(entity);

        HttpResponse response = client.execute(httpPost);
        //--------

//++++++++++
//        HttpEntity entity = MultipartEntityBuilder
//                .create()
//                .addTextBody("number", "5555555555")
//                .addTextBody("clip", "rickroll")
//                .addBinaryBody("upload_file", new File(""), ContentType.create("application/octet-stream"), "filename")
//                .addTextBody("list", "BRAF")
//                .build();
//
//        HttpPost httpPost = new HttpPost("http://amp.pharm.mssm.edu/Enrichr/addList");
//        httpPost.setEntity(entity);
//        HttpResponse response = client.execute(httpPost);
//        HttpEntity result = response.getEntity();
        //+++++++++++++





//        post.setEntity(new UrlEncodedFormEntity(urlParameters));
//
//        HttpResponse response = client.execute(post);
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + post.getEntity());
        System.out.println("Response Code : " +
                response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        System.out.println("results: ");
        System.out.println(result.toString());

        JSONParser parser = new JSONParser();
        geneResultJSON = (JSONObject) parser.parse(result.toString());

        listId = ((Long) geneResultJSON.get("userListId")).intValue();
        //EnrichrJSON = new JSONObject(result.toString());
        //EnrichrJSON = result;

        return listId;

    }


    public JSONObject computeNetwork(String pathwayClass, long listId) {

        int idx = 0;
        List<String> pathways = new ArrayList<String>();

        System.out.println("computing network for gene-pathway association");
        if (pathwayClass.equals("pathways")){


        pathways.add("KEGG_2015");
        pathways.add("WikiPathways_2015");
        pathways.add("Panther_2015");
        pathways.add("KEGG_2016");
        pathways.add("WikiPathways_2016");
        pathways.add("ARCHS4_Kinases_Coexp");
        pathways.add("Reactome_2016");
        pathways.add("BioCarta_2016");
        pathways.add("Humancyc_2016");
        pathways.add("NCI-Nature_2016");
        pathways.add("Panther_2016");
        pathways.add("BioPlex_2017");

        pathways.add("huMAP");
        pathways.add("PPI_Hub_Proteins");
        pathways.add("KEA_2015");
        pathways.add("LINCS_L1000_Kinase_Perturbations_down");
        pathways.add("LINCS_L1000_Kinase_Perturbations_up");
        pathways.add("Kinase_Perturbations_from_GEO_down");
        pathways.add("Kinase_Perturbations_from_GEO_up");
        pathways.add("NURSA_Human_Endogenous_Complexome");
        pathways.add("CORUM");
        pathways.add("SILAC_Phosphoproteomics");
        pathways.add("Phosphatase_Substrates_from_DEPOD");
//        }
//        if (pathwayClass.equals("diseaseDrugs")){
        //List<String> pathways = new ArrayList<>(Arrays.asList(
        pathways.add("LINCS_L1000_Chem_Pert_up");
        pathways.add("LINCS_L1000_Chem_Pert_down");
        pathways.add("LINCS_L1000_Ligand_Perturbations_up");

        pathways.add("LINCS_L1000_Ligand_Perturbations_down");
        pathways.add("ARCHS4_IDG_Coexp");
        pathways.add("DrugMatrix");

        pathways.add("Old_CMAP_up");
        pathways.add("Old_CMAP_down");
        pathways.add("GeneSigDB");

        pathways.add("OMIM_Disease");
        pathways.add("OMIM_Expanded");
        pathways.add("VirusMINT");

        pathways.add("MSigDB_Computational");
        pathways.add("MSigDB_Oncogenic_Signatures");
        pathways.add("Virus_Perturbations_from_GEO_up");

        pathways.add("Virus_Perturbations_from_GEO_down");
        pathways.add("Achilles_fitness_increase");
        pathways.add("Achilles_fitness_decrease");
        pathways.add("dbGaP");



        }

        if (pathwayClass.equals("ontology")){

        pathways.add("GO_Cellular_Component_2017b");
        pathways.add("GO_Biological_Process_2017b");
        pathways.add("GO_Molecular_Function_2017b");

        pathways.add("MGI_Mammalian_Phenotype_2017");
        pathways.add("Human_Phenotype_Ontology");
        pathways.add("Jensen_TISSUES");

        pathways.add("Jensen_COMPARTMENTS");
        pathways.add("Jensen_DISEASES");



        }

        if (pathwayClass.equals("transcription")){

        pathways.add("ChEA_2016");
        pathways.add("TRANSFAC_and_JASPAR_PWMs");
        pathways.add("ARCHS4_TFs_Coexp");

        pathways.add("Genome_Browser_PWMs");
        pathways.add("ENCODE_and_ChEA_Consensus_TFs_from_ChIP-X");
        pathways.add("Epigenomics_Roadmap_HM_ChIP-seq");

        pathways.add("TargetScan_microRNA");
        pathways.add("ENCODE_TF_ChIP-seq_2015");
        pathways.add("TF-LOF_Expression_from_GEO");

        pathways.add("ENCODE_Histone_Modifications_2015");
        pathways.add("Transcription_Factor_PPIs");



        }
//
        if (pathwayClass.equals("cellType")){

        pathways.add("Human_Gene_Atlas");
        pathways.add("Mouse_Gene_Atlas");
        pathways.add("ARCHS4_Tissues");

        pathways.add("ARCHS4_Cell-lines");
        pathways.add("Allen_Brain_Atlas_up");
        pathways.add("Allen_Brain_Atlas_down");

        pathways.add("GTEx_Tissue_Sample_Gene_Expression_Profiles_up");
        pathways.add("GTEx_Tissue_Sample_Gene_Expression_Profiles_down");
        pathways.add("Cancer_Cell_Line_Encyclopedia");

        pathways.add("NCI-60_Cancer_Cell_Lines");
        pathways.add("Tissue_Protein_Expression_from_ProteomicsDB");
        pathways.add("Tissue_Protein_Expression_from_Human_Proteome_Map");
        pathways.add("ESCAPE");



        }
        JSONObject newNode = new JSONObject();
        JSONObject newPathNode = new JSONObject();
        JSONObject newEdgeNode = new JSONObject();
        JSONObject network = new JSONObject();
        String response;
        String enrichrUrlEnrichmentInfo;
        JSONObject pathwayInfo = new JSONObject();
        JSONObject pathwayResults = new JSONObject();
        //System.out.println(String.valueOf(pathways));
        for (int index = 0; index < pathways.size(); index++) {
            String queryPathway = pathways.get(index);
            //System.out.println(queryPathway);
            idx = 0;
            JSONObject unique = new JSONObject();
            JSONArray nodes = new JSONArray();
            JSONArray edges = new JSONArray();
            enrichrUrlEnrichmentInfo = String.format(enrichrNetwork, listId, queryPathway);

            try {

                response = UtilsNetwork.getInstance().readUrlXml(enrichrUrlEnrichmentInfo);
                //log.info(response);

                JSONParser parser = new JSONParser();
                pathwayInfo = (JSONObject) parser.parse(response);
                System.out.println(queryPathway);
                //System.out.println(pathwayInfo);
                System.out.println("------------");
                //pathwayResults = (JSONObject) pathwayInfo.get("gene");

                //log.info(geneResults.toJSONString());
//            for (int index=0; index < pathways.size(); index++) {
//                String queryPathway = pathways.get(index);
//                //System.out.println(queryPathway);
//
//                if (geneResults.containsKey(queryPathway)) {
//                    pathwayJson = (JSONArray) geneResults.get(queryPathway);
//                    genePathways.put(queryPathway, pathwayJson);
////                log.info("KEGG_2013");
////                log.info(KEGG_2013.toJSONString());
//                }
//            }



            } catch (Exception e) {

                String msg =  String.format("Error in obtaining pathway info %s", queryPathway);
                log.warn(msg);
                throw new RuntimeException(msg);
            }




//            for (Object key : input.keySet()) {
//                //based on you key types
//                String keyStr = (String) key;
//                JSONObject keyValue = (JSONObject) input.get(keyStr);
//
//                String pathway;
//
//                if (keyValue.containsKey(queryPathway)) {
//                    JSONArray queryList = (JSONArray) keyValue.get(queryPathway);
//                    //JSONArray queryList = (JSONArray) (keyValue.get(queryPathway));
//
//
//                    newNode = generateNode(keyStr, idx, 1);
//                    idx = idx + 1;
//                    nodes.add(newNode);
//
//                    for (int i = 0; i < queryList.size(); i++) {
//                        pathway = (String) queryList.get(i);
//                        if (!unique.containsKey(pathway)) {
//                            newPathNode = generateNode(pathway, idx, 2);
//                            idx = idx + 1;
//
//                            unique.put(pathway, newPathNode);
//                            nodes.add(unique.get(pathway));
//                        }
//                        newEdgeNode = generateEdgeNode((int) newNode.get("idx"), (int) ((JSONObject) unique.get(pathway)).get("idx"));
//                        edges.add(newEdgeNode);
//
//                    }
//                }
//            }
//            JSONObject nodeEdgeJson = new JSONObject();
//            nodeEdgeJson.put("nodes", nodes);
//            nodeEdgeJson.put("edges", edges);
//            //==============================================
//            network.put(queryPathway, nodeEdgeJson);
        }

        System.out.println("Network is computed");
//        System.out.println(network.toString());
        return network;
    }



    public JSONObject computeNetwork2(String pathway, long listId) {

        int idx = 0;
        List<String> pathways = new ArrayList<String>();

        System.out.println("computing network for enrichment analysis");
        System.out.println(listId);
        JSONObject nodeEdgeJson = new JSONObject();
        JSONObject newNode = new JSONObject();
        JSONObject newPathNode = new JSONObject();
        JSONObject newEdgeNode = new JSONObject();
        JSONObject network = new JSONObject();
        String response;
        String enrichrUrlEnrichmentInfo;
        JSONObject pathwayInfo = new JSONObject();
        JSONArray pathwayResults = new JSONArray();
        String gene;
        //System.out.println(String.valueOf(pathways));
//        for (int index = 0; index < pathways.size(); index++) {
            String queryPathway = pathway;
            //System.out.println(queryPathway);
            idx = 0;
            JSONObject unique = new JSONObject();
            JSONArray nodes = new JSONArray();
            JSONArray edges = new JSONArray();
            JSONArray table = new JSONArray();
            enrichrUrlEnrichmentInfo = String.format(enrichrNetwork, Long.toString(listId), queryPathway);

            try {

                response = UtilsNetwork.getInstance().readUrlXml(enrichrUrlEnrichmentInfo);
                //log.info(response);
                idx = 0;
                JSONParser parser = new JSONParser();
                pathwayInfo = (JSONObject) parser.parse(response);
                System.out.println(queryPathway);
                System.out.println(pathwayInfo);
                System.out.println("------------");
                pathwayResults = (JSONArray) pathwayInfo.get(queryPathway);
                int iterator = 0;
                for (int i = 0; i < pathwayResults.size(); i++) {
                    JSONObject tableItem = new JSONObject();
                    JSONArray queryList = (JSONArray) pathwayResults.get(i);
                    String pathwayText = (String) queryList.get(1);
                    Double pValue = (Double) queryList.get(2);
                    JSONArray geneList = (JSONArray) queryList.get(5);
                    Double adjPValue = (Double) queryList.get(6);

                    iterator = iterator + 1;
                    if( iterator < 100) {
                        tableItem.put("pathway", pathwayText);
                        tableItem.put("pValue", pValue);
                        tableItem.put("adjPValue", adjPValue);
                        tableItem.put("genes", geneList);
                        System.out.println(tableItem);
                        table.add(tableItem);
                    }
                    if( iterator < 100)
                    {

                        newNode = generateNode(pathwayText, idx, 2, -1.0*Math.log(pValue));
                        idx = idx + 1;
                        System.out.println(newNode);
                        nodes.add(newNode);

                        for (int j = 0; j < geneList.size(); j++) {
                            gene = (String) geneList.get(j);
                            if (!unique.containsKey(gene)) {
                                newPathNode = generateNode(gene, idx, 1, 1.0);
                                idx = idx + 1;

                                unique.put(gene, newPathNode);
                                nodes.add(unique.get(gene));
                            }
                            newEdgeNode = generateEdgeNode((int) ((JSONObject) unique.get(gene)).get("idx"), (int) newNode.get("idx"));
                            edges.add(newEdgeNode);

                        }

                    }

                }



//                for (Object key : input.keySet()) {
//                    //based on you key types
//                    String keyStr = (String) key;
//                    JSONObject keyValue = (JSONObject) input.get(keyStr);
//
//                    String pathway;
//
//                    if (keyValue.containsKey(queryPathway)) {
//                        JSONArray queryList = (JSONArray) keyValue.get(queryPathway);
//                        //JSONArray queryList = (JSONArray) (keyValue.get(queryPathway));
//
//
//                        newNode = generateNode(keyStr, idx, 1);
//                        idx = idx + 1;
//                        nodes.add(newNode);
//
//                        for (int i = 0; i < queryList.size(); i++) {
//                            pathway = (String) queryList.get(i);
//                            if (!unique.containsKey(pathway)) {
//                                newPathNode = generateNode(pathway, idx, 2);
//                                idx = idx + 1;
//
//                                unique.put(pathway, newPathNode);
//                                nodes.add(unique.get(pathway));
//                            }
//                            newEdgeNode = generateEdgeNode((int) newNode.get("idx"), (int) ((JSONObject) unique.get(pathway)).get("idx"));
//                            edges.add(newEdgeNode);
//
//                        }
//                    }
//                }

                nodeEdgeJson.put("nodes", nodes);
                nodeEdgeJson.put("edges", edges);
                nodeEdgeJson.put("table", table);
                //==============================================
                //network.put(queryPathway, nodeEdgeJson);



                //log.info(geneResults.toJSONString());
//            for (int index=0; index < pathways.size(); index++) {
//                String queryPathway = pathways.get(index);
//                //System.out.println(queryPathway);
//
//                if (geneResults.containsKey(queryPathway)) {
//                    pathwayJson = (JSONArray) geneResults.get(queryPathway);
//                    genePathways.put(queryPathway, pathwayJson);
////                log.info("KEGG_2013");
////                log.info(KEGG_2013.toJSONString());
//                }
//            }



            } catch (Exception e) {

                String msg =  String.format("Error in obtaining pathway info %s", queryPathway);
                log.warn(msg);
                throw new RuntimeException(msg);
            }




//            for (Object key : input.keySet()) {
//                //based on you key types
//                String keyStr = (String) key;
//                JSONObject keyValue = (JSONObject) input.get(keyStr);
//
//                String pathway;
//
//                if (keyValue.containsKey(queryPathway)) {
//                    JSONArray queryList = (JSONArray) keyValue.get(queryPathway);
//                    //JSONArray queryList = (JSONArray) (keyValue.get(queryPathway));
//
//
//                    newNode = generateNode(keyStr, idx, 1);
//                    idx = idx + 1;
//                    nodes.add(newNode);
//
//                    for (int i = 0; i < queryList.size(); i++) {
//                        pathway = (String) queryList.get(i);
//                        if (!unique.containsKey(pathway)) {
//                            newPathNode = generateNode(pathway, idx, 2);
//                            idx = idx + 1;
//
//                            unique.put(pathway, newPathNode);
//                            nodes.add(unique.get(pathway));
//                        }
//                        newEdgeNode = generateEdgeNode((int) newNode.get("idx"), (int) ((JSONObject) unique.get(pathway)).get("idx"));
//                        edges.add(newEdgeNode);
//
//                    }
//                }
//            }
//            JSONObject nodeEdgeJson = new JSONObject();
//            nodeEdgeJson.put("nodes", nodes);
//            nodeEdgeJson.put("edges", edges);
//            //==============================================
//            network.put(queryPathway, nodeEdgeJson);


        System.out.println("Enrichment Network is computed");
        System.out.println(nodeEdgeJson.toString());
        return nodeEdgeJson;
    }




    JSONObject generateEdgeNode(int sourceTag, int targetTag)
    {
        JSONObject node = new JSONObject();
        node.put("source", sourceTag);
        node.put("target", targetTag);
        node.put("value", 1);
        node.put("weight", 1);
        return node;
    }



    JSONObject generateNode(String name, int idx, int tag, double value)//, float center, int value)
    {
        JSONObject node = new JSONObject();
//        Random random = new Random();
//        double rand = random.nextDouble();
//        double scaled = rand * 400;
        if (name.length() > 33) {
            node.put("name", name.substring(0, 30) + "...");
        }else{
            node.put("name", name);
        }

        node.put("full_name", name);
        node.put("idx", idx);
        //node.put("px", center + scaled);
        //node.put("x", center + scaled);
        //rand = random.nextDouble();
        //scaled = rand * 200;
        //node.put("y", center + scaled);
        //node.put("py", center + scaled);
        //node.put("value",value);
        node.put("group",tag);
        node.put("value",value);
        node.put("weight",0);
        return node;
    }

}