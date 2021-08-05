import java.util.HashMap;

public class TermIndex {
    public String term;
    public int NumOfDocs = 0;
    public Integer frequency = 0;//to all the corpus
    public HashMap<String,Integer> fts2 = new HashMap<>();

    public TermIndex(){}

    public TermIndex(String term,int numOfDocs, int frequency){
        this.term = term;
        this.NumOfDocs = numOfDocs;
        this.frequency = frequency;
    }

    public void addOne(){
        this.NumOfDocs++;
        this.frequency++;
    }
    public void addFrequency(){
        this.frequency++;
    }
}
