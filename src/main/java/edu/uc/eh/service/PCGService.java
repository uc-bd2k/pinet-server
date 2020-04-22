package edu.uc.eh.service;

/**
 * Created by behrouzsh on 11/7/16.
 */

import edu.uc.eh.utils.UtilsIO;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;


@Service
public class PCGService {

//    public PCGService() {
//        JSONObject geneCardInfo = UtilsIO.getInstance().readJsonFile("/PCG/pcgm.json");
//    }

    private static final Logger log = LoggerFactory.getLogger(PCGService.class);

    @Value("${resources.pcg}")
    String PCGTemplate;

    public ArrayList<Integer> checkGenes(String[] geneList)
    {
        ArrayList<Integer> genePositioins = new ArrayList<>();
        ArrayList geneFile = new ArrayList();
        ArrayList geneIndex = new ArrayList();
        int index;
        Integer indexForGene;

//        geneFile = UtilsIO.getInstance().readListFile("/PCG/genes-all.json");
        geneFile = UtilsIO.getInstance().readListFileConvertToLowerCase("/PCG/genes-all.json");
        geneIndex = UtilsIO.getInstance().readListFile("/PCG/genes-index.json");
        //geneFile = UtilsIO.getInstance().readJsonArrayFile("/PCG/uniprot-coding-genes.json");
        //log.info(geneIndex.toString());
        System.out.println(Arrays.toString(geneList));
        for (int i = 0; i < geneList.length; i++) {
            //log.info(geneIndex.toString());
            System.out.println(i);
            index = geneFile.indexOf(geneList[i].toLowerCase());

            System.out.println(index);
            if(index >= 0 ) {
                System.out.println(geneIndex.get(index));
                //Long.valueOf(dic[1]);
                indexForGene = Integer.valueOf((String) geneIndex.get(index));
                genePositioins.add(indexForGene);
            }
            else{
                genePositioins.add(-1);
            }

        }
    return genePositioins;
    }

    public JSONArray getTable(ArrayList<Integer> genePositions) {
        JSONArray geneCardInfo = new JSONArray();
        JSONArray PCGJson = new JSONArray();
        JSONObject notValid = new JSONObject();
        JSONArray empty = new JSONArray();
        notValid.put("gene_family",empty);
        notValid.put("hgnc_id","");
        notValid.put("name","Homo sapien counter gene not found.");
        notValid.put("symbol","");
        notValid.put("uniprot_ids",empty);
        notValid.put("prev_symbol",empty);
        notValid.put("locus_group",empty);


        try{
            //log.info("hereeeeee");
            JSONParser parser = new JSONParser();
            //log.info("hereeeeee1");
            geneCardInfo = UtilsIO.getInstance().readJsonArrayFile("/PCG/genes-info.json");

            for (int i = 0; i < genePositions.size(); i++)
            {
                if ((int)genePositions.get(i) >= 0)
                {

                    PCGJson.add(geneCardInfo.get(genePositions.get(i)));
                }
                else
                {
                    PCGJson.add(notValid);
                }
            }



        } catch (Exception e) {

            String msg =  String.format("Error in obtaining gene Information from PCG");
            log.warn(msg);
            throw new RuntimeException(msg);
        }


        return PCGJson;
        //return uniprotMap;//.toString();
    }

}

