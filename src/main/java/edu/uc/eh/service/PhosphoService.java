package edu.uc.eh.service;

import edu.uc.eh.controller.RestAPI;
import edu.uc.eh.uniprot.UniprotRepository;
import edu.uc.eh.utils.UtilsIO;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.parser.JSONParser;

import static java.lang.Math.min;
import static java.lang.StrictMath.max;

/**
 * Created by behrouz on 1/25/17.
 */
@Service
public class PhosphoService {

    private static final Logger log = LoggerFactory.getLogger(PhosphoService.class);


    @Value("${resources.phosphoGenes2Kinase}")
    String phosphoGenes2KinaseInfo;

    @Value("${resources.phosphoGeneProbability}")
    String phosphoGeneProbabilityInfo;

    @Value("${urls.harmonizomeGene}")
    String harmonizomeGeneTemplate;

    @Value("${urls.harmonizomeProtein}")
    String harmonizomeProteinTemplate;


    @Value("${resources.blosum50Json}")
    String blosum50Info;

    @Value("${resources.phosphoAmino2KinaseSequence}")
    String phosphoAmino2KinaseSequenceInfo;

    @Value("${resources.ptmAc}")
    String ptmAcInfo;

    @Value("${resources.ptmPh}")
    String ptmPhInfo;

    @Value("${resources.ptmMe}")
    String ptmMeInfo;

    @Value("${resources.ptmCg}")
    String ptmCgInfo;

    //        ptmCg: /PTM/ptm_cg.json
    @Value("${resources.ptmMy}")
    String ptmMyInfo;
    //        ptmMy: /PTM/ptm_my.json
    @Value("${resources.ptmNg}")
    String ptmNgInfo;
    //        ptmNg: /PTM/ptm_ng.json
    @Value("${resources.ptmOg}")
    String ptmOgInfo;
    //        ptmOg: /PTM/ptm_og.json
    @Value("${resources.ptmSg}")
    String ptmSgInfo;
    //        ptmSg: /PTM/ptm_sg.json
    @Value("${resources.ptmSn}")
    String ptmSnInfo;
    //        ptmSn: /PTM/ptm_sn.json
    @Value("${resources.ptmSu}")
    String ptmSuInfo;
    //        ptmSu: /PTM/ptm_su.json
    @Value("${resources.ptmUb}")
    String ptmUbInfo;

    @Value("${resources.modToPTM}")
    String modToPTM;

    @Value("${resources.signorProteinToProtein}")
    String signorProteinToProtein;

    @Value("${resources.signorPtmToProtein}")
    String signorPtmToProtein;

    //        ptmUb: /PTM/ptm_ub.json

    //private final UniprotRepository uniprotRepository;
//    @Autowired
//    public PhosphoService(UniprotRepository uniprotRepository, RestAPI restAPI)
//    {
//        //this.uniprotRepository = uniprotRepository;
//        this.restAPI = restAPI;
//    }

//    private final RestAPI restAPI;
//    @Autowired
//    public PhosphoService(RestAPI restAPI)
//    {
//        this.restAPI = restAPI;
//    }

//    @Autowired
//    UniprotService2 uniprotService2 = new UniprotService2();

    @Autowired
    UniprotService uniprotService;

    @Autowired
    DeepPhosService deepPhosService;
//    @Autowired
//    UniprotRepository uniprotRepository = new UniprotRepository();

