import java.io.*;
import java.util.HashMap;

public class Dictionary {

    private String savePath;
    private static HashMap<String, Integer>[] dic;

    public Dictionary(String savePath){
        dic = new HashMap[27];
        for (int i = 0; i < dic.length; i++)
            dic[i] = new HashMap<>();
        this.savePath = savePath;
    }

    public void load() throws IOException {
        String path = savePath+ "\\Dictionary.txt";
        File fromFile = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(fromFile));
        String st;
        String[] words;
        String term = "";
        int DF = 0;
        while ((st = br.readLine()) != null) {
            term = "";
            words = st.split(" ");
            int i;
            for (i = 0; i < words.length && !words[i].equals("totalTF"); i++) {
                term += (words[i] + " ");

            }
            term = term.substring(0, term.length() - 1);
            DF = Integer.parseInt(words[i + 1]);

            int location = correctCellDictionary(term);
            dic[location].put(term, DF);

        }//while
        br.close();
    }
    private int correctCellDictionary(String termToFind) {

        if (termToFind.charAt(0) >= 'a' && termToFind.charAt(0) <= 'z')
            return (int) termToFind.charAt(0) - 97;
        else if (termToFind.charAt(0) >= 'A' && termToFind.charAt(0) <= 'Z')
            return (int) termToFind.charAt(0) - 65;
        else
            return 26;
    }

    public static HashMap<String,Integer>[] getDic(){
        return dic;
    }
}
