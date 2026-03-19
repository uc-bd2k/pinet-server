package edu.uc.eh.service;

/**
 * Created by shamsabz on 3/14/18.
 */

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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.min;
import static java.lang.StrictMath.max;

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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.min;
import static java.lang.StrictMath.max;

/**
 * Created by behrouz on 1/25/17.
 */
@Service
public class PtmService {

    private static final Logger log = LoggerFactory.getLogger(PtmService.class);

    private long elapsedMs(long startNs) {
        return (System.nanoTime() - startNs) / 1_000_000;
    }

    private void timing(String template, Object... args) {
        System.out.println(String.format(template, args));
    }



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
    //        ptmUb: /PTM/ptm_ub.json


    @Autowired
    UniprotService uniprotService;

    private volatile JSONObject cachedPtmAcJson;
    private volatile JSONObject cachedPtmPhJson;
    private volatile JSONObject cachedPtmMeJson;
    private volatile JSONObject cachedPtmCgJson;
    private volatile JSONObject cachedPtmMyJson;
    private volatile JSONObject cachedPtmNgJson;
    private volatile JSONObject cachedPtmOgJson;
    private volatile JSONObject cachedPtmSgJson;
    private volatile JSONObject cachedPtmSnJson;
    private volatile JSONObject cachedPtmSuJson;
    private volatile JSONObject cachedPtmUbJson;

    private JSONObject loadRequiredJson(String path, String label) {
        try {
            return UtilsIO.getInstance().readJsonFile(path);
        } catch (Exception e) {
            String msg = String.format("Error in obtaining %s", label);
            log.warn(msg);
            throw new RuntimeException(msg);
        }
    }

    private void ensureResourceCachesLoaded() {
        if (cachedPtmAcJson != null
                && cachedPtmPhJson != null
                && cachedPtmMeJson != null
                && cachedPtmCgJson != null
                && cachedPtmMyJson != null
                && cachedPtmNgJson != null
                && cachedPtmOgJson != null
                && cachedPtmSgJson != null
                && cachedPtmSnJson != null
                && cachedPtmSuJson != null
                && cachedPtmUbJson != null) {
            return;
        }

        synchronized (this) {
            if (cachedPtmAcJson == null) {
                cachedPtmAcJson = loadRequiredJson(ptmAcInfo, "ptmAcInfo");
            }
            if (cachedPtmPhJson == null) {
                cachedPtmPhJson = loadRequiredJson(ptmPhInfo, "ptmPhInfo");
            }
            if (cachedPtmMeJson == null) {
                cachedPtmMeJson = loadRequiredJson(ptmMeInfo, "ptmMeInfo");
            }
            if (cachedPtmCgJson == null) {
                cachedPtmCgJson = loadRequiredJson(ptmCgInfo, "ptmCgInfo");
            }
            if (cachedPtmMyJson == null) {
                cachedPtmMyJson = loadRequiredJson(ptmMyInfo, "ptmMyInfo");
            }
            if (cachedPtmNgJson == null) {
                cachedPtmNgJson = loadRequiredJson(ptmNgInfo, "ptmNgInfo");
            }
            if (cachedPtmOgJson == null) {
                cachedPtmOgJson = loadRequiredJson(ptmOgInfo, "ptmOgInfo");
            }
            if (cachedPtmSgJson == null) {
                cachedPtmSgJson = loadRequiredJson(ptmSgInfo, "ptmSgInfo");
            }
            if (cachedPtmSnJson == null) {
                cachedPtmSnJson = loadRequiredJson(ptmSnInfo, "ptmSnInfo");
            }
            if (cachedPtmSuJson == null) {
                cachedPtmSuJson = loadRequiredJson(ptmSuInfo, "ptmSuInfo");
            }
            if (cachedPtmUbJson == null) {
                cachedPtmUbJson = loadRequiredJson(ptmUbInfo, "ptmUbInfo");
            }
        }
    }

