package edu.uc.eh.service;


import edu.uc.eh.utils.UtilsNetwork;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.Arrays;
import java.util.Map;

//import java.util.Map;

/**
 * Created by behrouzsh on 7/17/17.
 */

@Service
public class UniprotService2 {

    private static final Logger log = LoggerFactory.getLogger(UniprotService2.class);

    @Value("${urls.uniprot2}")
    String uniprotTemplate;

    public JSONObject getTable(String organism, String protein) {

        String response;
        System.out.println(protein);
        //String xmlResponse;
        String uniprotUrl = String.format(uniprotTemplate, protein, organism);
        Map uniprotMap;
        System.out.println("Querying: " + uniprotUrl + "from uniprot database");


        try {
            response = UtilsNetwork.getInstance().readUrlXml(uniprotUrl);
            //System.out.println("Response from readXml: " + response);

            uniprotMap = UtilsNetwork.loadXMLFromString(response);
            //System.out.println("uniprotMap:" + uniprotMap.toString());
        } catch (Exception e) {

            String msg =  String.format("Uniprot %s not found even in uniprot database!", protein);
            System.out.println(msg);
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
