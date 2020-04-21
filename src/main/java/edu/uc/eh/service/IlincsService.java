package edu.uc.eh.service;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by shamsabz on 2/6/18.
 */
@Service
public class IlincsService {
    private static final Logger log = LoggerFactory.getLogger(IlincsService.class);

    @Value("${urls.ilincsSignatureUrl}")
    String ilincsSignatureUrl;
    @Value("${urls.ilincsSignatureMetaUpload}")
    String ilincsSignatureMetaUpload;
    @Value("${urls.ilincsGetSignatureUrl}")
    String ilincsGetSignatureUrl;
    private final String USER_AGENT = "Mozilla/5.0";
    JSONObject ilincsErrorJson = new JSONObject();
    BufferedReader fr;



    public JSONObject getIlincsErrorJson() {
        return ilincsErrorJson;
    }

    private static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public JSONObject getSignatureIds(String[] geneList) throws Exception {
        String url = ilincsSignatureMetaUpload;
        String urlGetJson = ilincsGetSignatureUrl;


//For getting url to ilincs
        String urlSignature = ilincsSignatureUrl;
        //{"id":"S19ladHYz","fileName":"genes.txt","url":"http://www.ilincs.org/ilincs/uploadedSignature/S19ladHYz"}
        ilincsErrorJson.put("id",0);
        ilincsErrorJson.put("fileName","genesToIlincs.txt");
        ilincsErrorJson.put("url","http://www.ilincs.org");
        //String url = "https://selfsolve.apple.com/wcResults.do";
        HttpClient clientSignature = HttpClientBuilder.create().build();
        String signatureUrl;
        JSONObject geneResultJSON = new JSONObject();
        HttpPost postSignature = new HttpPost(urlSignature);
        postSignature.setHeader("User-Agent", USER_AGENT);



        //For getting actual signatures from ilincs
        HttpClient client = HttpClientBuilder.create().build();
        //String signatureUrl;
        List<String> fileNameList;
        String fileName;

        JSONObject uploadResultJSON = new JSONObject();
        JSONObject uploadFileResultJSON = new JSONObject();
        JSONObject uploadErrorJSON = new JSONObject();
        uploadErrorJSON.put("cMAP",new JSONArray());
        uploadErrorJSON.put("knockdown",new JSONArray());
        uploadErrorJSON.put("perturbations",new JSONArray());

        //HttpPost post = new HttpPost(url);
        HttpPost postGetJson2 = new HttpPost(urlGetJson);
        HttpPost postGetJson5 = new HttpPost(urlGetJson);
        HttpPost postGetJson6 = new HttpPost(urlGetJson);

        // add header

        //post.setHeader("User-Agent", USER_AGENT);
        postGetJson5.setHeader("User-Agent", USER_AGENT);
        postGetJson2.setHeader("User-Agent", USER_AGENT);
        postGetJson6.setHeader("User-Agent", USER_AGENT);
        String randomNumber = String.valueOf(getRandomNumberInRange(1, 10000));

        File payload5 = new File("genesToIlincs" + randomNumber + ".txt");

        try{
            //StringWriter stringWriter = new StringWriter();
            //PrintWriter writer = new PrintWriter(stringWriter);
            PrintWriter writer4 = new PrintWriter(payload5, "UTF-8");
            writer4.print("Name_GeneSymbol"+","+"Value_LogDiffExp"+ System.getProperty("line.separator"));
            //System.out.print("Name_GeneSymbol"+","+"Value_LogDiffExp"+ System.getProperty("line.separator"));
            for (int i = 0; i < geneList.length - 2; i++) {
                writer4.print(geneList[i] + "," + geneList[i + 1] + System.getProperty("line.separator"));
                //System.out.print(geneList[i] + "," + geneList[i + 1] + System.getProperty("line.separator"));
                i = i + 1;
            }
            writer4.print(geneList[geneList.length - 2] + "," + geneList[geneList.length - 1] );
//            System.out.print(geneList[geneList.length - 2] + "," + geneList[geneList.length - 1] );
//            System.out.println("=====Printing out writer=======");

            writer4.flush();

            writer4.close();
            //System.out.println("-------------");


//            try{
                //ContentType plainAsciiContentType = ContentType.create("text/plain", Consts.ASCII);

//
//                System.out.println("\nSending 'POST' request to URL : " + url);
//                System.out.println("Post parameters : " + post.getEntity());
//                System.out.println("Response Code : " +
//                        response.getStatusLine().getStatusCode());
//
//                BufferedReader rd = new BufferedReader(
//                        new InputStreamReader(response.getEntity().getContent()));
//
//                StringBuffer result = new StringBuffer();
//                String line = "";
//                while ((line = rd.readLine()) != null) {
//                    result.append(line);
//                }
//                System.out.println("results: ");
//                System.out.println(result.toString());
//
//                JSONParser parser = new JSONParser();
//                geneResultJSON = (JSONObject) parser.parse(result.toString());
//
//                signatureUrl = ((String) geneResultJSON.get("url"));
//                System.out.println(signatureUrl);
//                //EnrichrJSON = new JSONObject(result.toString());
//                //EnrichrJSON = result;
//
//                return geneResultJSON;
//
//            } catch (IOException e) {
//                // do something
//                return ilincsErrorJson;
//            }














//            HttpEntity entity = MultipartEntityBuilder.create()
//                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
//                    .addPart("list", new FileBody(payload5))
////                .addPart("username", new StringBody("bond", plainAsciiContentType))
////                .addPart("password", new StringBody("vesper", plainAsciiContentType))
//                    .build();




            //ContentType plainAsciiContentType = ContentType.create("text/plain", Consts.ASCII);
            HttpEntity entity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addPart("list", new FileBody(payload5))
//                .addPart("username", new StringBody("bond", plainAsciiContentType))
//                .addPart("password", new StringBody("vesper", plainAsciiContentType))
                    .build();


            try {
                HttpPost httpPostSignatureUrl = new HttpPost(ilincsSignatureUrl);
                httpPostSignatureUrl.setEntity(entity);

                HttpResponse response = client.execute(httpPostSignatureUrl);

//                System.out.println("\nSending 'POST' request to URL : " + url);
//                System.out.println("Post parameters : " + postSignature.getEntity());
//                System.out.println("Response Code : " +
//                        response.getStatusLine().getStatusCode());

                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));

                StringBuffer result = new StringBuffer();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
//                System.out.println("results: ");
//                System.out.println(result.toString());

                JSONParser parser = new JSONParser();
                geneResultJSON = (JSONObject) parser.parse(result.toString());

                signatureUrl = ((String) geneResultJSON.get("url"));
                //System.out.println(signatureUrl);
                //EnrichrJSON = new JSONObject(result.toString());
                //EnrichrJSON = result;
                uploadResultJSON.put("url", signatureUrl);
                //return geneResultJSON;

            }
            catch (Exception e){
                uploadResultJSON.put("url", "http://www.ilincs.org");

            }




