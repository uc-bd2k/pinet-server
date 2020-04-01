package edu.uc.eh.service;

import edu.uc.eh.uniprot.UniprotRepository;
import edu.uc.eh.utils.UtilsNetwork;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uk.ac.ebi.pride.utilities.pridemod.ModReader;
import uk.ac.ebi.pride.utilities.pridemod.controller.impl.PRIDEModDataAccessController;
import uk.ac.ebi.pride.utilities.pridemod.controller.impl.PSIModDataAccessController;
import uk.ac.ebi.pride.utilities.pridemod.controller.impl.UnimodDataAccessController;
//import uk.ac.ebi.pride.utilities.pridemod.ModReader;
//import uk.ac.ebi.pride.utilities.pridemod.controller.impl.UnimodDataAccessController;
//import uk.ac.ebi.pride.utilities.pridemod.model;

import uk.ac.ebi.pride.utilities.pridemod.model.PTM;
import uk.ac.ebi.pride.utilities.pridemod.model.Specificity;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Created by shamsabz on 1/24/19.
 */
@Service
public class PrideService {
    private static final Logger log = LoggerFactory.getLogger(PrideService.class);



    @Value("${resources.unimodXml}")
    String unimodDatabase;

    @Value("${resources.psimodXml}")
    String psimodDatabase;

    @Value("${resources.pridemodXml}")
    String pridemodDatabase;

    @Value("${urls.findPTMByIDAPIUrl}")
    String findPTMByIDAddress;
    @Value("${urls.findPTMByDescriptionAPIUrl}")
    String findPTMByDescriptionAddress;
    @Value("${urls.findPTMByMassAndDeltaAPIUrl}")
    String findPTMByMassAndDeltaAddress;

    public JSONObject findPTMByIDAPI(String mod) throws Exception {
        JSONObject ptmJson = new JSONObject();
        //mod = mod.toUpperCase().replace("_",":").replace("-",":").replace("(","").replace(")","");
        String findPTMByIDUrl = String.format(findPTMByIDAddress, mod);
        String predictionString;
        try{
            predictionString = UtilsNetwork.getInstance().readUrlXml(findPTMByIDUrl);
//            System.out.println("ResponseString:");
//            System.out.println(predictionString);
            //log.info(response);

            JSONParser parser = new JSONParser();
            ptmJson = (JSONObject) parser.parse(predictionString);

        }
        catch (Exception e){
            System.out.println(e);
            ptmJson.put("accession","");
            ptmJson.put("name","");
            ptmJson.put("monoDeltaMass","");
            ptmJson.put("averageDeltaMass","");
            ptmJson.put("description","");
            ptmJson.put("formula","");
            ptmJson.put("cvLabel","");
            ptmJson.put("shortName","");
            return ptmJson;

        }
        return ptmJson;

    }
    public JSONArray findPTMByDescriptionAPI(String description) throws Exception {
        String findPTMByDescriptionUrl = String.format(findPTMByDescriptionAddress, description);
        String predictionString;
        JSONArray ptmArray = new JSONArray();
        try {

            predictionString = UtilsNetwork.getInstance().readUrlXml(findPTMByDescriptionUrl);
//            System.out.println("ResponseString:");
//            System.out.println(predictionString);
            //log.info(response);

            JSONParser parser = new JSONParser();
            ptmArray = (JSONArray) parser.parse(predictionString);

        }
        catch (Exception e){
            System.out.println(e);

            return new JSONArray();

        }

        //ModReader modReader = ModReader.getInstance();
        //PTM ptm = modReader.getPTMbyAccession("MOD:00048");

        return ptmArray;

    }

    public JSONArray findPTMByMassAndDeltaAPI(Double mass, Double delta) throws Exception {

        String findPTMByMassAndDeltaUrl = String.format(findPTMByMassAndDeltaAddress, mass, delta);
        String predictionString;
        JSONArray ptmArray = new JSONArray();
        try {
            predictionString = UtilsNetwork.getInstance().readUrlXml(findPTMByMassAndDeltaUrl);
//            System.out.println("ResponseString:");
//            System.out.println(predictionString);
            //log.info(response);

            JSONParser parser = new JSONParser();
            ptmArray = (JSONArray) parser.parse(predictionString);
        }
        catch (Exception e){
            System.out.println(e);

            return new JSONArray();

        }

        //ModReader modReader = ModReader.getInstance();
        //PTM ptm = modReader.getPTMbyAccession("MOD:00048");

        return ptmArray;

    }

