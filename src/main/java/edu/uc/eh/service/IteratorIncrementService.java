package edu.uc.eh.service;
/**
 * Created by shamsabz on 8/9/17.
 */
import edu.uc.eh.utils.UtilsIO;
import edu.uc.eh.peptideMatch.PeptideMatchCMD;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Service
public class IteratorIncrementService {


//    public PCGService() {
//        JSONObject geneCardInfo = UtilsIO.getInstance().readJsonFile("/PCG/pcgm.json");
//    }

    private static final Logger log = LoggerFactory.getLogger(IteratorIncrementService.class);


    public JSONObject changeIterator(Integer param) {
        //param: 1 for add list of peptide, 2 for add list of ptm and 3 for add list of genes

        //JSONObject incrementJson = new JSONObject();
        JSONParser parser = new JSONParser();
        JSONObject incrementJsonOut = new JSONObject();
        JSONObject incrementJsonError = new JSONObject();
        Long peptide;
        Long ptm;
        Long gene;
        Long all;
        Long apipeptide;
        Long apiptm;
        Long apigene;
        Long apiall;



        try {

            String fileName = PeptideMatchCMD.getIncrementFile().getPath();

            Object obj = parser.parse(new FileReader(fileName));

            JSONObject incrementJson = (JSONObject) obj;
//        try {
//
//            incrementJson = UtilsIO.getInstance().readJsonFile("/increment/increment.json");

            //System.out.println(incrementJson);

            peptide = (Long) incrementJson.get("peptide");

            ptm = (Long) incrementJson.get("ptm");

            gene = (Long) incrementJson.get("gene");

            all = (Long) incrementJson.get("all");

            apipeptide = (Long) incrementJson.get("apipeptide");

            apiptm = (Long) incrementJson.get("apiptm");

            apigene = (Long) incrementJson.get("apigene");

            apiall = (Long) incrementJson.get("apiall");

            if (param == 1) {
                peptide += 1;
                all += 1;
                apipeptide += 12;
                apiall += 12;
            }
            if (param == 2) {
                ptm += 1;
                all += 1;
                apiptm += 11;
                apiall += 11;
            }
            if (param == 3) {
                gene += 1;
                all += 1;
                apigene += 10;
                apiall += 10;
            }
            if (param == 4) {
                apipeptide += 1;
                apiall += 1;
            }
            if (param == 5) {
                apiptm += 1;
                apiall += 1;
            }
            if (param == 6) {
                apigene += 1;
                apiall += 1;
            }



            incrementJsonOut.put("peptide", new Integer(Math.toIntExact(peptide)));
            incrementJsonOut.put("ptm", new Integer(Math.toIntExact(ptm)));
            incrementJsonOut.put("gene", new Integer(Math.toIntExact(gene)));
            incrementJsonOut.put("all", new Integer(Math.toIntExact(all)));
            incrementJsonOut.put("apipeptide", new Integer(Math.toIntExact(apipeptide)));
            incrementJsonOut.put("apiptm", new Integer(Math.toIntExact(apiptm)));
            incrementJsonOut.put("apigene", new Integer(Math.toIntExact(apigene)));
            incrementJsonOut.put("apiall", new Integer(Math.toIntExact(apiall)));

            //System.out.println(incrementJsonOut);


        } catch (Exception e) {

            String msg = String.format("Error in obtaining  Information from increment");
            log.warn(msg);
            //throw new RuntimeException(msg);
            incrementJsonError.put("peptide", "");
            incrementJsonError.put("ptm", "");
            incrementJsonError.put("gene", "");
            incrementJsonError.put("all", "");
            incrementJsonError.put("apipeptide", "");
            incrementJsonError.put("apiptm", "");
            incrementJsonError.put("apigene", "");
            incrementJsonError.put("apiall", "");
            return incrementJsonError;
//            incrementJsonOut.put("ptm", new Integer(Math.toIntExact(ptm)));
//            incrementJsonOut.put("gene", new Integer(Math.toIntExact(gene)));
//            incrementJsonOut.put("all", new Integer(Math.toIntExact(all)));
//            incrementJsonOut.put("apipeptide", new Integer(Math.toIntExact(apipeptide)));
//            incrementJsonOut.put("apiptm", new Integer(Math.toIntExact(apiptm)));
//            incrementJsonOut.put("apigene", new Integer(Math.toIntExact(apigene)));
//            incrementJsonOut.put("apiall", new Integer(Math.toIntExact(apiall)));
        }

//        return incrementJsonOut;
//    }
//
//
//    public JSONObject writeBack(JSONObject incrementJsonOut) {

        try (
                FileWriter file = new FileWriter(PeptideMatchCMD.getIncrementFile(), false)) {

            file.write(incrementJsonOut.toJSONString());
            file.flush();
            file.close();
            //System.out.println("here1-2");
        } catch (IOException e) {
            e.printStackTrace();
            String msg =  String.format("Error in writing to increment.json");
            log.warn(msg);
            incrementJsonError.put("peptide", "");
            incrementJsonError.put("ptm", "");
            incrementJsonError.put("gene", "");
            incrementJsonError.put("all", "");
            incrementJsonError.put("apipeptide", "");
            incrementJsonError.put("apiptm", "");
            incrementJsonError.put("apigene", "");
            incrementJsonError.put("apiall", "");
            return incrementJsonError;
        }

        return incrementJsonOut;
        //return uniprotMap;//.toString();
    }

}
