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
public class PhosphoServiceV2 {

    private static final Logger log = LoggerFactory.getLogger(PhosphoServiceV2.class);

    private long elapsedMs(long startNs) {
        return (System.nanoTime() - startNs) / 1_000_000;
    }

    private void timing(String template, Object... args) {
        System.out.println(String.format(template, args));
    }


    @Value("${resources.phosphoGenes2Kinase}")
    String phosphoGenes2KinaseInfo;

    @Value("${resources.phosphoGeneProbability}")
    String phosphoGeneProbabilityInfo;


    @Value("${resources.blosum50Json}")
    String blosum50Info;

    @Value("${resources.phosphoAmino2KinaseSequence}")
    String phosphoAmino2KinaseSequenceInfo;



    @Autowired
    UniprotService uniprotService;

    private volatile JSONObject cachedPhosphoGene2PrJson;
    private volatile JSONObject cachedPhosphoGene2KinaseJson;
    private volatile JSONObject cachedPhosphoAmino2KinaseSequenceJson;
    private volatile JSONObject cachedBlosum50Json;

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
        if (cachedPhosphoGene2PrJson != null
                && cachedPhosphoGene2KinaseJson != null
                && cachedPhosphoAmino2KinaseSequenceJson != null
                && cachedBlosum50Json != null) {
            return;
        }

