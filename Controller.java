import org.json.JSONException;

import java.io.IOException;
import java.util.LinkedList;

public class Controller {

    private static Model model;
    private static Controller singleton = null;

    private Controller() {
        this.model = Model.getInstance();
    }

    public static Controller getInstance() {
        if (singleton == null)
            singleton = new Controller();
        return singleton;
    }

    public static void parse(String workPath,String savePath, boolean stem) throws IOException {
        model.parse(workPath,savePath,stem);
        //return model;
    }
    public static void resetButton(String savePath) throws IOException {
        model.resetButton(savePath);
    }
    public static int getNumOfFDocs(){
        return model.getNumOfFDocs();
    }

    public static int getNumOfTerms(){
        return model.getNumOfTerms();
    }
    //public static int getNumOfDocs(Parser p){
    //return model.getNumOfDocs(p);
    //  }
    public static void show_dic(String savePath, Boolean checkbox_stemming) throws IOException {
        model.showDic(savePath, checkbox_stemming);
    }
    public static void load_dic(String savePath, boolean checkbox_stemming) throws IOException {
        model.loadDic(savePath, checkbox_stemming);
    }

    public static void runQueryFile(String queryText, String workPath, String savePath, Boolean checkbox_semantic, boolean checkbox_value, boolean tosave, String savefolder) throws IOException, JSONException {
        model.runQueryFile(queryText, workPath, savePath, checkbox_semantic, checkbox_value, tosave, savefolder);
    }

    public static void runQueryString(String queryText, String workPath, String savePath, boolean checkbox_semantic, boolean checkbox_stemming, boolean tosave, String saveFolder) throws IOException, JSONException {
        model.runQueryString(queryText, workPath, savePath, checkbox_semantic, checkbox_stemming, tosave, saveFolder);
    }
    public static void show_q(String savePath, Boolean checkbox_stemming) throws IOException {
        model.showq(savePath, checkbox_stemming);
    }
    public static LinkedList<String>[] show_results() throws IOException {
        return model.show_results();
    }

}