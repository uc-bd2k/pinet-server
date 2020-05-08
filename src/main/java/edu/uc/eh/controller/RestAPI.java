package edu.uc.eh.controller;

import com.opencsv.CSVReader;
import edu.uc.eh.uniprot.Uniprot;
import edu.uc.eh.uniprot.UniprotRepository;
//import edu.uc.eh.uniprot.UniprotRepositoryH2;
import edu.uc.eh.service.*;
import edu.uc.eh.structures.StringDoubleStringList;
import edu.uc.eh.utils.CsvToJson;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.apache.commons.math3.stat.inference.TestUtils.t;

//import org.apache.commons.math3.distribution.TDistribution;
//import org.apache.commons.math3.random.RandomGenerator;
//import org.apache.commons.math3.random.Well19937c;
//import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
//import org.apache.commons.math3.stat.inference.TTest;
//import org.springframework.mock.web.MockMultipartFile;

//import org.json.JSONObject;

/**
 * Created by chojnasm on 11/9/15.
 * Modified by Behrouz on 9/2/16.
 */


/**
 * This endpoint is to test slashes in values of parameters submitted to REST API
 *
 * @param
 * @return
 */

@Controller
public class RestAPI implements ErrorController {

    private static final Logger log = LoggerFactory.getLogger(RestAPI.class);
    //private static final Logger log2 = LoggerFactory.getLogger(UniprotService.class);
    private static String UPLOADED_FOLDER = "/Users/shamsabz/Documents/tmp/";

    private final PeptideSearchService peptideSearchService;
    private final PeptideWithValueService peptideWithValueService;
    private final PrositeService prositeService;
    private final PrositeService2 prositeService2;
    private final PsiModService psiModService;
    private final UniprotService uniprotService;
    private final UniprotService2 uniprotService2;
    private final EnrichrService enrichrService;
    private final IlincsService ilincsService;
    private final ShorthandService shorthandService;
    private final PCGService pcgService;
    private final KinaseService kinaseService;
    private final PhosphoServiceV2 phosphoServiceV2;
    private final PhosphoService phosphoService;
    private final PtmService ptmService;
    private final PirService pirService;
    private final EnrichrServiceV2 enrichrServiceV2;
    private final IteratorIncrementService iteratorIncrementService;
    private final NetworkFromCSVService networkFromCSV;
    private final PsiModExtensionService psiModExtensionService;
    //    private final UniprotRepositoryH2 proteinRepositoryH2;
    private final UniprotRepository uniprotRepository;
    private final PrideService prideService;
    private final FastaService fastaService;
    private final PeptideRegexServive peptideRegexServive;
    private final DeepPhosService deepPhosService;

//    private final HarmonizomeProteinService harmonizomeProteinService;
//    private final HarmonizomeGeneService harmonizomeGeneService;

//    @Value("${resources.pathway}")
//    String pathWay;


    @Autowired
    //public RestAPI(HarmonizomeGeneService harmonizomeGeneService, HarmonizomeProteinService harmonizomeProteinService, PrositeService prositeService, PsiModService psiModService, UniprotService uniprotService, EnrichrService enrichrService, PCGService pcgService, KinaseService kinaseService, ShorthandService shorthandService, PhosphoService phosphoService, HarmonizomeGeneService harmonizomeGeneServics1) {
    public RestAPI(ErrorAttributes errorAttributes, PeptideSearchService peptideSearchService, PeptideWithValueService peptideWithValueService, PrositeService prositeService, PrositeService2 prositeService2, PsiModService psiModService, UniprotService uniprotService, UniprotService2 uniprotService2, EnrichrService enrichrService, IlincsService ilincsService, PCGService pcgService, KinaseService kinaseService, ShorthandService shorthandService, PhosphoServiceV2 phosphoServiceV2, PhosphoService phosphoService, PirService pirService, EnrichrServiceV2 enrichrServiceV2, IteratorIncrementService iteratorIncrementService, NetworkFromCSVService networkFromCSV, PsiModExtensionService psiModExtensionService, PtmService ptmService, UniprotRepository uniprotRepository, PrideService prideService, FastaService fastaService, PeptideRegexServive peptideRegexServive, DeepPhosService deepPhosService) {
        this.peptideSearchService = peptideSearchService;
        this.peptideWithValueService = peptideWithValueService;

        this.prositeService = prositeService;
        this.prositeService2 = prositeService2;
        this.psiModService = psiModService;
        this.uniprotService = uniprotService;
        this.uniprotService2 = uniprotService2;
        this.enrichrService = enrichrService;
        this.ilincsService = ilincsService;
        this.shorthandService = shorthandService;
        this.pcgService = pcgService;
        this.kinaseService = kinaseService;
        this.phosphoServiceV2 = phosphoServiceV2;
        this.phosphoService = phosphoService;
        this.ptmService = ptmService;
        //this.harmonizomeGeneService = harmonizomeGeneService;
        //this.harmonizomeProteinService = harmonizomeProteinService;
        this.pirService = pirService;
        this.enrichrServiceV2 = enrichrServiceV2;

        this.iteratorIncrementService = iteratorIncrementService;
        this.networkFromCSV = networkFromCSV;
        this.psiModExtensionService = psiModExtensionService;
        this.errorAttributes = errorAttributes;
        //  this.proteinRepositoryH2 = proteinRepositoryH2;
        this.uniprotRepository = uniprotRepository;
        this.prideService = prideService;
        this.fastaService = fastaService;
        this.peptideRegexServive = peptideRegexServive;
        this.deepPhosService = deepPhosService;

    }

    //This part is for error handling, going to home page if there was an error in the address
    private ErrorAttributes errorAttributes;

    private final static String ERROR_PATH = "/error";

    /**
     * Controller for the Error Controller
     * @param errorAttributes
     */


    /**
     * Supports the HTML Error View
     *
     * @param request
     * @return
     */
    @RequestMapping(value = ERROR_PATH, produces = "text/html")
    public ModelAndView errorHtml(HttpServletRequest request) {
        return new ModelAndView("/", getErrorAttributes(request, false));
    }