    public JSONObject computePtmNetwork(String organism, String[] protList) throws Exception {

        int didx = 0; //definite idx
        int dbdx = 0; //definite blosum idx
        int iidx = 0; //indefinite idx
        int bidx = 0; //blosum idx
        int pidx = 0; //iptmnet idx
        int sidx = 0; //signor idx
        int ppidx = 0;
        String kinase;
        String gene;
        String inputGene;
        String inputGeneUpper;
        JSONObject protListToProtein = new JSONObject();
        String gene2KinaseString;
        String kinase2GeneString;
        String geneName2KinaseNAmeString;
        String inside;
        String geneString;
        String aminoString;
        String siteString;
        String ptmString;
        String inputphosphoProtein;
        String inputphosphoProteinUpper;
        // Get a copy of input data
        //String[] protList = inputProtList;
        PCGService pcgService = new PCGService();

//        for (int i = 0; i < inputProtList.length; i++) {
//
//            protList.add(inputProtList[i]);
//        }


        ArrayList inutArray = new ArrayList();
        ArrayList inputGeneArray = new ArrayList();
        ArrayList phosphoAminoArray = new ArrayList();
        ArrayList phosphoSiteArray = new ArrayList();
        JSONObject protListAndGeneName = new JSONObject();

        JSONObject newNode = new JSONObject();
        JSONObject newEdgeNode = new JSONObject();


        JSONObject phosphoGene2KinaseJson = new JSONObject();
        JSONObject phosphoGene2PrJson = new JSONObject();
        JSONObject phosphoAmino2KinaseSequenceJson = new JSONObject();
        JSONObject blosum50Json = new JSONObject();
        JSONObject ptmAcJson = new JSONObject();
        JSONObject ptmPhJson = new JSONObject();
        JSONObject ptmMeJson = new JSONObject();
//        ptmCg: /PTM/ptm_cg.json
//        ptmMy: /PTM/ptm_my.json
//        ptmNg: /PTM/ptm_ng.json
//        ptmOg: /PTM/ptm_og.json
//        ptmSg: /PTM/ptm_sg.json
//        ptmSn: /PTM/ptm_sn.json
//        ptmSu: /PTM/ptm_su.json
//        ptmUb: /PTM/ptm_ub.json
        JSONObject ptmCgJson = new JSONObject();
        JSONObject ptmMyJson = new JSONObject();
        JSONObject ptmNgJson = new JSONObject();
        JSONObject ptmOgJson = new JSONObject();
        JSONObject ptmSgJson = new JSONObject();
        JSONObject ptmSnJson = new JSONObject();
        JSONObject ptmSuJson = new JSONObject();
        JSONObject ptmUbJson = new JSONObject();
        JSONObject modToPTMJson = new JSONObject();
        JSONObject signorProteinToProteinJson = new JSONObject();
        JSONObject signorPtmToProteinJson = new JSONObject();


        JSONObject network = new JSONObject();


        JSONObject signor_Network = new JSONObject();
        JSONArray signor_NetworkEdges = new JSONArray();
        JSONArray signor_NetworkNodes = new JSONArray();
        JSONArray signorTableArray = new JSONArray();

        JSONObject signor_nodeUnique = new JSONObject();

        //definite is based on the kinase table
        JSONObject ptm_nodeUnique = new JSONObject();
        JSONObject definite_nodeUnique = new JSONObject();
        JSONObject definite_Kinase_Gene_Network = new JSONObject();
        JSONArray definite_Kinase_Gene_NetworkNodes = new JSONArray();
        JSONArray definite_Kinase_Gene_NetworkEdges = new JSONArray();
        //definite blosum is based on the search over the peptides
        JSONObject definite_blosum_nodeUnique = new JSONObject();
        JSONObject definite_blosum_Kinase_Gene_Network = new JSONObject();
        JSONArray definite_blosum_Kinase_Gene_NetworkNodes = new JSONArray();
        JSONArray definite_blosum_Kinase_Gene_NetworkEdges = new JSONArray();
        //indefinite is based on the probability and blosum peptide
        JSONObject indefinite_nodeUnique = new JSONObject();
        JSONObject indefinite_Kinase_Gene_Network = new JSONObject();
        JSONArray indefinite_Kinase_Gene_NetworkNodes = new JSONArray();
        JSONArray ptm_NetworkNodes = new JSONArray();
        JSONArray indefinite_Kinase_Gene_NetworkEdges = new JSONArray();
        JSONArray ptm_NetworkEdges = new JSONArray();

        JSONObject blosum_nodeUnique = new JSONObject();
        JSONObject ptm_Network = new JSONObject();
        JSONObject blosum_Kinase_Gene_Network = new JSONObject();
        JSONArray blosum_Kinase_Gene_NetworkNodes = new JSONArray();
        JSONArray blosum_Kinase_Gene_NetworkEdges = new JSONArray();


        JSONObject deepPhosNetwork = new JSONObject();
        JSONArray deepPhosNetwork_Nodes = new JSONArray();
        JSONArray deepPhosNetwork_Edges = new JSONArray();
        JSONArray deepPhosTableArray = new JSONArray();
        JSONObject deepPhos_nodeUnique = new JSONObject();

        JSONArray gene2KinaseList = new JSONArray();
        JSONArray kinase2GeneList = new JSONArray();
        JSONArray geneBlosumArray = new JSONArray();
        JSONArray ptmTableArray = new JSONArray();
        JSONObject geneBlosumJson = new JSONObject();
        JSONArray geneBlosumJsonArray = new JSONArray();
        JSONObject proteinToPhosphoAminoJson = new JSONObject();
        JSONObject proteinToPhosphoSiteJson = new JSONObject();
        JSONArray aminoSiteArray = new JSONArray();
        JSONObject aminoSiteJson = new JSONObject();
        JSONObject proteinToUniprot = new JSONObject();
        Pattern numberRegex = Pattern.compile("(\\d+(?:\\.\\d+)?)");
        Pattern characterRegex = Pattern.compile("(.*?)(\\d+)(.*)");
        String lowerCase = "[a-z]+";
        JSONObject deepPhosResults = new JSONObject();
        boolean lowerResult;
        //boolean hasLowercase = !password.equals(password.toUpperCase());

        Pattern ifHasLowerCase = Pattern.compile(lowerCase);

//        Pattern lowerCase = Pattern.compile("(\\d+(?:\\.\\d+)?)");
//        Pattern UpperCase = Pattern.compile("(.*?)(\\d+)(.*)");
        try {
            phosphoGene2PrJson = UtilsIO.getInstance().readJsonFile(phosphoGeneProbabilityInfo);
            //log.info(kinase2GeneJson.toString());

        } catch (Exception e) {
            String msg = String.format("Error in obtaining phosphoGeneProbabilityInfo");
            log.warn(msg);
            throw new RuntimeException(msg);
        }

        try {
            phosphoGene2KinaseJson = UtilsIO.getInstance().readJsonFile(phosphoGenes2KinaseInfo);
            //log.info(phosphoGene2KinaseJson.toString());
        } catch (Exception e) {
            String msg = String.format("Error in obtaining phosphoGenes2KinaseInfo");
            log.warn(msg);
            throw new RuntimeException(msg);
        }

        try {
            phosphoAmino2KinaseSequenceJson = UtilsIO.getInstance().readJsonFile(phosphoAmino2KinaseSequenceInfo);
            //log.info(phosphoAmino2KinaseSequenceJson.toString());
        } catch (Exception e) {
            String msg = String.format("Error in obtaining phosphoAmino2KinaseSequenceInfo");
            log.warn(msg);
            throw new RuntimeException(msg);
        }

        try {
            blosum50Json = UtilsIO.getInstance().readJsonFile(blosum50Info);
            //log.info(blosum50Json.toString());
        } catch (Exception e) {
            String msg = String.format("Error in obtaining blosum50Info");
            log.warn(msg);
            throw new RuntimeException(msg);
        }

        try {
            ptmAcJson = UtilsIO.getInstance().readJsonFile(ptmAcInfo);
            //log.info(blosum50Json.toString());
        } catch (Exception e) {
            String msg = String.format("Error in obtaining ptmAcInfo");
            log.warn(msg);
            throw new RuntimeException(msg);
        }

        try {
            ptmMeJson = UtilsIO.getInstance().readJsonFile(ptmMeInfo);
            //log.info(blosum50Json.toString());
        } catch (Exception e) {
            String msg = String.format("Error in obtaining ptmMeInfo");
            log.warn(msg);
            throw new RuntimeException(msg);
        }

        try {
            ptmPhJson = UtilsIO.getInstance().readJsonFile(ptmPhInfo);
            //log.info(blosum50Json.toString());
        } catch (Exception e) {
            String msg = String.format("Error in obtaining ptmPhInfo");
            log.warn(msg);
            throw new RuntimeException(msg);
        }
        try {
            ptmCgJson = UtilsIO.getInstance().readJsonFile(ptmCgInfo);
            //log.info(blosum50Json.toString());
        } catch (Exception e) {
            String msg = String.format("Error in obtaining ptmCgInfo");
            log.warn(msg);
            throw new RuntimeException(msg);
        }
//        ptmCg: /PTM/ptm_cg.json

        try {
            ptmMyJson = UtilsIO.getInstance().readJsonFile(ptmMyInfo);
            //log.info(blosum50Json.toString());
        } catch (Exception e) {
            String msg = String.format("Error in obtaining ptmMyInfo");
            log.warn(msg);
            throw new RuntimeException(msg);
        }
//        ptmMy: /PTM/ptm_my.json
        try {
            ptmNgJson = UtilsIO.getInstance().readJsonFile(ptmNgInfo);
            //log.info(blosum50Json.toString());
        } catch (Exception e) {
            String msg = String.format("Error in obtaining ptmNgInfo");
            log.warn(msg);
            throw new RuntimeException(msg);
        }
//        ptmNg: /PTM/ptm_ng.json
        try {
            ptmOgJson = UtilsIO.getInstance().readJsonFile(ptmOgInfo);
            //log.info(blosum50Json.toString());
        } catch (Exception e) {
            String msg = String.format("Error in obtaining ptmOgInfo");
            log.warn(msg);
            throw new RuntimeException(msg);
        }
//        ptmOg: /PTM/ptm_og.json
        try {
            ptmSgJson = UtilsIO.getInstance().readJsonFile(ptmSgInfo);
            //log.info(blosum50Json.toString());
        } catch (Exception e) {
            String msg = String.format("Error in obtaining ptmSgInfo");
            log.warn(msg);
            throw new RuntimeException(msg);
        }
        try {
            ptmSnJson = UtilsIO.getInstance().readJsonFile(ptmSnInfo);
            //log.info(blosum50Json.toString());
        } catch (Exception e) {
            String msg = String.format("Error in obtaining ptmSnInfo");
            log.warn(msg);
            throw new RuntimeException(msg);
        }
        try {
            ptmSuJson = UtilsIO.getInstance().readJsonFile(ptmSuInfo);
            //log.info(blosum50Json.toString());
        } catch (Exception e) {
            String msg = String.format("Error in obtaining ptmSuInfo");
            log.warn(msg);
            throw new RuntimeException(msg);
        }
        try {
            ptmUbJson = UtilsIO.getInstance().readJsonFile(ptmUbInfo);
            //log.info(blosum50Json.toString());
        } catch (Exception e) {
            String msg = String.format("Error in obtaining ptmUbInfo");
            log.warn(msg);
            throw new RuntimeException(msg);
        }
        try {
            modToPTMJson = UtilsIO.getInstance().readJsonFile(modToPTM);
            //log.info(blosum50Json.toString());
        } catch (Exception e) {
            String msg = String.format("Error in obtaining modToPTM");
            log.warn(msg);
            throw new RuntimeException(msg);
        }
        try {
            signorPtmToProteinJson = UtilsIO.getInstance().readJsonFile(signorPtmToProtein);
            //log.info(blosum50Json.toString());
        } catch (Exception e) {
            String msg = String.format("Error in obtaining signorPtmToProtein");
            log.warn(msg);
            throw new RuntimeException(msg);
        }
        try {
            signorProteinToProteinJson = UtilsIO.getInstance().readJsonFile(signorProteinToProtein);
            //log.info(blosum50Json.toString());
        } catch (Exception e) {
            String msg = String.format("Error in obtaining signorProteinToProtein");
            log.warn(msg);
            throw new RuntimeException(msg);
        }
//        ptmSg: /PTM/ptm_sg.json
//        ptmSn: /PTM/ptm_sn.json
//        ptmSu: /PTM/ptm_su.json
//        ptmUb: /PTM/ptm_ub.json

        try {
            //The last item is the organism
            String organismForQueryUniprot = organism;
//        //System.out.println("======== organism");
//        //System.out.println(organismForQueryUniprot);
            for (int i = 0; i < protList.length; i++) {

                inutArray.add(protList[i]);

//                //System.out.println("input Uniprot");
//                //System.out.println(protList[i]);

                inside = "";
                //geneString = "";
                aminoString = "";
                siteString = "";
                ptmString = "";

                List<String> matchList = new ArrayList<String>();
                Pattern regex = Pattern.compile("\\{(.*?)\\}");
                geneString = protList[i].replaceAll("\\{.*?\\} ?", "");
                protListToProtein.put(protList[i], geneString);
//            //System.out.println("geneString");
//            //System.out.println(geneString);
                //Pattern siteRegex = Pattern.compile("\\d+");
                //Pattern aminoRegex = Pattern.compile("[A-Z]{1}");

                Matcher insideMatcher = regex.matcher(protList[i]);


                //log.info("here1");
                aminoSiteArray = new JSONArray();
                while (insideMatcher.find()) {
                    ////System.out.println("here1");
                    ////System.out.println(insideMatcher.group(1));
                    String insideMatcherString = (String) insideMatcher.group(1).replace("[", "").replace("]", "");
                    String[] splitted = insideMatcherString.split("@");
                    siteString = splitted[1];
                    ////System.out.println(insideMatcherString);

                    Matcher matcherLower = ifHasLowerCase.matcher(splitted[1]);
//                lowerResult = matcherLower.matches();
//                //System.out.println(lowerResult);
                    if (splitted[0].indexOf("(") != -1) {
                        Matcher match = Pattern.compile("\\((.*?)\\)").matcher(splitted[0]);
                        while (match.find()) {
                            ptmString = match.group(1);
                            aminoString = splitted[0].replace(match.group(0), "");
                        }

//                    aminoString = splitted[0].substring(splitted[0].length() - 1);
//                    ptmString = splitted[0].substring(0, splitted[0].length() - 1);
                    } else {
                        if (splitted[0].equals(splitted[0].toUpperCase())) {
                            //if(!lowerResult) {
                            //splitted[0].matches(".*\\d+.*");
                            Matcher matcher = numberRegex.matcher(splitted[0]);
                            while (matcher.find()) {
                                ptmString = matcher.group(1);
                            }
                            Matcher matcher2 = characterRegex.matcher(splitted[0]);
                            while (matcher2.find()) {
                                aminoString = matcher2.group(1);
                                aminoString = aminoString.substring(0, 1);
                            }
                        } else {
                            aminoString = splitted[0].substring(splitted[0].length() - 1);
                            ptmString = splitted[0].substring(0, splitted[0].length() - 1);

                        }
                    }
//                    //System.out.println(aminoString);
//                    //System.out.println(siteString);
//                    //System.out.println(ptmString);


                    aminoSiteJson = new JSONObject();
                    //aminoSiteJson.put("amino",aminoString.substring(0,1));
                    aminoSiteJson.put("amino", aminoString);
                    aminoSiteJson.put("ptm", ptmString);
                    aminoSiteJson.put("site", siteString);

                    aminoSiteArray.add(aminoSiteJson);

                }
                proteinToPhosphoAminoJson.put(protList[i], aminoSiteArray);

                //inputGeneArray.add(geneString);


            }

            //Get info about the genes
            //String[] stringProteinArray = (String[]) inputGeneArray.toArray(new String[0]);
            ////System.out.println("stringProteinArray");
//        for (int i = 0; i < stringProteinArray.length;  i++) {
//            //System.out.println(stringProteinArray[i]);
//        }

//        ArrayList<Integer> geneProtenCheck = pcgService.checkGenes(stringGeneArray);
//        JSONArray geneProteinInfo = pcgService.getTable(geneProtenCheck);
            //log.info(geneProteinInfo.toString());
            JSONObject geneSequenceInfo = new JSONObject();

            JSONArray phosphoGeneSequenceArray = new JSONArray();
            JSONArray ptmArray = new JSONArray();
            JSONArray ptmPhArray = new JSONArray();
            JSONArray ptmMeArray = new JSONArray();
            JSONArray ptmAcArray = new JSONArray();
//        ptmCg: /PTM/ptm_cg.json
//        ptmMy: /PTM/ptm_my.json
//        ptmNg: /PTM/ptm_ng.json
//        ptmOg: /PTM/ptm_og.json
//        ptmSg: /PTM/ptm_sg.json
//        ptmSn: /PTM/ptm_sn.json
//        ptmSu: /PTM/ptm_su.json
//        ptmUb: /PTM/ptm_ub.json
            JSONArray ptmCgArray = new JSONArray();
            JSONArray ptmMyArray = new JSONArray();
            JSONArray ptmNgArray = new JSONArray();
            JSONArray ptmOgArray = new JSONArray();
            JSONArray ptmSgArray = new JSONArray();
            JSONArray ptmSnArray = new JSONArray();
            JSONArray ptmSuArray = new JSONArray();
            JSONArray ptmUbArray = new JSONArray();
            JSONArray ptmMe2Array = new JSONArray();
            JSONArray ptmMe3Array = new JSONArray();

            for (int i = 0; i < protList.length; i++) {

                JSONArray proteinToPhosphoAminoArray = (JSONArray) proteinToPhosphoAminoJson.get(protList[i]);
//            JSONArray uniprotId = (JSONArray)((JSONObject) geneProteinInfo.get(i)).get("uniprot_ids");
//                //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
//                //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
//                //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
//                //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
//                //System.out.println(i);
//                //System.out.println("uniprot_ids------");

                String uniprot_id = (String) protListToProtein.get(protList[i]);
                //System.out.println(uniprot_id);
//                //System.out.println("protList------");
//                //System.out.println(protList[i]);
                if (!proteinToUniprot.containsKey(uniprot_id)) {

                    //geneSequenceInfo = uniprotService.getTable(organismForQueryUniprot, uniprot_id);
                    geneSequenceInfo = uniprotService.findByAccessionApi(uniprot_id);


                    proteinToUniprot.put(uniprot_id, geneSequenceInfo);
                }
//                //System.out.println(geneSequenceInfo);
//                //System.out.println(geneSequenceInfo.toString());
                ArrayList<String> geneName = (ArrayList<String>) ((JSONObject) proteinToUniprot.get(uniprot_id)).get("primary_gene_name");
                ////System.out.println(geneName.toString());
                if (geneName.size() > 0) {
                    protListAndGeneName.put(protList[i], protList[i] + "(" + geneName.get(0).toString() + ")");
                }
                else{
                    protListAndGeneName.put(protList[i], protList[i] );
                }

                ////System.out.println("protListAndGeneName////////////////////");
//                //System.out.println(protList[i]);
//                //System.out.println(geneName.toString());
//                //System.out.println(protList[i] + "(" + geneName.get(0).toString() + ")");
//                //System.out.println(protListAndGeneName.get(protList[i]));
                ////System.out.println("////////////////////////////");


//                //System.out.println(protList[i]);
//                //System.out.println(proteinToPhosphoAminoArray.toString());
                //System.out.println("before loop");
                //System.out.println("----------------------------------------");
//                //System.out.println("j----------------------------------------");
//                //System.out.println("j----------------------------------------");
//                //System.out.println("j----------------------------------------");
                //System.out.println("proteinToPhosphoAminoJson");
                //System.out.println(proteinToPhosphoAminoJson);
                for (int j = 0; j < proteinToPhosphoAminoArray.size(); j++) {
                    //System.out.println("j----------------------------------------");
                    //System.out.println(j);
                    //System.out.println(((JSONObject) proteinToPhosphoAminoArray.get(j)).toJSONString());
                    JSONObject proteinToPhosphoAminoItem = (JSONObject) proteinToPhosphoAminoArray.get(j);
                    //System.out.println(proteinToPhosphoAminoItem.toJSONString());
                    //System.out.println(proteinToPhosphoAminoItem.get("ptm").toString());


                    //For phosphosite
//        ptmCg: /PTM/ptm_cg.json
//        ptmMy: /PTM/ptm_my.json
//        ptmNg: /PTM/ptm_ng.json
//        ptmOg: /PTM/ptm_og.json
//        ptmSg: /PTM/ptm_sg.json
//        ptmSn: /PTM/ptm_sn.json
//        ptmSu: /PTM/ptm_su.json
//        ptmUb: /PTM/ptm_ub.json
                    if (proteinToPhosphoAminoItem.get("ptm").equals("p") || proteinToPhosphoAminoItem.get("ptm").equals("ph")) {


                        //if (Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) < 81 && Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) > 79) {
                        JSONObject phosphoGeneSequenceJson = new JSONObject();
                        //String[] proteinAndOrganism = { uniprot_id, organismForQueryUniprot };
                        ////System.out.println(uniprot_id);


                        geneSequenceInfo = (JSONObject) proteinToUniprot.get(uniprot_id);

                        //System.out.println(proteinToPhosphoAminoItem.get("ptm"));

                        String cutSequence = cutSequenceMethod(geneSequenceInfo.get("sequence").toString(), Integer.parseInt((String) proteinToPhosphoAminoItem.get("site")));
                        ////System.out.println(protList[i]);

//                    log.info(phosphoAminoArray.get(i).toString());
//                    log.info(phosphoSiteArray.get(i).toString());
//                    //System.out.println("cutSequence ++++++++++++++++++++++");
//                    //System.out.println(cutSequence);
//                    //System.out.println("protListAndGeneName ++++++++++++++++++++++");
//                    //System.out.println(protListAndGeneName.get(protList[i]));

                        phosphoGeneSequenceJson.put("phosphoSite", uniprot_id + "{[p" + proteinToPhosphoAminoItem.get("amino") + "]@" + proteinToPhosphoAminoItem.get("site") + "}");
                        phosphoGeneSequenceJson.put("phosphoProtein", protList[i]);
                        phosphoGeneSequenceJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                        phosphoGeneSequenceJson.put("site", proteinToPhosphoAminoItem.get("site"));
                        phosphoGeneSequenceJson.put("sequence", cutSequence);
                        //phosphoGeneSequenceJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                        //System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                        //System.out.println(phosphoGeneSequenceJson.toString());
                        //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
//                    //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                        phosphoGeneSequenceArray.add(phosphoGeneSequenceJson);
                        //}
                        //for iptmnet
                        //if (Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) < 81 && Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) > 79) {

                        JSONObject ptmItemJson = new JSONObject();

                        ptmItemJson.put("phosphoSite", uniprot_id + "[ph" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                        ptmItemJson.put("phosphoProtein", protList[i]);
                        ptmItemJson.put("shorthand", uniprot_id + "[p" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");

                        ptmItemJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                        ptmItemJson.put("site", proteinToPhosphoAminoItem.get("site"));
                        //ptmItemJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                        //System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                        //System.out.println(ptmItemJson.toString());
                        //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                        ////System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                        ptmPhArray.add(ptmItemJson);
                        ptmArray.add(ptmItemJson);

                        //}


                    } else if (proteinToPhosphoAminoItem.get("ptm").equals("a")) {
                        JSONObject ptmItemJson = new JSONObject();

                        ptmItemJson.put("phosphoSite", uniprot_id + "[ac" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                        ptmItemJson.put("phosphoProtein", protList[i]);
                        ptmItemJson.put("shorthand", uniprot_id + "[a" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");

                        ptmItemJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                        ptmItemJson.put("site", proteinToPhosphoAminoItem.get("site"));
                        //ptmItemJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                        //System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                        //System.out.println(ptmItemJson.toString());
                        //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                        ////System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                        ptmAcArray.add(ptmItemJson);
                        ptmArray.add(ptmItemJson);
                    } else if (proteinToPhosphoAminoItem.get("ptm").equals("me") || proteinToPhosphoAminoItem.get("ptm").equals("me2") || proteinToPhosphoAminoItem.get("ptm").equals("me3")) {
                        JSONObject ptmItemJson = new JSONObject();

                        if (proteinToPhosphoAminoItem.get("ptm").equals("me")) {
                            ptmItemJson.put("phosphoSite", uniprot_id + "[me" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                            ptmItemJson.put("shorthand", uniprot_id + "[me" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                        }
                        if (proteinToPhosphoAminoItem.get("ptm").equals("me2")) {
                            ptmItemJson.put("phosphoSite", uniprot_id + "[me2" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                            ptmItemJson.put("shorthand", uniprot_id + "[me2" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                        }
                        if (proteinToPhosphoAminoItem.get("ptm").equals("me3")) {
                            ptmItemJson.put("phosphoSite", uniprot_id + "[me3" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                            ptmItemJson.put("shorthand", uniprot_id + "[me3" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                        }


                        ptmItemJson.put("phosphoProtein", protList[i]);
                        ptmItemJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                        ptmItemJson.put("site", proteinToPhosphoAminoItem.get("site"));
                        //ptmItemJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                        //System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                        //System.out.println(ptmItemJson.toString());
                        //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                        ////System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                        ptmMeArray.add(ptmItemJson);
                        ptmArray.add(ptmItemJson);
                    } else if (proteinToPhosphoAminoItem.get("ptm").equals("cg")) {
                        JSONObject ptmItemJson = new JSONObject();

                        ptmItemJson.put("phosphoSite", uniprot_id + "[cg" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                        ptmItemJson.put("shorthand", uniprot_id + "[cg" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");

                        ptmItemJson.put("phosphoProtein", protList[i]);
                        ptmItemJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                        ptmItemJson.put("site", proteinToPhosphoAminoItem.get("site"));
                        //ptmItemJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                        //System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                        //System.out.println(ptmItemJson.toString());
                        //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                        ////System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                        ptmCgArray.add(ptmItemJson);
                        ptmArray.add(ptmItemJson);
                    } else if (proteinToPhosphoAminoItem.get("ptm").equals("my")) {
                        JSONObject ptmItemJson = new JSONObject();

                        ptmItemJson.put("phosphoSite", uniprot_id + "[my" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                        ptmItemJson.put("shorthand", uniprot_id + "[my" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");

                        ptmItemJson.put("phosphoProtein", protList[i]);
                        ptmItemJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                        ptmItemJson.put("site", proteinToPhosphoAminoItem.get("site"));
                        //ptmItemJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                        //System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                        //System.out.println(ptmItemJson.toString());
                        //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                        ////System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                        ptmMyArray.add(ptmItemJson);
                        ptmArray.add(ptmItemJson);
                    } else if (proteinToPhosphoAminoItem.get("ptm").equals("ng")) {
                        JSONObject ptmItemJson = new JSONObject();

                        ptmItemJson.put("phosphoSite", uniprot_id + "[ng" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                        ptmItemJson.put("shorthand", uniprot_id + "[ng" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");

                        ptmItemJson.put("phosphoProtein", protList[i]);
                        ptmItemJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                        ptmItemJson.put("site", proteinToPhosphoAminoItem.get("site"));
                        //ptmItemJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                        //System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                        //System.out.println(ptmItemJson.toString());
                        //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                        ////System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                        ptmNgArray.add(ptmItemJson);
                        ptmArray.add(ptmItemJson);
                    } else if (proteinToPhosphoAminoItem.get("ptm").equals("og")) {
                        JSONObject ptmItemJson = new JSONObject();

                        ptmItemJson.put("phosphoSite", uniprot_id + "[og" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                        ptmItemJson.put("shorthand", uniprot_id + "[og" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");

                        ptmItemJson.put("phosphoProtein", protList[i]);
                        ptmItemJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                        ptmItemJson.put("site", proteinToPhosphoAminoItem.get("site"));
                        //ptmItemJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                        //System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                        //System.out.println(ptmItemJson.toString());
                        //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                        ////System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                        ptmOgArray.add(ptmItemJson);
                        ptmArray.add(ptmItemJson);
                    } else if (proteinToPhosphoAminoItem.get("ptm").equals("sg")) {
                        JSONObject ptmItemJson = new JSONObject();

                        ptmItemJson.put("phosphoSite", uniprot_id + "[sg" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                        ptmItemJson.put("shorthand", uniprot_id + "[sg" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");

                        ptmItemJson.put("phosphoProtein", protList[i]);
                        ptmItemJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                        ptmItemJson.put("site", proteinToPhosphoAminoItem.get("site"));
                        //ptmItemJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                        //System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                        //System.out.println(ptmItemJson.toString());
                        //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                        ////System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                        ptmSgArray.add(ptmItemJson);
                        ptmArray.add(ptmItemJson);
                    } else if (proteinToPhosphoAminoItem.get("ptm").equals("sn")) {
                        JSONObject ptmItemJson = new JSONObject();

                        ptmItemJson.put("phosphoSite", uniprot_id + "[sn" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                        ptmItemJson.put("shorthand", uniprot_id + "[sn" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");

                        ptmItemJson.put("phosphoProtein", protList[i]);
                        ptmItemJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                        ptmItemJson.put("site", proteinToPhosphoAminoItem.get("site"));
                        //ptmItemJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                        //System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                        //System.out.println(ptmItemJson.toString());
                        //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                        ////System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                        ptmSnArray.add(ptmItemJson);
                        ptmArray.add(ptmItemJson);
                    } else if (proteinToPhosphoAminoItem.get("ptm").equals("su")) {
                        JSONObject ptmItemJson = new JSONObject();

                        ptmItemJson.put("phosphoSite", uniprot_id + "[su" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                        ptmItemJson.put("shorthand", uniprot_id + "[su" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");

                        ptmItemJson.put("phosphoProtein", protList[i]);
                        ptmItemJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                        ptmItemJson.put("site", proteinToPhosphoAminoItem.get("site"));
                        //ptmItemJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                        //System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                        //System.out.println(ptmItemJson.toString());
                        //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                        ////System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                        ptmSuArray.add(ptmItemJson);
                        ptmArray.add(ptmItemJson);
                    } else if (proteinToPhosphoAminoItem.get("ptm").equals("ub")) {
                        JSONObject ptmItemJson = new JSONObject();

                        ptmItemJson.put("phosphoSite", uniprot_id + "[ub" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                        ptmItemJson.put("shorthand", uniprot_id + "[ub" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");

                        ptmItemJson.put("phosphoProtein", protList[i]);
                        ptmItemJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                        ptmItemJson.put("site", proteinToPhosphoAminoItem.get("site"));
                        //ptmItemJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                        //System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                        //System.out.println(ptmItemJson.toString());
                        //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                        ////System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                        ptmUbArray.add(ptmItemJson);
                        ptmArray.add(ptmItemJson);
                    } else if (((String) proteinToPhosphoAminoItem.get("ptm")).toUpperCase().contains("MOD")) {
                        String mod = ((String) proteinToPhosphoAminoItem.get("ptm")).toUpperCase().replace("_",":");
                        //System.out.println("There is a MOD modification before checking");
                        //System.out.println(mod);
                        if (modToPTMJson.containsKey(mod)) {
                            String modPTM = (String) modToPTMJson.get(mod);
                            JSONObject ptmItemJson = new JSONObject();
                            System.out.println("There is a MOD modification ");
                            System.out.println("////////////////////// ");
                            System.out.println(mod);
                            System.out.println(modPTM);
                            ptmItemJson.put("phosphoSite", uniprot_id + "[" + modPTM + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                            ptmItemJson.put("shorthand", uniprot_id + "[(" + mod + ")" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");

                            ptmItemJson.put("phosphoProtein", protList[i]);
                            ptmItemJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                            ptmItemJson.put("site", proteinToPhosphoAminoItem.get("site"));
                            //ptmItemJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                            //System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                            //System.out.println(ptmItemJson.toString());
                            //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                            ptmArray.add(ptmItemJson);
                            ////System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                            if (modPTM.equals("ub")) {
                                ptmUbArray.add(ptmItemJson);
                            }
                            if (modPTM.equals("ph")) {
                                JSONObject phosphoGeneSequenceJson = new JSONObject();
                              


                                geneSequenceInfo = (JSONObject) proteinToUniprot.get(uniprot_id);

                                //System.out.println(geneSequenceInfo.toString());

                                String cutSequence = cutSequenceMethod(geneSequenceInfo.get("sequence").toString(), Integer.parseInt((String) proteinToPhosphoAminoItem.get("site")));
                                //System.out.println(cutSequence);


                                phosphoGeneSequenceJson.put("phosphoSite", uniprot_id + "{" + proteinToPhosphoAminoItem.get("amino") + "(" + proteinToPhosphoAminoItem.get("ptm") + ")@" + proteinToPhosphoAminoItem.get("site") + "}" );
                                phosphoGeneSequenceJson.put("phosphoProtein", protList[i]);
                                phosphoGeneSequenceJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                                phosphoGeneSequenceJson.put("site", proteinToPhosphoAminoItem.get("site"));
                                phosphoGeneSequenceJson.put("sequence", cutSequence);

                                //System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                                //System.out.println(phosphoGeneSequenceJson.toString());
                                //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
//

                                phosphoGeneSequenceArray.add(phosphoGeneSequenceJson);











                                ptmPhArray.add(ptmItemJson);
                            }
                            if (modPTM.equals("me")) {
                                ptmMeArray.add(ptmItemJson);
                            }
                            if (modPTM.equals("me2")) {
                                ptmMeArray.add(ptmItemJson);
                            }
                            if (modPTM.equals("me3")) {
                                ptmMeArray.add(ptmItemJson);
                            }
                            if (modPTM.equals("ac")) {
                                ptmAcArray.add(ptmItemJson);
                            }
                            if (modPTM.equals("su")) {
                                ptmSuArray.add(ptmItemJson);
                            }
                        }
                    }
//                else if(proteinToPhosphoAminoItem.get("ptm").equals("me2")) {
//                    JSONObject ptmItemJson = new JSONObject();
//
//                    ptmItemJson.put("phosphoSite", uniprot_id + "[me2" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
//                    ptmItemJson.put("shorthand", uniprot_id + "[me2" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
//
//                    ptmItemJson.put("phosphoProtein", protList[i]);
//                    ptmItemJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
//                    ptmItemJson.put("site", proteinToPhosphoAminoItem.get("site"));
//                    //ptmItemJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
//                    //System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
//                    //System.out.println(ptmItemJson.toString());
//                    //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
//                    ////System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
//
//                    ptmMe2Array.add(ptmItemJson);
//                    ptmArray.add(ptmItemJson);
//                }
//                else if(proteinToPhosphoAminoItem.get("ptm").equals("me3")) {
//                    JSONObject ptmItemJson = new JSONObject();
//
//                    ptmItemJson.put("phosphoSite", uniprot_id + "[me3" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
//                    ptmItemJson.put("shorthand", uniprot_id + "[me3" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
//
//                    ptmItemJson.put("phosphoProtein", protList[i]);
//                    ptmItemJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
//                    ptmItemJson.put("site", proteinToPhosphoAminoItem.get("site"));
//                    //ptmItemJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
//                    //System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
//                    //System.out.println(ptmItemJson.toString());
//                    //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
//                    ////System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
//
//                    ptmMe3Array.add(ptmItemJson);
//                    ptmArray.add(ptmItemJson);
//                }
                    else {
                        try {
                            Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm"));


                            if (Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) < 79.981 && Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) > 79.96) {
                                JSONObject phosphoGeneSequenceJson = new JSONObject();
                                //String[] proteinAndOrganism = { uniprot_id, organismForQueryUniprot };
                                ////System.out.println(uniprot_id);


                                geneSequenceInfo = (JSONObject) proteinToUniprot.get(uniprot_id);

                                //System.out.println(geneSequenceInfo.toString());

                                String cutSequence = cutSequenceMethod(geneSequenceInfo.get("sequence").toString(), Integer.parseInt((String) proteinToPhosphoAminoItem.get("site")));
                                //System.out.println(cutSequence);

//                    log.info(phosphoAminoArray.get(i).toString());
//                    log.info(phosphoSiteArray.get(i).toString());
//                    //System.out.println("cutSequence ++++++++++++++++++++++");
//                    //System.out.println(cutSequence);
//                    //System.out.println("protListAndGeneName ++++++++++++++++++++++");
//                    //System.out.println(protListAndGeneName.get(protList[i]));

                                phosphoGeneSequenceJson.put("phosphoSite", uniprot_id + "{[" + proteinToPhosphoAminoItem.get("amino") + "+" + proteinToPhosphoAminoItem.get("ptm") + "]@" + proteinToPhosphoAminoItem.get("site") + "}");
                                phosphoGeneSequenceJson.put("phosphoProtein", protList[i]);
                                phosphoGeneSequenceJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                                phosphoGeneSequenceJson.put("site", proteinToPhosphoAminoItem.get("site"));
                                phosphoGeneSequenceJson.put("sequence", cutSequence);
                                //phosphoGeneSequenceJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                                //System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                                //System.out.println(phosphoGeneSequenceJson.toString());
                                //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
//                    //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                                phosphoGeneSequenceArray.add(phosphoGeneSequenceJson);
                            }
                            //for iptmnet
                            if (Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) < 79.981 && Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) > 79.96) {
                                JSONObject ptmItemJson = new JSONObject();

                                ptmItemJson.put("phosphoSite", uniprot_id + "[ph" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                                ptmItemJson.put("phosphoProtein", protList[i]);
                                ptmItemJson.put("shorthand", uniprot_id + "[p" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");

                                ptmItemJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                                ptmItemJson.put("site", proteinToPhosphoAminoItem.get("site"));
                                //ptmItemJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                                //System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                                //System.out.println(ptmItemJson.toString());
                                //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                                ////System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                                ptmPhArray.add(ptmItemJson);
                                ptmArray.add(ptmItemJson);
                            }

                            if (Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) < 42.04 && Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) > 42.03) {
                                JSONObject ptmItemJson = new JSONObject();

                                ptmItemJson.put("phosphoSite", uniprot_id + "[ac" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                                ptmItemJson.put("phosphoProtein", protList[i]);
                                ptmItemJson.put("shorthand", uniprot_id + "[a" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");

                                ptmItemJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                                ptmItemJson.put("site", proteinToPhosphoAminoItem.get("site"));
                                //ptmItemJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                                //System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                                //System.out.println(ptmItemJson.toString());
                                //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                                ////System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                                ptmAcArray.add(ptmItemJson);
                                ptmArray.add(ptmItemJson);
                            }

                            if (Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) < 14.03 && Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) > 14.01) {
                                JSONObject ptmItemJson = new JSONObject();

                                ptmItemJson.put("phosphoSite", uniprot_id + "[me" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                                ptmItemJson.put("shorthand", uniprot_id + "[me" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");

                                ptmItemJson.put("phosphoProtein", protList[i]);
                                ptmItemJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                                ptmItemJson.put("site", proteinToPhosphoAminoItem.get("site"));
                                //ptmItemJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                                //System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                                //System.out.println(ptmItemJson.toString());
                                //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                                ////System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                                ptmMeArray.add(ptmItemJson);
                                ptmArray.add(ptmItemJson);
                            }
                        } catch (NumberFormatException e) {
                            //not a double
                        }
                    }

                }

            }

            //System.out.println("ptmPhArray");
            //System.out.println(ptmPhArray.toString());
            //System.out.println("ptmMeArray");
            //System.out.println(ptmMeArray.toString());
            //System.out.println("ptmAcArray");
            //System.out.println(ptmAcArray.toString());

            //System.out.println("ptmCgArray");
            //System.out.println(ptmCgArray.toString());
            //System.out.println("ptmMyArray");
            //System.out.println(ptmMyArray.toString());
            //System.out.println("ptmNgArray");
            //System.out.println(ptmNgArray.toString());
//        ptmCg: /PTM/ptm_cg.json
//        ptmMy: /PTM/ptm_my.json
//        ptmNg: /PTM/ptm_ng.json
            //System.out.println("ptmOgArray");
            //System.out.println(ptmOgArray.toString());
            //System.out.println("ptmSgArray");
            //System.out.println(ptmSgArray.toString());
            //System.out.println("ptmSnArray");
            //System.out.println(ptmSnArray.toString());
//        ptmOg: /PTM/ptm_og.json
//        ptmSg: /PTM/ptm_sg.json
//        ptmSn: /PTM/ptm_sn.json
            //System.out.println("ptmSuArray");
            //System.out.println(ptmSuArray.toString());
            //System.out.println("ptmUbArray");
            //System.out.println(ptmUbArray.toString());
            //System.out.println("ptmMe2Array");
            //System.out.println(ptmMe2Array.toString());
            //System.out.println("ptmMe3Array");
            //System.out.println(ptmMe3Array.toString());
//        ptmSu: /PTM/ptm_su.json
//        ptmUb: /PTM/ptm_ub.json

            //System.out.println("phosphoGeneSequenceArray ========================");
            //log.info("phosphoGeneSequenceArray ========================");
            //System.out.println(phosphoGeneSequenceArray.toString());
            //First only add the input genes
            //System.out.println("//Adding to nodelists for all the networks");
            for (int i = 0; i < protList.length; i++) {


                //geneString = protList[i].replaceAll("\\[.*?\\] ?", "");
                //Adding gene name at the end
                //inputGene = protList[i] +"/" +proteinToUniprot.get(geneString);

                inputGene = protList[i];
                inputGeneUpper = inputGene.toUpperCase();
                //System.out.println(inputGeneUpper);
                //System.out.println((String) protListAndGeneName.get(protList[i]));
                //System.out.println("----------------------");
                if (!ptm_nodeUnique.containsKey(inputGeneUpper)) {

                    newNode = generateNode(inputGene, (String) protListAndGeneName.get(protList[i]), "No", pidx, 0, 0.0);//tag 0 is for grey, connected is for verifying if the
                    //uniprot is connected to an enzyme or not
                    pidx = pidx + 1;
                    ptm_NetworkNodes.add(newNode);
                    ptm_nodeUnique.put(inputGeneUpper, newNode);
                }

                if (!signor_nodeUnique.containsKey(inputGeneUpper)) {

                    newNode = generateNode(inputGene, (String) protListAndGeneName.get(protList[i]), "No", sidx, 0, 0.0);//tag 0 is for grey, connected is for verifying if the
                    //uniprot is connected to an enzyme or not
                    sidx = sidx + 1;

                    signor_NetworkNodes.add(newNode);
                    signor_nodeUnique.put(inputGeneUpper, newNode);
                }

                if (!indefinite_nodeUnique.containsKey(inputGeneUpper)) {

                    newNode = generateNode(inputGene, (String) protListAndGeneName.get(protList[i]), "", iidx, 1, 0.0);//tag 1 is for grey
                    iidx = iidx + 1;
                    indefinite_Kinase_Gene_NetworkNodes.add(newNode);
                    indefinite_nodeUnique.put(inputGeneUpper, newNode);
                }

                if (!blosum_nodeUnique.containsKey(inputGeneUpper)) {

                    newNode = generateNode(inputGene, (String) protListAndGeneName.get(protList[i]), "", bidx, 1, 0.0);//tag 1 is for grey
                    bidx = bidx + 1;
                    blosum_Kinase_Gene_NetworkNodes.add(newNode);
                    blosum_nodeUnique.put(inputGeneUpper, newNode);
                }

                //Add new node if not existed to definite_nodeUnique
                if (!definite_nodeUnique.containsKey(inputGeneUpper)) {
                    newNode = generateNode(inputGene, (String) protListAndGeneName.get(protList[i]), "", didx, 1, 0.0);//tag 1 is for grey
                    didx = didx + 1;
                    definite_Kinase_Gene_NetworkNodes.add(newNode);
                    definite_nodeUnique.put(inputGeneUpper, newNode);
                }

                //Add new node if not existed to nodeunique
                if (!definite_blosum_nodeUnique.containsKey(inputGeneUpper)) {
                    newNode = generateNode(inputGene, (String) protListAndGeneName.get(protList[i]), "", dbdx, 1, 0.0);//tag 1 is for grey
                    dbdx = dbdx + 1;
                    definite_blosum_Kinase_Gene_NetworkNodes.add(newNode);
                    definite_blosum_nodeUnique.put(inputGeneUpper, newNode);
                }


            }


            // Compute Network for the indefinite genes based on blosum50
            System.out.println("Compute Network for the indefinite genes based on blosum50");
            System.out.println(phosphoGeneSequenceArray);
            for (int i = 0; i < phosphoGeneSequenceArray.size(); i++) {
                //System.out.println("==========================================");
                //System.out.println("Compute Network for the indefinite genes based on blosum50");
                //log.info("inputgene");
                //log.info(input[i]);
                JSONObject phosphoGeneSequenceJson2 = (JSONObject) phosphoGeneSequenceArray.get(i);
                System.out.println("------------------------");
                System.out.println(phosphoGeneSequenceJson2);
                String phosphoAmino = phosphoGeneSequenceJson2.get("amino").toString();
                String phosphoSequence = phosphoGeneSequenceJson2.get("sequence").toString();
                inputGene = phosphoGeneSequenceJson2.get("phosphoProtein").toString();
                String ptm = phosphoGeneSequenceJson2.get("phosphoSite").toString();
                inputphosphoProtein = phosphoGeneSequenceJson2.get("phosphoProtein").toString();
                inputphosphoProteinUpper = inputphosphoProtein.toUpperCase();
                //inputGene = protList[i];
                inputGeneUpper = inputGene.toUpperCase();
                String phosphoGene = inputGeneUpper;


                JSONObject phosphoKinaseSequence = (JSONObject) phosphoAmino2KinaseSequenceJson.get(phosphoAmino);

                //int iteration = 0;
                int blosumScoreHigh = -1000;
                int blosumScoreMax = -1000;
                JSONArray geneBlosumJsonArrayFinal = new JSONArray();
                ////System.out.println (phosphoAmino + "  ++++++++++++++++++++++++++++++");


                for (Object key : phosphoKinaseSequence.keySet()) {
                    //iteration ++;
                    blosumScoreMax = computeBlosum50Score(blosum50Json, phosphoSequence, phosphoSequence);
                    blosumScoreHigh = (int) ((double) blosumScoreMax / 2.0);
                    ////System.out.println ("Blosum high schore " + blosumScoreHigh);
                    String keyStr = (String) key;
                    //log.info("keyStr");
                    //log.info(keyStr);
                    Object keyvalue = phosphoKinaseSequence.get(keyStr);
                    JSONArray keyvalueJsonArray = (JSONArray) keyvalue;
                    geneBlosumJsonArrayFinal = new JSONArray();
                    //Class cls = keyvalue.getClass();
                    ////System.out.println("The type of the object is: " + cls.getName());
                    //Print key and value
                    ////System.out.println("key: " + keyStr);
                    ////System.out.println ("value: " + keyvalueJsonArray.get(0));
                    ////System.out.println ("value: " + keyvalueJsonArray);
                    ////System.out.println (keyStr + "  =========================================");

                    int blosumScore;
                    String blosumString = "";
                    String blosumOrganism = "";
                    for (int keyValueIter = 0; keyValueIter < keyvalueJsonArray.size(); keyValueIter++) {
                        geneBlosumJson = new JSONObject();
                        geneBlosumJsonArray = new JSONArray();
                        String kinaseSequence = (String) ((List) keyvalueJsonArray.get(keyValueIter)).get(1);
                        //String organism = (String) ((List) keyvalueJsonArray.get(keyValueIter)).get(0);

                        blosumScore = computeBlosum50Score(blosum50Json, phosphoSequence, kinaseSequence.toUpperCase());
                        //if(phosphoSequence == kinaseSequence) {
                        //System.out.println("blosumScore " + blosumScore + " phosphoSequence " + phosphoSequence + " kinaseSequence " + kinaseSequence);
                        //}
                        if (blosumScore > blosumScoreHigh) {
//                        //System.out.println("here-1");
                            //System.out.println("blosumScore " + blosumScore + " kinase " + keyStr + " kinaseSequence " + kinaseSequence);


                            blosumScoreHigh = blosumScore;
                            blosumString = kinaseSequence;
                            blosumOrganism = organism;
                            geneBlosumJson = new JSONObject();
                            geneBlosumJsonArray = new JSONArray();
                            geneBlosumJson.put("phosphoGene", inputGene);
                            geneBlosumJson.put("ptm", ptm);
                            geneBlosumJson.put("amino", phosphoAmino);
                            geneBlosumJson.put("geneSequence", phosphoSequence);
                            geneBlosumJson.put("kinase", keyStr);
//                            geneBlosumJson.put("kinaseOrganism", blosumOrganism);
                            geneBlosumJson.put("kinasePeptide", blosumString);
                            geneBlosumJson.put("blosum50ScorePercent", blosumScoreHigh);
                            //geneBlosumJson.put("blosum50ScorePercent", Math.round(((double)blosumScoreHigh*100.0/(double)blosumScoreMax)));
                            geneBlosumJson.put("blosum50MaxScore", blosumScoreMax);
                            if (phosphoAmino.charAt(0) == phosphoSequence.charAt(7)) {
                                geneBlosumJson.put("valid", "valid");
                            } else {
                                geneBlosumJson.put("valid", "not valid");
                            }
                            geneBlosumJsonArray.add(geneBlosumJson);

                            log.info(geneBlosumJsonArray.toString());
                            //System.out.println("==========================================");
                            geneBlosumJsonArrayFinal = geneBlosumJsonArray;
                        } else if (blosumScore == blosumScoreHigh) {
                            //System.out.println("here-2");
                            //System.out.println("blosumScore " + blosumScore + " kinase " + keyStr + " kinaseSequence " + kinaseSequence);

                            blosumString = kinaseSequence;
                            blosumOrganism = organism;
                            geneBlosumJson = new JSONObject();
                            geneBlosumJson.put("phosphoGene", phosphoGene);
                            geneBlosumJson.put("ptm", ptm);
                            geneBlosumJson.put("amino", phosphoAmino);
                            geneBlosumJson.put("geneSequence", phosphoSequence);
                            geneBlosumJson.put("kinase", keyStr);
//                            geneBlosumJson.put("kinaseOrganism", blosumOrganism);
                            geneBlosumJson.put("kinasePeptide", blosumString);
                            geneBlosumJson.put("blosum50ScorePercent", blosumScoreHigh);
                            geneBlosumJson.put("blosum50MaxScore", blosumScoreMax);
                            if (phosphoAmino.charAt(0) == phosphoSequence.charAt(7)) {
                                geneBlosumJson.put("valid", "valid");
                            } else {
                                geneBlosumJson.put("valid", "not valid");
                            }
                            geneBlosumJsonArray.add(geneBlosumJson);
                            geneBlosumJsonArrayFinal = geneBlosumJsonArray;

                            log.info(geneBlosumJsonArray.toString());
                            //System.out.println("==========================================");
                        }

                        ////System.out.println ("organism: " + organism + " kinase: " + kinaseSequence);
                    }

//                log.info("geneBlosumJsonArray");
//                log.info(geneBlosumJsonArrayFinal.toString());
                    for (int blosumIter = 0; blosumIter < geneBlosumJsonArrayFinal.size(); blosumIter++) {
                        geneBlosumArray.add(geneBlosumJsonArrayFinal.get(blosumIter));

                        String kinaseKey = (String) ((JSONObject) geneBlosumJsonArrayFinal.get(blosumIter)).get("kinase");
                        long blScore = Math.round(((double) blosumScoreHigh * 100.0 / (double) blosumScoreMax));
//                    ((Long) ((JSONObject)geneBlosumJsonArrayFinal.get(blosumIter)).get("blosum50ScorePercent")).doubleValue() ;

                        if (!blosum_nodeUnique.containsKey(kinaseKey)) {
                            newNode = generateNode(kinaseKey, kinaseKey, "", bidx, 2, 0.0);//tag 2 is for red nodes that phosphorylate the query genes
                            bidx = bidx + 1;

                            blosum_nodeUnique.put(kinaseKey, newNode);
                            blosum_Kinase_Gene_NetworkNodes.add(newNode);
                        }

                        newEdgeNode = generateEdgeNode((int) ((JSONObject) blosum_nodeUnique.get(kinaseKey)).get("idx"),
                                (int) ((JSONObject) blosum_nodeUnique.get(inputGeneUpper)).get("idx"), blScore, 1);
                        blosum_Kinase_Gene_NetworkEdges.add(newEdgeNode);

                        if (blScore == 100.0) {
                            //Add it to definite_blosum network
                            if (!definite_blosum_nodeUnique.containsKey(kinaseKey)) {
                                newNode = generateNode(kinaseKey, kinaseKey, "", dbdx, 2, 0.0);//tag 2 is for red nodes that phosphorylate the query genes
                                dbdx = dbdx + 1;

                                definite_blosum_nodeUnique.put(kinaseKey, newNode);
                                definite_blosum_Kinase_Gene_NetworkNodes.add(newNode);
                            }
                            newEdgeNode = generateEdgeNode((int) ((JSONObject) definite_blosum_nodeUnique.get(kinaseKey)).get("idx"),
                                    (int) ((JSONObject) definite_blosum_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), blScore, 1);
                            definite_blosum_Kinase_Gene_NetworkEdges.add(newEdgeNode);

                            //Add it to indefinite network
                            if (!indefinite_nodeUnique.containsKey(kinaseKey)) {
                                newNode = generateNode(kinaseKey, kinaseKey, "", iidx, 2, 0.0);//tag 2 is for red nodes that phosphorylate the query genes
                                iidx = iidx + 1;

                                indefinite_nodeUnique.put(kinaseKey, newNode);
                                indefinite_Kinase_Gene_NetworkNodes.add(newNode);
                            }
                            newEdgeNode = generateEdgeNode((int) ((JSONObject) indefinite_nodeUnique.get(kinaseKey)).get("idx"),
                                    (int) ((JSONObject) indefinite_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), blScore, 1);
                            indefinite_Kinase_Gene_NetworkEdges.add(newEdgeNode);


                        }
                    }
                }
            }

            // Compute Network for the indefinite genes based on probability
            for (int i = 0; i < phosphoGeneSequenceArray.size(); i++) {
                System.out.println("==========================================");
                System.out.println("Compute Network for the indefinite genes based on probability");
                //log.info("inputgene");
                //log.info(input[i]);
                JSONObject phosphoGeneSequenceJson2 = (JSONObject) phosphoGeneSequenceArray.get(i);
                System.out.println("phosphoGeneSequenceJson2");
                System.out.println(phosphoGeneSequenceJson2.toJSONString());
                String phosphoAmino = phosphoGeneSequenceJson2.get("amino").toString();
                String phosphoSequence = phosphoGeneSequenceJson2.get("sequence").toString();
                inputphosphoProtein = phosphoGeneSequenceJson2.get("phosphoProtein").toString();
                inputphosphoProteinUpper = inputphosphoProtein.toUpperCase();
                inputGene = phosphoGeneSequenceJson2.get("phosphoSite").toString();
                inputGeneUpper = inputGene.toUpperCase();

                //log.info(phosphoAmino);
                //log.info(phosphoSequence);
                //log.info(phosphoGene);
                JSONObject phosphoProbability = (JSONObject) phosphoGene2PrJson.get(phosphoAmino);
                //log.info(String.valueOf(phosphoProbability.size()));

                //JSONObject jObject = new JSONObject(phosphoProbability);
                int iteration = 0;
                for (Object key : phosphoProbability.keySet()) {
                    iteration++;
                    //log.info("---------------------------------");
                    //log.info(String.valueOf(iteration));
                    //based on you key types
                    String keyStr = (String) key;
                    Object keyvalue = phosphoProbability.get(keyStr);
                    JSONArray keyvalueJsonArray = (JSONArray) keyvalue;
                    Class cls = keyvalue.getClass();
                    ////System.out.println("The type of the object is: " + cls.getName());
                    //Print key and value
                    ////System.out.println("key: " + keyStr);
                    ////System.out.println ("value: " + keyvalueJsonArray.get(0));

                    //Compute probability
                    double pr = computeProbability(keyvalueJsonArray, phosphoSequence);
//System.out.println("pr " + pr );

                    if (pr > 0.0) {

                        if (!indefinite_nodeUnique.containsKey(keyStr)) {
                            //log.info(gene);
                            System.out.println(keyStr);

                            newNode = generateNode(keyStr, keyStr, "", iidx, 2, 0.0);//tag 2 is for red nodes that phosphorylate the query genes
                            iidx = iidx + 1;


                            indefinite_nodeUnique.put(keyStr, newNode);
                            indefinite_Kinase_Gene_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) indefinite_nodeUnique.get(keyStr)).get("idx"),
                                (int) ((JSONObject) indefinite_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), pr, 2);
                        indefinite_Kinase_Gene_NetworkEdges.add(newEdgeNode);


                    }

                }


            }


            // Compute Network for the definite genes
            for (int i = 0; i < phosphoGeneSequenceArray.size(); i++) {
                log.info("==========================================");
                log.info("Compute Network for the definite genes");
                JSONObject phosphoGeneSequenceJson2 = (JSONObject) phosphoGeneSequenceArray.get(i);
//            String phosphoAmino = phosphoGeneSequenceJson2.get("amino").toString();
//            String phosphoSequence = phosphoGeneSequenceJson2.get("sequence").toString();
                inputGene = phosphoGeneSequenceJson2.get("phosphoSite").toString();
                inputphosphoProtein = phosphoGeneSequenceJson2.get("phosphoProtein").toString();
                //inputGene = protList[i];
                inputphosphoProteinUpper = inputphosphoProtein.toUpperCase();
                inputGeneUpper = inputGene.toUpperCase();


//            log.info("inputgene");
//            //System.out.println(protList[i]);
                gene2KinaseList = new JSONArray();


                if (phosphoGene2KinaseJson.containsKey(inputGeneUpper)) {
                    //log.info("gene2KinaseList");
                    gene2KinaseList = (JSONArray) (phosphoGene2KinaseJson.get(inputGeneUpper));
                    //log.info(gene2KinaseList.toString());
                }


                //Gene2Kinase ========================================
                //Gene2Kinase ========================================
                for (int j = 0; j < gene2KinaseList.size(); j++) {
                    gene = (String) gene2KinaseList.get(j);
                    //log.info("kinase");
                    //log.info(gene);
                    if (!definite_nodeUnique.containsKey(gene)) {
                        //log.info(gene);


                        newNode = generateNode(gene, gene, "", didx, 2, 0.0);//tag 2 is for red nodes that phosphorylate the query genes
                        didx = didx + 1;


                        definite_nodeUnique.put(gene, newNode);
                        definite_Kinase_Gene_NetworkNodes.add(newNode);
                    }
                    newEdgeNode = generateEdgeNode((int) ((JSONObject) definite_nodeUnique.get(gene)).get("idx"), (int) ((JSONObject) definite_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), 100.0, 1);
                    definite_Kinase_Gene_NetworkEdges.add(newEdgeNode);
                }
            }
            //==============================================




            for (int i = 0; i < protList.length; i++) {

                inputGene = protList[i];
                inputGeneUpper = inputGene.toUpperCase();
                String inputGeneProtein = (String) protListToProtein.get(protList[i]);
                JSONArray gene2SignorList = new JSONArray();

                if (signorProteinToProteinJson.containsKey(inputGeneProtein)) {
                    //log.info("gene2KinaseList");
                    gene2SignorList = (JSONArray) (signorProteinToProteinJson.get(inputGeneProtein));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < gene2SignorList.size(); j++) {
                    JSONObject ptmAndScore = (JSONObject) gene2SignorList.get(j);
                    //log.info("kinase");
                    //log.info(gene);
                    //{"upordown": "down", "enzyme_AC": "Q04771(ACVR1)", "effect": "down-regulates activity", "mechanism": "", "id": "P31749"}
                    String enzyme = (String) ptmAndScore.get("enzyme_AC");
                    int tag = 0;
                    String upOrDown = (String) ptmAndScore.get("upordown");
                    String effect = (String) ptmAndScore.get("effect");
                    String mechanism = (String) ptmAndScore.get("mechanism");
                    String id = (String) ptmAndScore.get("id");

                    if (upOrDown.equals("up")) {
                        tag = 2;
                    }
                    if (upOrDown.equals("down")) {
                        tag = 1;
                    }
                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) signor_nodeUnique.get(inputGeneUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputGeneUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputGeneUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!signor_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", sidx, 12, 0.0);//tag 2 is for protein-protein-interaction
                            sidx = sidx + 1;


                            signor_nodeUnique.put(enzyme, newNode);
                            signor_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) signor_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) signor_nodeUnique.get(inputGeneUpper)).get("idx"), 1, tag);
                        signor_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) signor_nodeUnique.get(inputGeneUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputGeneUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputGeneUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject signorTableJson = new JSONObject();

                    signorTableJson.put("PTMProtein", inputGene);
                    signorTableJson.put("effect", effect);
                    signorTableJson.put("mechanism", mechanism);
                    signorTableJson.put("id", id);
                    signorTableJson.put("enzyme", enzyme);
                    signorTableJson.put("target", inputGeneProtein);


                    signorTableArray.add(signorTableJson);
                }
            }




//
//
//                if (!signor_nodeUnique.containsKey(enzyme)) {
//                    //log.info(gene);
//
//
//                    newNode = generateNode(enzyme, enzyme, "", sidx, 12, 0.0);//tag 2 is for protein-protein-interaction
//                    sidx = sidx + 1;
//
//
//                    signor_nodeUnique.put(enzyme, newNode);
//                    signor_NetworkNodes.add(newNode);
//                }
//                newEdgeNode = generateEdgeNode((int) ((JSONObject) signor_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) signor_nodeUnique.get(inputGeneUpper)).get("idx"), 1, tag);
//                signor_NetworkEdges.add(newEdgeNode);
//
//                if (((JSONObject) signor_nodeUnique.get(inputGeneUpper)).get("connected") == "No") {
//                    ((JSONObject) signor_nodeUnique.get(inputGeneUpper)).remove("connected");
//                    ((JSONObject) signor_nodeUnique.get(inputGeneUpper)).put("connected", "Yes");
//                }
//


                //---------------------------------------
                //For ptmTableArray


//                //System.out.println("deepPhosResults=");
//                //System.out.println(deepPhosResults);
//            }catch (Exception e){
//                //System.out.println("-------- Error in deepPhosResults");
//                //System.out.println(deepPhosResults);
//
//            }







            // Compute Network for the iptmnet genes
            for (int i = 0; i < ptmPhArray.size(); i++) {
                //System.out.println("==========================================");
                //System.out.println("Compute Network for the ptm genes");
                JSONObject phosphoGeneSequenceJson2 = (JSONObject) ptmPhArray.get(i);
//            String phosphoAmino = phosphoGeneSequenceJson2.get("amino").toString();
//            String phosphoSequence = phosphoGeneSequenceJson2.get("sequence").toString();
                inputGene = phosphoGeneSequenceJson2.get("phosphoSite").toString();
                String shorthand = phosphoGeneSequenceJson2.get("shorthand").toString();
                inputphosphoProtein = phosphoGeneSequenceJson2.get("phosphoProtein").toString();
                //inputGene = protList[i];
                inputphosphoProteinUpper = inputphosphoProtein.toUpperCase();
                //inputGeneUpper = inputGene.toUpperCase();


//            log.info("inputgene");
//            //System.out.println(protList[i]);
                JSONArray gene2PtmList = new JSONArray();



                //This part if for signor
                //This part if for signor
                JSONArray ptm2SignorList = new JSONArray();

                if (signorPtmToProteinJson.containsKey(inputGene)) {
                    //log.info("gene2KinaseList");
                    ptm2SignorList = (JSONArray) (signorPtmToProteinJson.get(inputGene));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < ptm2SignorList.size(); j++) {
                    JSONObject signorAndEffect = (JSONObject) ptm2SignorList.get(j);
                    //log.info("kinase");
                    //log.info(gene);
                    //{"upordown": "down", "enzyme_AC": "Q04771(ACVR1)", "effect": "down-regulates activity", "mechanism": "", "id": "P31749"}
                    String enzyme = (String) signorAndEffect.get("enzyme_AC");
                    int tag = 0;
                    String upOrDown = (String) signorAndEffect.get("upordown");
                    String effect = (String) signorAndEffect.get("effect");
                    String mechanism = (String) signorAndEffect.get("mechanism");
                    String id = (String) signorAndEffect.get("id");

                    if (upOrDown.equals("up")) {
                        tag = 2;
                    }
                    if (upOrDown.equals("down")) {
                        tag = 1;
                    }
                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!signor_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", sidx, 1, 0.0);//tag 2 is for protein-protein-interaction
                            sidx = sidx + 1;


                            signor_nodeUnique.put(enzyme, newNode);
                            signor_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) signor_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), 1, tag);
                        signor_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject signorTableJson = new JSONObject();

                    signorTableJson.put("PTMProtein", inputphosphoProteinUpper);
                    signorTableJson.put("effect", effect);
                    signorTableJson.put("mechanism", mechanism);
                    signorTableJson.put("id", id);
                    signorTableJson.put("enzyme", enzyme);
                    signorTableJson.put("target", inputGene);


                    signorTableArray.add(signorTableJson);
                }
                //End of signor
                //End of signor












                //System.out.println(inputGene);
                if (ptmPhJson.containsKey(inputGene)) {
                    //System.out.println("gene2KinaseList");
                    gene2PtmList = (JSONArray) (ptmPhJson.get(inputGene));
                    //System.out.println(gene2KinaseList.toString());
                }
                //System.out.println("------");
                for (int j = 0; j < gene2PtmList.size(); j++) {
                    JSONObject ptmAndScore = (JSONObject) gene2PtmList.get(j);
                    //log.info("kinase");
                    //log.info(gene);

                    String enzyme = (String) ptmAndScore.get("enzyme_AC");
                    double score = Double.parseDouble((String) ptmAndScore.get("score"));
                    //Sometimes it is 0!!!!
                    if (score < 1) {
                        score = 1;
                    }
                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!ptm_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", pidx, 1, 0.0);//tag 1 is for red nodes that phosphorylate the query genes
                            pidx = pidx + 1;


                            ptm_nodeUnique.put(enzyme, newNode);
                            ptm_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) ptm_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), score, 2);
                        ptm_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject ptmTableJson = new JSONObject();

                    ptmTableJson.put("PTMProtein", inputphosphoProtein);
                    ptmTableJson.put("PTM", shorthand);
                    ptmTableJson.put("amino", phosphoGeneSequenceJson2.get("amino").toString());
                    ptmTableJson.put("site", phosphoGeneSequenceJson2.get("site").toString());
                    ptmTableJson.put("enzyme", enzyme);
                    ptmTableJson.put("type", "Phosphorylation");
                    ptmTableJson.put("score", score);

                    ptmTableArray.add(ptmTableJson);
                }
            }
            //==============================================

            for (int i = 0; i < ptmAcArray.size(); i++) {
                log.info("==========================================");
                log.info("Compute Network for the ptm genes");
                JSONObject phosphoGeneSequenceJson2 = (JSONObject) ptmAcArray.get(i);
//            String phosphoAmino = phosphoGeneSequenceJson2.get("amino").toString();
//            String phosphoSequence = phosphoGeneSequenceJson2.get("sequence").toString();
                inputGene = phosphoGeneSequenceJson2.get("phosphoSite").toString();
                inputphosphoProtein = phosphoGeneSequenceJson2.get("phosphoProtein").toString();
                String shorthand = phosphoGeneSequenceJson2.get("shorthand").toString();
                //inputGene = protList[i];
                inputphosphoProteinUpper = inputphosphoProtein.toUpperCase();
                //inputGeneUpper = inputGene.toUpperCase();




                //This part if for signor
                //This part if for signor
                JSONArray ptm2SignorList = new JSONArray();

                if (signorPtmToProteinJson.containsKey(inputGene)) {
                    //log.info("gene2KinaseList");
                    ptm2SignorList = (JSONArray) (signorPtmToProteinJson.get(inputGene));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < ptm2SignorList.size(); j++) {
                    JSONObject signorAndEffect = (JSONObject) ptm2SignorList.get(j);
                    //log.info("kinase");
                    //log.info(gene);
                    //{"upordown": "down", "enzyme_AC": "Q04771(ACVR1)", "effect": "down-regulates activity", "mechanism": "", "id": "P31749"}
                    String enzyme = (String) signorAndEffect.get("enzyme_AC");
                    int tag = 0;
                    String upOrDown = (String) signorAndEffect.get("upordown");
                    String effect = (String) signorAndEffect.get("effect");
                    String mechanism = (String) signorAndEffect.get("mechanism");
                    String id = (String) signorAndEffect.get("id");

                    if (upOrDown.equals("up")) {
                        tag = 2;
                    }
                    if (upOrDown.equals("down")) {
                        tag = 1;
                    }
                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!signor_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", sidx, 2, 0.0);//tag 2 is for protein-protein-interaction
                            sidx = sidx + 1;


                            signor_nodeUnique.put(enzyme, newNode);
                            signor_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) signor_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), 1, tag);
                        signor_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject signorTableJson = new JSONObject();

                    signorTableJson.put("PTMProtein", inputphosphoProteinUpper);
                    signorTableJson.put("effect", effect);
                    signorTableJson.put("mechanism", mechanism);
                    signorTableJson.put("id", id);
                    signorTableJson.put("enzyme", enzyme);
                    signorTableJson.put("target", inputGene);


                    signorTableArray.add(signorTableJson);
                }
                //End of signor
                //End of signor





//            log.info("inputgene");
//            //System.out.println(protList[i]);
                JSONArray gene2PtmList = new JSONArray();


                if (ptmAcJson.containsKey(inputGene)) {
                    //log.info("gene2KinaseList");
                    gene2PtmList = (JSONArray) (ptmAcJson.get(inputGene));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < gene2PtmList.size(); j++) {
                    JSONObject ptmAndScore = (JSONObject) gene2PtmList.get(j);
                    //log.info("kinase");
                    //log.info(gene);

                    String enzyme = (String) ptmAndScore.get("enzyme_AC");
                    double score = Double.parseDouble((String) ptmAndScore.get("score"));
                    if (score < 1) {
                        score = 1;
                    }
                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!ptm_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", pidx, 2, 0.0);//tag 2 is for acetylation
                            pidx = pidx + 1;


                            ptm_nodeUnique.put(enzyme, newNode);
                            ptm_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) ptm_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), score, 3);
                        ptm_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject ptmTableJson = new JSONObject();

                    ptmTableJson.put("PTMProtein", inputphosphoProtein);
                    ptmTableJson.put("PTM", shorthand);
                    ptmTableJson.put("amino", phosphoGeneSequenceJson2.get("amino").toString());
                    ptmTableJson.put("site", phosphoGeneSequenceJson2.get("site").toString());
                    ptmTableJson.put("enzyme", enzyme);
                    ptmTableJson.put("type", "Acetylation");
                    ptmTableJson.put("score", score);

                    ptmTableArray.add(ptmTableJson);
                }
            }

            //==============================================
            for (int i = 0; i < ptmMeArray.size(); i++) {
                log.info("==========================================");
                log.info("Compute Network for the ptm genes");
                JSONObject phosphoGeneSequenceJson2 = (JSONObject) ptmMeArray.get(i);
//            String phosphoAmino = phosphoGeneSequenceJson2.get("amino").toString();
//            String phosphoSequence = phosphoGeneSequenceJson2.get("sequence").toString();
                inputGene = phosphoGeneSequenceJson2.get("phosphoSite").toString();
                inputphosphoProtein = phosphoGeneSequenceJson2.get("phosphoProtein").toString();
                String shorthand = phosphoGeneSequenceJson2.get("shorthand").toString();
                //inputGene = protList[i];
                inputphosphoProteinUpper = inputphosphoProtein.toUpperCase();
                //inputGeneUpper = inputGene.toUpperCase();




                //This part if for signor
                //This part if for signor
                JSONArray ptm2SignorList = new JSONArray();

                if (signorPtmToProteinJson.containsKey(inputGene)) {
                    //log.info("gene2KinaseList");
                    ptm2SignorList = (JSONArray) (signorPtmToProteinJson.get(inputGene));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < ptm2SignorList.size(); j++) {
                    JSONObject signorAndEffect = (JSONObject) ptm2SignorList.get(j);
                    //log.info("kinase");
                    //log.info(gene);
                    //{"upordown": "down", "enzyme_AC": "Q04771(ACVR1)", "effect": "down-regulates activity", "mechanism": "", "id": "P31749"}
                    String enzyme = (String) signorAndEffect.get("enzyme_AC");
                    int tag = 0;
                    String upOrDown = (String) signorAndEffect.get("upordown");
                    String effect = (String) signorAndEffect.get("effect");
                    String mechanism = (String) signorAndEffect.get("mechanism");
                    String id = (String) signorAndEffect.get("id");

                    if (upOrDown.equals("up")) {
                        tag = 2;
                    }
                    if (upOrDown.equals("down")) {
                        tag = 1;
                    }
                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!signor_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", sidx, 3, 0.0);//tag 2 is for protein-protein-interaction
                            sidx = sidx + 1;


                            signor_nodeUnique.put(enzyme, newNode);
                            signor_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) signor_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), 1, tag);
                        signor_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject signorTableJson = new JSONObject();

                    signorTableJson.put("PTMProtein", inputphosphoProteinUpper);
                    signorTableJson.put("effect", effect);
                    signorTableJson.put("mechanism", mechanism);
                    signorTableJson.put("id", id);
                    signorTableJson.put("enzyme", enzyme);
                    signorTableJson.put("target", inputGene);


                    signorTableArray.add(signorTableJson);
                }
                //End of signor
                //End of signor





//            log.info("inputgene");
//            //System.out.println(protList[i]);
                JSONArray gene2PtmList = new JSONArray();


                if (ptmMeJson.containsKey(inputGene)) {
                    //log.info("gene2KinaseList");
                    gene2PtmList = (JSONArray) (ptmMeJson.get(inputGene));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < gene2PtmList.size(); j++) {
                    JSONObject ptmAndScore = (JSONObject) gene2PtmList.get(j);
                    //log.info("kinase");
                    //log.info(gene);

                    String enzyme = (String) ptmAndScore.get("enzyme_AC");
                    double score = Double.parseDouble((String) ptmAndScore.get("score"));
                    if (score < 1) {
                        score = 1;
                    }


                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!ptm_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", pidx, 3, 0.0);//tag 3 is for methylation
                            pidx = pidx + 1;


                            ptm_nodeUnique.put(enzyme, newNode);
                            ptm_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) ptm_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), score, 2);
                        ptm_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject ptmTableJson = new JSONObject();

                    ptmTableJson.put("PTMProtein", inputphosphoProtein);
                    ptmTableJson.put("PTM", shorthand);
                    ptmTableJson.put("amino", phosphoGeneSequenceJson2.get("amino").toString());
                    ptmTableJson.put("site", phosphoGeneSequenceJson2.get("site").toString());
                    ptmTableJson.put("enzyme", enzyme);
                    ptmTableJson.put("type", "Methylation");
                    ptmTableJson.put("score", score);

                    ptmTableArray.add(ptmTableJson);
                }
            }
            //==============================================
            //==============================================
            for (int i = 0; i < ptmCgArray.size(); i++) {
                log.info("==========================================");
                log.info("Compute Network for the ptm genes");
                JSONObject phosphoGeneSequenceJson2 = (JSONObject) ptmCgArray.get(i);
//            String phosphoAmino = phosphoGeneSequenceJson2.get("amino").toString();
//            String phosphoSequence = phosphoGeneSequenceJson2.get("sequence").toString();
                inputGene = phosphoGeneSequenceJson2.get("phosphoSite").toString();
                inputphosphoProtein = phosphoGeneSequenceJson2.get("phosphoProtein").toString();
                String shorthand = phosphoGeneSequenceJson2.get("shorthand").toString();
                //inputGene = protList[i];
                inputphosphoProteinUpper = inputphosphoProtein.toUpperCase();
                //inputGeneUpper = inputGene.toUpperCase();




                //This part if for signor
                //This part if for signor
                JSONArray ptm2SignorList = new JSONArray();

                if (signorPtmToProteinJson.containsKey(inputGene)) {
                    //log.info("gene2KinaseList");
                    ptm2SignorList = (JSONArray) (signorPtmToProteinJson.get(inputGene));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < ptm2SignorList.size(); j++) {
                    JSONObject signorAndEffect = (JSONObject) ptm2SignorList.get(j);
                    //log.info("kinase");
                    //log.info(gene);
                    //{"upordown": "down", "enzyme_AC": "Q04771(ACVR1)", "effect": "down-regulates activity", "mechanism": "", "id": "P31749"}
                    String enzyme = (String) signorAndEffect.get("enzyme_AC");
                    int tag = 0;
                    String upOrDown = (String) signorAndEffect.get("upordown");
                    String effect = (String) signorAndEffect.get("effect");
                    String mechanism = (String) signorAndEffect.get("mechanism");
                    String id = (String) signorAndEffect.get("id");

                    if (upOrDown.equals("up")) {
                        tag = 2;
                    }
                    if (upOrDown.equals("down")) {
                        tag = 1;
                    }
                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!signor_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", sidx, 4, 0.0);//tag 2 is for protein-protein-interaction
                            sidx = sidx + 1;


                            signor_nodeUnique.put(enzyme, newNode);
                            signor_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) signor_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), 1, tag);
                        signor_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject signorTableJson = new JSONObject();

                    signorTableJson.put("PTMProtein", inputphosphoProteinUpper);
                    signorTableJson.put("effect", effect);
                    signorTableJson.put("mechanism", mechanism);
                    signorTableJson.put("id", id);
                    signorTableJson.put("enzyme", enzyme);
                    signorTableJson.put("target", inputGene);


                    signorTableArray.add(signorTableJson);
                }
                //End of signor
                //End of signor




//            log.info("inputgene");
//            //System.out.println(protList[i]);
                JSONArray gene2PtmList = new JSONArray();


                if (ptmCgJson.containsKey(inputGene)) {
                    //log.info("gene2KinaseList");
                    gene2PtmList = (JSONArray) (ptmCgJson.get(inputGene));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < gene2PtmList.size(); j++) {
                    JSONObject ptmAndScore = (JSONObject) gene2PtmList.get(j);
                    //log.info("kinase");
                    //log.info(gene);

                    String enzyme = (String) ptmAndScore.get("enzyme_AC");
                    double score = Double.parseDouble((String) ptmAndScore.get("score"));
                    if (score < 1) {
                        score = 1;
                    }


                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!ptm_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", pidx, 4, 0.0);//tag 4 is for cg
                            pidx = pidx + 1;


                            ptm_nodeUnique.put(enzyme, newNode);
                            ptm_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) ptm_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), score, 2);
                        ptm_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject ptmTableJson = new JSONObject();

                    ptmTableJson.put("PTMProtein", inputphosphoProtein);
                    ptmTableJson.put("PTM", shorthand);
                    ptmTableJson.put("amino", phosphoGeneSequenceJson2.get("amino").toString());
                    ptmTableJson.put("site", phosphoGeneSequenceJson2.get("site").toString());
                    ptmTableJson.put("enzyme", enzyme);
                    ptmTableJson.put("type", "C-Glycosylation");
                    ptmTableJson.put("score", score);

                    ptmTableArray.add(ptmTableJson);
                }
            }
