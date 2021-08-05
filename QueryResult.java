import javafx.scene.control.Alert;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class QueryResult {
    public ListView<String> list = new ListView<String>();

    private Controller controller = Controller.getInstance();


    public void initialize(URL url, ResourceBundle resourceBundle) {
        start();
    }

    /**
     * @param title      - text to put in alert title
     * @param headerText - text to put in alert body
     *                   function to create alerts
     */
    public void doAlert(String title, String headerText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.showAndWait();
    }

    /**
     * function to create list view of query's and what docs they have
     */

    public void start() {
        try {
            //get results in string
            List<String>[] Qresult = controller.show_results();
            //make sure its not empty
            for (List<String>l:Qresult) {


                if (Qresult.length == 1) {
                   // String[] words = l.get(0).split("\\s");
                   // String lastowrd = words[0];
                    String ConnectedDocs = "";
                    //do it nicly with Q-->doc|doc|doc
                    for (int i = 0; i < l.size(); i++) {
                        String[] words = l.get(i).split("\\s");
                        ConnectedDocs += words[0] + "\n";
                    }
                    list.getItems().add(ConnectedDocs);
                }
            }
            } catch (IOException e) {
                doAlert("Error", "Problem O.O");
            }
    }

}
