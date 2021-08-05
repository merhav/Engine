import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Query {

    public int query_num;
    public String title;
    public String desc;
    public String narrative;
    HashMap<String,Integer>q_terms;
    LinkedList<String>results;


    public Query(int query_num, String title, String desc, String narrative){
        this.query_num = query_num;
        this.title = title;
        this.desc = desc;
        this.narrative = narrative;
        this.q_terms = new HashMap<String, Integer>();
    }

    public static ArrayList<Query> ParseQueryFile(File queryFile, String workpath, String savepath, boolean b) throws IOException {
        Searcher searcher = new Searcher(workpath,savepath,b);
        String line ="";
        String toParse = "";
        String description="";
        String narrative="";
        int num=0;
        FileReader fr = new FileReader(queryFile);
        ArrayList<Query> queries = new ArrayList<Query>();
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(fr);
        while((line=bufferedReader.readLine())!= null){
            if(line.equals("<top>")){
                stringBuilder = new StringBuilder();
            }
            else if(line.startsWith("<title>")){
                toParse = line.substring(8);
            }
            else if(line.startsWith("<num")){
                String[] aux = line.split(" ");
                num = Integer.parseInt(aux[aux.length-1]);
            }
            else if(line.startsWith("<desc>")){
                line = bufferedReader.readLine();
                while(!line.startsWith("<narr>")){
                    description = description+line;
                    line = bufferedReader.readLine();
                }
            }
            else if(line.equals("</top>")){
                Query q = new Query(num, toParse, description, narrative);
                q = searcher.parse(q, b);
                queries.add(q);
            }
        }
        bufferedReader.close();
        return queries;
    }

    public HashMap<String,Integer> getQ_terms(){
        return q_terms;
    }
    public int getQuery_num(){
        return this.query_num;
    }

    public String getTitle(){
        return this.title;
    }

//    public String getDesc(){
//        return this.desc;
//    }
//
//    public String getNarrative(){
//        return this.narrative;
//    }

    public void setQ_terms(HashMap<String,Integer> terms){
        this.q_terms = terms;
    }

//    public void setQuery_num(int num){
//        this.query_num = num;
//    }
//
//    public void setTitle(String title){
//        this.title = title;
//    }
//
//    public void setDesc(String desc){
//        this.desc = desc;
//    }
//
//    public void setNarrative(String narrative){
//        this.narrative = narrative;
//    }
//
//    public void addTerm(Map<String,Integer> terms){
//        int a;
//        for (Map.Entry<String,Integer> e:terms.entrySet()) {
//            if(!q_terms.containsKey(e.getKey())){
//                q_terms.put(e.getKey(),1);
//            }
//            else {
//              a= terms.get(e.getKey());
//              a++;
//              terms.put(e.getKey(),a);
//            }
//        }

//    }

}