//        ptmCg: /PTM/ptm_cg.json
            //==============================================
            //==============================================
            for (int i = 0; i < ptmMyArray.size(); i++) {
                log.info("==========================================");
                log.info("Compute Network for the ptm genes");
                JSONObject phosphoGeneSequenceJson2 = (JSONObject) ptmMyArray.get(i);
//            String phosphoAmino = phosphoGeneSequenceJson2.get("amino").toString();
//            String phosphoSequence = phosphoGeneSequenceJson2.get("sequence").toString();
                inputGene = phosphoGeneSequenceJson2.get("phosphoSite").toString();
                inputphosphoProtein = phosphoGeneSequenceJson2.get("phosphoProtein").toString();
                String shorthand = phosphoGeneSequenceJson2.get("shorthand").toString();
                //inputGene = protList[i];
                inputphosphoProteinUpper = inputphosphoProtein.toUpperCase();
                //inputGeneUpper = inputGene.toUpperCase();



                //This part if for signor
                //This part if for signor
                JSONArray ptm2SignorList = new JSONArray();

                if (signorPtmToProteinJson.containsKey(inputGene)) {
                    //log.info("gene2KinaseList");
                    ptm2SignorList = (JSONArray) (signorPtmToProteinJson.get(inputGene));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < ptm2SignorList.size(); j++) {
                    JSONObject signorAndEffect = (JSONObject) ptm2SignorList.get(j);
                    //log.info("kinase");
                    //log.info(gene);
                    //{"upordown": "down", "enzyme_AC": "Q04771(ACVR1)", "effect": "down-regulates activity", "mechanism": "", "id": "P31749"}
                    String enzyme = (String) signorAndEffect.get("enzyme_AC");
                    int tag = 0;
                    String upOrDown = (String) signorAndEffect.get("upordown");
                    String effect = (String) signorAndEffect.get("effect");
                    String mechanism = (String) signorAndEffect.get("mechanism");
                    String id = (String) signorAndEffect.get("id");

                    if (upOrDown.equals("up")) {
                        tag = 2;
                    }
                    if (upOrDown.equals("down")) {
                        tag = 1;
                    }
                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!signor_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", sidx, 5, 0.0);//tag 2 is for protein-protein-interaction
                            sidx = sidx + 1;


                            signor_nodeUnique.put(enzyme, newNode);
                            signor_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) signor_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), 1, tag);
                        signor_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject signorTableJson = new JSONObject();

                    signorTableJson.put("PTMProtein", inputphosphoProteinUpper);
                    signorTableJson.put("effect", effect);
                    signorTableJson.put("mechanism", mechanism);
                    signorTableJson.put("id", id);
                    signorTableJson.put("enzyme", enzyme);
                    signorTableJson.put("target", inputGene);


                    signorTableArray.add(signorTableJson);
                }
                //End of signor
                //End of signor


