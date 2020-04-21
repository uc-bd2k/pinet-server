package edu.uc.eh.utils;

//import org.json.JSONException;
//import org.json.JSONObject;

import edu.uc.eh.peptideMatch.Fasta;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by chojnasm on 11/17/15.
 * Edited by Behrouzsh on 9/6/16.
 */
public class UtilsNetwork {

    private static final Logger log = LoggerFactory.getLogger(UtilsNetwork.class);
    private static UtilsNetwork instance;

    private UtilsNetwork() {
    }

    static {
        instance = new UtilsNetwork();
    }

    public static UtilsNetwork getInstance() {
System.out.println(instance);
        return instance;
    }

    public String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);
            return buffer.toString().replace('\'', '\"');
        } finally {
            if (reader != null)
                reader.close();
        }
    }

//    public JSONObject parseNestedJSONObject(String jsonDataString) throws ParseException {
//
//
//        JSONParser parser = new JSONParser();
//        JSONObject jsonObject = (JSONObject) parser.parse(jsonDataString);
//
//        //JSONObject jsonObject = new JSONObject(jsonDataString);
//        List<String> list = new ArrayList<String>();
//        JSONArray jsonArray = jsonObject..getJSONArray("userInfo");
//        for(int i = 0 ; i < jsonArray.length(); i++) {
//            list.add(jsonArray.getJSONObject(i).getString("username"));
//            System.out.println(jsonArray.getJSONObject(i).getString("username")); // display usernames
//        }
//
//    }
    public JSONObject readFastaUrl(String urlString, String pr) throws Exception {
        JSONObject proteinJson = new JSONObject();
        JSONObject nullJson = new JSONObject();
        nullJson.put("protein_id", "");
        nullJson.put("info", "");
        nullJson.put("sequence", "");
        BufferedReader reader = null;
        BufferedReader br;
        System.out.println("in readFastaUrl");
        System.out.println(urlString);
        System.out.println(pr);
        try {



            URL url = new URL(urlString);
            br = new BufferedReader(new InputStreamReader(url.openStream()));
//            StringBuffer buffer = new StringBuffer();
//
//            br = new BufferedReader(new FileReader(file));
            String line;
            String id = "";
            String seq = "";
            long index = 0;
            while ((line = br.readLine()) != null) {
//                System.out.println(line);
//                System.out.println("-----");
                if (line.length() > 0) {
                    index++;
                    if (index == 1 && !line.startsWith(">")) {
                        System.err.println(" is not a valid Fasta file. Reason: first line must be started with \">\"\n");


                        return nullJson;
                    }
                    if (line.startsWith(">")) {
                        if (!id.equals("") && !seq.equals("")) {
                            Fasta protenFasta = new Fasta(id, seq);
                            id = "";
                            seq = "";
                        } else if (index > 1 && seq.equals("")) {
                            System.err.println(" is not a valid Fasta file. Reason: the sequence is missing for the id: " + id + "\n");


                            return nullJson;
                        } else if (index > 1 && id.equals("")) {
                            System.err.println(" is not a valid Fasta file. Reason: the id is missing for the following sequences: \n" + seq + "\n");


                            return nullJson;

                            //return null;
                        }
                        String gt = "^>";
                        line = line.replaceAll(gt, "");
                        String space = "\\s+";
                        String[] defline = line.split(space);
                        id = defline[0];
                    } else {
                        if (line.length() > 0) {
                            seq += line ;
//                            System.out.println("seq");
//                            System.out.println(seq);
                        }
                    }
                }
            }
            if (!id.equals("") && !seq.equals("")) {

                proteinJson.put("protein_id", pr);
                proteinJson.put("info", id);
                proteinJson.put("sequence", seq);
                System.out.println("Size of sequence");
                System.out.println(seq.length());
            } else if (seq.equals("")) {
                System.err.println(" is not a valid Fasta file. Reason: the sequence is missing for the id: " + id + "\n");
                return nullJson;
            } else if (id.equals("")) {
                System.err.println(" is not a valid Fasta file. Reason: the id is missing for the following sequences: \n" + seq + "\n");

                return nullJson;
            }

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return proteinJson;


    }

//    public JSONObject loadJSONFromJSON(String urlString) throws Exception {
//        BufferedReader reader = null;
//        try {
//            URL url = new URL(urlString);
//            reader = new BufferedReader(new InputStreamReader(url.openStream()));
//            StringBuffer buffer = new StringBuffer();
//            int read;
//            char[] chars = new char[1024];
//            while ((read = reader.read(chars)) != -1)
//                buffer.append(chars, 0, read);
//            return new JSONObject(buffer);
//        } finally {
//            if (reader != null)
//                reader.close();
//        }
//    }

    public String readUrlXml(String urlString) throws Exception {
        //System.out.println("inside readUrlXml");
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);
            //System.out.println(buffer.toString());
            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }



    public static Map loadJSONFromStringHarmonizomeGene(String json) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(json));
        Document doc = builder.parse(is);
        Map<String, Object> proteinToUniprotMap = new HashMap<String, Object>();
        //Map proteinToUniprotMap = new HashMap();

        // get the first element
        doc.getDocumentElement().normalize();
        //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
        //Element root = doc.getDocumentElement();
        NodeList uniprotNodeList = doc.getElementsByTagName("symbol");
        Node entryNode = uniprotNodeList.item(0);
        NodeList entryNodeList = entryNode.getChildNodes();
        int found = 0;
        if (entryNodeList != null && entryNodeList.getLength() > 0) {
            for (int i = 0; i < entryNodeList.getLength(); i++) {
                Node entryNodeListNode = entryNodeList.item(i);

                if (entryNodeListNode.getNodeName() == "gene") {

                    //if (found == 0) {
                    NodeList geneNodeList = entryNodeListNode.getChildNodes();
                    proteinToUniprotMap.put("primary_gene_name", geneNodeList.item(1).getTextContent());
                    //   found = 1;
//                    log.info("========================");
//                    log.info(geneNodeList.item(1).getTextContent());
//                    log.info("++++++++++++++++++++");
                    // }

                }


                if (entryNodeListNode.getNodeName() == "sequence") {
                    Element sequenceElement = (Element) entryNodeListNode;

                    proteinToUniprotMap.put("length", sequenceElement.getAttribute("length"));
                    log.info(entryNodeList.item(i).getTextContent());
                    proteinToUniprotMap.put("sequence", entryNodeList.item(i).getTextContent().replace("\n", ""));
                }


            }
        }


        return proteinToUniprotMap;

    }


