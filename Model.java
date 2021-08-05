import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

//import org.json.JSONArray;

public class Model {

    private String savePath="";
    private String workPath="";
    private boolean stemmimng = false;

    private static Model singleton = null;
    private ReadFile readFile = new ReadFile("", false);
    private Searcher searcher;
    LinkedList<String>[] fiftyRelevantDocs;
    private Model() {
    }

    public static Model getInstance() {
        if (singleton == null)
            singleton = new Model();
        return singleton;

    }

    public String getSavePath() {
        return this.savePath;
    }

    public String getWorkPath() {
        return this.workPath;
    }

    public Boolean getStemmimng() {
        return this.stemmimng;
    }

    public void parse(String workPath, String savePath, boolean checkbox_value) throws IOException {
        this.savePath = savePath;
        this.workPath = workPath;
        this.stemmimng = checkbox_value;
        //read corpus files from folder
        this.readFile = new ReadFile(workPath,savePath, checkbox_value);
        readFile.listFiles(workPath,savePath);

    }
    public void resetButton(String savePath) throws IOException {
        this.savePath = savePath;
        this.workPath = "";
        this.stemmimng = false;
        File directory = new File(savePath);
        if (!directory.exists())
            throw new IOException("bad path");
        deleteDirectory(directory, savePath);

        //readFile.indexer.cleardic();
    }

    public int getNumOfFDocs(){
        return readFile.parser.doc_info.size();
    }


    public int getNumOfTerms(){
        return readFile.term_count;
    }

    static public boolean deleteDirectory(File path, String savePath) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i], savePath);
                } else {
                    files[i].delete();
                }
            }
        }
        if (!path.getPath().equals(savePath))
            return (path.delete());
        return true;
    }
    public void showDic(String savePath, Boolean checkbox_stemming) throws IOException {
        this.savePath = savePath;
        this.stemmimng = checkbox_stemming;
        File fromFile;
        if (this.stemmimng == false)
            fromFile = new File(savePath + "\\Dictionary.txt");
        else
            fromFile = new File(savePath + "\\Dictionary.txt");
        Desktop.getDesktop().open(fromFile);

    }
    public void loadDic(String savePath, boolean checkbox_stemming) throws IOException {
        this.stemmimng = checkbox_stemming;
        this.savePath = savePath;
        Dictionary loadedDictionary = new Dictionary(savePath);
        loadedDictionary.load();
    }

    public void runQueryFile(String queryText, String workPath, String savePath, boolean checkbox_semantic, boolean checkbox_value, boolean tosave, String savefolder) throws IOException, JSONException {
        File queryFile = new File(queryText);
        if (!queryFile.exists())
            throw new IOException();
        if (tosave == true) {
            if (savefolder.length() < 1)
                throw new IOException();
        }
        ArrayList<Query> queriesToRanker = Query.ParseQueryFile(queryFile,workPath,savePath, false);

        runQuery(workPath, savePath, checkbox_semantic, checkbox_value, tosave, savefolder, queriesToRanker);
    }


    public void runQueryString(String queryText, String workPath, String savePath, boolean checkbox_semantic, boolean checkbox_value, boolean tosave, String savefolder) throws IOException, JSONException {
        ArrayList<Query> queriesToRanker = new ArrayList<Query>();
        Query q = new Query(11111, queryText, "", "");
        String[] querytext = queryText.split(" ");
        HashMap<String, Integer> terms = new HashMap<>();
        for (int i = 0; i < querytext.length; i++) {
            terms.put(querytext[i], 1);
        }
        q.setQ_terms(terms);
        queriesToRanker.add(q);
        runQuery(workPath, savePath, checkbox_semantic, checkbox_value, tosave, savefolder, queriesToRanker);
    }

    public void runQuery(String workPath, String savePath, boolean checkbox_semantic, boolean checkbox_value, boolean tosave, String savefolder, ArrayList<Query>queriesToRanker) throws IOException, JSONException {
        Searcher s = new Searcher(workPath, savePath, checkbox_value);
        this.savePath = savePath;
        this.workPath = workPath;
        this.stemmimng = checkbox_value;
        this.searcher = s;
        if(checkbox_semantic){
            for (Query q:queriesToRanker) {
                LinkedList<String> temp = new LinkedList<>();
                HashMap<String,Integer> terms_in_query = new HashMap<>();
                terms_in_query = q.q_terms;
                for (Map.Entry<String,Integer> e:terms_in_query.entrySet()) {
                    LinkedList<String> similars = findSim(e.getKey());
                    for (String word:similars) {
                        temp.add(word);
                    }
                }
                for (String string:temp) {
                    terms_in_query.put(string,1);
                }
                q.setQ_terms(terms_in_query);
            }

        }
        Ranker ranker = new Ranker(queriesToRanker);
        int numOfDocs = ranker.getNumOfDocs();
        double avarageLen = ranker.getAvarageLen();
        HashMap<String,Integer> docsLen = ranker.getAllSize();
        HashMap<String,Double>[] queriesResults = new HashMap[queriesToRanker.size()];
        fiftyRelevantDocs = new LinkedList[queriesToRanker.size()];
        int i=0;
        File f2 = new File(savePath + "\\Queryresults.txt");
        BufferedWriter bw2 = null;
        bw2 = new BufferedWriter(new FileWriter(f2));
        for (Query q:queriesToRanker) {
            LinkedList<Map<String, Integer>>relaventPosting = ranker.loadPosting(q);
            queriesResults[i] = ranker.BM25Algorithm(relaventPosting,avarageLen,numOfDocs,docsLen);
            bw2.write(q.query_num + "\tNumber of relevant Documents:"+ queriesResults[i].size()+"\n");
            for (String string:queriesResults[i].keySet()) {
                bw2.write(string+"\n");
            }
            fiftyRelevantDocs[i] = ranker.getTop50(queriesResults[i]);
            q.results = fiftyRelevantDocs[i];
            i++;
        }
        bw2.close();
        if (tosave == true) {
            File f = new File(savefolder + "\\results.txt");
            BufferedWriter bw = null;
            bw = new BufferedWriter(new FileWriter(f));
            for (int j = 0; j < fiftyRelevantDocs.length; j++) {
                int queryNum = queriesToRanker.get(j).getQuery_num();
                for (String docNo : fiftyRelevantDocs[j]) {
                    bw.write(queryNum + "\t" + "0\t" + docNo + "\t 1 \t 42.38 \t mt");
                    bw.newLine();
                }

            }
            bw.close();

        }

    }
    public LinkedList<String> findSim(String key) throws JSONException {
        LinkedList<String> res = new LinkedList<>();
        DatamuseQuery datamuseQuery = new DatamuseQuery();
        String simWords = datamuseQuery.findSimilar(key);
        JSONArray array = new JSONArray(simWords);
        for(int i=0; i< array.length();i++){
            JSONObject jsonObject = array.getJSONObject(i);
            String s = jsonObject.getString("word");
            res.add(s);
        }
        return res;

    }

    public void showq(String savePath, Boolean checkbox_stemming) throws IOException {
        this.savePath = savePath;
        this.stemmimng = checkbox_stemming;
        File fromFile;
        if (this.stemmimng == false)
            fromFile = new File(savePath + "\\Queryresults.txt");
        else
            fromFile = new File(savePath + "\\Queryresults.txt");
        Desktop.getDesktop().open(fromFile);

    }
    public LinkedList<String>[] show_results() throws IOException {
      return fiftyRelevantDocs;
    }
}