    /**
     * Supports other formats like JSON, XML
     *
     * @param request
     * @return
     */
    @RequestMapping(value = ERROR_PATH)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        Map<String, Object> body = getErrorAttributes(request, getTraceParameter(request));
        HttpStatus status = getStatus(request);
        return new ResponseEntity<Map<String, Object>>(body, status);
    }

    /**
     * Returns the path of the error page.
     *
     * @return the error path
     */
    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }


    private boolean getTraceParameter(HttpServletRequest request) {
        String parameter = request.getParameter("trace");
        if (parameter == null) {
            return false;
        }
        return !"false".equals(parameter.toLowerCase());
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request,
                                                   boolean includeStackTrace) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        return this.errorAttributes.getErrorAttributes(requestAttributes,
                includeStackTrace);
    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request
                .getAttribute("javax.servlet.error.status_code");
        if (statusCode != null) {
            try {
                return HttpStatus.valueOf(statusCode);
            } catch (Exception ex) {
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    // End of error handling.............


//
//    /**
//     * POST /uploadFile -> receive and reads a file.
//     *
//     * @param uploadfile The uploaded file as Multipart file parameter in the
//     * HTTP request. The RequestParam name must be the same of the attribute
//     * "name" in the input tag with type file.
//     *
//     * @return An http OK status in case of success, an http 4xx status in case
//     * of errors.
//     */


//
//    @RequestMapping("/NewTimesheet")
//    public class MyClassName {
//
//        @RequestMapping(value={ "", "/" }, method = RequestMethod.POST, headers = { "Content-type=application/json" })
//        @ResponseBody
//        public String addNewTimesheet(@RequestBody List<Timesheet> timesheet,HttpSession session){
//            logger.info("timesheet list size is"+timesheet.size());
//            return "success";
//        }
//    }
//    @RequestMapping(value = "api/prosite/{peptide}", method = RequestMethod.GET)
//    public
//    @ResponseBody
//    String getFromProsite(@PathVariable String peptide) {
//        //log.info(String.format("Run convertToPLN with argument: %s", peptide));
//
//        return prositeService.getTable(peptide);
//    }


//    @RequestMapping(value="api/upload", method=RequestMethod.POST)
//    public String handleFileUpload(@RequestParam("name") String name,
//                                   @RequestParam("file") MultipartFile file){
//        if (!file.isEmpty()) {
//            System.out.println("in /api/upload");
//            try {
//                byte[] bytes = file.getBytes();
//                BufferedOutputStream stream =
//                        new BufferedOutputStream(new FileOutputStream(new File(name)));
//                stream.write(bytes);
//                stream.close();
//                return "You successfully uploaded " + name + "!";
//            } catch (Exception e) {
//                return "You failed to upload " + name + " => " + e.getMessage();
//            }
//        } else {
//            return "You failed to upload " + name + " because the file was empty.";
//        }
//    }

    @RequestMapping(value = "api/uploadCSV", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject handleFileUpload(@RequestParam("file") MultipartFile file) {
        JSONArray outputError = new JSONArray();
        JSONObject outputErrorItem = new JSONObject();
        JSONObject net = new JSONObject();
        JSONObject output = new JSONObject();
        output.put("tag", 200);
        outputErrorItem.put("network", "");
//            outputError.put("peptidesParsed","");

        outputErrorItem.put("tag", 400);
        if (!file.isEmpty()) {
            String fileName = file.getOriginalFilename();

            if (fileName.endsWith(".csv")) {
                try {

                    net = networkFromCSV.computeNetworkFromInputJson(CsvToJson.convert(convertMultipartToFile(file)));
                    System.out.print(net);
                    output.put("network", net);
                    return net;

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    outputErrorItem.put("message", "Error: " + e);

                    return outputErrorItem;
                } catch (IOException e) {
                    e.printStackTrace();
                    outputErrorItem.put("message", "Error: " + e);

                    return outputErrorItem;
                } catch (Exception e) {
                    e.printStackTrace();

                    return outputErrorItem;
                }
            } else {
                outputErrorItem.put("message", "Try uploading a CSV file");


                return outputErrorItem;
            }
        } else {
            outputErrorItem.put("message", "File is Empty");

            return outputErrorItem;

        }
    }


    @RequestMapping(value = "api/upload", method = RequestMethod.POST)
    @ResponseBody
//    public JSONObject handleFileUpload2(@RequestParam("organism") String organism,

//                                        @RequestParam("file") MultipartFile file){
    public JSONObject handleFileUpload2(@RequestParam("organism") String organism,
                                        @RequestParam("inputType") String inputType,
                                        @RequestParam("inputCtrl") String[] inputCtrl,
                                        @RequestParam("inputTrt") String[] inputTrt,
                                        @RequestParam("normalFlag") String normalFlag,
                                        @RequestParam("quantileFlag") String quantileFlag,
                                        @RequestParam("discardFlag") String discardFlag,
                                        @RequestParam("imputeFlag") String imputeFlag,
//                                        @RequestParam("peptideModFormat") String peptideModFormat,
                                        @RequestParam("file") MultipartFile file) {
//        public ResponseEntity<?> handleFileUpload(@RequestParam("name") String name,
//                @RequestParam("file") MultipartFile file){
        System.out.println("------------inside upload file-----------");
        System.out.println(organism);
        System.out.println(inputType);
        System.out.println(normalFlag);
        System.out.println(quantileFlag);
        System.out.println(discardFlag);
        System.out.println(imputeFlag);
//        System.out.println(peptideModFormat);

        System.out.println(Arrays.toString(inputTrt));
        for (int i = 0; i < inputTrt.length; i++) {
            System.out.println(inputTrt[i]);

        }
        System.out.println(Arrays.toString(inputCtrl));
        for (int i = 0; i < inputCtrl.length; i++) {
            System.out.println(inputCtrl[i]);

        }
        List<String> inputCtrlArray = Arrays.asList(inputCtrl);
        List<String> inputTrtArray = Arrays.asList(inputTrt);

//        System.out.println(inputCtrlArray);
//        System.out.println(inputTrtArray);

        JSONObject normalSumJson = new JSONObject();
        for (int i = 0; i < inputCtrlArray.size(); i++) {
            normalSumJson.put(inputCtrlArray.get(i), Long.valueOf(0));
        }
        for (int i = 0; i < inputTrtArray.size(); i++) {
            normalSumJson.put(inputTrtArray.get(i), Long.valueOf(0));
        }

        //System.out.println(normalSumJson.toJSONString());
        Long a = Long.valueOf(12500);
//        System.out.println(a / 10);
//        System.out.println(a / 100);
//        System.out.println(a / 500);
//        System.out.println(a / 1000);
//
//        System.out.println(a / 10000);


        if (!file.isEmpty()) {
            System.out.println("in /api/upload");
            System.out.println("Reading file");
            System.out.println(organism);
//            System.out.println(inputType);
//            System.out.println(inputCtrl);
//            System.out.println(inputTrt);
            JSONArray inputArray = new JSONArray();
            JSONArray volcanoArray = new JSONArray();
//            JSONArray peptidesParsed = new JSONArray();
            ArrayList keys = new ArrayList();
            ArrayList<String> peptides = new ArrayList<String>();
            ArrayList<String> motifs = new ArrayList<String>();
            ArrayList<String> discardedArray = new ArrayList<String>();
            //JSONObject error1 = new JSONObject();
            JSONObject output = new JSONObject();
            Boolean useTrembl = false;

            JSONObject parsedPeptide = new JSONObject();
            //JSONArray errorArray1 = new JSONArray();
            String peptide;
            JSONObject groupsJson = new JSONObject();
            ArrayList<String> groupsArray = new ArrayList<String>();
            String motif;
            String firstGroup, secondGroup;
            Double pv, fc;
            String message;
            String fileName = file.getOriginalFilename();
            JSONObject peptideDictionary = new JSONObject();
            JSONObject peptideToRazorDictionary = new JSONObject();


            JSONObject outputError = new JSONObject();
            outputError.put("dataForAllPeptides", "");
            outputError.put("inputArray", "");
            outputError.put("volcanoArray", "");
            outputError.put("localMotifs", "");
            outputError.put("localPeptides", "");
//            outputError.put("peptidesParsed","");
            outputError.put("tag", 400);


//            if (fileName.endsWith(".csv")) {
//                try {
//
//                    // Create an object of filereader
//                    // class with CSV file as a parameter.
//                    FileReader filereader = new FileReader( convertMultipartToFile(file));
//                    XSSFWorkbook workBook = new XSSFWorkbook();
//                    XSSFSheet sheet = workBook.createSheet("sheet1");
//                    String currentLine=null;
//                    int RowNum=0;
//                    BufferedReader br = new BufferedReader(new FileReader(convertMultipartToFile(file)));
//                    while ((currentLine = br.readLine()) != null) {
//                        String str[] = currentLine.split(",");
//                        RowNum++;
//                        XSSFRow currentRow=sheet.createRow(RowNum);
//                        for(int i=0;i<str.length;i++){
//                            currentRow.createCell(i).setCellValue(str[i]);
//                        }
//                    }
//
//
//                    Iterator<org.apache.poi.ss.usermodel.Cell> cellIterator = row.cellIterator();
//
//
//                    FileOutputStream fileOutputStream =  new FileOutputStream(xlsxFileAddress);
//                    workBook.write(fileOutputStream);
//                    fileOutputStream.close();
//
//
//
//
//
//
//
//
//
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                    outputError.put("message", "Error: " + e);
//                    return outputError;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    outputError.put("message", "Error: " + e);
//                    return outputError;
//                }
//            }
//            else
            if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx") || fileName.endsWith(".csv") || fileName.endsWith(".tsv") || fileName.endsWith(".txt")) {
                try {


                    int rowIter;
                    int colIter;

                    Boolean first_line = true;

                    SXSSFWorkbook workbook = new SXSSFWorkbook();
                    if (fileName.endsWith(".csv") || inputType.equals("generic")) {


                        org.apache.poi.ss.usermodel.Sheet csvsheet = workbook.createSheet("sheet1");
                        String currentLine = null;
                        int RowNum = -1;
                        BufferedReader br = new BufferedReader(new FileReader(convertMultipartToFile(file)));
                        colIter = 0;
                        while ((currentLine = br.readLine()) != null) {
                            String str[] = currentLine.split(",");
                            RowNum++;
                            if (RowNum > 20000) {
                                outputError.put("message", "Error: please contact pinet support for pinet-stand-alone package to analyze larger files..");
                                return outputError;

                            }


                            Row currentRow = csvsheet.createRow(RowNum);


                            if (first_line) {

                                //System.out.println("first line true");
                                first_line = false;
                                colIter = 0;
                                for (int i = 0; i < str.length; i++) {
                                    keys.add(str[i]);
                                    colIter += 1;
                                }


                                //System.out.println(keys);
                                if (colIter > 2 && colIter < 5) {

                                    outputError.put("message", "Error: Data file column size error, at least one the groups has less than two samples.");
                                    return outputError;
                                }
                                if (colIter > 4) {
                                    int g1 = 0;
                                    int g2 = 0;
                                    JSONObject groups = new JSONObject();
                                    for (int colIterator = 1; colIterator < keys.size(); colIterator++) {
                                        String splitted = ((String) keys.get(colIterator)).split("_")[0];

                                        if (!groupsJson.containsKey(keys.get(colIterator))) {
                                            groupsJson.put(keys.get(colIterator), splitted);
                                            if (!groupsArray.contains(splitted)) {
                                                groupsArray.add(splitted);
                                            }

                                        } else {

                                            outputError.put("message", "Error: Duplicate column names. Please see the example for formatting");
                                            return outputError;
                                        }
                                    }
                                    if (groupsArray.size() != 2) {

                                        outputError.put("message", String.format("Error: Number of groups is %d which should be 2.", groupsArray.size()));
                                        return outputError;
                                    } else {

                                        firstGroup = groupsArray.get(0);
                                        secondGroup = groupsArray.get(1);
                                        for (Object key : groupsJson.keySet()) {
                                            String keyStr = (String) key;
                                            String val = (String) groupsJson.get(keyStr);
                                            if (val.equals(firstGroup)) {
                                                g1 += 1;
                                            }
                                            if (val.equals(secondGroup)) {
                                                g2 += 1;
                                            }

                                        }
                                        if (g1 < 2) {
                                            outputError.put("message", String.format("Error: Number of group %s is less than 2.", firstGroup));
                                            return outputError;
                                        }
                                        if (g2 < 2) {
                                            outputError.put("message", String.format("Error: Number of group %s is less than 2.", secondGroup));
                                            return outputError;
                                        }

                                    }

                                }

                            } else { //Not first line for csv

                                colIter = 0;
                                //RowNum += 1;
                                firstGroup = groupsArray.get(0);
                                secondGroup = groupsArray.get(1);
                                //System.out.println("\n" + RowNum + "------------");

                                JSONObject responseJSON = new JSONObject();
                                JSONObject volcanoJSON = new JSONObject();
                                peptide = "";

                                ArrayList list1 = new ArrayList<Double>();
                                ArrayList list2 = new ArrayList<Double>();
                                ArrayList pvAndFc = new ArrayList<Double>(2);
                                Boolean lis1Flag = false;
                                Boolean lis2Flag = false;
                                Boolean eachRowError = false;
                                JSONArray ctrlList = new JSONArray();
                                JSONArray trtList = new JSONArray();


                                for (colIter = 0; colIter < str.length; colIter++) {
                                    currentRow.createCell(colIter)
                                            .setCellValue(str[colIter]);
                                    //System.out.print(str[colIter] + "    ");


                                    if (colIter == 0) {

                                        peptide = str[colIter];
                                        if (!peptides.contains(peptide)) {
                                            peptides.add(peptide);
                                            parsedPeptide = getMotifAndModificationFromPeptide(peptide);

                                            motif = (String) parsedPeptide.get("motif");
                                            motifs.add(motif);

                                        } else {

                                            outputError.put("message", String.format("Error: Duplicate peptide %s in list.", peptide));
                                            return outputError;
                                            //                                    error1.put("error",String.format("Duplicate peptide %s in list.",peptide));
                                            //                                    //errorArray1.add(error1);
                                            //                                    return error1;
                                        }
                                        responseJSON.put("Peptide", peptide);
                                        responseJSON.put("sequence", motif);
                                        responseJSON.put("modification", parsedPeptide.get("modifications"));


                                    } else {


                                        //System.out.println(keys.get(colIter)+ "  "+cellValue );
                                        //                            System.out.print(cellValue + "++++++");

                                        try {
                                            Double cellValueDouble = Double.parseDouble(str[colIter]);

                                            if (groupsJson.get(keys.get(colIter)).equals(firstGroup)) {
                                                list1.add(cellValueDouble);
                                                JSONObject ctrlListItem = new JSONObject();
                                                ctrlListItem.put(keys.get(colIter), cellValueDouble);
                                                ctrlList.add(ctrlListItem);
                                                if (cellValueDouble != 0.0) lis1Flag = true;

                                            } else {
                                                list2.add(cellValueDouble);
                                                JSONObject trtListItem = new JSONObject();
                                                trtListItem.put(keys.get(colIter), cellValueDouble);
                                                trtList.add(trtListItem);
                                                if (cellValueDouble != 0.0) lis2Flag = true;
                                            }
                                            responseJSON.put(keys.get(colIter), cellValueDouble);

                                            if (cellValueDouble.isNaN()) {
                                                eachRowError = true;
                                                //System.out.println(eachRowError.toString() + "--------------");
                                            }

                                        } catch (Exception e) {
                                            outputError.put("message", String.format("Error: Value %s is not double.", str[colIter]));
                                            //return outputError;
                                            eachRowError = true;
                                            //System.out.println(eachRowError.toString() + ".........");
                                        }


                                    }


                                }

                                responseJSON.put("group1", ctrlList);
                                responseJSON.put("group2", trtList);
//                        System.out.println(list1);
                                System.out.println(eachRowError);
                                if (!eachRowError) {
                                    if (lis1Flag && lis2Flag) {

                                        if (list1.size() > 0 && list2.size() > 0) {
                                            pvAndFc = computePValueAndFoldChange(list1, list2);
                                            //System.out.println(pvAndFc);
                                            pv = (Double) pvAndFc.get(0);
                                            fc = (Double) pvAndFc.get(1);

                                            responseJSON.put("pv", pv);
                                            responseJSON.put("fc", fc);

                                            volcanoJSON.put("Peptide", peptide);
                                            volcanoJSON.put("p_value", pv);
                                            volcanoJSON.put("log2(fold_change)", fc);

                                            inputArray.add(responseJSON);
                                            volcanoArray.add(volcanoJSON);
                                        }

                                    } else {

                                        if (list1.size() > 0 && list2.size() > 0) {
                                            pvAndFc = computePValueAndFoldChange(list1, list2);
                                            //System.out.println(pvAndFc);

                                            pv = (Double) pvAndFc.get(0);
                                            fc = (Double) pvAndFc.get(1);
                                            if (fc.isInfinite()) {
                                                fc = Math.signum(fc) * 10.0;
                                            }

                                            responseJSON.put("pv", pv);
                                            responseJSON.put("fc", fc);

                                            volcanoJSON.put("Peptide", peptide);
                                            volcanoJSON.put("p_value", pv);
                                            volcanoJSON.put("log2(fold_change)", fc);

                                            inputArray.add(responseJSON);
                                            volcanoArray.add(volcanoJSON);


                                        }


                                    }
                                    if (!lis1Flag && !lis2Flag) {
                                        // Since one of the groups are zero
                                        //message += String.format("Error: control and treatment lists for peptide %s are all zeros. Please delete the row and submit again.",peptide);
                                        outputError.put("message", String.format("Error: control and treatment lists for peptide %s are all zeros. Please delete the row and submit again.", peptide));
                                        //return outputError;
                                    }


                                } else {

                                }
//                            System.out.println(fc);
//                            System.out.println("-------------");
//                            System.out.println("-------------");
//                            System.out.println("-------------");
//                            pv = 0.0000001;
//                            fc = 3.0 + 3.0 * Math.random();
                                // Since one of the groups are zero
//                            outputError.put("message",String.format("Error: list of control or treatment for peptide %s is all zeros. Please delete the row and submit again.",peptide));
//                            return outputError;


                            }


                        }


//
//                        FileReader filereader = new FileReader(convertMultipartToFile(file));
//                        InputStream inputFS = new FileInputStream(convertMultipartToFile(file));
//                        workbook = convertCsvToXlsx(file);
                    }
                    if (fileName.endsWith(".txt") || fileName.endsWith(".tsv") || inputType.equals("maxQuant")) {


                        org.apache.poi.ss.usermodel.Sheet csvsheet = workbook.createSheet("sheet1");
                        String currentLine = null;
                        JSONObject sheetDic = new JSONObject();
                        int RowNum = -1;

                        BufferedReader br = new BufferedReader(new FileReader(convertMultipartToFile(file)));
                        colIter = 0;
                        while ((currentLine = br.readLine()) != null) {
                            String str[] = currentLine.split("\t");

//                            for (int i = 0; i < str.length; i++) {
//                                System.out.print(str[i] + "  ,,  ");
//
//                            }
                            RowNum++;
                            //System.out.println(RowNum);
                            //                            if (RowNum > 4000) {
//                                outputError.put("message", "Error: please contact pinet support for pinet-stand-alone package to analyze larger files..");
//                                return outputError;
//
//                            }


                            Row currentRow = csvsheet.createRow(RowNum);


                            if (first_line) {

                                //System.out.println("first line true");
                                first_line = false;
                                colIter = 0;
                                for (int i = 0; i < str.length; i++) {
                                    //keys.add(str[i]);
                                    if (!str[i].toLowerCase().contains("modified") && str[i].toLowerCase().contains("sequence")) {
                                        sheetDic.put("sequence", i);
                                    }
                                    if (str[i].toLowerCase().contains("modified") && str[i].toLowerCase().contains("sequence")) {
                                        sheetDic.put("modifiedSequence", i);
                                    }
                                    if (str[i].toLowerCase().contains("protein") && str[i].toLowerCase().contains("leading") && !str[i].toLowerCase().contains("razor")) {
                                        sheetDic.put("leadingProtein", i);
                                    }
                                    if (str[i].toLowerCase().contains("proteins") && !str[i].toLowerCase().contains("leading") && !str[i].toLowerCase().contains("razor") && !str[i].toLowerCase().contains("name")) {
                                        sheetDic.put("proteins", i);
                                    }
                                    if (str[i].toLowerCase().contains("protein") && str[i].toLowerCase().contains("leading") && str[i].toLowerCase().contains("razor")) {
                                        sheetDic.put("leadingRazorprotein", i);
                                    }

                                    if (str[i].toLowerCase().contains("raw") && str[i].toLowerCase().contains("file")) {
                                        sheetDic.put("rawFile", i);
                                    }

                                    if (str[i].toLowerCase().equals("intensity")) {
                                        sheetDic.put("intensity", i);
                                    }

                                    //colIter += 1;
                                }
                                if (!sheetDic.containsKey("intensity")) {
                                    outputError.put("message", "Error: Intensity column not found.");
                                    return outputError;

                                }
                                if (!sheetDic.containsKey("rawFile")) {
                                    outputError.put("message", "Error: Raw File column not found.");
                                    return outputError;

                                }


                                //System.out.println(sheetDic.toJSONString());
//                                if (colIter > 2 && colIter < 5) {
//
//                                    outputError.put("message", "Error: Data file column size error, at least one the groups has less than two samples.");
//                                    return outputError;
//                                }
//                                if (colIter > 4) {
//                                    int g1 = 0;
//                                    int g2 = 0;
//                                    JSONObject groups = new JSONObject();
//                                    for (int colIterator = 1; colIterator < keys.size(); colIterator++) {
//                                        String splitted = ((String) keys.get(colIterator)).split("_")[0];
//
//                                        if (!groupsJson.containsKey(keys.get(colIterator))) {
//                                            groupsJson.put(keys.get(colIterator), splitted);
//                                            if (!groupsArray.contains(splitted)) {
//                                                groupsArray.add(splitted);
//                                            }
//
//                                        } else {
//
//                                            outputError.put("message", "Error: Duplicate column names. Please see the example for formatting");
//                                            return outputError;
//                                        }
//                                    }
//                                    if (groupsArray.size() != 2) {
//
//                                        outputError.put("message", String.format("Error: Number of groups is %d which should be 2.", groupsArray.size()));
//                                        return outputError;
//                                    } else {
//
//                                        firstGroup = groupsArray.get(0);
//                                        secondGroup = groupsArray.get(1);
//                                        for (Object key : groupsJson.keySet()) {
//                                            String keyStr = (String) key;
//                                            String val = (String) groupsJson.get(keyStr);
//                                            if (val.equals(firstGroup)) {
//                                                g1 += 1;
//                                            }
//                                            if (val.equals(secondGroup)) {
//                                                g2 += 1;
//                                            }
//
//                                        }
//                                        if (g1 < 2) {
//                                            outputError.put("message", String.format("Error: Number of group %s is less than 2.", firstGroup));
//                                            return outputError;
//                                        }
//                                        if (g2 < 2) {
//                                            outputError.put("message", String.format("Error: Number of group %s is less than 2.", secondGroup));
//                                            return outputError;
//                                        }
//
//                                    }
//
//                                }

                            } else { //Not first line for csv

                                //colIter = 0;
                                //RowNum += 1;
//                                firstGroup = groupsArray.get(0);
//                                secondGroup = groupsArray.get(1);
//                                try {

                                if (isNumeric(str[(int) sheetDic.get("intensity")])) {
                                    //Double.valueOf("9.78313E+2").longValue()
                                    Long intensity = Long.valueOf(Double.valueOf(str[(int) sheetDic.get("intensity")]).longValue());
                                    //System.out.println(intensity);

//                                    System.out.println("\n" + RowNum + "------------");
//                                    System.out.println(str[(int) sheetDic.get("proteins")]);


                                    String proteins = "";
                                    if (sheetDic.containsKey("proteins")) {
                                        proteins = str[(int) sheetDic.get("proteins")];
                                    }


                                    String leadingProtein = "";
                                    if (sheetDic.containsKey("leadingProtein")) {
                                        leadingProtein = str[(int) sheetDic.get("leadingProtein")];
                                    }


                                    String leadingRazorprotein = "";
                                    if (sheetDic.containsKey("leadingRazorprotein")) {
                                        leadingRazorprotein = str[(int) sheetDic.get("leadingRazorprotein")];
                                    }

                                    String modifiedSequence = "";
                                    if (sheetDic.containsKey("modifiedSequence")) {
                                        modifiedSequence = str[(int) sheetDic.get("modifiedSequence")];
                                    }


                                    String sequensce = str[(int) sheetDic.get("sequence")];

                                    String rawFile = str[(int) sheetDic.get("rawFile")];


                                    String peptideDictionaryKey = "";
                                    if (!modifiedSequence.equals("")) {
                                        peptideDictionaryKey = modifiedSequence;
                                    } else {
                                        peptideDictionaryKey = sequensce;
                                    }

//                                    System.out.println(sequensce);
//                                    System.out.println(modifiedSequence);
//                                    for (int i = 0; i < proteins.length; i++) {
//                                        System.out.print(proteins[i] + "  --  ");
//
//                                    }
//                                    System.out.println(modifiedSequence);
//                                    System.out.println(sequensce);
//                                    System.out.println(proteins);
//                                    System.out.println(leadingProtein);
//                                    System.out.println(leadingRazorprotein);
//                                    System.out.println(rawFile);
//                                    System.out.println(intensity);
                                    //peptideToRazorDictionary.put(sequensce, leadingRazorprotein);

                                    if (!peptideDictionary.containsKey(peptideDictionaryKey)) {
                                        JSONObject peptideDictionaryItem = new JSONObject();
                                        peptideDictionaryItem.put("ctrl", new JSONArray());
                                        peptideDictionaryItem.put("trt", new JSONArray());
                                        peptideDictionaryItem.put("leadingRazorprotein", leadingRazorprotein);
                                        peptideDictionaryItem.put("proteins", proteins);
                                        peptideDictionaryItem.put("leadingProtein", leadingProtein);
                                        peptideDictionaryItem.put("sequence", sequensce);


                                        peptideDictionary.put(peptideDictionaryKey, peptideDictionaryItem);

                                    }

                                    if (inputTrtArray.contains(rawFile)) {

                                        JSONObject peptideDictionaryKeyItem = new JSONObject();
                                        peptideDictionaryKeyItem.put(rawFile, intensity);
                                        ((JSONArray) ((JSONObject) peptideDictionary.get(peptideDictionaryKey)).get("trt")).add(peptideDictionaryKeyItem);
                                        if (normalFlag.equals("yes")) {

                                            Long value = (Long) normalSumJson.get(rawFile);
                                            normalSumJson.put(rawFile, value + intensity);

                                        }

                                    }
                                    if (inputCtrlArray.contains(rawFile)) {

                                        JSONObject peptideDictionaryKeyItem = new JSONObject();
                                        peptideDictionaryKeyItem.put(rawFile, intensity);
                                        ((JSONArray) ((JSONObject) peptideDictionary.get(peptideDictionaryKey)).get("ctrl")).add(peptideDictionaryKeyItem);


                                        if (normalFlag.equals("yes")) {

                                            Long value = (Long) normalSumJson.get(rawFile);
                                            normalSumJson.put(rawFile, value + intensity);

                                        }
                                    }
//                                }
//                                catch (Exception e){
//                                    System.out.println("There was an error in row ");
//
                                } else {
                                    //System.out.println(" non numeric ------------");

                                    //System.out.println(str[(int) sheetDic.get("modifiedSequence")]);
                                    //System.out.println(str[(int) sheetDic.get("intensity")]);
                                    //System.out.println(isNumeric(str[(int) sheetDic.get("intensity")]));
                                    //System.out.println("------------");
                                }
                            }
                        }
                        //System.out.println("After first loop");
                        //System.out.println("After first loop");
                        System.out.println("After first loop");
                        int discarded = 0;
                        JSONObject peptideDictionaryRefined = new JSONObject();
                        JSONObject peptideDictionaryModified = new JSONObject();

                        for (Object key : peptideDictionary.keySet()) {
                            //try {
                            //based on you key types
                            String keySter = (String) key;
                            Object keyvalue = peptideDictionary.get(keySter);

                            JSONArray trtArray = (JSONArray) ((JSONObject) peptideDictionary.get(keySter)).get("trt");


                            JSONArray ctrlArray = (JSONArray) ((JSONObject) peptideDictionary.get(keySter)).get("ctrl");


                            if (trtArray.size() > 0 && ctrlArray.size() > 0) {

                                if ((discardFlag.equals("yes") && (trtArray.size() > (Double) (inputTrtArray.size() / 2.0) && ctrlArray.size() > (Double) (inputCtrlArray.size() / 2.0))) || discardFlag.equals("no")) {


                                    peptideDictionaryRefined.put(keySter, keyvalue);



                                }//End of check for the length of treatment and ctrl
                                else {
//                                    System.out.println((imputeFlag.equals("yes")));
//                                    System.out.println((imputeFlag.equals("no")));
//                                    System.out.println((imputeFlag.equals("yes") && (trtArray.size() > (Double) (inputTrtArray.size() / 2.0) && ctrlArray.size() > (Double) (inputCtrlArray.size() / 2.0))));
//                                    System.out.println(( ctrlArray.size() > (Double) (inputCtrlArray.size() / 2.0))) ;
//                                    System.out.println(( trtArray.size() > (Double) (inputTrtArray.size() / 2.0) ));
//
//                                    System.out.println((imputeFlag.equals("yes") && (trtArray.size() > (Double) (inputTrtArray.size() / 2.0) && ctrlArray.size() > (Double) (inputCtrlArray.size() / 2.0))) || imputeFlag.equals("no"));

                                        discarded += 1;
                                    discardedArray.add((String) key);
//                                    System.out.println("here1");
//                                    System.out.println("notvalid " + discarded + "-----------");
//                                    System.out.println((String) key);
//                                    System.out.println(trtArray);
//                                    System.out.println(ctrlArray);
                                    //System.out.println("trtsize: " + trtArray.size() + "  inputtrtsize/2: " + (Double) (inputTrtArray.size() / 2.0) + " ctrlsize: " + ctrlArray.size() + "  inputctrl/2: " + (Double) (inputCtrlArray.size() / 2.0));
                                    //System.out.println("================================================");

                                }
                            } else {
                                //System.out.println((trtArray.size() > 1 && ctrlArray.size() > 1));
                                discarded += 1;
                                discardedArray.add((String) key);
//                                System.out.println("here2");
                                //System.out.println("notvalid " + discarded + "-----------");
//                                System.out.println((String) key);
//                                System.out.println(trtArray);
//                                System.out.println(ctrlArray);
                                //System.out.println("trtsize: " + trtArray.size() + "  inputtrtsize/2: " + (Double) (inputTrtArray.size() / 2.0) + " ctrlsize: " + ctrlArray.size() + "  inputctrl/2: " + (Double) (inputCtrlArray.size() / 2.0));
                                //System.out.println("================================================");
                            }
                        }
                        //System.out.println("before normalizeAndImputeAndPValAndFc");
                        //System.out.println("input is: " + peptideDictionaryRefined);
                        peptideDictionaryModified = normalizeAndImputeAndPValAndFc(peptideDictionaryRefined, normalSumJson, normalFlag, quantileFlag, imputeFlag, inputCtrlArray.size(), inputTrtArray.size());


                        for (Object key : peptideDictionaryModified.keySet()) {
                            //try {
                            //based on you key types
                            String keySter = (String) key;
                            Object keyvalue = peptideDictionary.get(keySter);

                            JSONArray trtArray = (JSONArray) ((JSONObject) peptideDictionary.get(keySter)).get("trt");


                            JSONArray ctrlArray = (JSONArray) ((JSONObject) peptideDictionary.get(keySter)).get("ctrl");

//
//
                            pv = (Double) ((JSONObject) peptideDictionary.get(keySter)).get("pv");

                            fc = (Double) ((JSONObject) peptideDictionary.get(keySter)).get("fc");

                            //System.out.println("key: " + keySter + " value: " + keyvalue);
                            JSONObject responseJSON = new JSONObject();
                            JSONObject volcanoJSON = new JSONObject();
                            peptide = "";

//                            ArrayList list1 = new ArrayList<Double>();
//                            ArrayList list2 = new ArrayList<Double>();
//                            ArrayList pvAndFc = new ArrayList<Double>(2);
//                            Boolean lis1Flag = false;
//                            Boolean lis2Flag = false;
//                            Boolean eachRowError = false;

//                                if (peptideModFormat.equals("modBefore")) {
//                                    parsedPeptide = getMotifAndModificationFromPeptideMaxQuantBefore(keySter);
//                                } else {
                            parsedPeptide = getMotifAndModificationFromPeptideMaxQuantBeforeModifiedToGetRidOfInitialModification(keySter);
                            //}

                            peptide = (String) parsedPeptide.get("peptide");

                            motif = (String) parsedPeptide.get("motif");


                            String leadingRazorprotein = (String)((JSONObject) peptideDictionary.get(keySter)).get("leadingRazorprotein");

                            peptideToRazorDictionary.put(motif, leadingRazorprotein);



                            peptides.add(peptide);
                            motifs.add(motif);

                            responseJSON.put("Peptide", peptide);
                            responseJSON.put("sequence", motif);
                            responseJSON.put("modification", parsedPeptide.get("modifications"));

                            //System.out.println(parsedPeptide);


                            responseJSON.put("group1", ctrlArray);
                            responseJSON.put("group2", trtArray);

                            responseJSON.put("pv", pv);
                            responseJSON.put("fc", fc);

                            volcanoJSON.put("Peptide", peptide);
                            volcanoJSON.put("p_value", pv);
                            volcanoJSON.put("log2(fold_change)", fc);

                            inputArray.add(responseJSON);
                            volcanoArray.add(volcanoJSON);

                            //System.out.println("loop over control");
//                            for (colIter = 0; colIter < ctrlArray.size(); colIter++) {
//
//                                JSONObject colIterItem = (JSONObject) ctrlArray.get(colIter);
//                                for (Object colIterItemKey : colIterItem.keySet()) {
//                                    String colIterItemKeyString = (String) colIterItemKey;
//
//                                    //System.out.println( colIterItemKeyString + " > " + colIterItem.get(colIterItemKeyString));
//
//
//                                    //try {
//                                    Double cellValueDouble = Double.parseDouble(String.valueOf(colIterItem.get(colIterItemKeyString)));
//                                    //System.out.println(cellValueDouble);
//
//
////                                            if (normalFlag.equals("yes")) {
////
////                                                cellValueDouble /= Double.parseDouble(String.valueOf(normalSumJson.get(colIterItemKeyString)));
////
////
////                                            }
//
//                                    list1.add(cellValueDouble);
//                                    if (cellValueDouble != 0.0) lis1Flag = true;
//
//
//                                    responseJSON.put(colIterItemKeyString, cellValueDouble);
//
//                                    if (cellValueDouble.isNaN()) {
//                                        eachRowError = true;
//                                        System.out.println(eachRowError.toString() + "--------------");
//                                    }
//
////
//
//
//                                }
//
//
//                            }
                            //System.out.println("loop over treatment");
//                            for (colIter = 0; colIter < trtArray.size(); colIter++) {
//
//                                JSONObject colIterItem = (JSONObject) trtArray.get(colIter);
//                                for (Object colIterItemKey : colIterItem.keySet()) {
//                                    String colIterItemKeyString = (String) colIterItemKey;
//
//
//                                    try {
//                                        Double cellValueDouble = Double.parseDouble(String.valueOf(colIterItem.get(colIterItemKeyString)));
////
////                                                if (normalFlag.equals("yes")) {
////
////                                                    cellValueDouble /= Double.parseDouble(String.valueOf(normalSumJson.get(colIterItemKeyString)));
////
////
////                                                }
//
//                                        list2.add(cellValueDouble);
//                                        if (cellValueDouble != 0.0) lis2Flag = true;
//
//
//                                        responseJSON.put(colIterItemKeyString, cellValueDouble);
//
//                                        if (cellValueDouble.isNaN()) {
//                                            eachRowError = true;
//                                            System.out.println(eachRowError.toString() + "--------------");
//                                        }
//
//                                    } catch (Exception e) {
//                                        outputError.put("message", String.format("Error: Value %s is not double.", colIterItemKeyString));
//                                        //return outputError;
//
//                                        eachRowError = true;
//                                        System.out.println("error");
//                                        System.out.println(colIterItemKeyString);
//                                        System.out.println(colIterItem.get(colIterItemKeyString));
//                                        System.out.println(eachRowError.toString() + ".........");
//                                    }
//
//
//                                }
//
//
//                            }


//                        System.out.println(list1);
//                            System.out.println(eachRowError);
//                            if (!eachRowError) {
//                                if (lis1Flag && lis2Flag) {
//
//                                    if (list1.size() > 0 && list2.size() > 0) {
//                                        pvAndFc = computePValueAndFoldChange(list1, list2);
//                                        //System.out.println(pvAndFc);
//                                        pv = (Double) pvAndFc.get(0);
//                                        fc = (Double) pvAndFc.get(1);


//                                    }
//
//                                } else {
//
//                                    if (list1.size() > 0 && list2.size() > 0) {
//                                        pvAndFc = computePValueAndFoldChange(list1, list2);
//                                        //System.out.println(pvAndFc);
//
//                                        pv = (Double) pvAndFc.get(0);
//                                        fc = (Double) pvAndFc.get(1);
//                                        if (fc.isInfinite()) {
//                                            fc = Math.signum(fc) * 10.0;
//                                        }
//
//                                        responseJSON.put("pv", pv);
//                                        responseJSON.put("fc", fc);
//
//                                        volcanoJSON.put("Peptide", peptide);
//                                        volcanoJSON.put("p_value", pv);
//                                        volcanoJSON.put("log2(fold_change)", fc);
//
//                                        inputArray.add(responseJSON);
//                                        volcanoArray.add(volcanoJSON);

//
//                                    }
//
//
//                                }
//                                if (!lis1Flag && !lis2Flag) {
//                                    // Since one of the groups are zero
//                                    //message += String.format("Error: control and treatment lists for peptide %s are all zeros. Please delete the row and submit again.",peptide);
//                                    outputError.put("message", String.format("Error: control and treatment lists for peptide %s are all zeros. Please delete the row and submit again.", peptide));
//                                    //return outputError;
//                                }
//
//
//                            } else {
//
//                            }
                        }
//
                    } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) { //If we have xls or xlsx


                        workbook = (SXSSFWorkbook) WorkbookFactory.create(convertMultipartToFile(file));
                        System.out.println("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");


                        // 2. Or you can use a for-each loop
                        System.out.println("Retrieving Sheets using for-each loop");
                        for (Sheet sheet : workbook) {
                            System.out.println("=> " + sheet.getSheetName());
                        }


                        // Getting the Sheet at index zero
                        Sheet sheet = workbook.getSheetAt(0);


                        // Create a DataFormatter to format and get each cell's value as String
                        DataFormatter dataFormatter = new DataFormatter();

                        // 1. You can obtain a rowIterator and columnIterator and iterate over them
                        System.out.println("\n\nIterating over Rows and Columns using Iterator\n");
                        Iterator<org.apache.poi.ss.usermodel.Row> rowIterator = sheet.rowIterator();
                        int rowiternum = 0;
                        while (rowIterator.hasNext()) {
                            org.apache.poi.ss.usermodel.Row row = rowIterator.next();
                            if (first_line) {

                                //System.out.println("first line true");
                                first_line = false;
                                colIter = 0;
                                Iterator<org.apache.poi.ss.usermodel.Cell> cellIterator = row.cellIterator();
                                while (cellIterator.hasNext()) {
                                    org.apache.poi.ss.usermodel.Cell cell = cellIterator.next();
                                    String cellValue = dataFormatter.formatCellValue(cell);
//                            System.out.print(cellValue + "\t");
//                            System.out.print(cellValue );
                                    keys.add(cellValue);
                                    colIter += 1;

                                }
                                System.out.println(keys);

                                if (colIter > 2 && colIter < 5) {

                                    outputError.put("message", "Error: Data file column size error, number of samples in each group is less than two. ");
                                    return outputError;
//                            error1.put("error","data file column size error, there are more than two and less than seven columns.");
//                            //errorArray1.add(error1);
//                            return error1;
                                }
                                if (colIter > 4) {
                                    int g1 = 0;
                                    int g2 = 0;
                                    JSONObject groups = new JSONObject();
                                    for (int colIterator = 1; colIterator < keys.size(); colIterator++) {
                                        String splitted = ((String) keys.get(colIterator)).split("_")[0];

                                        if (!groupsJson.containsKey(keys.get(colIterator))) {
                                            groupsJson.put(keys.get(colIterator), splitted);
                                            if (!groupsArray.contains(splitted)) {
                                                groupsArray.add(splitted);
                                            }

                                        } else {

                                            outputError.put("message", "Error: Duplicate column names. Please see the example for formatting");
                                            return outputError;
                                        }
                                    }
                                    if (groupsArray.size() != 2) {

                                        outputError.put("message", String.format("Error: Number of groups is %d which should be 2.", groupsArray.size()));
                                        return outputError;
                                    } else {

                                        firstGroup = groupsArray.get(0);
                                        secondGroup = groupsArray.get(1);
                                        for (Object key : groupsJson.keySet()) {
                                            String keyStr = (String) key;
                                            String val = (String) groupsJson.get(keyStr);
                                            if (val.equals(firstGroup)) {
                                                g1 += 1;
                                            }
                                            if (val.equals(secondGroup)) {
                                                g2 += 1;
                                            }

                                        }
                                        if (g1 < 2) {
                                            outputError.put("message", String.format("Error: Number of group %s is less than 2.", firstGroup));
                                            return outputError;
                                        }
                                        if (g2 < 2) {
                                            outputError.put("message", String.format("Error: Number of group %s is less than 2.", secondGroup));
                                            return outputError;
                                        }

                                    }


                                }


                            } else { // not first line xls
                                colIter = 0;
                                rowiternum += 1;

                                System.out.println(rowiternum + "------------");

                                // Now let's iterate over the columns of the current row
                                Iterator<org.apache.poi.ss.usermodel.Cell> cellIterator = row.cellIterator();
                                JSONObject responseJSON = new JSONObject();
                                JSONObject volcanoJSON = new JSONObject();
                                peptide = "";

                                ArrayList list1 = new ArrayList<Double>();
                                ArrayList list2 = new ArrayList<Double>();
                                ArrayList pvAndFc = new ArrayList<Double>(2);
                                Boolean lis1Flag = false;
                                Boolean lis2Flag = false;
                                while (cellIterator.hasNext()) {
                                    org.apache.poi.ss.usermodel.Cell cell = cellIterator.next();
                                    String cellValue = dataFormatter.formatCellValue(cell);
                                    System.out.print(cellValue + "  ");
                                    firstGroup = groupsArray.get(0);
                                    secondGroup = groupsArray.get(1);
                                    if (cellValue == null || cellValue.isEmpty()) {
                                        // doSomething

                                        outputError.put("message", String.format("Error: Null or empty values exist in the uploaded file, please change the null and empty values and submit again.", peptide));
                                        return outputError;
                                    }

                                    if (colIter == 0) {

                                        peptide = cellValue;
                                        if (!peptides.contains(peptide)) {
                                            peptides.add(peptide);
                                            parsedPeptide = getMotifAndModificationFromPeptide(peptide);
                                            //peptidesParsed.add(parsedPeptide);
                                            motif = (String) parsedPeptide.get("motif");
                                            motifs.add(motif);

                                        } else {

                                            outputError.put("message", String.format("Error: Duplicate peptide %s in list.", peptide));
                                            return outputError;
//                                    error1.put("error",String.format("Duplicate peptide %s in list.",peptide));
//                                    //errorArray1.add(error1);
//                                    return error1;
                                        }
                                        responseJSON.put("Peptide", cellValue);
                                        responseJSON.put("sequence", motif);
                                        responseJSON.put("modification", parsedPeptide.get("modifications"));
                                        colIter += 1;

                                    } else {


                                        //System.out.println(keys.get(colIter)+ "  "+cellValue );
                                        //                            System.out.print(cellValue + "++++++");

                                        try {
                                            Double cellValueDouble = Double.parseDouble(cellValue);

                                            if (groupsJson.get(keys.get(colIter)).equals(firstGroup)) {
                                                list1.add(cellValueDouble);
                                                if (cellValueDouble != 0.0) lis1Flag = true;

                                            } else {
                                                list2.add(cellValueDouble);
                                                if (cellValueDouble != 0.0) lis2Flag = true;
                                            }
                                            responseJSON.put(keys.get(colIter), cellValueDouble);
                                            colIter += 1;
                                        } catch (Exception e) {
                                            outputError.put("message", String.format("Error: Value %s is not double.", cellValue));
                                            return outputError;
                                        }


                                    }

                                }

                                responseJSON.put("group1", list1);
                                responseJSON.put("group2", list2);

//                        System.out.println(list1);
//                        System.out.println(list2 );

                                if (lis1Flag && lis2Flag) {
                                    pvAndFc = computePValueAndFoldChange(list1, list2);
                                    //System.out.println(pvAndFc);
                                    pv = (Double) pvAndFc.get(0);
                                    fc = (Double) pvAndFc.get(1);

                                } else {
                                    pvAndFc = computePValueAndFoldChange(list1, list2);
                                    //System.out.println(pvAndFc);

                                    pv = (Double) pvAndFc.get(0);
                                    fc = (Double) pvAndFc.get(1);
                                    if (fc.isInfinite()) {
                                        fc = Math.signum(fc) * 10.0;
                                    }
//                            System.out.println(fc);
//                            System.out.println("-------------");
//                            System.out.println("-------------");
//                            System.out.println("-------------");
//                            pv = 0.0000001;
//                            fc = 3.0 + 3.0 * Math.random();
                                    // Since one of the groups are zero
//                            outputError.put("message",String.format("Error: list of control or treatment for peptide %s is all zeros. Please delete the row and submit again.",peptide));
//                            return outputError;
                                }

                                if (!lis1Flag && !lis2Flag) {
                                    // Since one of the groups are zero
                                    //message += String.format("Error: control and treatment lists for peptide %s are all zeros. Please delete the row and submit again.",peptide);
                                    outputError.put("message", String.format("Error: control and treatment lists for peptide %s are all zeros. Please delete the row and submit again.", peptide));
                                    return outputError;
                                }

                                responseJSON.put("pv", pv);
                                responseJSON.put("fc", fc);

                                volcanoJSON.put("Peptide", peptide);
                                volcanoJSON.put("p_value", pv);
                                volcanoJSON.put("log2(fold_change)", fc);

                                inputArray.add(responseJSON);
                                volcanoArray.add(volcanoJSON);
                                //System.out.println(responseJSON.toString());

                            }

                        }


                    }
                    workbook.close();


//      stream.write(uploadfile.getBytes());
//      stream.close();
//      ExcelReader.readFile(filename);
                } catch (Exception e) {
                    System.out.println(e.getMessage());

                    outputError.put("message", "Error: Read input file error, please check supported browsers and check the format of input file with the provided example.");
                    return outputError;
//                error1.put("error",e.getMessage() + "/ Read input file error, please check for the format.");
//                //errorArray1.add(error1);
//                return error1;

                    //return new JSONArray();
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body( null);
                }
            } else {
                outputError.put("message", "Error: Unsupported file format, please check supported browsers and check the format of input file with the provided example.");
                return outputError;
            }


            String[] motifArray = motifs.toArray(new String[0]);
            JSONObject motifMatch = searchForPeptides(organism, motifArray);
            if (inputType.equals("maxQuant")) {
                //System.out.println("reordering motifMatch");
                JSONObject motifMatchReordered = reorderPeptideMatching(motifMatch, peptideToRazorDictionary);
                //System.out.println("After reordering motifMatch ================ ");
                //System.out.println(motifMatchReordered);
                //System.out.println("==============================");
                useTrembl = (Boolean) motifMatchReordered.get("useTrembl");
                JSONObject motifMatchReorderedRes = (JSONObject) motifMatchReordered.get("motifMatchReorderedRes");
                //System.out.println("motifMatchReordered: "+ motifMatchReorderedRes);

                output.put("dataForAllPeptides", motifMatchReorderedRes);
                output.put("useTrembl", useTrembl);
                //System.out.println(inputArrayJson);
            } else {
                output.put("dataForAllPeptides", motifMatch);
                output.put("useTrembl", useTrembl);
            }

            output.put("discarded", discardedArray);
            output.put("inputArray", inputArray);
            output.put("volcanoArray", volcanoArray);
            output.put("localMotifs", motifs);
            output.put("discarded", discardedArray);
            output.put("localPeptides", peptides);
//            output.put("peptidesParsed",peptidesParsed);
            output.put("tag", 200);
            output.put("message", "");
            //System.out.println(output);
            return output;
            //return ResponseEntity.ok(null);
        }
        return new JSONObject();
        //return ResponseEntity.ok(null);
    }

    public JSONObject reorderPeptideMatching(JSONObject motifMatch, JSONObject peptideToRazorDictionary) {
        JSONObject motifMatchReordered = new JSONObject();
        JSONObject motifMatchReorderedfinal = new JSONObject();

        //System.out.println("=============================");
        //System.out.println("inside reorderPeptideMatching");
        //System.out.println("motifMatch " + motifMatch);
        //System.out.println("peptideToRazorDictionary " + peptideToRazorDictionary);
        Boolean useTrembl = false;
        for (Object key : motifMatch.keySet()) {
            //try {
            //based on you key types
            String keySter = (String) key;
            JSONObject keyvalue = (JSONObject) motifMatch.get(keySter);
            JSONObject res = new JSONObject();
            res.put("n_match", keyvalue.get("n_match"));

            JSONArray input_matchset = (JSONArray) keyvalue.get("matchset");
            JSONArray matchset = new JSONArray();
            String razorProtein = "";


            if (peptideToRazorDictionary.containsKey(keySter)) {
                razorProtein = (String) peptideToRazorDictionary.get(keySter);

            }
            for (int iter = 0; iter < input_matchset.size(); iter++) {
                if (((((JSONObject) input_matchset.get(iter)).get("sequence_ac")).equals(razorProtein))) {

                    if(((((JSONObject) input_matchset.get(iter)).get("sequence_db")).equals("tr"))){
                        useTrembl = true;
                    }
                    matchset.add(input_matchset.get(iter));

                }
            }
            for (int iter = 0; iter < input_matchset.size(); iter++) {
                if (!((((JSONObject) input_matchset.get(iter)).get("sequence_ac")).equals(razorProtein))) {
                    matchset.add(input_matchset.get(iter));

                }
            }

            res.put("matchset", matchset);


            motifMatchReordered.put(keySter, res);

        }
//        ("useTrembl");
//        JSONObject motifMatchReorderedRes = (JSONObject) motifMatchReordered.get("motifMatchReorderedRes");
        motifMatchReorderedfinal.put("motifMatchReorderedRes", motifMatchReordered);
        motifMatchReorderedfinal.put("useTrembl", useTrembl);
        //System.out.println("my job is done =====================");
        //System.out.println("my job is done =====================");
        //System.out.println(motifMatchReorderedfinal);
        //System.out.println("my job is done =====================");
        //System.out.println("my job is done =====================");
        //System.out.println("my job is done =====================");
        return motifMatchReorderedfinal;

    }

    //
//    public double getMedian(ArrayList<Long> sets) {
//        Collections.sort(sets);
//        int middle = sets.size() / 2;
//        middle = middle > 0 && middle % 2 == 0 ? middle - 1 : middle;
//        return sets.get(middle);
//    }
    public double getMedian(ArrayList<Double> sets) {
        Collections.sort(sets);
        double median;
        //System.out.println("in getMedian");


        int totalElements = sets.size();
        if (totalElements % 2 == 0) {
            double sumOfMiddleElements = (double) (sets.get(totalElements / 2) + sets.get(totalElements / 2 - 1));
            // calculate average of middle elements
            median = ((double) sumOfMiddleElements) / 2.0;
        } else {
            // get the middle element
            median = (double) sets.get(sets.size() / 2);
        }
        //System.out.println(sets);
        //System.out.println(median);
        //System.out.println("================================");

        return median;
    }

    public JSONObject normalizeAndImputeAndPValAndFc(JSONObject peptideDictionary, JSONObject normalSumJson, String normalFlag, String quantileFlag, String imputeFlag, int ctrlNumber, int trtNumber) {
        JSONObject normalized = new JSONObject();
        JSONObject samplePosition = new JSONObject();
        JSONObject peptidePosition = new JSONObject();
        JSONObject sampleMedian = new JSONObject();
        ArrayList pvAndFc = new ArrayList<Double>(2);

        //ArrayList<Long> sets;
        ArrayList<ArrayList<Double>> matrixImputed
                = new ArrayList<ArrayList<Double>>();

        ArrayList<ArrayList<Double>> matrixRanking
                = new ArrayList<ArrayList<Double>>();

        ArrayList<ArrayList<Double>> matrixArrayToCalculateMedian
                = new ArrayList<ArrayList<Double>>();
        int iter_sample = 0;
        int iter_peptide = 0;
        //System.out.println("normalSumJson  :  " + normalSumJson);
        //System.out.println("samples are  :  ");
        for (Object key : normalSumJson.keySet()) {
            samplePosition.put((String) key, iter_sample);
            //System.out.print((String) key);
            //System.out.print(",");
            matrixImputed.add(new ArrayList<Double>());
            matrixRanking.add(new ArrayList<Double>());
            matrixArrayToCalculateMedian.add(new ArrayList<Double>());
            iter_peptide = 0;

            for (Object keyPeptide : peptideDictionary.keySet()) {

                String keySter = (String) keyPeptide;
                peptidePosition.put(keySter, iter_peptide);

                matrixImputed.get(iter_sample).add(iter_peptide, Double.valueOf(-1000));
                matrixRanking.get(iter_sample).add(iter_peptide, Double.valueOf(-1000));
                iter_peptide += 1;

            }
            iter_sample += 1;

        }
        //System.out.println("");
        //System.out.println("");
        int sample_number = iter_sample;

        ArrayList averages = new ArrayList<Double>();


        //int iter = 0;
        for (Object key : peptideDictionary.keySet()) {

            averages.add(0.0);

            String keySter = (String) key;
            //System.out.println("peptide is: " + keySter);
//            peptidePosition.put(keySter, iter);
//            iter += 1;

            ArrayList list1 = new ArrayList<Double>();
            ArrayList list2 = new ArrayList<Double>();

            JSONObject keyvalue = (JSONObject) peptideDictionary.get(keySter);

            JSONArray trtArray = (JSONArray) ((JSONObject) peptideDictionary.get(keySter)).get("trt");


            JSONArray ctrlArray = (JSONArray) ((JSONObject) peptideDictionary.get(keySter)).get("ctrl");

            //System.out.println("ctrlArray is: " + ctrlArray);
            //System.out.println("trtArray is: " + trtArray);

            for (int colIter = 0; colIter < ctrlArray.size(); colIter++) {

                JSONObject colIterItem = (JSONObject) ctrlArray.get(colIter);
                for (Object colIterItemKey : colIterItem.keySet()) {
                    String colIterItemKeyString = (String) colIterItemKey;

                    //System.out.println( colIterItemKeyString + " > " + colIterItem.get(colIterItemKeyString));


                    //try {
                    Double cellValueDouble = Double.parseDouble(String.valueOf(colIterItem.get(colIterItemKeyString)));

                    if (normalFlag.equals("yes")) {

                        cellValueDouble /= Double.parseDouble(String.valueOf(normalSumJson.get(colIterItemKeyString)));

                    }

                    if (quantileFlag.equals("yes")) {

                        matrixArrayToCalculateMedian.get((int) (samplePosition.get(colIterItemKeyString))).add(0, cellValueDouble);


                        matrixImputed.get((int) (samplePosition.get(colIterItemKeyString))).set((int) (peptidePosition.get(keySter)), cellValueDouble);
                        matrixRanking.get((int) (samplePosition.get(colIterItemKeyString))).set((int) (peptidePosition.get(keySter)), cellValueDouble);


                    } else {
                        list1.add(cellValueDouble);
                    }


                    //matrixArray.get(1).add(0, 366);
//


                }


            }
            //System.out.println("loop over treatment");
            for (int colIter = 0; colIter < trtArray.size(); colIter++) {

                JSONObject colIterItem = (JSONObject) trtArray.get(colIter);
                for (Object colIterItemKey : colIterItem.keySet()) {
                    String colIterItemKeyString = (String) colIterItemKey;


                    Double cellValueDouble = Double.parseDouble(String.valueOf(colIterItem.get(colIterItemKeyString)));
                    if (normalFlag.equals("yes")) {

                        cellValueDouble /= Double.parseDouble(String.valueOf(normalSumJson.get(colIterItemKeyString)));

                    }

                    if (quantileFlag.equals("yes")) {


                        matrixArrayToCalculateMedian.get((int) (samplePosition.get(colIterItemKeyString))).add(0, cellValueDouble);

                        matrixImputed.get((int) (samplePosition.get(colIterItemKeyString))).set((int) (peptidePosition.get(keySter)), cellValueDouble);
                        matrixRanking.get((int) (samplePosition.get(colIterItemKeyString))).set((int) (peptidePosition.get(keySter)), cellValueDouble);

                    } else {
                        list2.add(cellValueDouble);
                    }
                }

            }

            if (quantileFlag.equals("no")) {

                if(imputeFlag.equals("impute_with_mean")){

                    Double avg = getAverage(list1);
                    for( int itetocomp = list1.size(); itetocomp < ctrlNumber; itetocomp++){
                        list1.add(avg);
                    }

                    Double avg2 = getAverage(list2);
                    for( int itetocomp = list2.size(); itetocomp < trtNumber; itetocomp++){
                        list2.add(avg2);
                    }

                }

                if(imputeFlag.equals("impute_with_zero")){

                    //Double avg = getAverage(list1);
                    for( int itetocomp = list1.size(); itetocomp < ctrlNumber; itetocomp++){
                        list1.add(0.0);
                    }

                    //Double avg2 = getAverage(list2);
                    for( int itetocomp = list2.size(); itetocomp < trtNumber; itetocomp++){
                        list2.add(0.0);
                    }

                }


                pvAndFc = computePValueAndFoldChange(list1, list2);
                //System.out.println(pvAndFc);



                Double pv = (Double) pvAndFc.get(0);
                Double fc = (Double) pvAndFc.get(1);
                if (fc.isInfinite()) {
                    fc = Math.signum(fc) * 10.0;
                }
                keyvalue.put("pv", pv);
                keyvalue.put("fc", fc);
                normalized.put(keySter, keyvalue);
            }

        }
        //System.out.println("after the second first loop -------------------");
        //System.out.println(matrixImputed);
        //System.out.println(matrixRanking);
        //System.out.println("after the second first loop -------------------");


        if (quantileFlag.equals("yes")) {
            //int iter2 = 0;
            for (Object key : normalSumJson.keySet()) {

                double median = getMedian(matrixArrayToCalculateMedian.get((int) (samplePosition.get((String) key))));

                sampleMedian.put((String) key, median);
                //iter2 += 1;

            }
            //System.out.println("sampleMedian");
            //System.out.println(sampleMedian);

            int iter3 = 0;
            for (Object key : peptideDictionary.keySet()) {

                String peptideKey = (String) key;

                for (Object skey : samplePosition.keySet()) {

                    String sampleKey = (String) skey;
                    if (matrixImputed.get((int) (samplePosition.get(sampleKey))).get((int) (peptidePosition.get(peptideKey))) == -1000) {
                        //System.out.println("found NA for : " + peptideKey + " and " + sampleKey);
                        matrixImputed.get((int) (samplePosition.get(sampleKey))).set((int) (peptidePosition.get(peptideKey)), (Double) sampleMedian.get(sampleKey));
                    }
                }
            }
            //System.out.println(matrixImputed);
            //System.out.println("----------------");

            JSONObject sampleIndex = new JSONObject();
            JSONObject sampleRepeat = new JSONObject();
            //JSONObject sampleSorted = new JSONObject();
            //example = [1,5,4,11,5,5,4]
            //expected:
            //nfit =    [1,4,4,5,5,5,11]
            //indexes = [0,3,1,6,3,3,1]

            //nrepeat = [1,2,0,3,0,0,1]
            for (Object skey : samplePosition.keySet()) {

                String sampleKey = (String) skey;
                //sampleRepeat.put(sampleKey, new JSONObject());


                ArrayList<Double> nfit = matrixImputed.get((int) (samplePosition.get(sampleKey)));

                ArrayList<Double> nstore = new ArrayList<Double>(nfit); // may need to be new ArrayList(nfit)
                ArrayList<Integer> nrepeat = new ArrayList<Integer>(Collections.nCopies(nfit.size(), 0));
                ArrayList<Integer> indexes = new ArrayList<Integer>(Collections.nCopies(nfit.size(), 0));



                Collections.sort(nfit);
                //sampleSorted.put(sampleKey,nfit);

                //int[] indexes = new int[nfit.size()];


                for (int n = 0; n < nfit.size(); n++) {
                    indexes.set(n, nfit.indexOf(nstore.get(n)));
                    Double val = (Double) averages.get(n) + nfit.get(n);

                    averages.set(n,val);

                    int val2 = nrepeat.get(indexes.get(n));
                    val2 += 1;
                    nrepeat.set(indexes.get(n),val2);

                }

                sampleIndex.put(sampleKey,indexes);
                sampleRepeat.put(sampleKey,nrepeat);

                //System.out.println(Arrays.toString(indexes));

            }



            for (int n = 0; n < averages.size(); n++) {
                averages.set(n, (Double)averages.get(n) / Double.valueOf(sample_number));
            }
            //System.out.println("averages per peptide");
            //System.out.println(averages);
            //System.out.println("------------------------");
            //System.out.println("sampleIndex");
            //System.out.println(sampleIndex);
            //System.out.println("------------------------");
            //System.out.println("sampleRepeat");
            //System.out.println(sampleRepeat);
            //System.out.println("------------------------");

            for (Object skey : samplePosition.keySet()) {

                String sampleKey = (String) skey;
                int samplehere = (int)samplePosition.get(sampleKey);
                //System.out.println("sample: "+ samplehere);


                //int peptideiteratorhere = 0;
                for (Object keyPeptide : peptideDictionary.keySet()) {

                    String keySter = (String) keyPeptide;
                    //System.out.println("peptide: "+ keySter);
                    int peptidehere = (int) (peptidePosition.get(keySter));
                    //System.out.println("peptidehere: "+ peptidehere);
                    if(matrixRanking.get(samplehere).get(peptidehere) > -1000){

                        int poshere = (int) ((ArrayList)sampleIndex.get(sampleKey)).get(peptidehere);
                        //System.out.println("poshere: "+ poshere);
                        Double valhere = (Double)averages.get(poshere);

                        int repeathere = (int) ((ArrayList)sampleRepeat.get(sampleKey)).get(poshere);
                        //System.out.println("repeathere: "+ repeathere);
                        for(int iteratorhere = poshere; iteratorhere < poshere + repeathere - 1; iteratorhere++){
                            //System.out.println("in the loop iteratorhere: "+ repeathere);
                            //System.out.println("in the loop averages.get(iteratorhere + 1): "+ averages.get(iteratorhere + 1));
                            valhere += (Double) averages.get(iteratorhere + 1);
                        }

                        valhere /= repeathere;
                        matrixRanking.get((int)samplePosition.get(sampleKey)).set(peptidehere, valhere);
                    }
                    System.out.println("======================================");
                    //peptideiteratorhere += 1;

                }
            }
            //System.out.println("matrixRanking");
            //System.out.println(matrixRanking);
            //System.out.println("------------------------");


            for (Object key : peptideDictionary.keySet()) {

                //averages.add(0.0);

                String keySter = (String) key;
                int peptidehere = (int) (peptidePosition.get(keySter));
//            peptidePosition.put(keySter, iter);
//            iter += 1;

                ArrayList list1 = new ArrayList<Double>();
                ArrayList list2 = new ArrayList<Double>();

                JSONObject keyvalue = (JSONObject) peptideDictionary.get(keySter);

                JSONArray trtArray = (JSONArray) ((JSONObject) peptideDictionary.get(keySter)).get("trt");


                JSONArray ctrlArray = (JSONArray) ((JSONObject) peptideDictionary.get(keySter)).get("ctrl");

                //System.out.println("peptide is: " + keySter);
                //System.out.println("ctrlArray is: " + ctrlArray);
                //System.out.println("trtArray is: " + trtArray);

                for (int colIter = 0; colIter < ctrlArray.size(); colIter++) {

                    JSONObject colIterItem = (JSONObject) ctrlArray.get(colIter);
                    for (Object colIterItemKey : colIterItem.keySet()) {
                        String colIterItemKeyString = (String) colIterItemKey;

                        int samplehere = (int) samplePosition.get(colIterItemKeyString);
                        if(matrixRanking.get(samplehere).get(peptidehere) > -1000){

                            list1.add(matrixRanking.get(samplehere).get(peptidehere));

                        }


                    }


                }

                for (int colIter = 0; colIter < trtArray.size(); colIter++) {

                    JSONObject colIterItem = (JSONObject) trtArray.get(colIter);
                    for (Object colIterItemKey : colIterItem.keySet()) {
                        String colIterItemKeyString = (String) colIterItemKey;

                        int samplehere = (int) samplePosition.get(colIterItemKeyString);
                        if(matrixRanking.get(samplehere).get(peptidehere) > -1000){

                            list2.add(matrixRanking.get(samplehere).get(peptidehere));

                        }


                    }


                }
                //System.out.println("loop over treatment");

                //System.out.println("before imputation");
                //System.out.println("list1: "+ list1);
                //System.out.println("list2: "+ list2);




                if (imputeFlag.equals("impute_with_mean")) {

                    Double avg = getAverage(list1);
                    for (int itetocomp = list1.size(); itetocomp < ctrlNumber; itetocomp++) {
                        list1.add(avg);
                    }

                    Double avg2 = getAverage(list2);
                    for (int itetocomp = list2.size(); itetocomp < trtNumber; itetocomp++) {
                        list2.add(avg2);
                    }

                }

                if (imputeFlag.equals("impute_with_zero")) {

                    //Double avg = getAverage(list1);
                    for (int itetocomp = list1.size(); itetocomp < ctrlNumber; itetocomp++) {
                        list1.add(0.0);
                    }

                    //Double avg2 = getAverage(list2);
                    for (int itetocomp = list2.size(); itetocomp < trtNumber; itetocomp++) {
                        list2.add(0.0);
                    }

                }

                //System.out.println("after imputation");
                //System.out.println("list1: "+ list1);
                //System.out.println("list2: "+ list2);


                pvAndFc = computePValueAndFoldChange(list1, list2);
                //System.out.println(pvAndFc);


                Double pv = (Double) pvAndFc.get(0);
                Double fc = (Double) pvAndFc.get(1);
                if (fc.isInfinite()) {
                    fc = Math.signum(fc) * 10.0;
                }
                keyvalue.put("pv", pv);
                keyvalue.put("fc", fc);
                normalized.put(keySter, keyvalue);
            }

        }

        //System.out.println("normalized:  " + normalized);
        return normalized;
    }

    public Double getAverage(ArrayList<Double> list){
        Double avg = 0.0;
        if(list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                avg += list.get(i);
            }
            avg /= list.size();
        }
        return avg;
    }

    public ArrayList<Double> computePValueAndFoldChange(ArrayList<Double> list1, ArrayList<Double> list2) {

        ArrayList pvAndFc = new ArrayList<Double>();
        Double mean1 = 0.0;
        Double mean2 = 0.0;
        Double ttest;
        TTest tt = new TTest();
        Double fc;


        double[] objArray1 = new double[list1.size()];
        double[] objArray2 = new double[list2.size()];
        for (int i = 0; i < list1.size(); i++) {
            objArray1[i] = list1.get(i);
            mean1 += list1.get(i);

        }
        for (int i = 0; i < list2.size(); i++) {
            objArray2[i] = list2.get(i);
            mean2 += list2.get(i);


        }
        mean1 /= list1.size();
        mean2 /= list2.size();
        fc = Math.log(mean2 / mean1) / Math.log(2);
        //System.out.println(list1.size();
        if (list1.size() > 1 && list2.size() > 1) {
            ttest = tt.homoscedasticTTest(objArray1, objArray2);
            //System.out.println("ttest");
            //System.out.println(ttest);
            try{
                Double.valueOf(ttest);

                if(Double.isNaN(ttest)){
                    pvAndFc.add(0, 1.0);
                }else{
                    pvAndFc.add(0, ttest);
                    //System.out.println("ttest ====================================" + ttest);
                    //System.out.println(Double.valueOf(ttest));
                }
            }
            catch (Exception ex){
                //System.out.println("Not a valid double value");
                pvAndFc.add(0, 1.0);
            }

        } else {
//            ttest = 1.0;
//            pvAndFc.add(0, ttest);
            pvAndFc.add(0, 1.0);
        }

        try{
            Double.valueOf(fc);
            if(Double.isNaN(fc)){
                pvAndFc.add(0, 0.0);
            }else{
                pvAndFc.add(1, fc);
            }

//            System.out.println("fc ====================================" + fc);
//            System.out.println(Double.valueOf(fc));
        }
        catch (Exception ex){
            //System.out.println("Not a valid double value");
            pvAndFc.add(1, 0.0);

        }
//        System.out.println("fc");
//        System.out.println(fc);
//
//
//        pvAndFc.add(1, fc);

        return pvAndFc;
    }