//    public static Map loadJSONFromString(String json) throws Exception
//    {
//
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder builder = factory.newDocumentBuilder();
//        InputSource is = new InputSource(new StringReader(json));
//        Document doc = builder.parse(is);
//        Map<String, Object> proteinToUniprotMap = new HashMap<String, Object>();
//        //Map proteinToUniprotMap = new HashMap();
//
//        // get the first element
//        doc.getDocumentElement().normalize();
//        //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
//        //Element root = doc.getDocumentElement();
//        NodeList uniprotNodeList = doc.getElementsByTagName("entry");
//        Node entryNode = uniprotNodeList.item(0);
//        NodeList entryNodeList = entryNode.getChildNodes();
//        //int found = 0;
//        if (entryNodeList != null && entryNodeList.getLength() > 0) {
//            for (int i = 0; i < entryNodeList.getLength(); i++) {
//                Node entryNodeListNode = entryNodeList.item(i);
//
//                if(entryNodeListNode.getNodeName() == "gene")
//                {
//
//                    //if (found == 0) {
//                        NodeList geneNodeList = entryNodeListNode.getChildNodes();
//                        proteinToUniprotMap.put("gene_id", geneNodeList.item(1).getTextContent());
//                     //   found = 1;
////                    log.info("========================");
////                    log.info(geneNodeList.item(1).getTextContent());
////                    log.info("++++++++++++++++++++");
//                   // }
//
//                }
//
//
//                if(entryNodeListNode.getNodeName() == "sequence") {
//                    Element sequenceElement = (Element)entryNodeListNode;
//
//                    proteinToUniprotMap.put("length", sequenceElement.getAttribute("length"));
//                    log.info(entryNodeList.item(i).getTextContent());
//                    proteinToUniprotMap.put("sequence", entryNodeList.item(i).getTextContent().replace("\n", ""));
//                }
//
//
//
//            }
//        }
//
//
//        return proteinToUniprotMap;
//
//    }


    public static Map loadXMLFromString(String xml) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        Document doc = builder.parse(is);
        Map<String, Object> proteinToUniprotMap = new HashMap<String, Object>();
        ArrayList proteinToGeneList = new ArrayList();
        //Map proteinToUniprotMap = new HashMap();

        // get the first element
        doc.getDocumentElement().normalize();
        //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
        //Element root = doc.getDocumentElement();
        NodeList uniprotNodeList = doc.getElementsByTagName("entry");
        Node entryNode = uniprotNodeList.item(0);
        NodeList entryNodeList = entryNode.getChildNodes();
        int found = 0;
        if (entryNodeList != null && entryNodeList.getLength() > 0) {
            for (int i = 0; i < entryNodeList.getLength(); i++) {
                Node entryNodeListNode = entryNodeList.item(i);

                if (entryNodeListNode.getNodeName() == "gene") {

                    if (found == 0) {
                        NodeList geneNodeList = entryNodeListNode.getChildNodes();
                        proteinToGeneList.add(geneNodeList.item(1).getTextContent());
                        // found = 1;
                        System.out.println("========================");
                        System.out.println(geneNodeList.item(1).getTextContent());
                        System.out.println("++++++++++++++++++++");
                    }

                }


                if (entryNodeListNode.getNodeName() == "sequence") {
                    Element sequenceElement = (Element) entryNodeListNode;

                    proteinToUniprotMap.put("length", sequenceElement.getAttribute("length"));
                    log.info(entryNodeList.item(i).getTextContent());
                    proteinToUniprotMap.put("sequence", entryNodeList.item(i).getTextContent().replace("\n", ""));

                }


            }
            proteinToUniprotMap.put("primary_gene_name", proteinToGeneList);
        }


        return proteinToUniprotMap;

    }


    public static JSONObject loadXMLFromStringPIR(String xml) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        Document doc = builder.parse(is);
        Map<String, Object> peptideToProteinMap = new HashMap<String, Object>();
        JSONObject peptideToProteinJson = new JSONObject();
        JSONArray matchList = new JSONArray();
        JSONObject match = new JSONObject();
        String sequence_ac;
        String sequence_id;
        //int start;
        int stop;
        String length;
        String numberOfMatchedProteinsInt;
        String start;
        String end;
        //Map proteinToUniprotMap = new HashMap();

        // get the first element
        doc.getDocumentElement().normalize();
        //System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
        //Element root = doc.getDocumentElement();
        // <numberOfMatchedProteins>17</numberOfMatchedProteins>
        //Node numberOfMatchedProteins = doc.getElementsByTagName("numberOfMatchedProteins");
        //log.info("here--1");

        //Node numberOfMatchedProteins = (Node) doc.getElementsByTagName("numberOfMatchedProteins");