//            log.info("inputgene");
//            //System.out.println(protList[i]);
                JSONArray gene2PtmList = new JSONArray();


                if (ptmMyJson.containsKey(inputGene)) {
                    //log.info("gene2KinaseList");
                    gene2PtmList = (JSONArray) (ptmMyJson.get(inputGene));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < gene2PtmList.size(); j++) {
                    JSONObject ptmAndScore = (JSONObject) gene2PtmList.get(j);
                    //log.info("kinase");
                    //log.info(gene);

                    String enzyme = (String) ptmAndScore.get("enzyme_AC");
                    double score = Double.parseDouble((String) ptmAndScore.get("score"));
                    if (score < 1) {
                        score = 1;
                    }


                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!ptm_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", pidx, 5, 0.0);//tag 5 is for My
                            pidx = pidx + 1;


                            ptm_nodeUnique.put(enzyme, newNode);
                            ptm_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) ptm_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), score, 2);
                        ptm_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject ptmTableJson = new JSONObject();

                    ptmTableJson.put("PTMProtein", inputphosphoProtein);
                    ptmTableJson.put("PTM", shorthand);
                    ptmTableJson.put("amino", phosphoGeneSequenceJson2.get("amino").toString());
                    ptmTableJson.put("site", phosphoGeneSequenceJson2.get("site").toString());
                    ptmTableJson.put("enzyme", enzyme);
                    ptmTableJson.put("type", "Myristoylation");
                    ptmTableJson.put("score", score);

                    ptmTableArray.add(ptmTableJson);
                }
            }