    public JSONObject computePtmNetwork(String organism, String[] protList) throws Exception {
        long requestStartNs = System.nanoTime();
        long phaseStartNs = requestStartNs;

        int didx = 0; //definite idx
        int dbdx = 0; //definite blosum idx
        int iidx = 0; //indefinite idx
        int bidx = 0; //blosum idx
        int pidx = 0; //iptmnet idx
        String kinase;
        String gene;
        String inputGene;
        String inputGeneUpper;
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


        JSONObject network = new JSONObject();

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
        boolean lowerResult;
        //boolean hasLowercase = !password.equals(password.toUpperCase());

        Pattern ifHasLowerCase = Pattern.compile(lowerCase);

//        Pattern lowerCase = Pattern.compile("(\\d+(?:\\.\\d+)?)");
//        Pattern UpperCase = Pattern.compile("(.*?)(\\d+)(.*)");

        ensureResourceCachesLoaded();
        timing("timing service=PtmService phase=resource-cache-loaded inputs=%d elapsedMs=%d", protList.length, elapsedMs(phaseStartNs));
        phaseStartNs = System.nanoTime();
        ptmAcJson = cachedPtmAcJson;
        ptmMeJson = cachedPtmMeJson;
        ptmPhJson = cachedPtmPhJson;
        ptmCgJson = cachedPtmCgJson;
        ptmMyJson = cachedPtmMyJson;
        ptmNgJson = cachedPtmNgJson;
        ptmOgJson = cachedPtmOgJson;
        ptmSgJson = cachedPtmSgJson;
        ptmSnJson = cachedPtmSnJson;
        ptmSuJson = cachedPtmSuJson;
        ptmUbJson = cachedPtmUbJson;
//        ptmSg: /PTM/ptm_sg.json
//        ptmSn: /PTM/ptm_sn.json
//        ptmSu: /PTM/ptm_su.json
//        ptmUb: /PTM/ptm_ub.json


        //The last item is the organism
        String organismForQueryUniprot = organism;
//        System.out.println("======== organism");
//        System.out.println(organismForQueryUniprot);
            for (int i = 0; i < protList.length; i++) {

            inutArray.add(protList[i]);

            System.out.println("input Uniprot");
            System.out.println(protList[i]);

            inside = "";
            //geneString = "";
            aminoString = "";
            siteString = "";
            ptmString = "";

            List<String> matchList = new ArrayList<String>();
            Pattern regex = Pattern.compile("\\{(.*?)\\}");
            geneString = protList[i]
                    .replaceAll("\\{.*?\\}", "")
                    .replaceAll("\\[.*?\\] ?", "");
//            System.out.println("geneString");
//            System.out.println(geneString);
            //Pattern siteRegex = Pattern.compile("\\d+");
            //Pattern aminoRegex = Pattern.compile("[A-Z]{1}");

            Matcher insideMatcher = regex.matcher(protList[i]);


            log.info("here1");
            aminoSiteArray = new JSONArray();
            while (insideMatcher.find()) {
                System.out.println("here1");
                System.out.println(insideMatcher.group(1));
                String token = insideMatcher.group(1);
                String normalizedToken = token.replace("[", "").replace("]", "");
                String[] splitted = normalizedToken.split("@");
                if (splitted.length < 2) {
                    continue;
                }
                siteString = splitted[1];


                Matcher matcherLower = ifHasLowerCase.matcher(splitted[1]);
//                lowerResult = matcherLower.matches();
//                System.out.println(lowerResult);

                if (splitted[0].indexOf("(") != -1) {
                    Matcher match = Pattern.compile("\\((.*?)\\)").matcher(splitted[0]);
                    while (match.find()) {
                        ptmString = match.group(1);
                        aminoString = splitted[0].replace(match.group(0), "");
                    }
                } else if (splitted[0].equals(splitted[0].toUpperCase())) {
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
                System.out.println(aminoString);
                System.out.println(siteString);
                System.out.println(ptmString);


                aminoSiteJson = new JSONObject();
                //aminoSiteJson.put("amino",aminoString.substring(0,1));
                aminoSiteJson.put("amino", aminoString);
                aminoSiteJson.put("ptm", ptmString);
                aminoSiteJson.put("site", siteString);

                aminoSiteArray.add(aminoSiteJson);

            }
            proteinToPhosphoAminoJson.put(protList[i], aminoSiteArray);

            inputGeneArray.add(geneString);


        }

        //Get info about the genes
        String[] stringProteinArray = (String[]) inputGeneArray.toArray(new String[0]);
        System.out.println("stringProteinArray");
//        for (int i = 0; i < stringProteinArray.length;  i++) {
//            System.out.println(stringProteinArray[i]);
//        }
        System.out.println("proteinToPhosphoAminoJson");
        System.out.println(proteinToPhosphoAminoJson);
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
            timing("timing service=PtmService phase=input-parsed inputs=%d elapsedMs=%d", protList.length, elapsedMs(phaseStartNs));
            phaseStartNs = System.nanoTime();

            for (int i = 0; i < protList.length; i++) {
            String uniprot_id = stringProteinArray[i];
            if (!proteinToUniprot.containsKey(uniprot_id)) {
                geneSequenceInfo = uniprotService.findByAccessionApi(uniprot_id);
                proteinToUniprot.put(uniprot_id, geneSequenceInfo);
            }
            ArrayList<String> geneName = (ArrayList<String>) ((JSONObject) proteinToUniprot.get(uniprot_id)).get("primary_gene_name");
            if (geneName != null && geneName.size() > 0) {
                protListAndGeneName.put(protList[i], protList[i] + "(" + geneName.get(0).toString() + ")");
            } else {
                protListAndGeneName.put(protList[i], protList[i]);
            }
        }
        for (int i = 0; i < protList.length; i++) {

            JSONArray proteinToPhosphoAminoArray = (JSONArray) proteinToPhosphoAminoJson.get(protList[i]);
//            JSONArray uniprotId = (JSONArray)((JSONObject) geneProteinInfo.get(i)).get("uniprot_ids");
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println(i);
            System.out.println("uniprot_ids------");
            String uniprot_id = stringProteinArray[i];


            for (int j = 0; j < proteinToPhosphoAminoArray.size(); j++) {
                System.out.println("j----------------------------------------");
                System.out.println(j);
                JSONObject proteinToPhosphoAminoItem = (JSONObject) proteinToPhosphoAminoArray.get(j);
                System.out.println(proteinToPhosphoAminoItem.toJSONString());
                System.out.println(proteinToPhosphoAminoItem.get("ptm").toString());


                if (proteinToPhosphoAminoItem.get("ptm").equals("p")) {



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
                    System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                    System.out.println(ptmItemJson.toString());
                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                    //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

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
                    System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                    System.out.println(ptmItemJson.toString());
                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                    //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

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
                    System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                    System.out.println(ptmItemJson.toString());
                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                    //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

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
                    System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                    System.out.println(ptmItemJson.toString());
                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                    //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

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
                    System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                    System.out.println(ptmItemJson.toString());
                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                    //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

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
                    System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                    System.out.println(ptmItemJson.toString());
                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                    //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

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
                    System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                    System.out.println(ptmItemJson.toString());
                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                    //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

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
                    System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                    System.out.println(ptmItemJson.toString());
                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                    //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

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
                    System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                    System.out.println(ptmItemJson.toString());
                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                    //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

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
                    System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                    System.out.println(ptmItemJson.toString());
                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                    //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

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
                    System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                    System.out.println(ptmItemJson.toString());
                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                    //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                    ptmUbArray.add(ptmItemJson);
                    ptmArray.add(ptmItemJson);
                }

                else {
                    try {
                        Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm"));



                        //for iptmnet
                        if (Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) < 81 && Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) > 79) {
                            JSONObject ptmItemJson = new JSONObject();

                            ptmItemJson.put("phosphoSite", uniprot_id + "[ph" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                            ptmItemJson.put("phosphoProtein", protList[i]);
                            ptmItemJson.put("shorthand", uniprot_id + "[p" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");

                            ptmItemJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                            ptmItemJson.put("site", proteinToPhosphoAminoItem.get("site"));
                            //ptmItemJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                            System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                            System.out.println(ptmItemJson.toString());
                            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                            //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                            ptmPhArray.add(ptmItemJson);
                            ptmArray.add(ptmItemJson);
                        }

                        if (Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) < 43 && Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) > 41) {
                            JSONObject ptmItemJson = new JSONObject();

                            ptmItemJson.put("phosphoSite", uniprot_id + "[ac" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                            ptmItemJson.put("phosphoProtein", protList[i]);
                            ptmItemJson.put("shorthand", uniprot_id + "[a" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");

                            ptmItemJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                            ptmItemJson.put("site", proteinToPhosphoAminoItem.get("site"));
                            //ptmItemJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                            System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                            System.out.println(ptmItemJson.toString());
                            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                            //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                            ptmAcArray.add(ptmItemJson);
                            ptmArray.add(ptmItemJson);
                        }

                        if (Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) < 15 && Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) > 13) {
                            JSONObject ptmItemJson = new JSONObject();

                            ptmItemJson.put("phosphoSite", uniprot_id + "[me" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                            ptmItemJson.put("shorthand", uniprot_id + "[me" + proteinToPhosphoAminoItem.get("amino") + "@" + proteinToPhosphoAminoItem.get("site") + "]");

                            ptmItemJson.put("phosphoProtein", protList[i]);
                            ptmItemJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                            ptmItemJson.put("site", proteinToPhosphoAminoItem.get("site"));
                            //ptmItemJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                            System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                            System.out.println(ptmItemJson.toString());
                            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                            //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                            ptmMeArray.add(ptmItemJson);
                            ptmArray.add(ptmItemJson);
                        }
                    } catch (NumberFormatException e) {
                        //not a double
                    }
                }

            }

        }

        System.out.println("ptmPhArray");
        System.out.println(ptmPhArray.toString());
        System.out.println("ptmMeArray");
        System.out.println(ptmMeArray.toString());
        System.out.println("ptmAcArray");
        System.out.println(ptmAcArray.toString());

        System.out.println("ptmCgArray");
        System.out.println(ptmCgArray.toString());
        System.out.println("ptmMyArray");
        System.out.println(ptmMyArray.toString());
        System.out.println("ptmNgArray");
        System.out.println(ptmNgArray.toString());
//        ptmCg: /PTM/ptm_cg.json
//        ptmMy: /PTM/ptm_my.json
//        ptmNg: /PTM/ptm_ng.json
        System.out.println("ptmOgArray");
        System.out.println(ptmOgArray.toString());
        System.out.println("ptmSgArray");
        System.out.println(ptmSgArray.toString());
        System.out.println("ptmSnArray");
        System.out.println(ptmSnArray.toString());
//        ptmOg: /PTM/ptm_og.json
//        ptmSg: /PTM/ptm_sg.json
//        ptmSn: /PTM/ptm_sn.json
        System.out.println("ptmSuArray");
        System.out.println(ptmSuArray.toString());
        System.out.println("ptmUbArray");
        System.out.println(ptmUbArray.toString());
        System.out.println("ptmMe2Array");
        System.out.println(ptmMe2Array.toString());
        System.out.println("ptmMe3Array");
        System.out.println(ptmMe3Array.toString());
//        ptmSu: /PTM/ptm_su.json
//        ptmUb: /PTM/ptm_ub.json

        System.out.println("phosphoGeneSequenceArray ========================");
        log.info("phosphoGeneSequenceArray ========================");
        System.out.println(phosphoGeneSequenceArray.toString());
            timing("timing service=PtmService phase=uniprot-enriched uniqueProteins=%d elapsedMs=%d", proteinToUniprot.size(), elapsedMs(phaseStartNs));
            phaseStartNs = System.nanoTime();

            //First only add the input genes
        for (int i = 0; i < protList.length; i++) {


            //geneString = protList[i].replaceAll("\\[.*?\\] ?", "");
            //Adding gene name at the end
            //inputGene = protList[i] +"/" +proteinToUniprot.get(geneString);

            inputGene = protList[i];
            inputGeneUpper = inputGene.toUpperCase();
            System.out.println("--------(String) protListAndGeneName.get(protList[i])--------");
            System.out.println((String) protListAndGeneName.get(protList[i]));
            if (!ptm_nodeUnique.containsKey(inputGeneUpper)) {

                newNode = generateNode(inputGene, (String) protListAndGeneName.get(protList[i]), "No", pidx, 0, 0.0);//tag 0 is for grey, connected is for verifying if the
                //uniprot is connected to an enzyme or not
                pidx = pidx + 1;
                ptm_NetworkNodes.add(newNode);
                ptm_nodeUnique.put(inputGeneUpper, newNode);
            }

        }




        // Compute Network for the iptmnet genes
        for (int i = 0; i < ptmPhArray.size(); i++) {
            System.out.println("==========================================");
            System.out.println("Compute Network for the ptm genes");
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
//            System.out.println(protList[i]);
            JSONArray gene2PtmList = new JSONArray();


            System.out.println(inputGene);
            if (ptmPhJson.containsKey(inputGene)) {
                System.out.println("gene2KinaseList");
                gene2PtmList = (JSONArray) (ptmPhJson.get(inputGene));
                System.out.println(gene2KinaseList.toString());
            }
            System.out.println("------");
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
                if (isUnknownEnzyme(enzyme, "Unknown_Ph")) {
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


//            log.info("inputgene");
//            System.out.println(protList[i]);
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
                if (isUnknownEnzyme(enzyme, "Unknown_Ac")) {
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


//            log.info("inputgene");
//            System.out.println(protList[i]);
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


                if (isUnknownEnzyme(enzyme, "Unknown_Me")) {
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


//            log.info("inputgene");
//            System.out.println(protList[i]);
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


                if (isUnknownEnzyme(enzyme, "Unknown_Cg")) {
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


//            log.info("inputgene");
//            System.out.println(protList[i]);
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


                if (isUnknownEnzyme(enzyme, "Unknown_My")) {
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


//            log.info("inputgene");
//            System.out.println(protList[i]);
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


                if (isUnknownEnzyme(enzyme, "Unknown_Ng")) {
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


//            log.info("inputgene");
//            System.out.println(protList[i]);
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


                if (isUnknownEnzyme(enzyme, "Unknown_Og")) {
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


//            log.info("inputgene");
//            System.out.println(protList[i]);
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


                if (isUnknownEnzyme(enzyme, "Unknown_Sg")) {
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


//            log.info("inputgene");
//            System.out.println(protList[i]);
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


                if (isUnknownEnzyme(enzyme, "Unknown_Sn")) {
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


//            log.info("inputgene");
//            System.out.println(protList[i]);
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


                if (isUnknownEnzyme(enzyme, "Unknown_Su")) {
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


//            log.info("inputgene");
//            System.out.println(protList[i]);
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


                if (isUnknownEnzyme(enzyme, "Unknown_Ub")) {
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


        ptm_Network.put("nodes", ptm_NetworkNodes);
        ptm_Network.put("edges", ptm_NetworkEdges);
        //==============================================
        network.put("ptm_Network", ptm_Network);

        network.put("PTM_table", ptmTableArray);


        System.out.println("PTM_table");
        System.out.println(ptmTableArray.toString());

        System.out.println("ptm network");
        System.out.println(ptm_Network.toString());
        timing("timing service=PtmService phase=network-built nodes=%d edges=%d tableRows=%d elapsedMs=%d",
                ptm_NetworkNodes.size(), ptm_NetworkEdges.size(), ptmTableArray.size(), elapsedMs(phaseStartNs));
        timing("timing service=PtmService phase=total inputs=%d elapsedMs=%d", protList.length, elapsedMs(requestStartNs));
        return network;
    }


    private boolean isUnknownEnzyme(String enzyme, String legacyPlaceholder) {
        return enzyme == null
            || "unknown".equalsIgnoreCase(enzyme)
            || legacyPlaceholder.equals(enzyme);
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
//        System.out.println("******************");
//        System.out.println("******************");
//
//        System.out.println("++++++++++");
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
//                //System.out.println("uniprotMap:" + uniprotMap.toString());
//
////            response = UtilsNetwork.getInstance().readUrlXml(uri);
////            log.info(response);
////            JSONParser parser = new JSONParser();
////
////            geneInfoJson = (JSONObject) parser.parse(response);
////            System.out.println("--------------------");
////            JSONObject geneSymbol = (JSONObject) geneInfoJson.get("symbol");
////            System.out.println("===================");
////            System.out.println(geneInfoJson.toString());
////            System.out.println(geneSymbol.toString());
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
