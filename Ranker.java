import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

import static java.lang.Character.toUpperCase;

public class Ranker {
    List<Query> queries;

    public Ranker(List<Query> queryList) {
        queries = queryList;
    }


    public int getNumOfDocs() {
//        return Parser.doc_info.size();
        BufferedReader br;
        int answer = 0;
        String st = "";
        File file = new File(Model.getInstance().getSavePath() + "\\CorpusTotalNumOfDocs.txt");
        try {
            br = new BufferedReader(new FileReader(file));
            if ((st = br.readLine()) != null) {
                answer = Integer.parseInt(st);
            }
            br.close();
        } catch (Exception e) {

        }
        return answer;
    }

    public double getAvarageLen() {
        BufferedReader br;
        double answer = 0;
        String st = "";
        File file = new File(Model.getInstance().getSavePath() + "\\CorpusAvgDocLength.txt");
        try {
            br = new BufferedReader(new FileReader(file));
            if ((st = br.readLine()) != null) {
                answer = Double.parseDouble(st);
            }
            br.close();
        } catch (Exception e) {

        }
        return answer;
    }

    public HashMap<String, Integer> getAllSize() {
        return ReadFile.lens;
    }

    public LinkedList<Map<String, Integer>> loadPosting(Query q) {
        LinkedList<Map<String, Integer>> posts = new LinkedList<>();
        Map<String, Integer> terms = q.getQ_terms();
        for (String s : terms.keySet()) {
            int index = correctCellDictionary(s);
            String string;
            if (Dictionary.getDic()[index].containsKey(s)) {
                File file = new File(Model.getInstance().getSavePath() +"\\"+ toUpperCase(s.charAt(0)) + ".txt");
                try {
                    BufferedReader fr = new BufferedReader(new FileReader(file));
                    while ((string = fr.readLine()) != null) {
                        if (string.startsWith(s)) {
                            String[] words = string.split("#");
                            if (words[0].equals(s)) {
                                HashMap<String, Integer> postingList = new HashMap<>();
                                String[] docs = words[3].split(",");
                                for(int k=0; k< docs.length;k++){
                                    while(docs[k].charAt(0) == ' '){
                                        docs[k] = docs[k].substring(1);
                                    }
                                    String[] aux = docs[k].split("=");
                                    if(aux[0].contains("{")|| aux[0].contains(" ")){
                                        aux[0] = aux[0].substring(1);
                                    }
                                    if(aux[1].contains("}")){
                                        aux[1] = aux[1].substring(0,aux[1].length()-1);
                                    }
                                    postingList.put(aux[0], Integer.parseInt(aux[1]));
                                }
                                postingList.put(s, -1);
                                posts.add(postingList);
                            }
                        }
                    }
                    fr.close();
                } catch (Exception e) {

                }

            }
        }
        return posts;
    }

    public HashMap<String, Double> BM25Algorithm(LinkedList<Map<String, Integer>> relaventPosting, double avarageLen, int numOfDocs, HashMap<String, Integer> docsLen) {
        HashMap<String, Double> docsAndValuesOfQuery = new HashMap<>();
        double k1 = 1.2;
        double b = 0.75;
        double IDF = 0;
        String term = "";
        for (Map<String, Integer> postsOfOneTerm : relaventPosting) {
            for (Map.Entry<String, Integer> entry : postsOfOneTerm.entrySet()) {
                if (entry.getValue() == -1) {
                    term = entry.getKey();
                    break;
                }
            }
            int k = correctCellDictionary(term);
            if (Dictionary.getDic()[k].containsKey(term)) {
                int DF = Dictionary.getDic()[k].get(term);
                IDF = Math.log((numOfDocs - DF + 0.5) / (DF + 0.5));
                for (String docNo : postsOfOneTerm.keySet()) {
                    if (postsOfOneTerm.get(docNo) != -1) {
                        int TF = postsOfOneTerm.get(docNo);
                        if (TF != -1) {
                            int DL = docsLen.get(docNo);
                            double docRelevance = IDF * ((TF * (k1 + 1)) / (TF + (k1 * (1 - b + (b * DL / avarageLen)))));
                            if (docsAndValuesOfQuery.containsKey(docNo)) {

                                docsAndValuesOfQuery.put(docNo, docsAndValuesOfQuery.get(docNo) + docRelevance);//update docrelevance because 2 terms of query existed on this doc.

                            } else docsAndValuesOfQuery.put(docNo, docRelevance);
                        }
                    }
                }
            }
        }
        return docsAndValuesOfQuery;
    }

    public LinkedList<String> getTop50(HashMap<String, Double> queriesResult) {
        LinkedList<String> res = new LinkedList<>();
        List<Map.Entry<String, Double>> top = findTop(queriesResult, 50);
        for (Map.Entry<String, Double> e : top) {
            res.add(e.getKey());
        }
        return res;
    }

    private static <String, Double extends Comparable<? super Double>> List<Map.Entry<String, Double>> findTop(HashMap<String, Double> queriesResult, int x) {
        Comparator<Map.Entry<String, Double>> comparator = new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                Double d1 = o1.getValue();
                Double d2 = o2.getValue();
                return d1.compareTo(d2);
            }
        };
        PriorityQueue<Map.Entry<String, Double>> priorityQueue = new PriorityQueue<Map.Entry<String, Double>>(x, comparator);
        for (Map.Entry<String, Double> e : queriesResult.entrySet()) {
            priorityQueue.offer(e);
            while (priorityQueue.size() > x) {
                priorityQueue.poll();
            }
        }

        List<Map.Entry<String, Double>> res = new ArrayList<Map.Entry<String, Double>>();
        while (priorityQueue.size() > 0) {
            res.add(priorityQueue.poll());
        }
        return res;
    }

    private int correctCellDictionary(String termToFind) {
        if (termToFind.charAt(0) >= 'a' && termToFind.charAt(0) <= 'z')
            return (int) termToFind.charAt(0) - 97;
        else if (termToFind.charAt(0) >= 'A' && termToFind.charAt(0) <= 'Z')
            return (int) termToFind.charAt(0) - 65;
        else
            return 26;


    }
}