//        ptmMy: /PTM/ptm_my.json
//        ptmNg: /PTM/ptm_ng.json
            //==============================================
            //==============================================
            for (int i = 0; i < ptmNgArray.size(); i++) {
                log.info("==========================================");
                log.info("Compute Network for the ptm genes");
                JSONObject phosphoGeneSequenceJson2 = (JSONObject) ptmNgArray.get(i);
//            String phosphoAmino = phosphoGeneSequenceJson2.get("amino").toString();
//            String phosphoSequence = phosphoGeneSequenceJson2.get("sequence").toString();
                inputGene = phosphoGeneSequenceJson2.get("phosphoSite").toString();
                inputphosphoProtein = phosphoGeneSequenceJson2.get("phosphoProtein").toString();
                String shorthand = phosphoGeneSequenceJson2.get("shorthand").toString();
                //inputGene = protList[i];
                inputphosphoProteinUpper = inputphosphoProtein.toUpperCase();
                //inputGeneUpper = inputGene.toUpperCase();


                //This part if for signor
                //This part if for signor
                JSONArray ptm2SignorList = new JSONArray();

                if (signorPtmToProteinJson.containsKey(inputGene)) {
                    //log.info("gene2KinaseList");
                    ptm2SignorList = (JSONArray) (signorPtmToProteinJson.get(inputGene));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < ptm2SignorList.size(); j++) {
                    JSONObject signorAndEffect = (JSONObject) ptm2SignorList.get(j);
                    //log.info("kinase");
                    //log.info(gene);
                    //{"upordown": "down", "enzyme_AC": "Q04771(ACVR1)", "effect": "down-regulates activity", "mechanism": "", "id": "P31749"}
                    String enzyme = (String) signorAndEffect.get("enzyme_AC");
                    int tag = 0;
                    String upOrDown = (String) signorAndEffect.get("upordown");
                    String effect = (String) signorAndEffect.get("effect");
                    String mechanism = (String) signorAndEffect.get("mechanism");
                    String id = (String) signorAndEffect.get("id");

                    if (upOrDown.equals("up")) {
                        tag = 2;
                    }
                    if (upOrDown.equals("down")) {
                        tag = 1;
                    }
                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!signor_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", sidx, 6, 0.0);//tag 2 is for protein-protein-interaction
                            sidx = sidx + 1;


                            signor_nodeUnique.put(enzyme, newNode);
                            signor_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) signor_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), 1, tag);
                        signor_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject signorTableJson = new JSONObject();

                    signorTableJson.put("PTMProtein", inputphosphoProteinUpper);
                    signorTableJson.put("effect", effect);
                    signorTableJson.put("mechanism", mechanism);
                    signorTableJson.put("id", id);
                    signorTableJson.put("enzyme", enzyme);
                    signorTableJson.put("target", inputGene);


                    signorTableArray.add(signorTableJson);
                }
                //End of signor
                //End of signor


