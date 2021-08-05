import java.io.*;
import java.util.*;

public class Indexer {


    public TreeMap<String, TermIndex> sorted = new TreeMap<String, TermIndex>();
    public String save_Path;

    public Indexer(String save_Path) {
        this.save_Path = save_Path;
    }

    public File buildTermIndex(HashMap<String, TermIndex> terms, String folder_path, String name, int i) throws IOException {
        //int i = 1;

        File out = new File(folder_path + "\\" + name + "_" + i + ".txt");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        StringBuilder stringBuilder = new StringBuilder();
        int lineNum = 1;
        sorted = sort(terms);
        for (Map.Entry<String, TermIndex> termIndexEntry : sorted.entrySet()) {
            stringBuilder.append(termIndexEntry.getKey() + "#" + termIndexEntry.getValue().NumOfDocs + "#" + termIndexEntry.getValue().frequency + "#" + termIndexEntry.getValue().fts2 + "\n");
        }
        bw.write(stringBuilder.toString());
        //bw.newLine();

        bw.close();
        //writeDicToDisk(save_Path, terms);
        sorted.clear();
        return out;

    }

    private class SortIgnoreCase implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            if (s1.toLowerCase().compareTo(s2.toLowerCase()) == 0 && !s1.equals(s2)) {
                if (Character.isUpperCase(s1.charAt(0)))
                    return 3;
                else
                    return -2;
            }
            int compare = s1.toLowerCase().compareTo(s2.toLowerCase());
            return compare;
        }
    }

    public TreeMap<String, TermIndex> sort(HashMap<String, TermIndex> terms) {
        TreeMap<String, TermIndex> sortedMap = new TreeMap<String, TermIndex>(new SortIgnoreCase()/*String.CASE_INSENSITIVE_ORDER*/);
        sortedMap.putAll(terms);
        return sortedMap;

    }

    public void merge(String path) throws IOException {
        String filename;
        filename = path + "\\Dictionary.txt";
        File folder = new File(path);
        File[] files = folder.listFiles();
        char[] alfabet = new char[36];
        int j = 0;
        int i = 0;
        String line = "";
        for (i = 65; i < 91; i++) {
            alfabet[j] = (char) i;
            j++;
        }
        i = 0;
        while (j < 36) {
            alfabet[j] = (char) (i + '0');
            i++;
            j++;
        }

        StringBuilder sb = new StringBuilder();
        HashMap<String, TermIndex> aux = new HashMap<String, TermIndex>();
        TermIndex temp = new TermIndex();
        for (char letter : alfabet) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path + "\\" + letter + ".txt"));

            for (File f : files) {

                BufferedReader reader = new BufferedReader(new FileReader(f));
                line = reader.readLine();
                while (line.charAt(0) != letter) {
                    line = reader.readLine();
                }
                //sb.append(line);
                while (line != null && (line.charAt(0) == letter || line.charAt(0) == Character.toLowerCase(letter))) {
                    TermIndex ti = extractTerm(line);

                    if (aux.containsKey(ti.term)) {
                        temp = aux.get(ti.term);
                        temp.NumOfDocs += ti.NumOfDocs;
                        temp.frequency += ti.frequency;
                        temp.fts2.putAll(ti.fts2);
                        aux.remove(ti.term);
                        aux.put(temp.term, temp);
                    }
                    else {
                        aux.put(ti.term, ti);
                    }
                    //sb.append(line+"\n");
                    line = reader.readLine();
                }
                for (Map.Entry<String, TermIndex> termIndexEntry : aux.entrySet()) {
                    sb.append(termIndexEntry.getKey() + "#" + termIndexEntry.getValue().NumOfDocs + "#" + termIndexEntry.getValue().frequency + "#" + termIndexEntry.getValue().fts2 + "\n");

                    if(sb.length()%1000 == 0){
                       bw.write(sb.toString());
                       sb.setLength(0);
                    }
                }

            }

            bw.write(sb.toString());
            sb.setLength(0);
            writeDicToDisk(path,aux,filename);
            writeNumberOfDocs(save_Path);
            writeAvgDocsSize(save_Path);
            aux.clear();
        }
        System.out.println("Merging-Done !");
    }

//    public boolean IsNumeric(char c) {
//        if (c == 1 || c == 2 || c == 3 || c == 4 || c == 5 || c == 6 || c == 7 || c == 8 || c == 9 || c == 0) {
//            return true;
//        }
//        return false;
//    }