            HttpPost httpPost = new HttpPost(ilincsSignatureMetaUpload);
            httpPost.setEntity(entity);

            HttpResponse response = client.execute(httpPost);
//
//            System.out.println("\nSending 'POST' request to URL : " + url);
//            System.out.println("Post parameters : " + httpPost.getEntity());
//            System.out.println("Response Code : " +
//                    response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer resultIlincs = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                resultIlincs.append(line);
            }
            //System.out.println("results: ");
            //System.out.println(resultIlincs.toString());

            JSONParser parser = new JSONParser();
            uploadFileResultJSON = (JSONObject) parser.parse(resultIlincs.toString());

            fileNameList = (List<String>) ((JSONObject)uploadFileResultJSON.get("status")).get("fileName");
            fileName = fileNameList.get(0);
            //System.out.println(fileName);

            String ilincsurl = urlGetJson;

            try{
                URL obj2 = new URL(ilincsurl);
                HttpURLConnection con2 = (HttpURLConnection) obj2.openConnection();

                //add reuqest header
                con2.setRequestMethod("POST");
                con2.setRequestProperty("User-Agent", "Mozilla/5.0");
                con2.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                //--------------------------------------------------------------
                String urlParameters2 = String.format("lib=LIB_2&file=%s&path=/mnt/raid/tmp/", fileName);
                //System.out.println(urlParameters2);
                // Send post request
                con2.setDoOutput(true);
                DataOutputStream wr2 = new DataOutputStream(con2.getOutputStream());
                wr2.writeBytes(urlParameters2);
                wr2.flush();
                wr2.close();

                int responseCode = con2.getResponseCode();
                //System.out.println("\nSending 'POST' request to URL : " + url);
//                System.out.println("Post parameters : " + urlParameters2);
//                System.out.println("Response Code : " + responseCode);

                BufferedReader in2 = new BufferedReader(
                        new InputStreamReader(con2.getInputStream()));
                String inputLine2;
                StringBuffer response2 = new StringBuffer();

                while ((inputLine2 = in2.readLine()) != null) {
                    response2.append(inputLine2);
                }
                in2.close();
                JSONParser parser2 = new JSONParser();
                JSONObject uploadResultJSON2 = (JSONObject) parser2.parse(response2.toString());
                //System.out.println(uploadResultJSON2.toString());
                JSONArray concordanceTable2 = (JSONArray)uploadResultJSON2.get("concordanceTable");
                //print result
                if ((concordanceTable2.get(0)).equals("NA")){
                    //String remark = (String)uploadResultJSON6.get("Remark");
                    uploadResultJSON.put("cMAP",new JSONArray());
                }
                else {
                    uploadResultJSON.put("cMAP", concordanceTable2);
                }
                //System.out.println(response2.toString());

            }catch (IOException e2) {
            // do something
                uploadResultJSON.put("cMAP",new JSONArray());
            }
            try{
                URL obj5 = new URL(ilincsurl);
                HttpURLConnection con5 = (HttpURLConnection) obj5.openConnection();

                //add reuqest header
                con5.setRequestMethod("POST");
                con5.setRequestProperty("User-Agent", "Mozilla/5.0");
                con5.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                //--------------------------------------------------------------
                String urlParameters5 = String.format("lib=LIB_5&file=%s&path=/mnt/raid/tmp/", fileName);

                // Send post request
                con5.setDoOutput(true);
                DataOutputStream wr5 = new DataOutputStream(con5.getOutputStream());
                wr5.writeBytes(urlParameters5);
                wr5.flush();
                wr5.close();

                int responseCode5 = con5.getResponseCode();
                //System.out.println("\nSending 'POST' request to URL : " + url);
//                System.out.println("Post parameters : " + urlParameters5);
//                System.out.println("Response Code : " + responseCode5);

                BufferedReader in5 = new BufferedReader(
                        new InputStreamReader(con5.getInputStream()));
                String inputLine5;
                StringBuffer response5 = new StringBuffer();

                while ((inputLine5 = in5.readLine()) != null) {
                    response5.append(inputLine5);
                }
                in5.close();
                //System.out.println(response5.toString());
                JSONParser parser5 = new JSONParser();
                JSONObject uploadResultJSON5 = (JSONObject) parser5.parse(response5.toString());
                //System.out.println(uploadResultJSON5.toString());
                JSONArray concordanceTable5 = (JSONArray)uploadResultJSON5.get("concordanceTable");
                //print result
                if ((concordanceTable5.get(0)).equals("NA")){
                    //String remark = (String)uploadResultJSON6.get("Remark");
                    uploadResultJSON.put("perturbations",new JSONArray());
                }
                else {
                    uploadResultJSON.put("perturbations", concordanceTable5);
                }


            }catch (IOException e5) {
                // do something
                uploadResultJSON.put("perturbations",new JSONArray());
                //return ilincsErrorJson;
            }
            try{
                URL obj6 = new URL(ilincsurl);
                HttpURLConnection con6 = (HttpURLConnection) obj6.openConnection();

                //add reuqest header
                con6.setRequestMethod("POST");
                con6.setRequestProperty("User-Agent", "Mozilla/5.0");
                con6.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                //--------------------------------------------------------------
                String urlParameters6 = String.format("lib=LIB_6&file=%s&path=/mnt/raid/tmp/", fileName);
                // Send post request
                con6.setDoOutput(true);
                DataOutputStream wr6 = new DataOutputStream(con6.getOutputStream());
                wr6.writeBytes(urlParameters6);
                wr6.flush();
                wr6.close();

                int responseCode = con6.getResponseCode();
                //System.out.println("\nSending 'POST' request to URL : " + url);
//                System.out.println("Post parameters : " + urlParameters6);
//                System.out.println("Response Code : " + responseCode);

                BufferedReader in6 = new BufferedReader(
                        new InputStreamReader(con6.getInputStream()));
                String inputLine;
                StringBuffer response6 = new StringBuffer();

                while ((inputLine = in6.readLine()) != null) {
                    response6.append(inputLine);
                }
                //System.out.println(response6.toString());
                in6.close();
                JSONParser parser6 = new JSONParser();
                JSONObject uploadResultJSON6 = (JSONObject) parser6.parse(response6.toString());
                //System.out.println(uploadResultJSON6.toString());
                JSONArray concordanceTable6 = (JSONArray)uploadResultJSON6.get("concordanceTable");
                //print result
                if ((concordanceTable6.get(0)).equals("NA")){
                        //String remark = (String)uploadResultJSON6.get("Remark");
                    uploadResultJSON.put("knockdown",new JSONArray());
                }
                else {
                    uploadResultJSON.put("knockdown", concordanceTable6);
                }

                //print result


            }catch (IOException e6) {
                // do something
                uploadResultJSON.put("knockdown",new JSONArray());
                //return ilincsErrorJson;
            }

            //EnrichrJSON = new JSONObject(result.toString());
            //EnrichrJSON = result;

            //return uploadResultJSON;

        } catch (IOException e) {
            // do something
            return uploadErrorJSON;
        }

    //    System.out.println(uploadResultJSON);
        return uploadResultJSON;
    }

    public JSONObject getSignatureUrl(String[] geneList) throws Exception {
        String url = ilincsSignatureUrl;
        //{"id":"S19ladHYz","fileName":"genes.txt","url":"http://www.ilincs.org/ilincs/uploadedSignature/S19ladHYz"}
        ilincsErrorJson.put("id",0);
        ilincsErrorJson.put("fileName","genesToIlincs.txt");
        ilincsErrorJson.put("url","http://www.ilincs.org");
        //String url = "https://selfsolve.apple.com/wcResults.do";
        HttpClient client = HttpClientBuilder.create().build();
        String signatureUrl;
        //JSONObject EnrichrJSON = new JSONObject();

        JSONObject geneResultJSON = new JSONObject();
        HttpPost post = new HttpPost(url);

        // add header

        post.setHeader("User-Agent", USER_AGENT);

        String randomNumber = String.valueOf(getRandomNumberInRange(10000, 100000));

        //File payload5 = new File("genesToIlincs" + randomNumber + ".txt");
        File payload2 = new File("genesToIlincs" + randomNumber + ".txt");

        try{
            //StringWriter stringWriter = new StringWriter();
            //PrintWriter writer = new PrintWriter(stringWriter);
            PrintWriter writer4 = new PrintWriter(payload2, "UTF-8");
            writer4.print("Name_GeneSymbol"+","+"Value_LogDiffExp"+ System.getProperty("line.separator"));
            //System.out.print("Name_GeneSymbol"+","+"Value_LogDiffExp"+ System.getProperty("line.separator"));
            for (int i = 0; i < geneList.length - 2; i++) {
                writer4.print(geneList[i] + "," + geneList[i + 1] + System.getProperty("line.separator"));
                System.out.print(geneList[i] + "," + geneList[i + 1] + System.getProperty("line.separator"));
                i = i + 1;
            }
            writer4.print(geneList[geneList.length - 2] + "," + geneList[geneList.length - 1] );
            //System.out.print(geneList[geneList.length - 2] + "," + geneList[geneList.length - 1] );
            //System.out.println("=====Printing out writer=======");

            writer4.flush();


//            fr = new BufferedReader(new FileReader(String.valueOf(writer)));
//            String lines = "";
//            while((lines = fr.readLine()) != null) {
//                System.out.println(lines);
//            }
//            System.out.println(writer.toString());
            //System.out.println(stringWriter.toString());

            writer4.close();
            //System.out.println("-------------");






            ContentType plainAsciiContentType = ContentType.create("text/plain", Consts.ASCII);
            HttpEntity entity = MultipartEntityBuilder.create()
                    .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                    .addPart("list", new FileBody(payload2))
//                .addPart("username", new StringBody("bond", plainAsciiContentType))
//                .addPart("password", new StringBody("vesper", plainAsciiContentType))
                    .build();
            HttpPost httpPost = new HttpPost(ilincsSignatureUrl);
            httpPost.setEntity(entity);

            HttpResponse response = client.execute(httpPost);

//            System.out.println("\nSending 'POST' request to URL : " + url);
//            System.out.println("Post parameters : " + post.getEntity());
//            System.out.println("Response Code : " +
//                    response.getStatusLine().getStatusCode());

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
//            System.out.println("results: ");
//            System.out.println(result.toString());

            JSONParser parser = new JSONParser();
            geneResultJSON = (JSONObject) parser.parse(result.toString());

            signatureUrl = ((String) geneResultJSON.get("url"));
//            System.out.println(signatureUrl);
            //EnrichrJSON = new JSONObject(result.toString());
            //EnrichrJSON = result;

            return geneResultJSON;

        } catch (IOException e) {
            // do something
            return ilincsErrorJson;
        }


    }
}
