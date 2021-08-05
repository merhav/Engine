import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class Documents {

        public HashMap<String, String> All_Documents = new HashMap<String, String>() {
        };
        public void add(String key, String value){
            All_Documents.put(key,value);
        }

    public void addTo(Documents docs) {
        for (Map.Entry<String,String>doc:docs.All_Documents.entrySet()
             ) {
            this.All_Documents.put(doc.getKey(),doc.getValue());
        }
    }
}