//            log.info("inputgene");
//            //System.out.println(protList[i]);
                JSONArray gene2PtmList = new JSONArray();


                if (ptmNgJson.containsKey(inputGene)) {
                    //log.info("gene2KinaseList");
                    gene2PtmList = (JSONArray) (ptmNgJson.get(inputGene));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < gene2PtmList.size(); j++) {
                    JSONObject ptmAndScore = (JSONObject) gene2PtmList.get(j);
                    //log.info("kinase");
                    //log.info(gene);

                    String enzyme = (String) ptmAndScore.get("enzyme_AC");
                    double score = Double.parseDouble((String) ptmAndScore.get("score"));
                    if (score < 1) {
                        score = 1;
                    }


                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!ptm_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", pidx, 6, 0.0);//tag 6 is for Ng
                            pidx = pidx + 1;


                            ptm_nodeUnique.put(enzyme, newNode);
                            ptm_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) ptm_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), score, 2);
                        ptm_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject ptmTableJson = new JSONObject();

                    ptmTableJson.put("PTMProtein", inputphosphoProtein);
                    ptmTableJson.put("PTM", shorthand);
                    ptmTableJson.put("amino", phosphoGeneSequenceJson2.get("amino").toString());
                    ptmTableJson.put("site", phosphoGeneSequenceJson2.get("site").toString());
                    ptmTableJson.put("enzyme", enzyme);
                    ptmTableJson.put("type", "N-Glycosylation");
                    ptmTableJson.put("score", score);

                    ptmTableArray.add(ptmTableJson);
                }
            }
//        ptmOg: /PTM/ptm_og.json

            //=========================================================================
            //=========================================================================
            //=========================================================================
            //=========================================================================
            for (int i = 0; i < ptmOgArray.size(); i++) {
                log.info("==========================================");
                log.info("Compute Network for the ptm genes");
                JSONObject phosphoGeneSequenceJson2 = (JSONObject) ptmOgArray.get(i);
//            String phosphoAmino = phosphoGeneSequenceJson2.get("amino").toString();
//            String phosphoSequence = phosphoGeneSequenceJson2.get("sequence").toString();
                inputGene = phosphoGeneSequenceJson2.get("phosphoSite").toString();
                inputphosphoProtein = phosphoGeneSequenceJson2.get("phosphoProtein").toString();
                String shorthand = phosphoGeneSequenceJson2.get("shorthand").toString();
                //inputGene = protList[i];
                inputphosphoProteinUpper = inputphosphoProtein.toUpperCase();
                //inputGeneUpper = inputGene.toUpperCase();





                //This part if for signor
                //This part if for signor
                JSONArray ptm2SignorList = new JSONArray();

                if (signorPtmToProteinJson.containsKey(inputGene)) {
                    //log.info("gene2KinaseList");
                    ptm2SignorList = (JSONArray) (signorPtmToProteinJson.get(inputGene));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < ptm2SignorList.size(); j++) {
                    JSONObject signorAndEffect = (JSONObject) ptm2SignorList.get(j);
                    //log.info("kinase");
                    //log.info(gene);
                    //{"upordown": "down", "enzyme_AC": "Q04771(ACVR1)", "effect": "down-regulates activity", "mechanism": "", "id": "P31749"}
                    String enzyme = (String) signorAndEffect.get("enzyme_AC");
                    int tag = 0;
                    String upOrDown = (String) signorAndEffect.get("upordown");
                    String effect = (String) signorAndEffect.get("effect");
                    String mechanism = (String) signorAndEffect.get("mechanism");
                    String id = (String) signorAndEffect.get("id");

                    if (upOrDown.equals("up")) {
                        tag = 2;
                    }
                    if (upOrDown.equals("down")) {
                        tag = 1;
                    }
                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!signor_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", sidx, 7, 0.0);//tag 2 is for protein-protein-interaction
                            sidx = sidx + 1;


                            signor_nodeUnique.put(enzyme, newNode);
                            signor_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) signor_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), 1, tag);
                        signor_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject signorTableJson = new JSONObject();

                    signorTableJson.put("PTMProtein", inputphosphoProteinUpper);
                    signorTableJson.put("effect", effect);
                    signorTableJson.put("mechanism", mechanism);
                    signorTableJson.put("id", id);
                    signorTableJson.put("enzyme", enzyme);
                    signorTableJson.put("target", inputGene);


                    signorTableArray.add(signorTableJson);
                }
                //End of signor
                //End of signor




//            log.info("inputgene");
//            //System.out.println(protList[i]);
                JSONArray gene2PtmList = new JSONArray();


                if (ptmOgJson.containsKey(inputGene)) {
                    //log.info("gene2KinaseList");
                    gene2PtmList = (JSONArray) (ptmOgJson.get(inputGene));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < gene2PtmList.size(); j++) {
                    JSONObject ptmAndScore = (JSONObject) gene2PtmList.get(j);
                    //log.info("kinase");
                    //log.info(gene);

                    String enzyme = (String) ptmAndScore.get("enzyme_AC");
                    double score = Double.parseDouble((String) ptmAndScore.get("score"));
                    if (score < 1) {
                        score = 1;
                    }


                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!ptm_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", pidx, 7, 0.0);//tag 7 is for Og
                            pidx = pidx + 1;


                            ptm_nodeUnique.put(enzyme, newNode);
                            ptm_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) ptm_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), score, 2);
                        ptm_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject ptmTableJson = new JSONObject();

                    ptmTableJson.put("PTMProtein", inputphosphoProtein);
                    ptmTableJson.put("PTM", shorthand);
                    ptmTableJson.put("amino", phosphoGeneSequenceJson2.get("amino").toString());
                    ptmTableJson.put("site", phosphoGeneSequenceJson2.get("site").toString());
                    ptmTableJson.put("enzyme", enzyme);
                    ptmTableJson.put("type", "O-Glycosylation");
                    ptmTableJson.put("score", score);

                    ptmTableArray.add(ptmTableJson);
                }
            }

//        ptmSg: /PTM/ptm_sg.json
            //=========================================================================
            //=========================================================================
            //=========================================================================
            //=========================================================================
            for (int i = 0; i < ptmSgArray.size(); i++) {
                log.info("==========================================");
                log.info("Compute Network for the ptm genes");
                JSONObject phosphoGeneSequenceJson2 = (JSONObject) ptmSgArray.get(i);
//            String phosphoAmino = phosphoGeneSequenceJson2.get("amino").toString();
//            String phosphoSequence = phosphoGeneSequenceJson2.get("sequence").toString();
                inputGene = phosphoGeneSequenceJson2.get("phosphoSite").toString();
                inputphosphoProtein = phosphoGeneSequenceJson2.get("phosphoProtein").toString();
                String shorthand = phosphoGeneSequenceJson2.get("shorthand").toString();
                //inputGene = protList[i];
                inputphosphoProteinUpper = inputphosphoProtein.toUpperCase();
                //inputGeneUpper = inputGene.toUpperCase();




                //This part if for signor
                //This part if for signor
                JSONArray ptm2SignorList = new JSONArray();

                if (signorPtmToProteinJson.containsKey(inputGene)) {
                    //log.info("gene2KinaseList");
                    ptm2SignorList = (JSONArray) (signorPtmToProteinJson.get(inputGene));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < ptm2SignorList.size(); j++) {
                    JSONObject signorAndEffect = (JSONObject) ptm2SignorList.get(j);
                    //log.info("kinase");
                    //log.info(gene);
                    //{"upordown": "down", "enzyme_AC": "Q04771(ACVR1)", "effect": "down-regulates activity", "mechanism": "", "id": "P31749"}
                    String enzyme = (String) signorAndEffect.get("enzyme_AC");
                    int tag = 0;
                    String upOrDown = (String) signorAndEffect.get("upordown");
                    String effect = (String) signorAndEffect.get("effect");
                    String mechanism = (String) signorAndEffect.get("mechanism");
                    String id = (String) signorAndEffect.get("id");

                    if (upOrDown.equals("up")) {
                        tag = 2;
                    }
                    if (upOrDown.equals("down")) {
                        tag = 1;
                    }
                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!signor_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", sidx, 8, 0.0);//tag 2 is for protein-protein-interaction
                            sidx = sidx + 1;


                            signor_nodeUnique.put(enzyme, newNode);
                            signor_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) signor_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), 1, tag);
                        signor_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject signorTableJson = new JSONObject();

                    signorTableJson.put("PTMProtein", inputphosphoProteinUpper);
                    signorTableJson.put("effect", effect);
                    signorTableJson.put("mechanism", mechanism);
                    signorTableJson.put("id", id);
                    signorTableJson.put("enzyme", enzyme);
                    signorTableJson.put("target", inputGene);


                    signorTableArray.add(signorTableJson);
                }
                //End of signor
                //End of signor