//        if (numberOfMatchedProteins) {
//        } else {
//            numberOfMatchedProteinsInt = numberOfMatchedProteins.getTagName();
//        }


        //umberOfMatchedProteins = numberOfMatchedProteins.item(0).getTextContent();
        //log.info("here0");
        NodeList matchPerPeptide = doc.getElementsByTagName("matchPerPeptide");
//        numberOfMatchedProteinsInt = (String) ((Element) bookslist.item(0)).getElementsByTagName("servername").
//                item(0).getChildNodes().item(0).getNodeValue();
        Node matchPerPeptideNode = matchPerPeptide.item(0);
        NodeList perPeptideNodeList = matchPerPeptideNode.getChildNodes();
        numberOfMatchedProteinsInt = "";
        //int found = 0;
        //log.info("here1");
        if (perPeptideNodeList != null && perPeptideNodeList.getLength() > 0) {
            //match = null;

            for (int i = 0; i < perPeptideNodeList.getLength(); i++) {
                //log.info("here2");

                Node perPeptideNodeListNode = perPeptideNodeList.item(i);

                if (perPeptideNodeListNode.getNodeName() == "numberOfMatchedProteins") {
                    numberOfMatchedProteinsInt = perPeptideNodeListNode.getTextContent();
                    log.info(numberOfMatchedProteinsInt);
                }

                if (perPeptideNodeListNode.getNodeName() == "matchedProtein") {
                    NodeList matchedProteinNodeList = perPeptideNodeListNode.getChildNodes();
                    //log.info("here3");
                    start = "";
                    end = "";
                    length = "";
                    sequence_ac = "";
                    sequence_id = "";
                    for (int j = 0; j < matchedProteinNodeList.getLength(); j++) {
                        //log.info("here4");
                        Node matchedProteinNode = matchedProteinNodeList.item(j);
                        if (matchedProteinNode.getNodeName() == "proteinAC") {

                            sequence_ac = matchedProteinNodeList.item(j).getTextContent();

                            log.info(sequence_ac);



                        }

                        if (matchedProteinNode.getNodeName() == "proteinID") {

                            sequence_id = matchedProteinNodeList.item(j).getTextContent();


                            log.info(sequence_id);
                        }

                        if (matchedProteinNode.getNodeName() == "seqLength") {

                            length = matchedProteinNodeList.item(j).getTextContent();

                            log.info("length ------- ");
                            log.info(length);



                        }
                        if (matchedProteinNode.getNodeName() == "matchRanges") {
                            log.info("here5");
                            NodeList matchRangesNodeList = matchedProteinNode.getChildNodes();

                            //log.info("here6");
                            for (int k = 0; k < matchRangesNodeList.getLength(); k++) {
                                Node matchRangeNode = matchRangesNodeList.item(k);
                                //match = null;
                                match = new JSONObject();
                                log.info("++++");
                                log.info(matchRangeNode.getNodeName());
                                log.info("----");
                                if (matchRangeNode.getNodeName() == "matchRange") {

//                                    Element sequenceElement = (Element) entryNodeListNode;
//
//                                    proteinToUniprotMap.put("length", sequenceElement.getAttribute("length"));
//                                    //log.info(entryNodeList.item(i).getTextContent());
//                                    proteinToUniprotMap.put("sequence", entryNodeList.item(i).getTextContent().replace("\n", ""));




                                    log.info("here8");
                                    Element matchRangeElement = (Element) matchRangeNode;
                                    start = matchRangeElement.getAttribute("start");
                                    end = matchRangeElement.getAttribute("end");

                                    match.put("start", matchRangeElement.getAttribute("start"));
                                    match.put("stop", matchRangeElement.getAttribute("end"));
                                    match.put("length", length);
                                    match.put("sequence_ac", sequence_ac);
                                    match.put("sequence_id", sequence_id);
                                    log.info("----------------");
                                    log.info(start);
                                    log.info(end);
                                    log.info(length);
                                    log.info(sequence_ac);
                                    log.info(sequence_id);
                                    log.info(match.toJSONString());
                                    matchList.add(match);
                                    log.info(match.toJSONString());
//                                    proteinToUniprotMap.put("sequence", entryNodeList.item(i).getTextContent().replace("\n", ""));
//
//                                    sequence_ac = matchedProteinNodeList.item(j).getTextContent();
//                                    match.put("sequence_ac", sequence_ac);

                                }

//                            NodeList geneNodeList = entryNodeListNode.getChildNodes();
//                            proteinToGeneList.add(geneNodeList.item(1).getTextContent());


                            }
                        }
                        ///


                    }


                }

            }
            peptideToProteinMap.put("matchset", matchList.toJSONString());
            peptideToProteinMap.put("n_match", numberOfMatchedProteinsInt);
            peptideToProteinJson.put("matchset", matchList.toJSONString());
            peptideToProteinJson.put("n_match", numberOfMatchedProteinsInt);
        }

        return peptideToProteinJson;

    }

//    public static Map<String, Object> parse(JSONObject json , Map<String,Object> out) throws JSONException{
//        Iterator<String> keys = json.keys();
//        while(keys.hasNext()){
//            String key = keys.next();
//            String val = null;
//            try{
//                JSONObject value = json.getJSONObject(key);
//                parse(value,out);
//            }catch(Exception e){
//                val = json.getString(key);
//            }
//
//            if(val != null){
//                out.put(key,val);
//            }
//        }
//        return out;
//    }


}