//    public File mergeFiles(String path_1, File[] files, int i) throws IOException {
//        File fromFile;
//        File out;
//        BufferedReader br = new BufferedReader(new FileReader(path_1 + "\\out_" + i + ".txt"));
//        HashMap<String, TermIndex> dictionary1 = new HashMap();
//        for (File file : files) {
//
//            fromFile = new File(file.getAbsolutePath());
//            br = new BufferedReader(new FileReader(fromFile));
//            String st;
//            String[] wordLine;
//            String term;
//            int numofDocs;
//            int frequency;
//
//            //String termLocation;
//
//            while ((st = br.readLine()) != null) {
//                wordLine = st.split("#");
//                term = wordLine[0];
//                try {
//                    numofDocs = Integer.parseInt(wordLine[1]);
//                    frequency = Integer.parseInt(wordLine[2]);
//                    TermIndex ti = new TermIndex(term, numofDocs, frequency);
//                    ti.fts2 = getList(wordLine[3]);
//                    if (dictionary1.containsKey(term)) {
//                        for (Map.Entry<String, Integer> docNum : ti.fts2.entrySet()
//                        ) {
//                            if (dictionary1.get(term).fts2.containsKey(docNum.getKey())) {
//                                dictionary1.get(term).frequency++;
//                            } else {
//                                dictionary1.get(term).NumOfDocs++;
//                                dictionary1.get(term).fts2.put(docNum.getKey(), dictionary1.get(term).frequency);
//                            }
//                        }
//                    } else {
//                        dictionary1.put(term, ti);
//                    }
//                } catch (Exception e) {
//
//                }
//            }
//        }

//        //write the merged to the disk
//        out = buildTermIndex(dictionary1, path_1, "merged", i);
//        return out;
//
//    }


    public LinkedHashMap<String, Integer> getList(String s) {
        LinkedHashMap<String, Integer> ans = new LinkedHashMap<String, Integer>();
        String[] tokens = s.split(",");
        for (int i = 0; i < tokens.length; i++) {
            String[] aux = tokens[i].split("=");
            if (aux[0].contains("{")) {
                aux[0] = aux[0].substring(1, aux[0].length());
            }
            String docnum = aux[0];
            if (aux.length < 2) {
                continue;
            }
            if (aux[1].contains("}")) {
                aux[1] = aux[1].substring(0, 1);
            }
            Integer x = Integer.parseInt(aux[1]);
            ans.put(docnum, x);
        }
        return ans;
    }

    public TermIndex extractTerm(String s) {

        String[] wordLine;
        String term;
        int numofDocs;
        int frequency;
        wordLine = s.split("#");
        term = wordLine[0];
        if (wordLine[1].equals("")) {
            numofDocs = 1;
        } else {
            numofDocs = Integer.parseInt(wordLine[1]);
        }
        if (wordLine[1].equals("")) {
            frequency = 1;
        } else {
            frequency = Integer.parseInt(wordLine[2]);
        }
        TermIndex ti = new TermIndex(term, numofDocs, frequency);
        ti.fts2 = getList(wordLine[3]);
        return ti;
    }


    private void writeDicToDisk(String savePath, HashMap<String, TermIndex> terms,String filename) {
        TreeMap<String, Integer> sorted = new TreeMap<String, Integer>();
        for (Map.Entry<String, TermIndex> entey : terms.entrySet()) {
            TermIndex ti = entey.getValue();
            String t = entey.getKey();
            Integer freq = ti.frequency;
            sorted.put(t, freq);
        }
        File file;
        FileWriter fw;
        BufferedWriter bw;


        file = new File(filename);
        try {
            fw = new FileWriter(file,true);
            bw = new BufferedWriter(fw);
            for (Map.Entry<String, Integer> entry : sorted.entrySet()) {
                String updateTermOnDic = entry.getKey();
                //int df = entry.getValue();
                int totalOccurences = entry.getValue();
                bw.write(updateTermOnDic + " totalTF " + totalOccurences);

                bw.newLine();

            }
            bw.close();
            fw.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void writeNumberOfDocs(String savePath) {

        File file;
        FileWriter fw;
        BufferedWriter bw;
        file = new File(savePath + "\\CorpusTotalNumOfDocs.txt");
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            int number = Parser.getNumOfDocs();
            String write = "" + number;
            bw.write(write);
            bw.close();
            fw.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void writeAvgDocsSize(String savePath) {
        double sum = 0;
        for (Map.Entry<String, Integer> entry : ReadFile.lens.entrySet()) {
            sum += entry.getValue();
        }
        File file;
        FileWriter fw;
        BufferedWriter bw;
        file = new File(savePath + "\\CorpusAvgDocLength.txt");
        try {
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            sum = sum / Parser.getNumOfDocs();
            String writeMe = "" + sum;
            bw.write(writeMe);
            bw.close();
            fw.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
