package edu.uc.eh.peptideMatch;

/**
 * Created by shamsabz on 9/20/17.
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;


import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.LimitTokenCountAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NGramPhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class PeptideMatchCMD {
    private static final String DEFAULT_DATA_BASE_DIR = "/opt/raid10/genomics/behrouz/PeptideMatchCMD_src_1.0";
    private static final String DEFAULT_INDEX_SUBDIR = "jan-30-2019";

    private static PeptideMatchCMD instance;

    private PeptideMatchCMD() {
    }

    static {
        instance = new PeptideMatchCMD();
    }

    public static String getDataBaseDir() {
        String configured = System.getProperty("peptide.data.base-dir");
        if (configured == null || configured.trim().isEmpty()) {
            configured = System.getenv("PEPTIDE_DATA_BASE_DIR");
        }
        if (configured == null || configured.trim().isEmpty()) {
            configured = DEFAULT_DATA_BASE_DIR;
        }
        return configured;
    }

    public static String getIndexBaseDir() {
        String configured = System.getProperty("peptide.index.base-dir");
        if (configured == null || configured.trim().isEmpty()) {
            configured = System.getenv("PEPTIDE_INDEX_BASE_DIR");
        }
        if (configured == null || configured.trim().isEmpty()) {
            configured = new File(getDataBaseDir(), DEFAULT_INDEX_SUBDIR).getPath();
        }
        return configured;
    }

    public static File getIndexFile(String indexDir) {
        return new File(getIndexBaseDir(), indexDir + "_index");
    }

    public static File getIncrementFile() {
        return new File(getDataBaseDir(), "increment.json");
    }


    public static void main(String[] args) {
        final int nGram = 3;
        String usage  = "java -jar PeptideMatchCMD_1.0.jar [options]\n";
        usage += "Available options:\n";
        usage += "------------------\n";

        // Create the command line parser
        CommandLineParser parser = new BasicParser();

        // Create the options
        Options options = new Options();
        options.addOption("h", "help", false, "Print this message.");
        options.addOption("a", "action", true, "The action to perform (\"index\" or \"query\").");
        options.addOption("e", "LeqI", false, "Treat Leucine (L) and Isoleucine (I) as equivalent (default: no).");
        options.addOption("i", "indexDir", true, "The directory where the index is stored.");
        options.addOption("f", "force", false, "Overwrite the indexDir (default: no).");
        options.addOption("d", "dataFile", true, "The path to a FASTA file to be indexed.");
        options.addOption("q", "query", true, "One peptide sequence or a comma-separated list of peptide sequences.");
        options.addOption("Q", "queryFile", true, "The path to the query peptide sequence file in either FASTA format or a list of peptide sequences, one sequence per line.");
        options.addOption("o", "outputFile", true, "The path to the query result file.");
        options.addOption("l", "list", false, "The query peptide sequence file is a list of peptide sequences, one sequence per line (default: no).");

        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();

        boolean hasCommandLineError = false;
        String action = null;
        boolean lEqi = false;
        boolean overwrite = false;
        String dataFile = null;
        String indexDir = null;
        String querySeqFile = null;
        String outputFile = null;
        ArrayList<Fasta> queries = null;
        String commandLineArguments = "Command line options: ";
        try {
            for (int i = 0; i < args.length; i++) {
                commandLineArguments += args[i] + " ";
            }
            //System.out.println(commandLineArguments);

            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            if (line.getOptions().length == 0 || line.hasOption("h")) {

                formatter.printHelp(usage, options);
            } else {
                if (line.hasOption('a')) {
                    action = line.getOptionValue("a");
                }
                if (line.hasOption('e')) {
                    lEqi = true;
                }
                if (line.hasOption('l')) {
                }
                if (line.hasOption('f')) {
                    overwrite = true;
                }
                if (line.hasOption('d')) {
                    dataFile = line.getOptionValue('d');
                }

                if (line.hasOption('i')) {
                    indexDir = line.getOptionValue('i');
                }

                if (line.hasOption('o')) {
                    outputFile = line.getOptionValue('o');
                }
                if (line.hasOption('q')) {

                    String queryLine = line.getOptionValue('q').replaceAll(" ", "");
                    String comma = ",";
                    String[] querySeqs = queryLine.split(comma);
                    queries = new ArrayList<Fasta>();
                    for (int i = 0; i < querySeqs.length; i++) {
                        queries.add(new Fasta(querySeqs[i], querySeqs[i]));
                    }
                    if (queries.size() == 0) {
                        System.err.println("No query. Should be one peptide sequence or a comma-separated list of peptide sequences.");
                    }
                }

                if (line.hasOption('Q')) {
                    querySeqFile = line.getOptionValue('Q');
                    if (!line.hasOption('l')) {
                        queries = getFromFastaFile(querySeqFile);
                    } else {
                        queries = getFromListFile(querySeqFile);
                    }

                }

                if (action == null) {
                    System.err.println("Command line parsing failed.  Reason: action (-a, --action) is required");
                    hasCommandLineError = true;
                } else if (action.equals("index")) {
                    if (indexDir == null) {
                        System.err.println("Command line parsing failed.  Reason: indexDir (-i, --indexDir) is required");
                        hasCommandLineError = true;
                    } else if (dataFile == null) {
                        System.err.println("Command line parsing failed.  Reason: dataFile (-d, --dataFile) is required");
                        hasCommandLineError = true;
                    } else if (queries != null) {
                        System.err.println("Command line parsing failed.  Reason: the action is \"index\", can not specify \"-q, --querySeqs or -Q, --querySeqFile\".");
                        hasCommandLineError = true;
                    } else {
                        createIndex(indexDir, overwrite, dataFile, nGram, true);
                    }
                } else if (action.equals("query")) {
                    if (indexDir == null) {
                        System.err.println("Command line parsing failed.  Reason: indexDir (-i, --indexDir) is required");
                        hasCommandLineError = true;
                    } else if (outputFile == null) {
                        System.err.println("Command line parsing failed.  Reason: outputFile (-o, --outputFile) is required.");
                        hasCommandLineError = true;
                    } else if (dataFile != null) {
                        System.err.println("Command line parsing failed.  Reason: the action is \"query\", can not specify \"-d --dataFile\".");
                        hasCommandLineError = true;
                    } else {
                        if (queries == null) {
                            System.err.println("Command line parsing failed.  Reason: no query specified");
                            hasCommandLineError = true;
                        } else {

                            PrintWriter printWriter;
                            try {
                                printWriter = new PrintWriter(outputFile);
                                printWriter.println("#" + commandLineArguments);
                                if (lEqi) {
                                    printWriter.println("##Query\tSubject\tSubjectLength\tMatchStart\tMatchEnd\tMatchedLEqIPositions");
                                } else {
                                    printWriter.println("##Query\tSubject\tSubjectLength\tMatchStart\tMatchEnd");
                                }
                                doQuery(indexDir, queries, nGram, printWriter, outputFile, lEqi);
                                printWriter.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            }
        } catch (ParseException exp) {
            System.err.println("Command line parsing failed.  Reason: " + exp.getMessage());
            hasCommandLineError = true;
        }

        if (hasCommandLineError) {
            formatter.setWidth(200);
            formatter.printHelp(usage, options);
        }

    }

    private static ArrayList<Fasta> getFromListFile(String file) {
        ArrayList<Fasta> queries = new ArrayList<Fasta>();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            String id = "";
            String seq = "";
            while ((line = br.readLine()) != null) {
                id = line;
                seq = line;
                queries.add(new Fasta(id, seq));
            }

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return queries;
    }

    @SuppressWarnings("resource")
    private static ArrayList<Fasta> getFromFastaFile(String file) {
        ArrayList<Fasta> queries = new ArrayList<Fasta>();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            String id = "";
            String seq = "";
            long index = 0;
            while ((line = br.readLine()) != null) {
                if (line.length() > 0) {
                    index++;
                    if (index == 1 && !line.startsWith(">")) {
                        System.err.println("\"" + file + "\" is not a valid Fasta file. Reason: first line must be started with \">\"\n");
                        return null;
                    }
                    if (line.startsWith(">")) {
                        if (!id.equals("") && !seq.equals("")) {
                            queries.add(new Fasta(id, seq));
                            id = "";
                            seq = "";
                        } else if (index > 1 && seq.equals("")) {
                            System.err.println("\"" + file + "\" is not a valid Fasta file. Reason: the sequence is missing for the id: " + id + "\n");
                            return null;
                        } else if (index > 1 && id.equals("")) {
                            System.err.println("\"" + file + "\" is not a valid Fasta file. Reason: the id is missing for the following sequences: \n" + seq + "\n");
                            return null;
                        }
                        String gt = "^>";
                        line = line.replaceAll(gt, "");
                        String space = "\\s+";
                        String[] defline = line.split(space);
                        id = defline[0];
                    } else {
                        if (line.length() > 0) {
                            seq += line + "\n";
                        }
                    }
                }
            }
            if (!id.equals("") && !seq.equals("")) {
                queries.add(new Fasta(id, seq));
            } else if (seq.equals("")) {
                System.err.println("\"" + file + "\" is not a valid Fasta file. Reason: the sequence is missing for the id: " + id + "\n");
                return null;
            } else if (id.equals("")) {
                System.err.println("\"" + file + "\" is not a valid Fasta file. Reason: the id is missing for the following sequences: \n" + seq + "\n");
                return null;
            }

            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return queries;
    }

    private static void createIndex(String indexDirPath, boolean overwrite, String dataFilePath, int nGram, boolean lEqi) {
        File indexDir = new File(indexDirPath);
        if (indexDir.exists()) {
            if (!overwrite) {
                System.err.println("\"" + indexDirPath + "\" alreday exists, please change to different directory or use \"-f, --force\" to overwrite.");
                System.exit(1);
            } else {
                try {
                    FileUtils.deleteDirectory(indexDir);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        File dataFile = new File(dataFilePath);
        if (!dataFile.exists()) {
            System.err.println("\"" + dataFilePath + "\" does not exist.");
            System.exit(1);
        }

        // call the NGramAnalyzer
        NGramAnalyzer nGramAnalyzer = new NGramAnalyzer(nGram);
        Map<String, Analyzer> analyzerPerField = new LinkedHashMap<String, Analyzer>();
        analyzerPerField.put("id", new StandardAnalyzer(Version.LUCENE_46));
        analyzerPerField.put("originalSeq", nGramAnalyzer);
        analyzerPerField.put("lToiSeq", nGramAnalyzer);

        PerFieldAnalyzerWrapper aWrapper = new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_46), analyzerPerField);

        LimitTokenCountAnalyzer analyzer = new LimitTokenCountAnalyzer(aWrapper, Integer.MAX_VALUE);

        try {
            Directory dir = FSDirectory.open(new File(indexDirPath));
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_46, analyzer);
            iwc.setOpenMode(OpenMode.CREATE);

            IndexWriter writer = new IndexWriter(dir, iwc);

            indexDataFile(writer, indexDir, dataFilePath, nGram, lEqi);

            writer.close();
            dir.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void indexDataFile(IndexWriter writer, File indexDir, String dataFilePath, int nGram, boolean lEqi) {
        long begin = System.currentTimeMillis();
        System.out.println("Indexing to directory \"" + indexDir.getAbsolutePath() + "\" ...");
        System.out.println("Indexing \"" + dataFilePath + "\" ...");

        ArrayList<Fasta> data = getFromFastaFile(dataFilePath);
        if (data == null) {

            try {
                writer.close();
                FileUtils.deleteDirectory(indexDir);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            for (int i = 0; i < data.size(); i++) {
                Fasta record = data.get(i);
                addDoc(writer, record.getId(), record.getSeq(), nGram, lEqi);
            }

            System.out.println("Indexing \"" + dataFilePath + "\" finished");
            long elapsed = System.currentTimeMillis() - begin;

            DateFormat df = new SimpleDateFormat("HH 'hours', mm 'mins,' ss.SSS 'seconds'");
            df.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            System.out.println("Time used: " + df.format(new Date(elapsed)));
        }
    }

    private static void addDoc(IndexWriter writer, String id, String seq, int nGram, boolean lEqi) {
        try {
            Document doc = new Document();
            seq = seq.replaceAll("\n", "");

            doc.add(new StringField("id", id, Field.Store.YES));
            doc.add(new TextField("originalSeq", seq.toLowerCase(), Field.Store.YES));
            if (lEqi) {
                doc.add(new TextField("lToiSeq", seq.toLowerCase().replaceAll("l", "i"), Field.Store.NO));
            }

            writer.addDocument(doc);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void doQuery(String indexDir, ArrayList<Fasta> queries, int nGram, PrintWriter printWriter, String outputFile, boolean lEqi) {
        System.out.println("Quering...\n");
        long begin = System.currentTimeMillis();
        IndexReader reader;
        TopScoreDocCollector collector = null;
        try {
            reader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
            IndexSearcher searcher = new IndexSearcher(reader);

            for (int i = 0; i < queries.size(); i++) {
                Fasta querySeq = queries.get(i);
                NGramPhraseQuery nGramPhraseQueryOptimizer = new NGramPhraseQuery(nGram);
                Query optimizedQuery = null;
                String queryStr = querySeq.getSeq().toLowerCase();

                queryStr = queryStr.replaceAll("\\r", "").replaceAll("\\n", "");
                String originalQueryStr = queryStr;

                if (lEqi) {
                    queryStr = queryStr.replaceAll("l", "i");
                }
                for (int j = 0; j <= queryStr.length() - nGram; j++) {
                    if (lEqi) {
                        nGramPhraseQueryOptimizer.add(new Term("lToiSeq", queryStr.substring(j, j + 3)));
                    } else {
                        nGramPhraseQueryOptimizer.add(new Term("originalSeq", queryStr.substring(j, j + 3)));
                    }
                }

                optimizedQuery = nGramPhraseQueryOptimizer;

                TotalHitCountCollector thcc = new TotalHitCountCollector();
                try {
                    searcher.search(optimizedQuery, thcc);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int totalHits = thcc.getTotalHits();

                if (totalHits > 0) {
                    collector = TopScoreDocCollector.create(totalHits, true);
                    try {
                        searcher.search(optimizedQuery, collector);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ScoreDoc[] hits = collector.topDocs().scoreDocs;

                    if (hits.length > 0) {
                        if (hits.length == 1) {
                            System.out.println(querySeq.getId() + "\t" + "has " + hits.length + " match");
                        } else {
                            System.out.println(querySeq.getId() + "\t" + "has " + hits.length + " matches");
                        }
                        writeMatchResults(printWriter, searcher, hits, querySeq.getId(), originalQueryStr, queryStr, lEqi);
                    } else {
                        System.out.println(querySeq.getId() + "\t" + "has no match");
                    }

                } else {
                    printWriter.println(querySeq.getId() + "\tNo match");
                    System.out.println(querySeq.getId() + "\t" + "has no match");
                }
            }
            System.out.println("\nQuery is finished.");
            System.out.println("The result is saved in \"" + outputFile+"\".");
            long elapsed = System.currentTimeMillis() - begin;

            DateFormat df = new SimpleDateFormat("HH 'hours', mm 'mins,' ss.SSS 'seconds'");
            df.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            System.out.println("Time used: " + df.format(new Date(elapsed)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static JSONObject doLocalQuery(String indexDir, ArrayList<Fasta> queries, int nGram, boolean lEqi) {
        System.out.println("Quering...\n");
        long begin = System.currentTimeMillis();
        IndexReader reader;
        TopScoreDocCollector collector = null;
        JSONObject peptideMatchJson = new JSONObject();
        JSONObject peptideMatchJsonArrayPlusMatch = new JSONObject();
        JSONArray peptideMatchJsonArray = new JSONArray();
        String index_file = getIndexFile(indexDir).getPath();
        //String index_file = "/Users/shamsabz/Documents/uniprot+peptideMatch/PeptideMatchCMD_src_1.0/jan-30-2019/" + indexDir + "_index";
        try {

            reader = DirectoryReader.open(FSDirectory.open(new File(index_file)));
            IndexSearcher searcher = new IndexSearcher(reader);

            for (int i = 0; i < queries.size(); i++) {
                peptideMatchJsonArrayPlusMatch = new JSONObject();
                Fasta querySeq = queries.get(i);
                NGramPhraseQuery nGramPhraseQueryOptimizer = new NGramPhraseQuery(nGram);
                Query optimizedQuery = null;
                String queryStr = querySeq.getSeq().toLowerCase();

                queryStr = queryStr.replaceAll("\\r", "").replaceAll("\\n", "");
                String originalQueryStr = queryStr;

                if (lEqi) {
                    queryStr = queryStr.replaceAll("l", "i");
                }
                for (int j = 0; j <= queryStr.length() - nGram; j++) {
                    if (lEqi) {
                        nGramPhraseQueryOptimizer.add(new Term("lToiSeq", queryStr.substring(j, j + 3)));
                    } else {
                        nGramPhraseQueryOptimizer.add(new Term("originalSeq", queryStr.substring(j, j + 3)));
                    }
                }

                optimizedQuery = nGramPhraseQueryOptimizer;

                TotalHitCountCollector thcc = new TotalHitCountCollector();
                try {
                    searcher.search(optimizedQuery, thcc);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int totalHits = thcc.getTotalHits();


                if (totalHits > 0) {
                    collector = TopScoreDocCollector.create(totalHits, true);
                    try {
                        searcher.search(optimizedQuery, collector);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ScoreDoc[] hits = collector.topDocs().scoreDocs;


                    if (hits.length > 0) {
//                        if (hits.length == 1) {
//                            System.out.println(querySeq.getId() + "\t" + "has " + hits.length + " match");
//                        } else {
//                            System.out.println(querySeq.getId() + "\t" + "has " + hits.length + " matches");
//                        }
                        //System.out.println(searcher + "\t" + hits + "\t"+ querySeq.getId() + "\t"+ originalQueryStr + "\t"+ queryStr + "\t");
                        peptideMatchJsonArray = writeMatchResultsJson(searcher, hits, querySeq.getId(), originalQueryStr, queryStr, lEqi);
                        peptideMatchJsonArrayPlusMatch.put("n_match", peptideMatchJsonArray.size());
                        peptideMatchJsonArrayPlusMatch.put("matchset", peptideMatchJsonArray);
                        peptideMatchJson.put(originalQueryStr.toUpperCase(),peptideMatchJsonArrayPlusMatch);
                    } else {
                        //System.out.println(querySeq.getId() + "\t" + "has no match - here 1");
                        JSONArray emptyList = new JSONArray();


                        peptideMatchJsonArrayPlusMatch.put("n_match", 0);
                        peptideMatchJsonArrayPlusMatch.put("matchset", emptyList);
                        peptideMatchJson.put(originalQueryStr.toUpperCase(),peptideMatchJsonArrayPlusMatch);
                    }

                } else {
             //       printWriter.println(querySeq.getId() + "\tNo match");
                    //System.out.println(querySeq.getId() + "\t" + "has no match - here 2");
                    JSONArray emptyList = new JSONArray();


                    peptideMatchJsonArrayPlusMatch.put("n_match", 0);
                    peptideMatchJsonArrayPlusMatch.put("matchset", emptyList);
                    peptideMatchJson.put(originalQueryStr.toUpperCase(),peptideMatchJsonArrayPlusMatch);
                }
            }
            System.out.println("\nQuery is finished.");
         //   System.out.println("The result is saved in \"" + outputFile+"\".");
            long elapsed = System.currentTimeMillis() - begin;

            DateFormat df = new SimpleDateFormat("HH 'hours', mm 'mins,' ss.SSS 'seconds'");
            df.setTimeZone(TimeZone.getTimeZone("GMT+0"));
            System.out.println("Time used: " + df.format(new Date(elapsed)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return peptideMatchJson;
    }


    private static JSONArray writeMatchResultsJson(IndexSearcher searcher, ScoreDoc[] hits, String id, String originalQueryStr, String queryStr, boolean lEqi) throws IOException {

        JSONArray matchJson = new JSONArray();
        for (int i = 0; i < hits.length; i++) {
            int docId = hits[i].doc;
            Document doc = searcher.doc(docId);

            String subjectId = doc.get("id");
            //System.out.println(subjectId);
            String originalSubject = doc.get("originalSeq");
            int subjectLength = originalSubject.length();
            MatchedRange[] matchedRanges = getMatchedRanges(originalQueryStr, originalSubject, lEqi);

            for (int j = 0; j < matchedRanges.length; j++) {
                JSONObject json = new JSONObject();

                int start = matchedRanges[j].getStart();
                int end = matchedRanges[j].getEnd();

                String lEqiPos = "";
                int[] replacedPositions = matchedRanges[j].getReplacedPos();
                if (replacedPositions != null && replacedPositions.length > 0) {
                    for (int k = 0; k < replacedPositions.length; k++) {
                        if (lEqiPos.equals("")) {
                            lEqiPos += replacedPositions[k];
                        } else {
                            lEqiPos += "," + replacedPositions[k];
                        }
                    }
                }

                String[] stringArray = subjectId.split("\\|");


                //This is for including only the swissProt "sp", not the Trembl "tr" proteins
//                if(stringArray[0].equals("sp"))
//                {
                    json.put("sequence_ac",stringArray[1]);
                    json.put("sequence_id",stringArray[2]);
                    json.put("sequence_db",stringArray[0]);
                    json.put("length",subjectLength);
                    json.put("start",start);
                    json.put("stop",end);
                    matchJson.add(json);
                //}


            }
        }
        return matchJson;
    }


    private static void writeMatchResults(PrintWriter printWriter, IndexSearcher searcher, ScoreDoc[] hits, String id, String originalQueryStr, String queryStr, boolean lEqi) throws IOException {
        for (int i = 0; i < hits.length; i++) {
            int docId = hits[i].doc;
            Document doc = searcher.doc(docId);
            String subjectId = doc.get("id");
            String originalSubject = doc.get("originalSeq");
            int subjectLength = originalSubject.length();
            MatchedRange[] matchedRanges = getMatchedRanges(originalQueryStr, originalSubject, lEqi);

            for (int j = 0; j < matchedRanges.length; j++) {
                int start = matchedRanges[j].getStart();
                int end = matchedRanges[j].getEnd();

                String lEqiPos = "";
                int[] replacedPositions = matchedRanges[j].getReplacedPos();
                if (replacedPositions != null && replacedPositions.length > 0) {
                    for (int k = 0; k < replacedPositions.length; k++) {
                        if (lEqiPos.equals("")) {
                            lEqiPos += replacedPositions[k];
                        } else {
                            lEqiPos += "," + replacedPositions[k];
                        }
                    }
                }
                printWriter.println(id + "\t" + subjectId + "\t" + subjectLength + "\t" + start + "\t" + end + "\t" + lEqiPos);
            }
        }

    }

    private static MatchedRange[] getMatchedRanges(String originalQueryPeptide, String originalSeq, boolean ilEquivalent) {
        String sequence = originalSeq;
        String queryPeptide = originalQueryPeptide;
        ArrayList<MatchedRange> matchedRangeList = new ArrayList<MatchedRange>();
        if (ilEquivalent) {
            sequence = sequence.replaceAll("l", "i");
            queryPeptide = queryPeptide.replaceAll("l", "i");
        }
        int seqLength = sequence.length();
        for (int i = 0; i <= seqLength - queryPeptide.length(); i++) {
            if (sequence.substring(i, i + queryPeptide.length()).toUpperCase().equals(queryPeptide.toUpperCase())) {
                MatchedRange matchRange = new MatchedRange((i + 1), i + queryPeptide.length());
                if (ilEquivalent) {
                    ArrayList<Integer> replacedPosList = new ArrayList<Integer>();
                    for (int j = i; j < i + queryPeptide.length(); j++) {
                        char originalChar = originalSeq.charAt(j);
                        char replacedChar = sequence.charAt(j);
                        if (originalChar != replacedChar && originalChar != originalQueryPeptide.charAt(j - i)) {
                            replacedPosList.add(new Integer(j + 1));
                        }
                    }

                    int[] replacedPos = new int[replacedPosList.size()];
                    for (int j = 0; j < replacedPosList.size(); j++)
                        replacedPos[j] = replacedPosList.get(j);

                    matchRange.setReplacedPos(replacedPos);
                }
                matchedRangeList.add(matchRange);
            }
        }
        MatchedRange[] matchedRanges = new MatchedRange[matchedRangeList.size()];
        matchedRangeList.toArray(matchedRanges);
        return matchedRanges;
    }
}
