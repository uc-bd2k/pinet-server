package edu.uc.eh.service;

import edu.uc.eh.peptideMatch.Fasta;
import edu.uc.eh.peptideMatch.PeptideMatchCMD;
import edu.uc.eh.uniprot.UniprotRepository;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by shamsabz on 4/10/20.
 */



@Service
public class PeptideWithValueService {
    private static final Logger log = LoggerFactory.getLogger(PeptideWithValueService.class);

    private final PeptideRegexServive peptideRegexServive;

    @Autowired
    //public RestAPI(HarmonizomeGeneService harmonizomeGeneService, HarmonizomeProteinService harmonizomeProteinService, PrositeService prositeService, PsiModService psiModService, UniprotService uniprotService, EnrichrService enrichrService, PCGService pcgService, KinaseService kinaseService, ShorthandService shorthandService, PhosphoService phosphoService, HarmonizomeGeneService harmonizomeGeneServics1) {
    public PeptideWithValueService(PeptideRegexServive peptideRegexServive) {
        this.peptideRegexServive = peptideRegexServive;
    }

    //http://localhost:8090/pln/api/peptidewithvalue/organism/9606/peptides/K[me2S]TGG[aK]APR%7B-0.0649%7D,KA[pY]SF[myC]GT[+22.2]V[+21.2]E[pY]MAPE(MOD:00259)T[+22.2]VNR%7B-0.5987%7D
//http://localhost:8090/pln/api/peptidewithvalue/organism/9606/peptides/[meK]STGG[aK]APR{-0.0649},[aK]STGG[aK]APR{-0.0215},[meK][pS]TGGKAPR{-0.2427},[meK]SAPATGGV[meK]KPHR{0.2786},EIAQDF[meK]TDLR{0.1569},IYQ[pY]IQSR[+42.010]{1.3012},[aK]AYSFCGTVEY(UNIMOD:21)MAP{-2.1456},KA[pY]SF[myC]GTVE[pY]MAPEVVNR{-0.5987},SVDLIKTYKHINEVYYAKKKRRHQ{-0.3312},PHSHQY[pS]DRR{1.3267}

