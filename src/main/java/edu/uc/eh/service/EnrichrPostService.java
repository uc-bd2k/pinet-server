package edu.uc.eh.service;

/**
 * Created by shamsabz on 7/18/17.
 */

import org.python.util.PythonInterpreter;
import org.python.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;






@Service
public class EnrichrPostService {

    private static final Logger log = LoggerFactory.getLogger(EnrichrPostService.class);

    @Value("${urls.pir}")
    String pirTemplate;



    public String[] getTable(String[] genes) throws PyException
    {
        PythonInterpreter pi = new PythonInterpreter();

        pi.set("ENRICHR_URL", new PyString("http://maayanlab.cloud/Enrichr/addList/addList"));

        pi.set("genes_str" , new PyList());
        PyList genes_str = new PyList();
        for (int i = 0; i < genes.length; i++) {
            genes_str.add(genes[i] + '\n');
        }

//        PyJSONArray payload = {"list": (None, genes_str), "description": (None, "Example gene list")};
//        response = requests.post(pi.get("ENRICHR_URL"), Py.yfiles=payload);
//        genes_str.add()
//
//        pi.exec();
//
//        '\n'.pyJoin([
//                'PHF14', 'RBM3', 'MSL1', 'PHF21A', 'ARL10', 'INSR', 'JADE2', 'P2RX7',
//                'LINC00662', 'CCDC101', 'PPM1B', 'KANSL1L', 'CRYZL1', 'ANAPC16', 'TMCC1',
//                'CDH8', 'RBM11', 'CNPY2', 'HSPA1L', 'CUL2', 'PLBD2', 'LARP7', 'TECPR2',
//                'ZNF302', 'CUX1', 'MOB2', 'CYTH2', 'SEC22C', 'EIF4E3', 'ROBO2',
//                'ADAMTS9-AS2', 'CXXC1', 'LINC01314', 'ATF7', 'ATP5F1'
//]);
//        )=
//        description = 'Example gene list'
//        JSONArray payload = {'list': (None, ['PHF14']),
//        'description': (None, 'Example gene list')
//}
//
//        response = requests.post(ENRICHR_URL, files=payload)

        System.out.println(" here ===================== ");
        pi.set("integer", new PyInteger(42));
        pi.exec("square = integer*integer");
        PyInteger square = (PyInteger)pi.get("square");
        System.out.println("square: " + square.asInt());
        System.out.println("gene_str: " + genes_str);

        //JSONObject pirJsonSecond = new JSONObject(pirMap);
        //System.out.println("JsonFormat");
        //System.out.println(pirJsonSecond.toString());

        return (genes);
    }
}