    public JSONObject findPTMByID(String mod) throws Exception {
        JSONObject ptmJson = new JSONObject();
        mod = mod.toUpperCase().replace("_",":").replace("-",":").replace("(","").replace(")","");
        try{
            ModReader modReader = ModReader.getInstance();
            PTM ptm = modReader.getPTMbyAccession(mod);
            ptmJson = toJSON(ptm);
        }
        catch (Exception e){
            System.out.println(e);
            ptmJson.put("accession","");
            ptmJson.put("name","");
            ptmJson.put("monoDeltaMass","");
            ptmJson.put("averageDeltaMass","");
            ptmJson.put("description","");
            ptmJson.put("formula","");
            ptmJson.put("cvLabel","");
            ptmJson.put("shortName","");
            return new JSONObject();

        }
        return ptmJson;

    }
    public JSONArray findPTMByDescription(String description) throws Exception {
        ModReader modReader = ModReader.getInstance();
        List<PTM> ptmListByMonoDeltaMass = modReader.getPTMListByPatternDescription(description);
        JSONArray ptmArray = new JSONArray();
        System.out.print(description);
        try {
            for (int i = 0; i < ptmListByMonoDeltaMass.size(); i++)
            {
                PTM ptm = (PTM)ptmListByMonoDeltaMass.get(i);
                JSONObject ptmJson = toJSON(ptm);
                ptmArray.add(ptmJson);
                if (ptmJson.get("averageDeltaMass") != null) {

                    //acetyl
//                if (ptmJson.get("averageDeltaMass").equals(42.04)){
//                    System.out.print("'" + ptmJson.get("accession") + "':'ac',");
//                }
                    //phospho
//                if (ptmJson.get("averageDeltaMass").equals(79.9799)){
//                    System.out.print("'" + ptmJson.get("accession") + "':'ph',");
//                }
////                //phospho
//                if (ptmJson.get("averageDeltaMass").equals(79.98)){
//
//                    System.out.print("'" + ptmJson.get("accession") + "':'ph',");
//                }
                    //methyl
//                if (14. < (Double)ptmJson.get("averageDeltaMass") && (Double)ptmJson.get("averageDeltaMass")< 14.33){
//                    System.out.print("'" + ptmJson.get("accession") + "':'me',");
//                }
//                //dimethyl

//                    if (28. < (Double) ptmJson.get("averageDeltaMass") && (Double) ptmJson.get("averageDeltaMass") < 28.06) {
//                        System.out.print("'" + ptmJson.get("accession") + "':'me2',");
//                    }

//                //trimethyl
                if (42. < (Double)ptmJson.get("averageDeltaMass") && (Double)ptmJson.get("averageDeltaMass")< 42.08){
                    System.out.print("'" + ptmJson.get("accession") + "':'me3',");
                }
//                //Myristo
//                if (ptmJson.get("averageDeltaMass").equals(210.36)){
//                    System.out.print("(" + ptmJson.get("accession") + "),");
//                }
                }
            }
        }
        catch (Exception e){
            System.out.println(e);

            return new JSONArray();

        }

        //ModReader modReader = ModReader.getInstance();
        //PTM ptm = modReader.getPTMbyAccession("MOD:00048");

        return ptmArray;

    }

    public JSONArray findPTMByMassAndDelta(Double mass, Double delta) throws Exception {
        ModReader modReader = ModReader.getInstance();
        List<PTM> ptmListByMonoDeltaMass = modReader.getPTMListByMonoDeltaMass(mass, delta);
        JSONArray ptmArray = new JSONArray();
        try {
            for (int i = 0; i < ptmListByMonoDeltaMass.size(); i++)
            {
                PTM ptm = (PTM)ptmListByMonoDeltaMass.get(i);
                if(ptm.getCvLabel() == "PSI-MOD") {
                    JSONObject ptmJson = toJSON(ptm);
                    ptmArray.add(ptmJson);
                }
            }
        }
        catch (Exception e){
            System.out.println(e);

            return new JSONArray();

        }

        //ModReader modReader = ModReader.getInstance();
        //PTM ptm = modReader.getPTMbyAccession("MOD:00048");

        return ptmArray;

    }
    public JSONObject toJSON(PTM ptm) throws Exception {
        JSONObject ptmJson = new JSONObject();

        ptmJson.put("accession",ptm.getAccession());
        ptmJson.put("name",ptm.getName());
        ptmJson.put("monoDeltaMass",ptm.getMonoDeltaMass());
        ptmJson.put("averageDeltaMass",ptm.getAveDeltaMass());
        ptmJson.put("description",ptm.getDescription());
        ptmJson.put("formula",ptm.getFormula());
        ptmJson.put("cvLabel",ptm.getCvLabel());
        ptmJson.put("shortName",ptm.getShortName());
//        ptmJson.put("synonyms",ptm.getSynonyms());
//        ptmJson.put("synonyms",ptm.getUni());





        return ptmJson;
    }

}
