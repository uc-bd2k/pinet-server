package edu.uc.eh.service;


import edu.uc.eh.uniprot.Uniprot;
import edu.uc.eh.uniprot.UniprotRepository;
import edu.uc.eh.utils.UtilsNetwork;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.stereotype.Service;


import java.util.Map;

//import java.util.Map;

/**
 * Created by behrouzsh on 9/2/16.
 */

@Service
public class UniprotService {

    private static final Logger log = LoggerFactory.getLogger(UniprotService.class);

    @Value("${urls.uniprot}")
    String uniprotTemplate;

    @Autowired
    FastaService fastaService = new FastaService();

    private final UniprotRepository uniprotRepository;

    @Autowired
    //public RestAPI(HarmonizomeGeneService harmonizomeGeneService, HarmonizomeProteinService harmonizomeProteinService, PrositeService prositeService, PsiModService psiModService, UniprotService uniprotService, EnrichrService enrichrService, PCGService pcgService, KinaseService kinaseService, ShorthandService shorthandService, PhosphoService phosphoService, HarmonizomeGeneService harmonizomeGeneServics1) {
    public UniprotService(UniprotRepository uniprotRepository) {
        this.uniprotRepository = uniprotRepository;
    }

//    @Autowired
//    UniprotRepository uniprotRepository = new UniprotRepository();

    public JSONObject findByAccessionApi(String inputAccession)
    {
        Uniprot response = new Uniprot();
        JSONObject responseUniprot = new JSONObject();
        String[] canonicalAccessionList = inputAccession.split("-");
        String organism = "";
//        JSONObject responseUniprot = new JSONObject();
//
//        String[] canonicalAccessionList = accession.split("-");
        System.out.println("In findByAccessionApi" + inputAccession);
        String canonicalAccession = canonicalAccessionList[0];
        try {

//            System.out.println("uniprotRepository.findByAccession");
//            System.out.println(canonicalAccession);
            response = uniprotRepository.findByAccession(canonicalAccession);
            responseUniprot = response.toJson();
            //System.out.println(response.toString());

        } catch (Exception e) {


            String msg =  String.format("Uniprot %s not found in uniprot localdb", canonicalAccession);
            System.out.println(msg);
            //throw new RuntimeException(msg);
            try {
                responseUniprot = getTable(canonicalAccession);


            }
            catch(Exception e2){
                String msg2 =  String.format("Uniprot %s not found  in uniprot database", canonicalAccession);
                System.out.println(msg2);

            }
//        response = new Uniprot();

        }

        if (canonicalAccession != inputAccession)
        {
            try {
                System.out.println(inputAccession);
                JSONObject fastaResult = fastaService.getTable(inputAccession);
                String fastaSeq = (String) fastaResult.get("sequence");
                responseUniprot.remove("sequence");
                responseUniprot.put("sequence", fastaSeq);
                responseUniprot.remove("length");
                responseUniprot.put("length", fastaSeq.length());

            }
            catch (Exception e2)
            {
                String msg =  String.format("Uniprot %s not found in uniprot localdb", inputAccession);
                System.out.println(msg);
            }
        }
        return responseUniprot;








//
//
//        String canonicalAccession = canonicalAccessionList[0];
//        try {
//
//
//            response = UniprotRepository.findByAccession(canonicalAccession);
//            responseUniprot = response.toJson();
//
//            //System.out.println(responseUniprot.toString());
//
//        } catch (Exception e) {
//
//
//            String msg =  String.format("Uniprot %s not found in uniprot localdb", accession);
//            System.out.println(msg);
//
//            //throw new RuntimeException(msg);
//            try {
//                responseUniprot =  getTable(canonicalAccession);
//                System.out.println(msg);
//
//            }
//            catch(Exception e2){
//                String msg2 =  String.format("Uniprot %s not found  in uniprot ", accession);
//                System.out.println(msg2);
//
//            }
////        response = new Uniprot();
//
//        }
//
//        if (canonicalAccession != accession)
//        {
//            try {
//                System.out.println(accession);
//                JSONObject fastaResult = fastaService.getTable(accession);
//                String fastaSeq = (String) fastaResult.get("sequence");
//                responseUniprot.remove("sequence");
//                responseUniprot.put("sequence", fastaSeq);
//                responseUniprot.remove("length");
//                responseUniprot.put("length", fastaSeq.length());
//
//            }
//            catch (Exception e2)
//            {
//                String msg =  String.format("Uniprot %s error in fasta", accession);
//                System.out.println(msg);
//            }
//        }
//
//
//
//
////        Uniprot protein = findByAccession(accession);
//        return responseUniprot;

    }


    public JSONObject getTable(String protein) {

        String response;
        //String xmlResponse;
        String uniprotUrl = String.format(uniprotTemplate, protein);
        Map uniprotMap;
        log.info("Querying: " + uniprotUrl);


        try {
            response = UtilsNetwork.getInstance().readUrlXml(uniprotUrl);
            //log.info("Response from readXml: ");

            uniprotMap = UtilsNetwork.loadXMLFromString(response);
            //System.out.println("uniprotMap:" + uniprotMap.toString());
        } catch (Exception e) {

            String msg =  String.format("Uniprot %s not found", protein);
            log.warn(msg);
            throw new RuntimeException(msg);

        }



        //String uniprotJson = "{\"length\": "+uniprotMap.get("length").toString()+", \"sequence\": \""+uniprotMap.get("sequence").toString()+"\"}";

        JSONObject uniprotJsonSecond = new JSONObject(uniprotMap);
        //System.out.println("JsonFormat");
        //System.out.println(uniprotJsonSecond.toString());

        return uniprotJsonSecond;
        //return uniprotMap;//.toString();
    }

}