    public JSONObject getTable(String organism, String[] peptides) {

        JSONObject response = new JSONObject();
        JSONObject peptideSummary = new JSONObject();
        JSONObject avgProteinSummary = new JSONObject();
        JSONObject uniqueProteinSummary = new JSONObject();
        String peptide;
        //System.out.println(Arrays.toString(peptides));
        JSONObject peptideJson = new JSONObject();
        //peptideItem = parsedPeptide;
        String motif;
        //String xmlResponse;
        //peptide[0] is the peptide, peptide[1] is the organism
        ArrayList<String> modifications;
        JSONObject protein_matches = new JSONObject();
        int n_match;
        JSONArray matches = new JSONArray();
        int p_match = 0;
        Double value;
        JSONObject proteinJson = new JSONObject();
        JSONObject ptmProteinJson = new JSONObject();
        JSONArray proteinJsonArray = new JSONArray();
        JSONArray ptmProteinJsonArray = new JSONArray();
        JSONArray proteinUniqueJsonArray = new JSONArray();
        JSONArray ptmProteinUniqueJsonArray = new JSONArray();
        ArrayList uniqueProteinList = new ArrayList();
        ArrayList uniquePtmProteinList = new ArrayList();
        ArrayList firstFoundProteinList = new ArrayList();
        ArrayList firstFoundPtmProteinList = new ArrayList();
        ArrayList averageProteinList = new ArrayList();
        ArrayList averagePtmProteinList = new ArrayList();
        JSONObject uniqueProteinJson = new JSONObject();
        JSONObject uniquePtmProteinJson = new JSONObject();
        JSONObject firstFoundProteinJson = new JSONObject();
        JSONObject firstFoundPtmProteinJson = new JSONObject();
        JSONObject averageProteinJson = new JSONObject();
        JSONObject averagePtmProteinJson = new JSONObject();


        ArrayList<Fasta> queries = new ArrayList<Fasta>();

        for (int i = 0; i < peptides.length; i++) {
            //System.out.println(peptides[i]);
            //JSONObject peptideItem = new JSONObject();
            //peptideItem.put("peptide", peptides[i]);

            JSONObject parsedPeptide = peptideRegexServive.getMotifAndModification(peptides[i]);
            //peptideItem = parsedPeptide;
            motif = (String)parsedPeptide.get("motif");
            peptideSummary.put(peptides[i], parsedPeptide);

            queries.add(new Fasta(motif, motif));
        }
        peptideJson = PeptideMatchCMD.doLocalQuery(organism, queries, 3, false);
//        System.out.println(peptideJson);
//        System.out.println("====");

        for (int i = 0; i < peptides.length; i++) {
            motif = (String)((JSONObject)peptideSummary.get(peptides[i])).get("motif");
            modifications = (ArrayList)((JSONObject)peptideSummary.get(peptides[i])).get("modifications");
            value = (Double)((JSONObject)peptideSummary.get(peptides[i])).get("value");
            protein_matches = (JSONObject) peptideJson.get(motif);
            n_match = (int)protein_matches.get("n_match");
            peptide = peptides[i].split("\\{")[0];

            matches = (JSONArray) protein_matches.get("matchset");
            System.out.println("peptide = " + peptide);
//            System.out.println("motif = " + motif);
//            System.out.println("modifications = " + modifications);
//            System.out.println("protein_matches = " + protein_matches);
//            System.out.println("n_match = " + n_match);
//            System.out.println("matches = " + matches);
//            System.out.println("value = " + value);
//            System.out.println("'''''''''''''" );
//            System.out.println("'''''''''''''" );
//            System.out.println("'''''''''''''" );
            //System.out.println("'''''''''''''" );

            p_match = 0;

            for (int j = 0; j < n_match; j++){
                String db = (String) ((JSONObject)matches.get(j)).get("sequence_db");

                if(db.equals("sp")) {

                    p_match += 1;
                }

            }

            int n_match_iter = 0;
            for (int j = 0; j < n_match; j++){

                //System.out.println("match = " + matches.get(j));

                int start = (int)((JSONObject)matches.get(j)).get("start");
                String sequence_ac = (String) ((JSONObject)matches.get(j)).get("sequence_ac");
                String db = (String) ((JSONObject)matches.get(j)).get("sequence_db");
                String protein = "";
                String ptm_protein = "";


                if(db.equals("sp")){
                    n_match_iter += 1;
                            protein = sequence_ac;
                    ptm_protein = sequence_ac;




                    for (int k = 0; k < modifications.size(); k++){
                        //System.out.println("modifications = " + modifications.get(k));
                        ArrayList offsets = new ArrayList();

                        int offset = peptides[i].indexOf(modifications.get(k));
                        if(offset >= 0){
                            offsets.add(offset); //.push(offset);
                        }
                        while (offset >= 0) {
                            //System.out.println("offset = " + offset);
                            offset = peptides[i].indexOf(modifications.get(k), offset + 1);
                            if(offset >= 0){
                                offsets.add(offset); //.push(offset);
                            }


                        }
                        //System.out.println("offsets = " + offsets);

                        for (int offsetIter = 0; offsetIter < offsets.size(); offsetIter++) {
                            int ptmIndex = 0;

                            offset = (int)offsets.get(offsetIter);

                            String beforeOffset = peptides[i].substring(0, offset);

                            int decreaseOffset = beforeOffset.length() - beforeOffset.replaceAll("\\([^)]*?\\)", "").replaceAll("[^A-Z]+", "").length();

                            int position = offset - decreaseOffset + start;
                            ptm_protein += "{" + modifications.get(k)+"@"+position+"}";
                        }


                    }
//                    System.out.println("protein: " + protein);
//                    System.out.println("ptm_protein: " + ptm_protein);
//                    System.out.println("------------------");

                    if(!averageProteinList.contains(protein)){
                        averageProteinList.add(protein);
                        proteinJson = new JSONObject();
                        proteinJson.put("value",value/p_match);
                        proteinJson.put("number_of_connections",1);
                        ArrayList peptide_list = new ArrayList();
                        peptide_list.add(peptide);
                        proteinJson.put("peptide_list",peptide_list);
                        averageProteinJson.put(protein,proteinJson);

                    }
                    else{
                        averageProteinJson.get(protein);
                        Double jValue = (Double)((JSONObject)averageProteinJson.get(protein)).get("value");
                        int jNumber = (int)((JSONObject)averageProteinJson.get(protein)).get("number_of_connections");
                        ArrayList peptide_list = (ArrayList)((JSONObject)averageProteinJson.get(protein)).get("peptide_list");
                        peptide_list.add(peptide);

                        jValue += value/p_match;
                        jNumber += 1;
                        proteinJson = new JSONObject();
                        proteinJson.put("peptide_list",peptide_list);
                        proteinJson.put("value",jValue);
                        proteinJson.put("number_of_connections",jNumber);
                        averageProteinJson.put(protein,proteinJson);
                    }


                    if(p_match == 1){
                        if(!uniqueProteinList.contains(protein)){
                            uniqueProteinList.add(protein);
                            proteinJson = new JSONObject();
                            proteinJson.put("value",value/p_match);
                            proteinJson.put("number_of_connections",1);
                            ArrayList peptide_list = new ArrayList();
                            peptide_list.add(peptide);
                            proteinJson.put("peptide_list",peptide_list);
                            uniqueProteinJson.put(protein,proteinJson);

                        }
                        else{
                            uniqueProteinJson.get(protein);
                            Double jValue = (Double)((JSONObject)uniqueProteinJson.get(protein)).get("value");
                            int jNumber = (int)((JSONObject)uniqueProteinJson.get(protein)).get("number_of_connections");
                            ArrayList peptide_list = (ArrayList)((JSONObject)uniqueProteinJson.get(protein)).get("peptide_list");
                            peptide_list.add(peptide);

                            jValue += value/p_match;
                            jNumber += 1;
                            proteinJson = new JSONObject();
                            proteinJson.put("peptide_list",peptide_list);
                            proteinJson.put("value",jValue);
                            proteinJson.put("number_of_connections",jNumber);
                            uniqueProteinJson.put(protein,proteinJson);
                        }

                    }


                    if(n_match_iter == 1){
                        System.out.println(peptide);
                        System.out.println(ptm_protein);
                        System.out.println(protein);
                        System.out.println(j);
                        System.out.println("-------");
                        if(!firstFoundProteinList.contains(protein)){
                            firstFoundProteinList.add(protein);
                            proteinJson = new JSONObject();
                            proteinJson.put("value",value);
                            proteinJson.put("number_of_connections",1);
                            ArrayList peptide_list = new ArrayList();
                            peptide_list.add(peptide);
                            proteinJson.put("peptide_list",peptide_list);
                            firstFoundProteinJson.put(protein,proteinJson);

                        }
                        else{
                            firstFoundProteinJson.get(protein);
                            Double jValue = (Double)((JSONObject)firstFoundProteinJson.get(protein)).get("value");
                            int jNumber = (int)((JSONObject)firstFoundProteinJson.get(protein)).get("number_of_connections");
                            ArrayList peptide_list = (ArrayList)((JSONObject)firstFoundProteinJson.get(protein)).get("peptide_list");
                            peptide_list.add(peptide);

                            jValue += value;
                            jNumber += 1;
                            proteinJson = new JSONObject();
                            proteinJson.put("peptide_list",peptide_list);
                            proteinJson.put("value",jValue);
                            proteinJson.put("number_of_connections",jNumber);
                            firstFoundProteinJson.put(protein,proteinJson);
                        }



                        if(!firstFoundPtmProteinList.contains(ptm_protein)){
                            firstFoundPtmProteinList.add(ptm_protein);
                            proteinJson = new JSONObject();
                            proteinJson.put("value",value);
                            proteinJson.put("number_of_connections",1);
                            ArrayList peptide_list = new ArrayList();
                            peptide_list.add(peptide);
                            proteinJson.put("peptide_list",peptide_list);
                            firstFoundPtmProteinJson.put(ptm_protein,proteinJson);

                        }
                        else{

                            Double jValue = (Double)((JSONObject)firstFoundPtmProteinJson.get(ptm_protein)).get("value");
                            int jNumber = (int)((JSONObject)firstFoundPtmProteinJson.get(ptm_protein)).get("number_of_connections");
                            ArrayList peptide_list = (ArrayList)((JSONObject)firstFoundPtmProteinJson.get(ptm_protein)).get("peptide_list");
                            peptide_list.add(peptide);

                            jValue += value;
                            jNumber += 1;
                            proteinJson = new JSONObject();
                            proteinJson.put("peptide_list",peptide_list);
                            proteinJson.put("value",jValue);
                            proteinJson.put("number_of_connections",jNumber);
                            firstFoundPtmProteinJson.put(ptm_protein,proteinJson);
                        }

                    }





                    if(!averagePtmProteinList.contains(ptm_protein)){
                        averagePtmProteinList.add(ptm_protein);
                        proteinJson = new JSONObject();
                        proteinJson.put("value",value/p_match);
                        proteinJson.put("number_of_connections",1);
                        ArrayList peptide_list = new ArrayList();
                        peptide_list.add(peptide);
                        proteinJson.put("peptide_list",peptide_list);
                        averagePtmProteinJson.put(ptm_protein,proteinJson);

                    }
                    else{

                        Double jValue = (Double)((JSONObject)averagePtmProteinJson.get(ptm_protein)).get("value");
                        int jNumber = (int)((JSONObject)averagePtmProteinJson.get(ptm_protein)).get("number_of_connections");
                        ArrayList peptide_list = (ArrayList)((JSONObject)averagePtmProteinJson.get(ptm_protein)).get("peptide_list");
                        peptide_list.add(peptide);

                        jValue += value/p_match;
                        jNumber += 1;
                        proteinJson = new JSONObject();
                        proteinJson.put("peptide_list",peptide_list);
                        proteinJson.put("value",jValue);
                        proteinJson.put("number_of_connections",jNumber);
                        averagePtmProteinJson.put(ptm_protein,proteinJson);
                    }


                    if(p_match == 1){
                        if(!uniquePtmProteinList.contains(ptm_protein)){
                            uniquePtmProteinList.add(ptm_protein);
                            proteinJson = new JSONObject();
                            proteinJson.put("value",value/p_match);
                            proteinJson.put("number_of_connections",1);
                            ArrayList peptide_list = new ArrayList();
                            peptide_list.add(peptide);
                            proteinJson.put("peptide_list",peptide_list);
                            uniquePtmProteinJson.put(ptm_protein,proteinJson);

                        }
                        else{

                            Double jValue = (Double)((JSONObject)uniquePtmProteinJson.get(ptm_protein)).get("value");
                            int jNumber = (int)((JSONObject)uniquePtmProteinJson.get(ptm_protein)).get("number_of_connections");
                            ArrayList peptide_list = (ArrayList)((JSONObject)uniquePtmProteinJson.get(ptm_protein)).get("peptide_list");
                            peptide_list.add(peptide);

                            jValue += value/p_match;
                            jNumber += 1;
                            proteinJson = new JSONObject();
                            proteinJson.put("peptide_list",peptide_list);
                            proteinJson.put("value",jValue);
                            proteinJson.put("number_of_connections",jNumber);
                            uniquePtmProteinJson.put(ptm_protein,proteinJson);
                        }

                    }











                }



            }

            ((JSONObject)peptideSummary.get(peptides[i])).put("protein_match", protein_matches);

        }

        JSONObject averageProteinJsonSummary = new JSONObject();
        JSONObject uniqueProteinJsonSummary = new JSONObject();
        JSONObject firstFoundProteinJsonSummary = new JSONObject();
        JSONObject averagePtmProteinJsonSummary = new JSONObject();
        JSONObject uniquePtmProteinJsonSummary = new JSONObject();
        JSONObject firstFoundPtmProteinJsonSummary = new JSONObject();


        firstFoundProteinJson.keySet().forEach(keyStr ->
        {
            //JSONObject keyvalue = (JSONObject)averageProteinJson.get(keyStr);
            Double jValue = (Double)((JSONObject)firstFoundProteinJson.get(keyStr)).get("value");
            int jNumber = (int)((JSONObject)firstFoundProteinJson.get(keyStr)).get("number_of_connections");
            ArrayList peptide_list = (ArrayList)((JSONObject)firstFoundProteinJson.get(keyStr)).get("peptide_list");


            jValue = jValue/jNumber;

            JSONObject proteinJson2 = new JSONObject();
            proteinJson2.put("peptide_list",peptide_list);
            proteinJson2.put("value",jValue);
            proteinJson2.put("number_of_connections",jNumber);

            firstFoundProteinJsonSummary.put(keyStr, proteinJson2);

        });

        averageProteinJson.keySet().forEach(keyStr ->
        {
            //JSONObject keyvalue = (JSONObject)averageProteinJson.get(keyStr);
            Double jValue = (Double)((JSONObject)averageProteinJson.get(keyStr)).get("value");
            int jNumber = (int)((JSONObject)averageProteinJson.get(keyStr)).get("number_of_connections");
            ArrayList peptide_list = (ArrayList)((JSONObject)averageProteinJson.get(keyStr)).get("peptide_list");


            jValue = jValue/jNumber;

            JSONObject proteinJson2 = new JSONObject();
            proteinJson2.put("peptide_list",peptide_list);
            proteinJson2.put("value",jValue);
            proteinJson2.put("number_of_connections",jNumber);

            averageProteinJsonSummary.put(keyStr, proteinJson2);

        });

        uniqueProteinJson.keySet().forEach(keyStr ->
        {
            //JSONObject keyvalue = (JSONObject)averageProteinJson.get(keyStr);
            Double jValue = (Double)((JSONObject)uniqueProteinJson.get(keyStr)).get("value");
            int jNumber = (int)((JSONObject)uniqueProteinJson.get(keyStr)).get("number_of_connections");
            ArrayList peptide_list = (ArrayList)((JSONObject)uniqueProteinJson.get(keyStr)).get("peptide_list");


            jValue = jValue/jNumber;

            JSONObject proteinJson2 = new JSONObject();
            proteinJson2.put("peptide_list",peptide_list);
            proteinJson2.put("value",jValue);
            proteinJson2.put("number_of_connections",jNumber);

            uniqueProteinJsonSummary.put(keyStr, proteinJson2);

        });

        firstFoundPtmProteinJson.keySet().forEach(keyStr ->
        {
            //JSONObject keyvalue = (JSONObject)averageProteinJson.get(keyStr);
            Double jValue = (Double)((JSONObject)firstFoundPtmProteinJson.get(keyStr)).get("value");
            int jNumber = (int)((JSONObject)firstFoundPtmProteinJson.get(keyStr)).get("number_of_connections");
            ArrayList peptide_list = (ArrayList)((JSONObject)firstFoundPtmProteinJson.get(keyStr)).get("peptide_list");


            jValue = jValue/jNumber;

            JSONObject proteinJson2 = new JSONObject();
            proteinJson2.put("peptide_list",peptide_list);
            proteinJson2.put("value",jValue);
            proteinJson2.put("number_of_connections",jNumber);

            firstFoundPtmProteinJsonSummary.put(keyStr, proteinJson2);

        });

        averagePtmProteinJson.keySet().forEach(keyStr ->
        {
            //JSONObject keyvalue = (JSONObject)averageProteinJson.get(keyStr);
            Double jValue = (Double)((JSONObject)averagePtmProteinJson.get(keyStr)).get("value");
            int jNumber = (int)((JSONObject)averagePtmProteinJson.get(keyStr)).get("number_of_connections");
            ArrayList peptide_list = (ArrayList)((JSONObject)averagePtmProteinJson.get(keyStr)).get("peptide_list");


            jValue = jValue/jNumber;

            JSONObject proteinJson2 = new JSONObject();
            proteinJson2.put("peptide_list",peptide_list);
            proteinJson2.put("value",jValue);
            proteinJson2.put("number_of_connections",jNumber);

            averagePtmProteinJsonSummary.put(keyStr, proteinJson2);

        });

        uniquePtmProteinJson.keySet().forEach(keyStr ->
        {
            //JSONObject keyvalue = (JSONObject)averageProteinJson.get(keyStr);
            Double jValue = (Double)((JSONObject)uniquePtmProteinJson.get(keyStr)).get("value");
            int jNumber = (int)((JSONObject)uniquePtmProteinJson.get(keyStr)).get("number_of_connections");
            ArrayList peptide_list = (ArrayList)((JSONObject)uniquePtmProteinJson.get(keyStr)).get("peptide_list");


            jValue = jValue/jNumber;

            JSONObject proteinJson2 = new JSONObject();
            proteinJson2.put("peptide_list",peptide_list);
            proteinJson2.put("value",jValue);
            proteinJson2.put("number_of_connections",jNumber);

            uniquePtmProteinJsonSummary.put(keyStr, proteinJson2);

        });

//        System.out.println("-----");
//        System.out.println(averageProteinJsonSummary.toJSONString());
//        System.out.println("-----");
//        System.out.println(uniqueProteinJsonSummary.toJSONString());
//        System.out.println("-----");
//        System.out.println("-----");
//        System.out.println(averagePtmProteinJsonSummary.toJSONString());
//        System.out.println("-----");
//        System.out.println(uniquePtmProteinJsonSummary.toJSONString());
//        System.out.println("-----");


        response.put("peptide_summary", peptideSummary);
        response.put("protein_summary_averaged_sp", averageProteinJsonSummary);
        response.put("protein_summary_distinct_sp", uniqueProteinJsonSummary);
        response.put("protein_summary_representative_sp", firstFoundProteinJsonSummary);
        response.put("(ptm)Protein_summary_averaged_sp", averagePtmProteinJsonSummary);
        response.put("(ptm)protein_summary_distinct_sp", uniquePtmProteinJsonSummary);
        response.put("(ptm)protein_summary_representative_sp", firstFoundPtmProteinJsonSummary);
        return response;
    }
}
