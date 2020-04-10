package edu.uc.eh.service;

import edu.uc.eh.utils.UtilsNetwork;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by shamsabz on 1/22/20.
 */
@Service
public class DeepPhosService {

    private static final Logger log = LoggerFactory.getLogger(DeepPhosService.class);

    @Value("${urls.deepPhosUrl}")
    String deepPhosAddress;

    public JSONObject getPhosphoPrediction(String organism, String ptmList) throws Exception {
        System.out.println("in getPhosphoPrediction");
        System.out.println(organism);
        System.out.println(ptmList);
        JSONObject prediction = new JSONObject();
        String predictionString;

        String deepPhosUrl = String.format(deepPhosAddress, organism, ptmList);
        System.out.println("url:");
        System.out.println(deepPhosUrl);
        ArrayList<String> kinases = new ArrayList<String>(
                Arrays.asList("family_CDK","family_CK2","family_MAPK","family_PKC","family_Src",
                        "group_AGC","group_Atypical","group_CAMK","group_CMGC","group_TK",
                        "kinase_CDC2","kinase_CK2a1","kinase_PKACa","kinase_PKCa","kinase_SRC",
                        "subfamily_CDC2","subfamily_CDK2","subfamily_ERK1","subfamily_PKCa"));

        try {

            predictionString = UtilsNetwork.getInstance().readUrlXml(deepPhosUrl);
//            System.out.println("ResponseString:");
//            System.out.println(predictionString);
            //log.info(response);

        JSONParser parser = new JSONParser();
        JSONObject geneInfo = (JSONObject) parser.parse(predictionString);
        for (String kinase : kinases){
//            System.out.println(kinase);
//            System.out.println("---------------------");
            JSONArray kinaseInfo = (JSONArray) parser.parse((String)geneInfo.get(kinase));

            prediction.put(kinase,kinaseInfo);
            //object=jsonParser.parse(data);
//            JSONArray kinaseList = (JSONArray) geneInfo.get(kinase);
//            System.out.println(kinaseList.toJSONString());
            //System.out.println(geneInfo..get(kinase));

        }

//        JSONObject geneResults = (JSONObject) geneInfo.get("gene");
//        JSONObject rootJSON = (JSONObject) new JSONParser().parse(predictionString);
//
//
//        for (String kinase : kinases){
//            JSONArray kinaseList = (JSONArray) rootJSON.get(kinase);
//            System.out.println(kinaseList.toJSONString());
//
//        }

//        JSONArray rootJSONArray;
//        kinases.forEach(kinase->
//                JSONArray rootJSONArray = (JSONArray) rootJSON.get(kinase);
//                System.out.println(number)
//
//
//        );
//
//        for(Object projectObj: dataList.toArray()){
//            JSONObject project = (JSONObject)projectObj;
//            JSONArray issueList = (JSONArray) project.get("issue");
//            for(Object issueObj: issueList.toArray()){
//                JSONObject issue = (JSONObject) issueObj;
//                //do something with the issue
//            }
//        }
//            JSONParser parser = new JSONParser();
//            prediction = (JSONObject) parser.parse(predictionString);
//
//
//            System.out.println(prediction.toJSONString());
//            //System.out.println(pathwayInfo);
//            System.out.println("------------");
        }
        catch (Exception e) {

            String msg =  String.format("Error in obtaining phosphoPredict");
            log.warn(msg);
            //throw new RuntimeException(msg);
            return prediction;
        }


        return prediction;
    }

}