        synchronized (this) {
            if (cachedPhosphoGene2PrJson == null) {
                cachedPhosphoGene2PrJson = loadRequiredJson(phosphoGeneProbabilityInfo, "phosphoGeneProbabilityInfo");
            }
            if (cachedPhosphoGene2KinaseJson == null) {
                cachedPhosphoGene2KinaseJson = loadRequiredJson(phosphoGenes2KinaseInfo, "phosphoGenes2KinaseInfo");
            }
            if (cachedPhosphoAmino2KinaseSequenceJson == null) {
                cachedPhosphoAmino2KinaseSequenceJson = loadRequiredJson(phosphoAmino2KinaseSequenceInfo, "phosphoAmino2KinaseSequenceInfo");
            }
            if (cachedBlosum50Json == null) {
                cachedBlosum50Json = loadRequiredJson(blosum50Info, "blosum50Info");
            }
        }
    }

    public JSONObject computePhosphoNetwork(String organism, String[] protList) throws Exception {
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
        JSONObject geneProbJson = new JSONObject();
        JSONArray geneProbJsonArray = new JSONArray();
        JSONObject geneDefJson = new JSONObject();
        JSONArray geneDefJsonArray = new JSONArray();
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


        ensureResourceCachesLoaded();
        timing("timing service=PhosphoServiceV2 phase=resource-cache-loaded inputs=%d elapsedMs=%d", protList.length, elapsedMs(phaseStartNs));
        phaseStartNs = System.nanoTime();
        phosphoGene2PrJson = cachedPhosphoGene2PrJson;
        phosphoGene2KinaseJson = cachedPhosphoGene2KinaseJson;
        phosphoAmino2KinaseSequenceJson = cachedPhosphoAmino2KinaseSequenceJson;
        blosum50Json = cachedBlosum50Json;


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
                String token = insideMatcher.group(1);
                System.out.println(token);
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

        long startFirstTime   = System.nanoTime();

            timing("timing service=PhosphoServiceV2 phase=input-parsed inputs=%d elapsedMs=%d", protList.length, elapsedMs(phaseStartNs));
            phaseStartNs = System.nanoTime();

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
            System.out.println(uniprot_id);


            if (!proteinToUniprot.containsKey(uniprot_id)) {
                geneSequenceInfo = uniprotService.findByAccessionApi(uniprot_id);
                proteinToUniprot.put(uniprot_id, geneSequenceInfo);
            } else {
                geneSequenceInfo = (JSONObject) proteinToUniprot.get(uniprot_id);
            }

            ArrayList<String> geneName = (ArrayList<String>) geneSequenceInfo.get("primary_gene_name");
            if (geneName != null && geneName.size() > 0) {
                protListAndGeneName.put(protList[i], protList[i] + "(" + geneName.get(0).toString() + ")");
            } else {
                protListAndGeneName.put(protList[i], protList[i]);
            }
            System.out.println("protListAndGeneName////////////////////");
            System.out.println(protList[i]);
            System.out.println(geneName);
            System.out.println(protListAndGeneName.get(protList[i]));
            System.out.println("////////////////////////////");

            System.out.println(protList[i]);
            System.out.println(proteinToPhosphoAminoArray.toString());
            System.out.println("before loop");

            for (int j = 0; j < proteinToPhosphoAminoArray.size(); j++) {
                System.out.println("j----------------------------------------");
                System.out.println(j);
                JSONObject proteinToPhosphoAminoItem = (JSONObject) proteinToPhosphoAminoArray.get(j);
                System.out.println(proteinToPhosphoAminoItem.toJSONString());
                System.out.println(proteinToPhosphoAminoItem.get("ptm").toString());


                //For phosphosite
//        ptmCg: /PTM/ptm_cg.json
//        ptmMy: /PTM/ptm_my.json
//        ptmNg: /PTM/ptm_ng.json
//        ptmOg: /PTM/ptm_og.json
//        ptmSg: /PTM/ptm_sg.json
//        ptmSn: /PTM/ptm_sn.json
//        ptmSu: /PTM/ptm_su.json
//        ptmUb: /PTM/ptm_ub.json
                if (proteinToPhosphoAminoItem.get("ptm").equals("p")) {


                    //if (Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) < 81 && Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) > 79) {
                    JSONObject phosphoGeneSequenceJson = new JSONObject();
                    //String[] proteinAndOrganism = { uniprot_id, organismForQueryUniprot };
                    //System.out.println(uniprot_id);


                    geneSequenceInfo = (JSONObject) proteinToUniprot.get(uniprot_id);

                    System.out.println(proteinToPhosphoAminoItem.get("ptm"));

                    String cutSequence = cutSequenceMethod(geneSequenceInfo.get("sequence").toString(), Integer.parseInt((String) proteinToPhosphoAminoItem.get("site")));
                    //System.out.println(protList[i]);

//                    log.info(phosphoAminoArray.get(i).toString());
//                    log.info(phosphoSiteArray.get(i).toString());
//                    System.out.println("cutSequence ++++++++++++++++++++++");
//                    System.out.println(cutSequence);
//                    System.out.println("protListAndGeneName ++++++++++++++++++++++");
//                    System.out.println(protListAndGeneName.get(protList[i]));

                    phosphoGeneSequenceJson.put("phosphoSite", uniprot_id + "{[p" + proteinToPhosphoAminoItem.get("amino") + "]@" + proteinToPhosphoAminoItem.get("site") + "}");
                    phosphoGeneSequenceJson.put("phosphoProtein", protList[i]);
                    phosphoGeneSequenceJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                    phosphoGeneSequenceJson.put("site", proteinToPhosphoAminoItem.get("site"));
                    phosphoGeneSequenceJson.put("sequence", cutSequence);
                    //phosphoGeneSequenceJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                    System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                    System.out.println(phosphoGeneSequenceJson.toString());
                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
//                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

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
                    System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                    System.out.println(ptmItemJson.toString());
                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
                    //System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                    ptmPhArray.add(ptmItemJson);
                    ptmArray.add(ptmItemJson);

                    //}


                }
                else {
                    try {
                        Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm"));


                        if (Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) < 81 && Double.parseDouble((String) proteinToPhosphoAminoItem.get("ptm")) > 79) {
                            JSONObject phosphoGeneSequenceJson = new JSONObject();
                            //String[] proteinAndOrganism = { uniprot_id, organismForQueryUniprot };
                            //System.out.println(uniprot_id);


                            geneSequenceInfo = (JSONObject) proteinToUniprot.get(uniprot_id);

                            //System.out.println(geneSequenceInfo.toString());

                            String cutSequence = cutSequenceMethod(geneSequenceInfo.get("sequence").toString(), Integer.parseInt((String) proteinToPhosphoAminoItem.get("site")));
                            //System.out.println(protList[i]);

//                    log.info(phosphoAminoArray.get(i).toString());
//                    log.info(phosphoSiteArray.get(i).toString());
//                    System.out.println("cutSequence ++++++++++++++++++++++");
//                    System.out.println(cutSequence);
//                    System.out.println("protListAndGeneName ++++++++++++++++++++++");
//                    System.out.println(protListAndGeneName.get(protList[i]));

                            phosphoGeneSequenceJson.put("phosphoSite", uniprot_id + "[" + proteinToPhosphoAminoItem.get("amino") + "+" + proteinToPhosphoAminoItem.get("ptm") + "@" + proteinToPhosphoAminoItem.get("site") + "]");
                            phosphoGeneSequenceJson.put("phosphoProtein", protList[i]);
                            phosphoGeneSequenceJson.put("amino", proteinToPhosphoAminoItem.get("amino"));
                            phosphoGeneSequenceJson.put("site", proteinToPhosphoAminoItem.get("site"));
                            phosphoGeneSequenceJson.put("sequence", cutSequence);
                            //phosphoGeneSequenceJson.put("proteinToGene",protListAndGeneName.get(protList[i]));
                            System.out.println("phosphoGeneSequenceJson.toString() ----------------- ");
                            System.out.println(phosphoGeneSequenceJson.toString());
                            System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");
//                    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++ ");

                            phosphoGeneSequenceArray.add(phosphoGeneSequenceJson);
                        }



                    } catch (NumberFormatException e) {
                        //not a double
                    }
                }

            }

        }


        long endFirstTime   = System.nanoTime();
        long totalFirstTime = endFirstTime - startFirstTime;

        long startSecondTime   = System.nanoTime();

        System.out.println("phosphoGeneSequenceArray ========================");
        log.info("phosphoGeneSequenceArray ========================");
        System.out.println(phosphoGeneSequenceArray.toString());
            timing("timing service=PhosphoServiceV2 phase=uniprot-enriched uniqueProteins=%d elapsedMs=%d", proteinToUniprot.size(), elapsedMs(phaseStartNs));
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

            //Add new node if not existed to nodeunique
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
        for (int i = 0; i < phosphoGeneSequenceArray.size(); i++) {
            System.out.println("==========================================");
            System.out.println("Compute Network for the indefinite genes based on blosum50");
            //log.info("inputgene");
            //log.info(input[i]);
            JSONObject phosphoGeneSequenceJson2 = (JSONObject) phosphoGeneSequenceArray.get(i);
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
            //System.out.println (phosphoAmino + "  ++++++++++++++++++++++++++++++");


            for (Object key : phosphoKinaseSequence.keySet()) {
                //iteration ++;
                blosumScoreMax = computeBlosum50Score(blosum50Json, phosphoSequence, phosphoSequence);
                blosumScoreHigh = (int) ((double) blosumScoreMax / 2.0);
                //System.out.println ("Blosum high schore " + blosumScoreHigh);
                String keyStr = (String) key;
                //log.info("keyStr");
                //log.info(keyStr);
                Object keyvalue = phosphoKinaseSequence.get(keyStr);
                JSONArray keyvalueJsonArray = (JSONArray) keyvalue;
                geneBlosumJsonArrayFinal = new JSONArray();
                //Class cls = keyvalue.getClass();
                //System.out.println("The type of the object is: " + cls.getName());
                //Print key and value
                //System.out.println("key: " + keyStr);
                //System.out.println ("value: " + keyvalueJsonArray.get(0));
                //System.out.println ("value: " + keyvalueJsonArray);
                //System.out.println (keyStr + "  =========================================");

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
//                        System.out.println("here-1");
                        System.out.println("blosumScore " + blosumScore + " kinase " + keyStr + " kinaseSequence " + kinaseSequence);


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
                        System.out.println("==========================================");
                        geneBlosumJsonArrayFinal = geneBlosumJsonArray;
                    } else if (blosumScore == blosumScoreHigh) {
                        System.out.println("here-2");
                        System.out.println("blosumScore " + blosumScore + " kinase " + keyStr + " kinaseSequence " + kinaseSequence);

                        blosumString = kinaseSequence;
                        blosumOrganism = organism;
                        geneBlosumJson = new JSONObject();
                        geneBlosumJson.put("phosphoGene", phosphoGene);
                        geneBlosumJson.put("ptm", ptm);
                        geneBlosumJson.put("amino", phosphoAmino);
                        geneBlosumJson.put("geneSequence", phosphoSequence);
                        geneBlosumJson.put("kinase", keyStr);
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
                        System.out.println("==========================================");
                    }

                    //System.out.println ("organism: " + organism + " kinase: " + kinaseSequence);
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
        System.out.println("==========================================");
        System.out.println("Compute Network for the indefinite genes based on probability");
        System.out.println(phosphoGeneSequenceArray.toString());
        for (int i = 0; i < phosphoGeneSequenceArray.size(); i++) {

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
//                System.out.println("keyStr: " + keyStr);
//                System.out.println("keyvalue: " + keyvalue);
                //Print key and value
                //System.out.println("key: " + keyStr);
                //System.out.println ("value: " + keyvalueJsonArray.get(0));

                //Compute probability
                double pr = computeProbability(keyvalueJsonArray, phosphoSequence);

                if (pr > 0.0) {

                    geneProbJson = new JSONObject();
                    geneProbJson.put("phosphoGene", inputphosphoProtein);
                    geneProbJson.put("amino", phosphoAmino);
                    geneProbJson.put("geneSequence", phosphoSequence);
                    geneProbJson.put("kinase", keyStr);
                    geneProbJson.put("probability", pr);
                    geneProbJson.put("phosphoSite", inputGene);


                    geneProbJsonArray.add(geneProbJson);
                    if (!indefinite_nodeUnique.containsKey(keyStr)) {
                        //log.info(gene);


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
//            System.out.println(protList[i]);
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

                geneDefJson = new JSONObject();
                geneDefJson.put("phosphoGene", inputphosphoProtein);

                geneDefJson.put("kinase", gene);

                geneDefJson.put("phosphoSite", inputGene);


                geneDefJsonArray.add(geneDefJson);

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

        long endSecondTime   = System.nanoTime();
        long totalSecondTime = endSecondTime - startSecondTime;



        indefinite_Kinase_Gene_Network.put("nodes", indefinite_Kinase_Gene_NetworkNodes);
        indefinite_Kinase_Gene_Network.put("edges", indefinite_Kinase_Gene_NetworkEdges);
        definite_Kinase_Gene_Network.put("nodes", definite_Kinase_Gene_NetworkNodes);
        definite_Kinase_Gene_Network.put("edges", definite_Kinase_Gene_NetworkEdges);
        definite_blosum_Kinase_Gene_Network.put("nodes", definite_blosum_Kinase_Gene_NetworkNodes);
        definite_blosum_Kinase_Gene_Network.put("edges", definite_blosum_Kinase_Gene_NetworkEdges);
        blosum_Kinase_Gene_Network.put("nodes", blosum_Kinase_Gene_NetworkNodes);
        blosum_Kinase_Gene_Network.put("edges", blosum_Kinase_Gene_NetworkEdges);

        //==============================================


//        {value: "Known_Kinase_TargetGene"},
//        {value: "Known+PredictedbyBlosum50_Kinase_TargetGene"},
//        {value: "Known+PredictedbyPWM_Kinase_TargetGene"}
        network.put("Blosum50_table", geneBlosumArray);
        network.put("Definite_table", geneDefJsonArray);
        network.put("Indefinite_table", geneProbJsonArray);
        network.put("Known_Kinase_TargetGene", definite_Kinase_Gene_Network);
        network.put("Blosum50_Exact_Match_Kinase_TargetGene", definite_blosum_Kinase_Gene_Network);
        network.put("Predicted_Probability_Kinase_TargetGene", indefinite_Kinase_Gene_Network);
        network.put("Predicted_Blosum50_Kinase_TargetGene", blosum_Kinase_Gene_Network);
        network.put("Known+Blosum50_Exact_Match_Kinase_TargetGene", definite_blosum_Kinase_Gene_Network);
        network.put("Known+PredictedbyPWM_Kinase_TargetGene", indefinite_Kinase_Gene_Network);
        network.put("Known+PredictedbyBlosum50_Kinase_TargetGene", blosum_Kinase_Gene_Network);
        System.out.println("phosphoNetwork");
        System.out.println(network);
        System.out.println("totalFirstTime");
        System.out.println(totalFirstTime/1000000000);
        System.out.println("totalSecondTime");
        System.out.println(totalSecondTime/1000000000);
        timing("timing service=PhosphoServiceV2 phase=network-built definiteNodes=%d blosumRows=%d ptmRows=%d elapsedMs=%d",
                definite_Kinase_Gene_NetworkNodes.size(), geneBlosumArray.size(), ptmTableArray.size(), elapsedMs(phaseStartNs));
        timing("timing service=PhosphoServiceV2 phase=total inputs=%d elapsedMs=%d", protList.length, elapsedMs(requestStartNs));
        return network;
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
//            System.out.println("The type of the object is: " + cls2.getName());
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
//                System.out.println("The type of the object is: " + cls2.getName());

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