//            log.info("inputgene");
//            //System.out.println(protList[i]);
                JSONArray gene2PtmList = new JSONArray();


                if (ptmSgJson.containsKey(inputGene)) {
                    //log.info("gene2KinaseList");
                    gene2PtmList = (JSONArray) (ptmSgJson.get(inputGene));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < gene2PtmList.size(); j++) {
                    JSONObject ptmAndScore = (JSONObject) gene2PtmList.get(j);
                    //log.info("kinase");
                    //log.info(gene);

                    String enzyme = (String) ptmAndScore.get("enzyme_AC");
                    double score = Double.parseDouble((String) ptmAndScore.get("score"));
                    if (score < 1) {
                        score = 1;
                    }


                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!ptm_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", pidx, 8, 0.0);//tag 8 is for Sg
                            pidx = pidx + 1;


                            ptm_nodeUnique.put(enzyme, newNode);
                            ptm_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) ptm_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), score, 2);
                        ptm_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject ptmTableJson = new JSONObject();

                    ptmTableJson.put("PTMProtein", inputphosphoProtein);
                    ptmTableJson.put("PTM", shorthand);
                    ptmTableJson.put("amino", phosphoGeneSequenceJson2.get("amino").toString());
                    ptmTableJson.put("site", phosphoGeneSequenceJson2.get("site").toString());
                    ptmTableJson.put("enzyme", enzyme);
                    ptmTableJson.put("type", "S-Glycosylation");
                    ptmTableJson.put("score", score);

                    ptmTableArray.add(ptmTableJson);
                }
            }
//        ptmSn: /PTM/ptm_sn.json
            //=========================================================================
            //=========================================================================
            //=========================================================================
            //=========================================================================
            for (int i = 0; i < ptmSnArray.size(); i++) {
                log.info("==========================================");
                log.info("Compute Network for the ptm genes");
                JSONObject phosphoGeneSequenceJson2 = (JSONObject) ptmSnArray.get(i);
//            String phosphoAmino = phosphoGeneSequenceJson2.get("amino").toString();
//            String phosphoSequence = phosphoGeneSequenceJson2.get("sequence").toString();
                inputGene = phosphoGeneSequenceJson2.get("phosphoSite").toString();
                inputphosphoProtein = phosphoGeneSequenceJson2.get("phosphoProtein").toString();
                String shorthand = phosphoGeneSequenceJson2.get("shorthand").toString();
                //inputGene = protList[i];
                inputphosphoProteinUpper = inputphosphoProtein.toUpperCase();
                //inputGeneUpper = inputGene.toUpperCase();




                //This part if for signor
                //This part if for signor
                JSONArray ptm2SignorList = new JSONArray();

                if (signorPtmToProteinJson.containsKey(inputGene)) {
                    //log.info("gene2KinaseList");
                    ptm2SignorList = (JSONArray) (signorPtmToProteinJson.get(inputGene));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < ptm2SignorList.size(); j++) {
                    JSONObject signorAndEffect = (JSONObject) ptm2SignorList.get(j);
                    //log.info("kinase");
                    //log.info(gene);
                    //{"upordown": "down", "enzyme_AC": "Q04771(ACVR1)", "effect": "down-regulates activity", "mechanism": "", "id": "P31749"}
                    String enzyme = (String) signorAndEffect.get("enzyme_AC");
                    int tag = 0;
                    String upOrDown = (String) signorAndEffect.get("upordown");
                    String effect = (String) signorAndEffect.get("effect");
                    String mechanism = (String) signorAndEffect.get("mechanism");
                    String id = (String) signorAndEffect.get("id");

                    if (upOrDown.equals("up")) {
                        tag = 2;
                    }
                    if (upOrDown.equals("down")) {
                        tag = 1;
                    }
                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!signor_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", sidx, 9, 0.0);//tag 2 is for protein-protein-interaction
                            sidx = sidx + 1;


                            signor_nodeUnique.put(enzyme, newNode);
                            signor_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) signor_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), 1, tag);
                        signor_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject signorTableJson = new JSONObject();

                    signorTableJson.put("PTMProtein", inputphosphoProteinUpper);
                    signorTableJson.put("effect", effect);
                    signorTableJson.put("mechanism", mechanism);
                    signorTableJson.put("id", id);
                    signorTableJson.put("enzyme", enzyme);
                    signorTableJson.put("target", inputGene);


                    signorTableArray.add(signorTableJson);
                }
                //End of signor
                //End of signor




//            log.info("inputgene");
//            //System.out.println(protList[i]);
                JSONArray gene2PtmList = new JSONArray();


                if (ptmSnJson.containsKey(inputGene)) {
                    //log.info("gene2KinaseList");
                    gene2PtmList = (JSONArray) (ptmSnJson.get(inputGene));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < gene2PtmList.size(); j++) {
                    JSONObject ptmAndScore = (JSONObject) gene2PtmList.get(j);
                    //log.info("kinase");
                    //log.info(gene);

                    String enzyme = (String) ptmAndScore.get("enzyme_AC");
                    double score = Double.parseDouble((String) ptmAndScore.get("score"));
                    if (score < 1) {
                        score = 1;
                    }


                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!ptm_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", pidx, 9, 0.0);//tag 9 is for Sn
                            pidx = pidx + 1;


                            ptm_nodeUnique.put(enzyme, newNode);
                            ptm_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) ptm_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), score, 2);
                        ptm_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject ptmTableJson = new JSONObject();

                    ptmTableJson.put("PTMProtein", inputphosphoProtein);
                    ptmTableJson.put("PTM", shorthand);
                    ptmTableJson.put("amino", phosphoGeneSequenceJson2.get("amino").toString());
                    ptmTableJson.put("site", phosphoGeneSequenceJson2.get("site").toString());
                    ptmTableJson.put("enzyme", enzyme);
                    ptmTableJson.put("type", "S-Nitrosylation");
                    ptmTableJson.put("score", score);

                    ptmTableArray.add(ptmTableJson);
                }
            }
//        ptmSu: /PTM/ptm_su.json
            //=========================================================================
            //=========================================================================
            //=========================================================================
            //=========================================================================
            for (int i = 0; i < ptmSuArray.size(); i++) {
                log.info("==========================================");
                log.info("Compute Network for the ptm genes");
                JSONObject phosphoGeneSequenceJson2 = (JSONObject) ptmSuArray.get(i);
//            String phosphoAmino = phosphoGeneSequenceJson2.get("amino").toString();
//            String phosphoSequence = phosphoGeneSequenceJson2.get("sequence").toString();
                inputGene = phosphoGeneSequenceJson2.get("phosphoSite").toString();
                inputphosphoProtein = phosphoGeneSequenceJson2.get("phosphoProtein").toString();
                String shorthand = phosphoGeneSequenceJson2.get("shorthand").toString();
                //inputGene = protList[i];
                inputphosphoProteinUpper = inputphosphoProtein.toUpperCase();
                //inputGeneUpper = inputGene.toUpperCase();



                //This part if for signor
                //This part if for signor
                JSONArray ptm2SignorList = new JSONArray();

                if (signorPtmToProteinJson.containsKey(inputGene)) {
                    //log.info("gene2KinaseList");
                    ptm2SignorList = (JSONArray) (signorPtmToProteinJson.get(inputGene));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < ptm2SignorList.size(); j++) {
                    JSONObject signorAndEffect = (JSONObject) ptm2SignorList.get(j);
                    //log.info("kinase");
                    //log.info(gene);
                    //{"upordown": "down", "enzyme_AC": "Q04771(ACVR1)", "effect": "down-regulates activity", "mechanism": "", "id": "P31749"}
                    String enzyme = (String) signorAndEffect.get("enzyme_AC");
                    int tag = 0;
                    String upOrDown = (String) signorAndEffect.get("upordown");
                    String effect = (String) signorAndEffect.get("effect");
                    String mechanism = (String) signorAndEffect.get("mechanism");
                    String id = (String) signorAndEffect.get("id");

                    if (upOrDown.equals("up")) {
                        tag = 2;
                    }
                    if (upOrDown.equals("down")) {
                        tag = 1;
                    }
                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!signor_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", sidx, 10, 0.0);//tag 2 is for protein-protein-interaction
                            sidx = sidx + 1;


                            signor_nodeUnique.put(enzyme, newNode);
                            signor_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) signor_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), 1, tag);
                        signor_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject signorTableJson = new JSONObject();

                    signorTableJson.put("PTMProtein", inputphosphoProteinUpper);
                    signorTableJson.put("effect", effect);
                    signorTableJson.put("mechanism", mechanism);
                    signorTableJson.put("id", id);
                    signorTableJson.put("enzyme", enzyme);
                    signorTableJson.put("target", inputGene);


                    signorTableArray.add(signorTableJson);
                }
                //End of signor
                //End of signor

//            log.info("inputgene");
//            //System.out.println(protList[i]);
                JSONArray gene2PtmList = new JSONArray();


                if (ptmSuJson.containsKey(inputGene)) {
                    //log.info("gene2KinaseList");
                    gene2PtmList = (JSONArray) (ptmSuJson.get(inputGene));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < gene2PtmList.size(); j++) {
                    JSONObject ptmAndScore = (JSONObject) gene2PtmList.get(j);
                    //log.info("kinase");
                    //log.info(gene);

                    String enzyme = (String) ptmAndScore.get("enzyme_AC");
                    double score = Double.parseDouble((String) ptmAndScore.get("score"));
                    if (score < 1) {
                        score = 1;
                    }


                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!ptm_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", pidx, 10, 0.0);//tag 10 is for Su
                            pidx = pidx + 1;


                            ptm_nodeUnique.put(enzyme, newNode);
                            ptm_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) ptm_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), score, 2);
                        ptm_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject ptmTableJson = new JSONObject();

                    ptmTableJson.put("PTMProtein", inputphosphoProtein);
                    ptmTableJson.put("PTM", shorthand);
                    ptmTableJson.put("amino", phosphoGeneSequenceJson2.get("amino").toString());
                    ptmTableJson.put("site", phosphoGeneSequenceJson2.get("site").toString());
                    ptmTableJson.put("enzyme", enzyme);
                    ptmTableJson.put("type", "Sumoylation");
                    ptmTableJson.put("score", score);

                    ptmTableArray.add(ptmTableJson);
                }
            }
//        ptmUb: /PTM/ptm_ub.json
            //=========================================================================
            //=========================================================================
            //=========================================================================
            //=========================================================================
            for (int i = 0; i < ptmUbArray.size(); i++) {
                log.info("==========================================");
                log.info("Compute Network for the ptm genes");
                JSONObject phosphoGeneSequenceJson2 = (JSONObject) ptmUbArray.get(i);
//            String phosphoAmino = phosphoGeneSequenceJson2.get("amino").toString();
//            String phosphoSequence = phosphoGeneSequenceJson2.get("sequence").toString();
                inputGene = phosphoGeneSequenceJson2.get("phosphoSite").toString();
                inputphosphoProtein = phosphoGeneSequenceJson2.get("phosphoProtein").toString();
                String shorthand = phosphoGeneSequenceJson2.get("shorthand").toString();
                //inputGene = protList[i];
                inputphosphoProteinUpper = inputphosphoProtein.toUpperCase();
                //inputGeneUpper = inputGene.toUpperCase();



                //This part if for signor
                //This part if for signor
                JSONArray ptm2SignorList = new JSONArray();

                if (signorPtmToProteinJson.containsKey(inputGene)) {
                    //log.info("gene2KinaseList");
                    ptm2SignorList = (JSONArray) (signorPtmToProteinJson.get(inputGene));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < ptm2SignorList.size(); j++) {
                    JSONObject signorAndEffect = (JSONObject) ptm2SignorList.get(j);
                    //log.info("kinase");
                    //log.info(gene);
                    //{"upordown": "down", "enzyme_AC": "Q04771(ACVR1)", "effect": "down-regulates activity", "mechanism": "", "id": "P31749"}
                    String enzyme = (String) signorAndEffect.get("enzyme_AC");
                    int tag = 0;
                    String upOrDown = (String) signorAndEffect.get("upordown");
                    String effect = (String) signorAndEffect.get("effect");
                    String mechanism = (String) signorAndEffect.get("mechanism");
                    String id = (String) signorAndEffect.get("id");

                    if (upOrDown.equals("up")) {
                        tag = 2;
                    }
                    if (upOrDown.equals("down")) {
                        tag = 1;
                    }
                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!signor_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", sidx, 11, 0.0);//tag 2 is for protein-protein-interaction
                            sidx = sidx + 1;


                            signor_nodeUnique.put(enzyme, newNode);
                            signor_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) signor_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), 1, tag);
                        signor_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) signor_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject signorTableJson = new JSONObject();

                    signorTableJson.put("PTMProtein", inputphosphoProteinUpper);
                    signorTableJson.put("effect", effect);
                    signorTableJson.put("mechanism", mechanism);
                    signorTableJson.put("id", id);
                    signorTableJson.put("enzyme", enzyme);
                    signorTableJson.put("target", inputGene);


                    signorTableArray.add(signorTableJson);
                }
                //End of signor
                //End of signor

//            log.info("inputgene");
//            //System.out.println(protList[i]);
                JSONArray gene2PtmList = new JSONArray();


                if (ptmUbJson.containsKey(inputGene)) {
                    //log.info("gene2KinaseList");
                    gene2PtmList = (JSONArray) (ptmUbJson.get(inputGene));
                    //log.info(gene2KinaseList.toString());
                }

                for (int j = 0; j < gene2PtmList.size(); j++) {
                    JSONObject ptmAndScore = (JSONObject) gene2PtmList.get(j);
                    //log.info("kinase");
                    //log.info(gene);

                    String enzyme = (String) ptmAndScore.get("enzyme_AC");
                    double score = Double.parseDouble((String) ptmAndScore.get("score"));
                    if (score < 1) {
                        score = 1;
                    }


                    if (enzyme.equals("unknown")) {
                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    } else {
                        if (!ptm_nodeUnique.containsKey(enzyme)) {
                            //log.info(gene);


                            newNode = generateNode(enzyme, enzyme, "", pidx, 11, 0.0);//tag 11 is for Ub
                            pidx = pidx + 1;


                            ptm_nodeUnique.put(enzyme, newNode);
                            ptm_NetworkNodes.add(newNode);
                        }
                        newEdgeNode = generateEdgeNode((int) ((JSONObject) ptm_nodeUnique.get(enzyme)).get("idx"), (int) ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("idx"), score, 2);
                        ptm_NetworkEdges.add(newEdgeNode);

                        if (((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).get("connected") == "No") {
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).remove("connected");
                            ((JSONObject) ptm_nodeUnique.get(inputphosphoProteinUpper)).put("connected", "Yes");
                        }
                    }


                    //---------------------------------------
                    //For ptmTableArray
                    JSONObject ptmTableJson = new JSONObject();

                    ptmTableJson.put("PTMProtein", inputphosphoProtein);
                    ptmTableJson.put("PTM", shorthand);
                    ptmTableJson.put("amino", phosphoGeneSequenceJson2.get("amino").toString());
                    ptmTableJson.put("site", phosphoGeneSequenceJson2.get("site").toString());
                    ptmTableJson.put("enzyme", enzyme);
                    ptmTableJson.put("type", "Ubiquitination");
                    ptmTableJson.put("score", score);

                    ptmTableArray.add(ptmTableJson);
                }
            }








            String urlString = "";
            // Compute Network for the iptmnet genes
//            try {
            //System.out.println("==========================================");
            //System.out.println("Computing deepPhosResults");
            //System.out.println(ptmPhArray.toJSONString());
            //System.out.println("------");
            JSONObject phosphoSiteToPhosphoProtein = new JSONObject();
            for (int i = 0; i < ptmPhArray.size(); i++) {


                JSONObject ptmPhosphoJson = (JSONObject) ptmPhArray.get(i);
                //System.out.println(ptmPhosphoJson);
                //
                String shorthand = ptmPhosphoJson.get("phosphoSite").toString();
                phosphoSiteToPhosphoProtein.put(shorthand, ptmPhosphoJson);

                urlString += shorthand + ",";
            }
            if(urlString.length() > 0){
                urlString = urlString.substring(0, urlString.length() - 1);
            }


