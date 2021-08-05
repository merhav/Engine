//import jdk.nashorn.internal.runtime.CodeStore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class ReadFile  {

    public Parser parser;
    public Indexer index;
    public int term_count;
    public static HashMap<String,Integer> lens= new HashMap<>();
    //public HashMap<String,TermIndex> all_terms = new HashMap<String, TermIndex>();
    public ReadFile(String stop_words_path,String save_path,boolean stem){
        this.parser = new Parser(stop_words_path,stem);
        this.index = new Indexer(save_path);
        term_count=0;
    }

    public ReadFile(String save_path,boolean stem){
        this.parser = new Parser(stem);
        this.index = new Indexer(save_path);
    }

    public ReadFile(Parser p , Indexer index){
        this.parser = p ;
        this.index = index ;
    }
    public void listFiles(String folder,String savePath) throws IOException {
        //parser.setStopWords(folder);

        int len=0;
        int counter = 1;
        String st;
        String name="";
        String text = "";
        int numberOfFiles = 0 ;
        Documents allDocs = new Documents();
        File directory = new File(folder);
        File[] contents = directory.listFiles();
        for (File file : contents) {
            if(file.getPath().contains("05 stop_words.txt")){
                continue;
            }
            Documents docs = new Documents();
            File[] f = file.listFiles();
            //read the file
            File f1 = new File(f[0].toString());
            Scanner sc = new Scanner(f1);
            if (sc.hasNextLine()) {
                st = sc.nextLine();
                if (st.equals("<DOC>") || st.equals("")) {
                    sc.close();
                    sc = new Scanner(f1);
                    List<String> lines = new ArrayList<String>();
                    while (sc.hasNext()) {
                        lines.add(sc.nextLine());
                        len++;
                    }
                    sc.close();
                    BufferedWriter bw = new BufferedWriter(new FileWriter(f1));
                    bw.write("<ROOT>");
                    bw.newLine();
                    for (int i = 0; i < lines.size(); i++) {
                        String line = lines.get(i);
                        bw.write(line);
                        bw.newLine();
                    }
                    bw.write("</ROOT>");
                    bw.close();
                    sc = new Scanner(f1);
                    st = sc.nextLine();
                }
                while (!st.equals("</ROOT>")) {
                    //System.out.println(sc.nextLine());
                    st = sc.nextLine();
                    if (st.contains("<DOCNO>")) {
                        //String temp="";
                        for (int i = 0; i < st.length(); i++) {
                            if (!isUnique(st.charAt(i))) {
                                name += st.charAt(i);
                            }
                        }
                    }
                    if (st.equals("<TEXT>")) {
                        while (!st.equals("</TEXT>")) {
                            if (st.equals("</ROOT>")) {
                                break;
                            }
                            st = sc.nextLine();
                            len++;
                            if (st.contains("<")) {
                                continue;
                            }
                            if(st.equals("Article Type:BFN ")){
                                continue;
                            }
                            String textToadd = "";
                            StringTokenizer defaultTokenizer = new StringTokenizer(st);
                            while(defaultTokenizer.hasMoreTokens()) {
                                st = defaultTokenizer.nextToken();
                                len++;
                                String regex = "^[a-zA-Z0-9]+$";
                                Pattern pattern = Pattern.compile(regex);
                                Matcher matcher = pattern.matcher(st.substring(0,1));
                                if (matcher.matches() == false)
                                    st= st.substring(1);
                                matcher = pattern.matcher(st.substring(st.length()));
                                if (matcher.matches() == false)
                                    st= st.substring(0, st.length());

                                if (!parser.ifStopWord(st))
                                    textToadd = textToadd + st + " ";
                            }
                            text = text + textToadd;

                        }
                        if (!text.equals("")  ) {
                            docs.add(name, text);
                            lens.put(name,len);
                            name = "";
                            text = "";
                            len=0;
                        }
                    }
                }
                //System.out.println("hi");
            }
            // System.out.println("hi");

            allDocs.addTo(docs);
            numberOfFiles++;
            if(numberOfFiles%50==0) {
                //numberOfFiles=0;
                HashMap<String,TermIndex> partialTerms = new HashMap<String, TermIndex>();
                partialTerms = parser.createTerms(allDocs); //call the parser and get from the parser dictionary with term and tf
                term_count+=partialTerms.size();
                allDocs.All_Documents.clear();
                // index.createPostingFile(partialTerms);
                //addAll(all_terms,partialTerms);//update all_terms
                index.buildTermIndex(partialTerms,savePath,"out",counter);
                counter++;
                //delete the partial terms
                partialTerms.clear();
            }

            //write to a posting
        }
        HashMap<String,TermIndex> partialTerms = new HashMap<String, TermIndex>();
        partialTerms = parser.createTerms(allDocs); //call the parser and get from the parser dictionary with term and tf
        term_count+=partialTerms.size();
        allDocs.All_Documents.clear();
        // index.createPostingFile(partialTerms);
        //addAll(all_terms,partialTerms);//update all_terms
        index.buildTermIndex(partialTerms,savePath,"out",counter);
        counter++;
        //delete the partial terms

        partialTerms.clear();
        System.out.println("Partial Posting - Done !");

        //SortedSet<String> keySet = new TreeSet<>(partialTerms.keySet());
        //merging
        this.parser.all_terms.clear();
        index.merge(savePath);

        //return allDocs;
    }

//    private void addAll(HashMap<String,TermIndex> all_terms, HashMap<String,TermIndex> partialTerms) {
//        for (Map.Entry<String, TermIndex> e : new LinkedHashMap<String, TermIndex>(partialTerms).entrySet()){
//            all_terms.put(e.getKey(),e.getValue());
//        }
//    }

    public boolean isUnique(char c){
        if(c=='<' || c=='D' || c=='O' || c=='C' || c=='N' || c=='>' || c==' ' || c=='/'){
            return true;
        }
        return false;
    }


}