//
//    @RequestMapping(value = "api/uniprot2/{uniprot}", method = RequestMethod.GET)
//    public
//    @ResponseBody
//    JSONObject getFromUniprot(@PathVariable String uniprot)

//    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
//    @ResponseBody
//    public ResponseEntity<?> uploadFile(
//            @RequestParam("uploadfile") MultipartFile uploadfile) {
//        try {
//            // Get the filename and build the local file path
//            String filename = uploadfile.getOriginalFilename();
////            String directory = env.getProperty("netgloo.paths.uploadedFiles");
////            String filepath = Paths.get(directory, filename).toString();
//
//            // Save the file locally
////      BufferedOutputStream stream =
////          new BufferedOutputStream(new FileOutputStream(new File(filepath)));
//
//            //=====================
//            Workbook workbook = WorkbookFactory.create(convert(uploadfile));
//
//            // Retrieving the number of sheets in the Workbook
//            System.out.println("Workbook has " + workbook.getNumberOfSheets() + " Sheets : ");
//
//        /*
//           =============================================================
//           Iterating over all the sheets in the workbook (Multiple ways)
//           =============================================================
//        */
//
//            // 1. You can obtain a sheetIterator and iterate over it
//            Iterator<Sheet> sheetIterator = workbook.sheetIterator();
//            System.out.println("Retrieving Sheets using Iterator");
//            while (sheetIterator.hasNext()) {
//                Sheet sheet = sheetIterator.next();
//                System.out.println("=> " + sheet.getSheetName());
//            }
//
//            // 2. Or you can use a for-each loop
//            System.out.println("Retrieving Sheets using for-each loop");
//            for(Sheet sheet: workbook) {
//                System.out.println("=> " + sheet.getSheetName());
//            }
//
//            // 3. Or you can use a Java 8 forEach with lambda
//            System.out.println("Retrieving Sheets using Java 8 forEach with lambda");
//            workbook.forEach(sheet -> {
//                System.out.println("=> " + sheet.getSheetName());
//            });
//
//        /*
//           ==================================================================
//           Iterating over all the rows and columns in a Sheet (Multiple ways)
//           ==================================================================
//        */
//
//            // Getting the Sheet at index zero
//            Sheet sheet = workbook.getSheetAt(0);
//
//            // Create a DataFormatter to format and get each cell's value as String
//            DataFormatter dataFormatter = new DataFormatter();
//
//            // 1. You can obtain a rowIterator and columnIterator and iterate over them
//            System.out.println("\n\nIterating over Rows and Columns using Iterator\n");
//            Iterator<org.apache.poi.ss.usermodel.Row> rowIterator = sheet.rowIterator();
//            while (rowIterator.hasNext()) {
//                org.apache.poi.ss.usermodel.Row row = rowIterator.next();
//
//                // Now let's iterate over the columns of the current row
//                Iterator<org.apache.poi.ss.usermodel.Cell> cellIterator = row.cellIterator();
//
//                while (cellIterator.hasNext()) {
//                    org.apache.poi.ss.usermodel.Cell cell = cellIterator.next();
//                    String cellValue = dataFormatter.formatCellValue(cell);
//                    System.out.print(cellValue + "\t");
//                }
//                System.out.println();
//            }
//
//            // 2. Or you can use a for-each loop to iterate over the rows and columns
//            System.out.println("\n\nIterating over Rows and Columns using for-each loop\n");
//            for (org.apache.poi.ss.usermodel.Row row: sheet) {
//                for(org.apache.poi.ss.usermodel.Cell cell: row) {
//                    String cellValue = dataFormatter.formatCellValue(cell);
//                    System.out.print(cellValue + "\t");
//                }
//                System.out.println();
//            }
//
//            // 3. Or you can use Java 8 forEach loop with lambda
//            System.out.println("\n\nIterating over Rows and Columns using Java 8 forEach with lambda\n");
//            sheet.forEach(row -> {
//                row.forEach(cell -> {
//                    String cellValue = dataFormatter.formatCellValue(cell);
//                    System.out.print(cellValue + "\t");
//                });
//                System.out.println();
//            });
//
//            // Closing the workbook
//            workbook.close();
//
//
////      stream.write(uploadfile.getBytes());
////      stream.close();
////      ExcelReader.readFile(filename);
//        }
//        catch (Exception e) {
//            System.out.println(e.getMessage());
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//
//
//        return new ResponseEntity<>(HttpStatus.OK);
//    } // method uploadFile

    public File multipartToFile(MultipartFile multipart) throws IllegalStateException, IOException {
        File convFile = new File(multipart.getOriginalFilename());
        multipart.transferTo(convFile);
        return convFile;
    }

    public File convertMultipartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    public JSONObject convertFileReaderToJson(FileReader filereader) throws IOException {


        CSVReader csvReader = new CSVReader(filereader);
        String[] nextRecord;
        JSONObject jsonOutput = new JSONObject();
        boolean firstLine = true;
        // we are going to read data line by line

        while ((nextRecord = csvReader.readNext()) != null) {
            if (firstLine) {
                for (String cell : nextRecord) {

                    System.out.print(cell + "\t");
                }
                firstLine = false;
            }
            System.out.println();
        }

        return jsonOutput;
    }


    @RequestMapping(value = "api/uniprot2/{protein}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject getFromUniprot(@PathVariable String protein) {
        log.info(String.format("Get the uniprot information from uniprot with argument: %s", protein));

//        try {
//            incrementList(5);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        Path path = Paths.get("/Users/shamsabz/Documents/tmp/file.txt");
        String name = "file.txt";
        String originalFileName = "file.txt";
        String contentType = "text/plain";
        byte[] content = null;
        try {
            content = Files.readAllBytes(path);


            //MultipartFile result = new MockMultipartFile(name,
            //  originalFileName, contentType, content);

            //uploadFile(result);
        } catch (final IOException e) {
        }


        return uniprotService.getTable(protein);
    }


    @RequestMapping(value = "api/proteinptm/{mod}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject getPTMByID(@PathVariable String mod) throws Exception {
        //log.info(String.format("Run convertToPLN with argument: %s", peptide));

//        try {
//            incrementList(4);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        return prideService.findPTMByIDAPI(mod);
    }


    @RequestMapping(value = "api/proteinptmpride/{mod}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject getPTMByIDPride(@PathVariable String mod) throws Exception {
        //log.info(String.format("Run convertToPLN with argument: %s", peptide));

//        try {
//            incrementList(4);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        return prideService.findPTMByID(mod);
    }

    @RequestMapping(value = "api/proteinptmbydescription/{description}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONArray getPTMByDescription(@PathVariable String description) throws Exception {
        //log.info(String.format("Run convertToPLN with argument: %s", peptide));

//        try {
//            incrementList(4);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        System.out.println(description);
        return prideService.findPTMByDescriptionAPI(description);
    }

    @RequestMapping(value = "api/proteinptmbydescriptionpride/{description}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONArray getPTMByDescriptionPride(@PathVariable String description) throws Exception {
        //log.info(String.format("Run convertToPLN with argument: %s", peptide));

//        try {
//            incrementList(4);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        System.out.println(description);
        return prideService.findPTMByDescription(description);
    }


    @RequestMapping(value = "api/proteinptmbymass/{mass:.+}/delta/{delta:.+}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONArray getPTMByMassAndDelta(@PathVariable Double mass, @PathVariable Double delta) throws Exception {
        //log.info(String.format("Run convertToPLN with argument: %s", peptide));

//        try {
//            incrementList(4);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        return prideService.findPTMByMassAndDeltaAPI(mass, delta);
    }

    @RequestMapping(value = "api/proteinptmbymasspride/{mass:.+}/delta/{delta:.+}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONArray getPTMByMassAndDeltaPride(@PathVariable Double mass, @PathVariable Double delta) throws Exception {
        //log.info(String.format("Run convertToPLN with argument: %s", peptide));

//        try {
//            incrementList(4);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        return prideService.findPTMByMassAndDelta(mass, delta);
    }

    @RequestMapping(value = "api/prosite/{peptide}", method = RequestMethod.GET)
    public
    @ResponseBody
    String getFromProsite(@PathVariable String peptide) {
        //log.info(String.format("Run convertToPLN with argument: %s", peptide));

//        try {
//            incrementList(4);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        return prositeService.getTable(peptide);
    }


    @RequestMapping(value = "api/increment/{param}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject incrementList(@PathVariable Integer param) {
        //log.info(String.format("Run convertToPLN with argument: %s", peptide));

        return iteratorIncrementService.changeIterator(param);
//        return iteratorIncrementService.writeBack(increment);
//
//        JSONObject increment = iteratorIncrementService.changeIterator(param);
//        return iteratorIncrementService.writeBack(increment);
    }

    @RequestMapping(value = "api/regex/{input:.+}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject getMotifAndModificationFromPeptide(@PathVariable String input) {
        log.info(String.format("Get getMotifAndModification from: %s", input));

        return peptideRegexServive.getMotifAndModification(input);
    }

    @RequestMapping(value = "api/regexMaxQuantBefore/{input:.+}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject getMotifAndModificationFromPeptideMaxQuantBefore(@PathVariable String input) {
        log.info(String.format("Get getMotifAndModification from: %s", input));

        return peptideRegexServive.getMotifAndModificationMaxQuantBefore(input);
    }

    @RequestMapping(value = "api/regexMaxQuantBeforeModified/{input:.+}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject getMotifAndModificationFromPeptideMaxQuantBeforeModifiedToGetRidOfInitialModification(@PathVariable String input) {
        log.info(String.format("Get getMotifAndModification from: %s", input));

        return peptideRegexServive.getMotifAndModificationMaxQuantBeforeModified(input);
    }


    @RequestMapping(value = "api/regexMaxQuantAfter/{input:.+}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject getMotifAndModificationFromPeptideMaxQuantAfter(@PathVariable String input) {
        log.info(String.format("Get getMotifAndModification from: %s", input));

        return peptideRegexServive.getMotifAndModificationMaxQuantAfter(input);
    }

    @RequestMapping(value = "api/fasta/{protein}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject getFastaFromUniprot(@PathVariable String protein) {
        log.info(String.format("Get the uniprot information from uniprot with argument: %s", protein));

        return fastaService.getTable(protein);
    }

    @RequestMapping(value = "api/uniprot/organism/{organism}/protein/{protein}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject getFromUniprot2(@PathVariable String organism, @PathVariable String protein) {
        log.info(String.format("Get the uniprot information from uniprot with argument: %s", protein));

        return uniprotService2.getTable(organism, protein);
    }

    @RequestMapping(value = "api/prosite2/{peptideAndOrganism}", method = RequestMethod.GET)
    public
    @ResponseBody
    String getFromProsite2(@PathVariable String[] peptideAndOrganism) {
        //log.info(String.format("Run convertToPLN with argument: %s", peptide));

        return prositeService2.getTable(peptideAndOrganism);
    }

    @RequestMapping(value = "api/ilincs/signature/{geneList:.+}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject getSignaturesFromIlincs(@PathVariable String[] geneList) throws Exception {

        JSONObject url = new JSONObject();

//        try {
//            incrementList(6);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        url = ilincsService.getSignatureIds(geneList);
        //System.out.println(url);
        return url;

    }

    @RequestMapping(value = "api/ilincs/signatureUrl/{geneList:.+}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject getFromIlincs(@PathVariable String[] geneList) throws Exception {

        JSONObject url = new JSONObject();

//        try {
//            incrementList(6);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        url = ilincsService.getSignatureUrl(geneList);
        System.out.println(url);
        return url;

    }

    @RequestMapping(value = "api/phosphoPredict/organism/{organism}/ptms/{ptmList}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject getFromDeepPhos(@PathVariable String organism, @PathVariable String ptmList) throws Exception {
        JSONObject results = new JSONObject();

        results = deepPhosService.getPhosphoPrediction(organism, ptmList);
        return results;
    }

    @RequestMapping(value = "api/networkinPredictApi/organism/{organism}/ptms/{ptmList}", method = RequestMethod.GET)
    public
    @ResponseBody
    String getFromDeepPhosApi(@PathVariable String organism, @PathVariable String ptmList) throws Exception {
        // JSONObject results = new JSONObject();

        String results = deepPhosService.getPhosphoPredictionNetworkinApi(organism, ptmList);
        return results;
    }


    @RequestMapping(value = "api/networkinPredict/organism/{organism}/ptms/{ptmList}", method = RequestMethod.GET)
    public
    @ResponseBody
    String getFromDeepPhosLocal(@PathVariable String organism, @PathVariable String ptmList) throws Exception {
        //JSONObject results = new JSONObject();

        String results = deepPhosService.getPhosphoPredictionNetworkinLocal(organism, ptmList);
        return results;
    }


    @RequestMapping(value = "api/enrichment/target/{pathway}/genes/{geneList}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject getFromEnrichrV3(@PathVariable String pathway, @PathVariable String[] geneList) throws Exception {
        //log.info(String.format("Run convertToPLN with argument: %s", peptide));
        //httpPostService.doPost();
        //httpURLConnectionExample.sendPost();

//        try {
//            incrementList(6);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        long listId;
        listId = enrichrServiceV2.getListId(geneList);
        System.out.println("listId");
        System.out.println(String.valueOf(listId));
        JSONObject networkInput = new JSONObject();
        JSONObject network = new JSONObject();

        //networkInput = getFromEnrichr(geneList);
        network = enrichrServiceV2.computeNetwork2(pathway, listId);
//        System.out.println(Arrays.toString(geneList));
//        for (int i = 0; i < geneList.length; i++) {
//            System.out.println(geneList[i]);
//            geneListinfo.put(geneList[i].replaceAll("[^a-zA-Z0-9\\s]", ""), enrichrService.getGeneInfo(geneList[i].replaceAll("[^a-zA-Z0-9\\s]", "")));
//        }
        //System.out.println(network.toJSONString());
        return network;
        //return enrichrPostService.getTable(genes);

    }


    @RequestMapping(value = "api/enrichment3/genes/{pathwayType}/{geneList}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject getFromEnrichrV2(@PathVariable String pathwayType, @PathVariable String[] geneList) throws Exception {
        //log.info(String.format("Run convertToPLN with argument: %s", peptide));
        //httpPostService.doPost();
        //httpURLConnectionExample.sendPost();

//        try {
//            incrementList(6);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        int listId;
        listId = enrichrServiceV2.getListId(geneList);
        log.info("listId");
        log.info(String.valueOf(listId));
        JSONObject networkInput = new JSONObject();
        JSONObject network = new JSONObject();

        //networkInput = getFromEnrichr(geneList);
        network = enrichrServiceV2.computeNetwork(pathwayType, listId);
//        System.out.println(Arrays.toString(geneList));
//        for (int i = 0; i < geneList.length; i++) {
//            System.out.println(geneList[i]);
//            geneListinfo.put(geneList[i].replaceAll("[^a-zA-Z0-9\\s]", ""), enrichrService.getGeneInfo(geneList[i].replaceAll("[^a-zA-Z0-9\\s]", "")));
//        }

        return network;
        //return enrichrPostService.getTable(genes);

    }

    @RequestMapping(value = "api/networkFromCSV/", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject generateNetworkFromCSV() throws Exception {

        JSONObject network = new JSONObject();


        network = networkFromCSV.computeNetwork();


        return network;


    }

    @RequestMapping(value = "api/peptidewithvalue/organism/{organism}/peptides/{peptideswithvalues:.+}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject searchForPeptidesAndValues(@PathVariable String organism, @PathVariable String[] peptideswithvalues) {
        //log.info(String.format("Run convertToPLN with argument: %s", peptide));
//        try {
//            incrementList(4);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        return peptideWithValueService.getTable(organism, peptideswithvalues);
    }

    @RequestMapping(value = "api/peptide/organism/{organism}/peptides/{peptides}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject searchForPeptides(@PathVariable String organism, @PathVariable String[] peptides) {
        //log.info(String.format("Run convertToPLN with argument: %s", peptide));
//        try {
//            incrementList(4);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        return peptideSearchService.getTable(organism, peptides);
    }

    @RequestMapping(value = "api/pir/{peptideAndOrganism}", method = RequestMethod.GET)
    public
    @ResponseBody
    String getFromPir(@PathVariable String[] peptideAndOrganism) {
        //log.info(String.format("Run convertToPLN with argument: %s", peptide));

//        try {
//            incrementList(4);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        return pirService.getTable(peptideAndOrganism).toJSONString();
    }

    @RequestMapping(value = "api/psimod/{modification:.+}", method = RequestMethod.GET)
    public
    @ResponseBody
    StringDoubleStringList getFromPsiMod(@PathVariable("modification") String modification) {
        log.info(String.format("Get modification from api/psimod identifier: %s", modification));
        //log.info(String.format("==== %s ======", modification));

//        try {
//            incrementList(4);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        return psiModService.getIdentifier(modification, 2.0);
    }

    @RequestMapping(value = "api/psimodExtension/{modification}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject getFromPsiModExtension(@PathVariable String modification) {
        log.info(String.format("Get modification extension from api/psimodExtension identifier: %s", modification));
        //log.info(String.format("==== %s ======", modification));

//        try {
//            incrementList(4);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        return psiModExtensionService.getInfo(modification);
    }

    @RequestMapping(value = "api/pcg/checkgenes/{geneList}", method = RequestMethod.GET)
    public
    @ResponseBody
    ArrayList checkFromPCG(@PathVariable String[] geneList) {
        ArrayList<Integer> genePlaces;

        //log.info(String.format("Get gene positions from api/pcg/checkgenes/%s", geneList));
        //log.info(String.format("==== %s ======", modification));
        genePlaces = pcgService.checkGenes(geneList);
        return genePlaces;
    }


    @RequestMapping(value = "api/pcg/geneinfo/{genePositions}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONArray getFromPCG(@PathVariable ArrayList<Integer> genePositions) {
        //Integer[] genePlaces;
        log.info(String.format("Get information about uniprot coding genes in positions %s from api/pcg/geneinfo/%s", genePositions, genePositions));
        //log.info(String.format("==== %s ======", modification));

        return pcgService.getTable(genePositions);
    }


    @RequestMapping(value = "api/pathway/genes/{type}/{geneList}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject getFromEnrichr(@PathVariable String type, @PathVariable String[] geneList) {
        //log.info(String.format("Run pathway analysis with argument: %s ", library));
//        String[] geneList =
//        String[] geneListSplit = geneList.split(",");

//        try {
//            incrementList(6);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        JSONObject geneListInfo = new JSONObject();

        System.out.println(Arrays.toString(geneList));
        for (int i = 0; i < geneList.length; i++) {
            System.out.println(geneList[i]);
            geneListInfo.put(geneList[i].replaceAll("[^a-zA-Z0-9\\s]", ""), enrichrService.getGeneInfo(type, geneList[i].replaceAll("[^a-zA-Z0-9\\s]", "")));
            System.out.println("done!");
        }
//        log.info("here");
//        log.info(String.valueOf(geneListInfo));
        return geneListInfo;
    }

    @RequestMapping(value = "api/pathway/genes/{geneList}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject getFromEnrichr2(@PathVariable String[] geneList) {
        //log.info(String.format("Run pathway analysis with argument: %s ", library));
//        String[] geneList =
//        String[] geneListSplit = geneList.split(",");

//        try {
//            incrementList(6);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        JSONObject geneListInfo = new JSONObject();

        System.out.println(Arrays.toString(geneList));
        for (int i = 0; i < geneList.length; i++) {
            System.out.println(geneList[i]);
            geneListInfo.put(geneList[i].replaceAll("[^a-zA-Z0-9\\s]", ""), enrichrService.getGeneInfo2(geneList[i].replaceAll("[^a-zA-Z0-9\\s]", "")));
            System.out.println("done!");
        }
//        log.info("here");
//        log.info(String.valueOf(geneListInfo));
        return geneListInfo;
    }

    @RequestMapping(value = "api/network/{type}/{geneList}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject computeNetworkFromEnrichr(@PathVariable String type, @PathVariable String[] geneList) {
        //log.info(String.format("Run pathway analysis with argument: %s ", library));
//        String[] geneList =
//        String[] geneListSplit = geneList.split(",");

//        try {
//            incrementList(6);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        JSONObject networkInput = new JSONObject();
        JSONObject network = new JSONObject();

        networkInput = getFromEnrichr(type, geneList);
        System.out.println("getFromEnrichr done! ");
        //System.out.println(String.valueOf(networkInput));
        network = enrichrService.computeNetwork3(type, networkInput);
//        System.out.println(Arrays.toString(geneList));
//        for (int i = 0; i < geneList.length; i++) {
//            System.out.println(geneList[i]);
//            geneListinfo.put(geneList[i].replaceAll("[^a-zA-Z0-9\\s]", ""), enrichrService.getGeneInfo(geneList[i].replaceAll("[^a-zA-Z0-9\\s]", "")));
//        }

        return network;
    }

    @RequestMapping(value = "api/network/genes/{geneList}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject computeNetworkFromEnrichr2(@PathVariable String[] geneList) {
        //log.info(String.format("Run pathway analysis with argument: %s ", library));
//        String[] geneList =
//        String[] geneListSplit = geneList.split(",");

//        try {
//            incrementList(6);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        JSONObject networkInput = new JSONObject();
        JSONObject network = new JSONObject();

        networkInput = getFromEnrichr2(geneList);
        System.out.println("getFromEnrichr2 done! ");
        //System.out.println(String.valueOf(networkInput));
        network = enrichrService.computeNetwork2(networkInput);
//        System.out.println(Arrays.toString(geneList));
//        for (int i = 0; i < geneList.length; i++) {
//            System.out.println(geneList[i]);
//            geneListinfo.put(geneList[i].replaceAll("[^a-zA-Z0-9\\s]", ""), enrichrService.getGeneInfo(geneList[i].replaceAll("[^a-zA-Z0-9\\s]", "")));
//        }

        return network;
    }

    @RequestMapping(value = "api/kinase/genes/{geneList}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject computeNetworkForKinase(@PathVariable String[] geneList) {

//        try {
//            incrementList(5);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        JSONObject kinaseNetwork = new JSONObject();
        kinaseNetwork = kinaseService.computeKinaseNetwork(geneList);

        return kinaseNetwork;
    }

    @RequestMapping(value = "api/ptm/organism/{organism}/ptmprotein/{geneList:.+}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject computeNetworkForPhosphoGenes(@PathVariable String organism, @PathVariable String[] geneList) throws Exception {
//        try {
//            incrementList(5);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        JSONObject phosphoNetwork = new JSONObject();
        phosphoNetwork = ptmService.computePtmNetwork(organism, geneList);
        //phosphoNetwork = phosphoService.computePtmNetwork(organism,geneList);
        return phosphoNetwork;

    }

    @RequestMapping(value = "api/phosphoandptm/organism/{organism}/ptmprotein/{geneList:.+}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject computePhosphoAndPtmNetworkForPtmGenes(@PathVariable String organism, @PathVariable String[] geneList) throws Exception {
//        try {
//            incrementList(5);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        JSONObject ptmNetwork = new JSONObject();
        ptmNetwork = phosphoService.computePtmNetwork(organism, geneList);
        //phosphoNetwork = phosphoService.computePtmNetwork(organism,geneList);
        return ptmNetwork;

    }

    @RequestMapping(value = "api/phospho2/organism/{organism}/ptmprotein/{geneList:.+}", method = RequestMethod.GET)
    public
    @ResponseBody
    JSONObject computeNetworkForPtmGenes(@PathVariable String organism, @PathVariable String[] geneList) throws Exception {
//        try {
//            incrementList(5);
//        }catch (Exception e)
//        {
//            System.out.println(e);
//        }
        JSONObject ptmNetwork = new JSONObject();
        ptmNetwork = phosphoServiceV2.computePhosphoNetwork(organism, geneList);
        //phosphoNetwork = phosphoService.computePtmNetwork(organism,geneList);

        return ptmNetwork;

    }

    //    @RequestMapping(value = "api/uniprotaccession/{accession}", method = RequestMethod.GET)
//    @ResponseBody
//    public String getFromUniprotAccession(@PathVariable String accession) {
//        System.out.println(String.format("Get the uniprot information from uniprot with argument: %s", accession));
//        Uniprot response;
//        response = proteinRepositoryH2.findByAccession(accession);
//        return response.toString();
//    }
    @RequestMapping(value = "api/uniprotdb/organism/{organism}/accession/{accession}", method = RequestMethod.GET)
    @ResponseBody
    public JSONObject getFromUniprotDBAccession(@PathVariable String organism, @PathVariable String accession) {
        System.out.println(String.format("Get the protein information from uniprot with argument: %s", accession));
        Uniprot response = new Uniprot();
//    try {
//        incrementList(5);
//    }catch (Exception e)
//    {
//        System.out.println(e);
//    }
        JSONObject responseUniprot = new JSONObject();

        String[] canonicalAccessionList = accession.split("-");

        String canonicalAccession = canonicalAccessionList[0];
        try {


            response = uniprotRepository.findByAccession(canonicalAccession);
            responseUniprot = response.toJson();


        } catch (Exception e) {


            String msg = String.format("Uniprot %s not found in uniprot localdb 1", accession);
            log.warn(msg);
            //throw new RuntimeException(msg);
            try {
                responseUniprot = uniprotService2.getTable(organism, canonicalAccession);


            } catch (Exception e2) {
                String msg2 = String.format("Uniprot %s not found  in uniprot localdb 2", accession);
                log.warn(msg2);

            }
//        response = new Uniprot();

        }

        if (canonicalAccession != accession) {
            try {
                JSONObject fastaResult = fastaService.getTable(accession);
                String fastaSeq = (String) fastaResult.get("sequence");
                responseUniprot.remove("sequence");
                responseUniprot.put("sequence", fastaSeq);
                responseUniprot.remove("length");
                responseUniprot.put("length", fastaSeq.length());

            } catch (Exception e2) {
                String msg = String.format("Uniprot %s not found in uniprot localdb", accession);
                log.warn(msg);
            }
        }
        return responseUniprot;
        //System.out.print(response.toString());


    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

//    @RequestMapping(value = "api/uniproth2db/accession/{accession}", method = RequestMethod.GET)
//    @ResponseBody
//    public String getFromUniprotH2DBAccession(@PathVariable String accession) {
//        System.out.println(String.format("Get the uniprot information from uniprot with argument: %s", accession));
//        Uniprot response;
//        response = proteinRepositoryH2.findByAccession(accession);
//        return response.toString();
//    }

//    @RequestMapping(value = "api/uniproth2name/{name}", method = RequestMethod.GET)
//    @ResponseBody
//    public JSONObject getFromUniprotH2DBName(@PathVariable String name) {
//        System.out.println(String.format("Get the uniprot information from uniprot with argument: %s", name));
//        Uniprot response;
//        response = proteinRepositoryH2.findByName(name);
//        return response.toJson();
//    }

    @RequestMapping(value = "api/test/{notation}", method = RequestMethod.GET)
    public
    @ResponseBody
    String parseTest(@PathVariable String notation) {
        //log.info(notation);
        return notation;
    }
}