//Adding nodes to deepPhos
            for (int i = 0; i < protList.length; i++) {

                inputGene = protList[i];
                inputGeneUpper = inputGene.toUpperCase();
                //System.out.println("inputGene");
                //System.out.println(inputGene);
                //System.out.println("inputGeneUpper");
                //System.out.println(inputGeneUpper);
//
//                String deepItemName = (String) deepItem.get("name");
//                String deepItemNameTrans = (String) deepItem.get("full_name");
//
//                String deepItemFullName = "";
//                if (phosphoSiteToPhosphoProtein.containsKey(deepItemName)) {
//                    deepItemFullName = (String) ((JSONObject) phosphoSiteToPhosphoProtein.get(deepItemName)).get("phosphoProtein");
//                }
//
//                if (!deepPhos_nodeUnique.containsKey(deepItemFullName)) {
//                    newNode = generateNode(deepItemNameTrans, deepItemFullName, "", ppidx, 0, 0.0);//tag 2 is for protein-protein-interaction
//                    ppidx = ppidx + 1;
//
//
//                    deepPhos_nodeUnique.put(deepItemFullName, newNode);
//                    deepPhosNetwork_Nodes.add(newNode);
//                }
//
//
//
//
//
//
//
//
//
//
//
//
                String deepprotListAndGeneName = (String) protListAndGeneName.get(protList[i]);
                if (!deepPhos_nodeUnique.containsKey(deepprotListAndGeneName)) {

                    newNode = generateNode(inputGene, deepprotListAndGeneName, "", ppidx, 0, 0.0);//tag 0 is for grey, connected is for verifying if the
                    //uniprot is connected to an enzyme or not
                    ppidx = ppidx + 1;
                    deepPhosNetwork_Nodes.add(newNode);
                    deepPhos_nodeUnique.put(inputGene, newNode);
                }
            }


                //System.out.println("deepPhosurlString=");
                //System.out.println(urlString);
                //System.out.println("deepPhosResults=");
            deepPhosResults = deepPhosService.getPhosphoPrediction(organism, urlString);
            ////System.out.println(deepPhosResults);


            for (Object deepKeyStr : deepPhosResults.keySet()) {
                //System.out.println("interating in key values in deepPhos");
                System.out.println(deepKeyStr.toString());

                try{

                    //JSONArray deepKeyvalue = (JSONArray) parser.parse(deepPhosResults.get(deepKeyStr.toString()));
                    JSONArray deepKeyvalue = (JSONArray) deepPhosResults.get(deepKeyStr);
                    System.out.println(deepKeyvalue.toString());
                    if (!deepPhos_nodeUnique.containsKey(deepKeyStr)) {
                        newNode = generateNode((String) deepKeyStr, (String) deepKeyStr, "", ppidx, 1, 0.0);//tag 2 is for protein-protein-interaction
                        ppidx = ppidx + 1;


                        deepPhos_nodeUnique.put(deepKeyStr, newNode);
                        deepPhosNetwork_Nodes.add(newNode);

                    }

                    //Print key and value

                    ////System.out.println("key: "+ deepKeyStr + " value: " + deepKeyvalue);

                    for (int deepi = 0; deepi < deepKeyvalue.size(); deepi++) {

                        JSONObject deepItem = (JSONObject) deepKeyvalue.get(deepi);
                        ////System.out.println(deepItem.toJSONString());
//                        if (deepItem.containsKey("full_name")) {
                        ////System.out.println("----------------------");
                        String deepItemName = (String) deepItem.get("name");
                        String deepItemNameTrans = (String) deepItem.get("full_name");
                        //String deepItemFullNameTrunc = deepItemName.split("\\(")[0];
                        String deepItemFullName = "";
                        if (phosphoSiteToPhosphoProtein.containsKey(deepItemName)) {
                            deepItemFullName = (String) ((JSONObject) phosphoSiteToPhosphoProtein.get(deepItemName)).get("phosphoProtein");
                        }
                        //System.out.println(deepItemFullName);

                        Double deepItemScore = Double.parseDouble((String) deepItem.get("score"));
                        //Double deepItemScore = Double.parseDouble((String) deepItem.get("score"));
                        //double deepItemScore = Double.valueOf((Double) deepItem.get("score"));
                        int deepPosition = Integer.parseInt((String) deepItem.get("position"));
//                    //System.out.println("---------");
//                    //System.out.println(deepItemName);
//
//                    //System.out.println(deepItemFullName);
//
//                    //System.out.println(deepItemScore);
//                    //System.out.println(deepPosition);
//                        if (!deepPhos_nodeUnique.containsKey(deepItemFullName)) {
//                            newNode = generateNode(deepItemNameTrans, deepItemFullName, "", ppidx, 0, 0.0);//tag 2 is for protein-protein-interaction
//                            ppidx = ppidx + 1;
//
//
//                            deepPhos_nodeUnique.put(deepItemFullName, newNode);
//                            deepPhosNetwork_Nodes.add(newNode);
//                        }

                        newEdgeNode = generateEdgeNode((int) ((JSONObject) deepPhos_nodeUnique.get(deepKeyStr)).get("idx"), (int) ((JSONObject) deepPhos_nodeUnique.get(deepItemFullName)).get("idx"), deepItemScore, 0);
                        deepPhosNetwork_Edges.add(newEdgeNode);
                        JSONObject deepPhosTableJson = new JSONObject();

                        deepPhosTableJson.put("PTMProtein", deepItemFullName);
                        deepPhosTableJson.put("PTM", deepItemNameTrans);

                        deepPhosTableJson.put("score", deepItemScore);
                        deepPhosTableJson.put("position", deepPosition);
                        deepPhosTableJson.put("kinaseGroup", (String) deepKeyStr);
                        //deepPhosTableJson.put("target", inputGeneProtein);


                        deepPhosTableArray.add(deepPhosTableJson);
                    }


                        //}






                    }
                catch (Exception e){
                    String msg = String.format("Error in one kinase family in deepphos");

                }



                //for nested objects iteration if required
                //if (keyvalue instanceof JSONObject)
                //    printJsonObject((JSONObject)keyvalue);
            }
            //System.out.println(deepPhosNetwork_Nodes.toJSONString());
            //System.out.println(deepPhosNetwork_Edges.toJSONString());
            //System.out.println(deepPhosTableArray.toJSONString());


            indefinite_Kinase_Gene_Network.put("nodes", indefinite_Kinase_Gene_NetworkNodes);
            indefinite_Kinase_Gene_Network.put("edges", indefinite_Kinase_Gene_NetworkEdges);
            definite_Kinase_Gene_Network.put("nodes", definite_Kinase_Gene_NetworkNodes);
            definite_Kinase_Gene_Network.put("edges", definite_Kinase_Gene_NetworkEdges);
            definite_blosum_Kinase_Gene_Network.put("nodes", definite_blosum_Kinase_Gene_NetworkNodes);
            definite_blosum_Kinase_Gene_Network.put("edges", definite_blosum_Kinase_Gene_NetworkEdges);
            blosum_Kinase_Gene_Network.put("nodes", blosum_Kinase_Gene_NetworkNodes);
            blosum_Kinase_Gene_Network.put("edges", blosum_Kinase_Gene_NetworkEdges);
            ptm_Network.put("nodes", ptm_NetworkNodes);
            ptm_Network.put("edges", ptm_NetworkEdges);
            signor_Network.put("nodes", signor_NetworkNodes);
            signor_Network.put("edges", signor_NetworkEdges);
            deepPhosNetwork.put("nodes", deepPhosNetwork_Nodes);
            deepPhosNetwork.put("edges", deepPhosNetwork_Edges);
            //==============================================
            network.put("deepPhosNetwork", deepPhosNetwork);
            network.put("deepPhosTable", deepPhosTableArray);
            network.put("signor_Network", signor_Network);
            network.put("signor_table", signorTableArray);
            network.put("ptm_Network", ptm_Network);
            network.put("Blosum50_table", geneBlosumArray);
            network.put("PTM_table", ptmTableArray);
            network.put("Known_Kinase_TargetGene", definite_Kinase_Gene_Network);
            network.put("Blosum50_Exact_Match_Kinase_TargetGene", definite_blosum_Kinase_Gene_Network);
            network.put("Predicted_Probability_Kinase_TargetGene", indefinite_Kinase_Gene_Network);
            network.put("Predicted_Blosum50_Kinase_TargetGene", blosum_Kinase_Gene_Network);

            //System.out.println("PTM_table");
            //System.out.println(ptmTableArray.toString());

            //System.out.println("ptm network");
            //System.out.println(ptm_Network.toString());

            //System.out.println("network without error");
            //System.out.println(network.toString());
            return network;
        }catch (Exception e){
            String msg = String.format("Error in obtaining network");
            //System.out.println(msg);

            indefinite_Kinase_Gene_Network.put("nodes", indefinite_Kinase_Gene_NetworkNodes);
            indefinite_Kinase_Gene_Network.put("edges", indefinite_Kinase_Gene_NetworkEdges);
            definite_Kinase_Gene_Network.put("nodes", definite_Kinase_Gene_NetworkNodes);
            definite_Kinase_Gene_Network.put("edges", definite_Kinase_Gene_NetworkEdges);
            definite_blosum_Kinase_Gene_Network.put("nodes", definite_blosum_Kinase_Gene_NetworkNodes);
            definite_blosum_Kinase_Gene_Network.put("edges", definite_blosum_Kinase_Gene_NetworkEdges);
            blosum_Kinase_Gene_Network.put("nodes", blosum_Kinase_Gene_NetworkNodes);
            blosum_Kinase_Gene_Network.put("edges", blosum_Kinase_Gene_NetworkEdges);
            ptm_Network.put("nodes", ptm_NetworkNodes);
            ptm_Network.put("edges", ptm_NetworkEdges);
            signor_Network.put("nodes", signor_NetworkNodes);
            signor_Network.put("edges", signor_NetworkEdges);
            deepPhosNetwork.put("nodes", deepPhosNetwork_Nodes);
            deepPhosNetwork.put("edges", deepPhosNetwork_Edges);
            //==============================================
            network.put("signor_Network", signor_Network);
            network.put("signor_table", signorTableArray);
            network.put("ptm_Network", ptm_Network);
            network.put("Blosum50_table", geneBlosumArray);
            network.put("PTM_table", ptmTableArray);
            network.put("deepPhosNetwork", deepPhosNetwork);
            network.put("deepPhosTable", deepPhosTableArray);
            network.put("Known_Kinase_TargetGene", definite_Kinase_Gene_Network);
            network.put("Blosum50_Exact_Match_Kinase_TargetGene", definite_blosum_Kinase_Gene_Network);
            network.put("Predicted_Probability_Kinase_TargetGene", indefinite_Kinase_Gene_Network);
            network.put("Predicted_Blosum50_Kinase_TargetGene", blosum_Kinase_Gene_Network);
            //System.out.println("network with error");
            //System.out.println(network.toString());
            return network;
        }
    }

    int computeBlosum50Score(JSONObject blosum50, String geneSequence, String kinaseSequence) {
        int score = 0;
        Object scoreObject;
        for (int iter = 0; iter < geneSequence.length(); iter++) {
            String kinaseStr = String.valueOf(kinaseSequence.charAt(iter));
            String geneStr = String.valueOf(geneSequence.charAt(iter));
            //log.info(blosum50.get(kinaseStr).toString());
            //log.info(((JSONObject)blosum50.get(kinaseStr)).get(geneStr).toString());
            score += ((Long) ((JSONObject) blosum50.get(kinaseStr)).get(geneStr)).intValue();
//            Class cls2 = scoreObject.getClass();
//            //System.out.println("The type of the object is: " + cls2.getName());
        }
        return score;
    }


    double computeProbability(JSONArray keyvalue, String sequence) {
        double pr = 0.0;
        double prob;
        JSONObject amino_acids_dic = new JSONObject();
        amino_acids_dic.put("A", 0);
        amino_acids_dic.put("R", 1);
        amino_acids_dic.put("N", 2);
        amino_acids_dic.put("D", 3);
        amino_acids_dic.put("C", 4);
        amino_acids_dic.put("Q", 5);
        amino_acids_dic.put("E", 6);
        amino_acids_dic.put("G", 7);
        amino_acids_dic.put("H", 8);
        amino_acids_dic.put("I", 9);
        amino_acids_dic.put("L", 10);
        amino_acids_dic.put("K", 11);
        amino_acids_dic.put("M", 12);
        amino_acids_dic.put("F", 13);
        amino_acids_dic.put("P", 14);
        amino_acids_dic.put("S", 15);
        amino_acids_dic.put("T", 16);
        amino_acids_dic.put("W", 17);
        amino_acids_dic.put("Y", 18);
        amino_acids_dic.put("V", 19);

        for (int i = 0; i < sequence.length(); i++) {
            char ch = sequence.charAt(i);
            String key = String.valueOf(ch);

            //log.info(String.valueOf(key));
            if (!(ch == '_')) {

                Object charDic = amino_acids_dic.get(key);


//                Class cls2 = charDic.getClass();
//                //System.out.println("The type of the object is: " + cls2.getName());

                int charDickInt = (int) charDic;

                //log.info(String.valueOf(i*20 + charDickInt));
                if ((double) keyvalue.get(i * 20 + charDickInt) < 0.04) {
                    return 0.0;
                } else if ((double) keyvalue.get(i * 20 + charDickInt) != 1.0) {
                    //log.info(String.valueOf((double) keyvalue.get(i * 20 + charDickInt)));
                    //log.info(String.valueOf(Math.log((double) keyvalue.get(i * 20 + charDickInt))));
                    prob = (-1.0) / (Math.log((double) keyvalue.get(i * 20 + charDickInt)));
                    pr += prob;
                } else {
                    //In this situation the probability is 1 that we want to embold it!
                    pr += 10.0;
                }

            }
        }

        log.info(String.valueOf(pr));
        return pr;
    }


    String cutSequenceMethod(String sequence, int site) {
        String cutSeq = new String();
        //log.info("in cutSequenceMEthod");
        //log.info(sequence);
        //log.info(String.valueOf(site));
        try {
            int start = max(0, site - 8);
            int stop = min(sequence.length(), site + 7);

            //log.info(String.valueOf(start));
            //log.info(String.valueOf(stop));
            cutSeq = sequence.substring(start, stop);

            if (site - 8 < 0) {
                char[] repeat = new char[8 - site];
                char c = '_';
                Arrays.fill(repeat, c);
                cutSeq = new String(repeat) + cutSeq;
            }
            if (site + 7 > sequence.length()) {
                char[] repeat2 = new char[site + 7 - sequence.length()];
                char c = '_';
                Arrays.fill(repeat2, c);
                cutSeq = cutSeq + new String(repeat2);
            }
            log.info(cutSeq);
            return cutSeq;
        }catch (Exception e){
            //System.out.println("Error in cutSequenceMethod");
            //System.out.println(sequence);
            //System.out.println(site);
            return cutSeq;
        }
    }

    JSONObject generateEdgeNode(int sourceTag, int targetTag, double value, int tag) {
//        log.info("&&&&&& Generating Edges &&&&&&&&&&");
//        log.info("&&&&&& Generating Edges &&&&&&&&&&");
//        log.info("&&&&&& Generating Edges &&&&&&&&&&");
//        log.info("&&&&&& Generating Edges &&&&&&&&&&");
//        log.info("&&&&&& Generating Edges &&&&&&&&&&");
//        log.info("&&&&&& Generating Edges &&&&&&&&&&");
        JSONObject node = new JSONObject();
        node.put("source", sourceTag);
        node.put("target", targetTag);
        node.put("score", value);
        node.put("tag", tag);//tag 1 is for straight line, 2 for shadowed
        //node.put("value", 1);
        //node.put("weight", 1);
        return node;
    }


    JSONObject generateNode(String name, String nameAndGene, String id, int idx, int tag, double value)//, float center, int value)
    {
        JSONObject node = new JSONObject();

        node.put("name", name);
        node.put("full_name", nameAndGene);
        node.put("connected", id);
        node.put("idx", idx);
        node.put("group", tag);
        node.put("value", value);

        node.put("weight", 0);
        return node;
    }

//    JSONObject getGeneInfo(String url, String gene) throws Exception {
//
//        //String enrichrUrlGeneInfo = String.format(enrichrGeneInfo, gene);
//        String uri = String.format(url, gene);
//        String response;
//
//
//
//
//        RestTemplate restTemplate = new RestTemplate();
//        //String result = restTemplate.getForObject(uri, HarmonizomeGeneDomain.class);
//        //System.out.println("******************");
//        //System.out.println("******************");
//
//        //System.out.println("++++++++++");
//
//        JSONObject geneInfoJson;
//        //HashMap geneMap;
//        Map geneMap;
//
//
//        try {
//
//            geneMap = UtilsNetwork.loadJSONFromStringHarmonizomeGene(result);
//
//
//
//
//
//                ////System.out.println("uniprotMap:" + uniprotMap.toString());
//
////            response = UtilsNetwork.getInstance().readUrlXml(uri);
////            log.info(response);
////            JSONParser parser = new JSONParser();
////
////            geneInfoJson = (JSONObject) parser.parse(response);
////            //System.out.println("--------------------");
////            JSONObject geneSymbol = (JSONObject) geneInfoJson.get("symbol");
////            //System.out.println("===================");
////            //System.out.println(geneInfoJson.toString());
////            //System.out.println(geneSymbol.toString());
////
//        } catch (Exception e) {
//
//            String msg =  String.format("Error in obtaining geneInfo");
//            log.warn(msg);
//            throw new RuntimeException(msg);
//        }
//
//        geneInfoJson = new JSONObject(geneMap);
//
//        return geneInfoJson;
//    }

}

