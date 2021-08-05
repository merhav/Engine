//import javafx.event.ActionEvent;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class GUI {


    //<editor-fold desc="buttons">
    public Button Browsew; //browse button to load working path
    public Button parseb; //parse button
    public Button resetb; //reset button
    public Button loadb; //load dictionary button
    public Button showQR;


    public CheckBox stemming_option; //stemming checkbox
    public CheckBox semantic_option; //semantic checkbox
    public TextField work_path; //text field for working path
    public TextField save_path;
    public TextField query_path; //query path
    public TextField query_text; //query text
    //public Menu menu;
    protected boolean checkbox_stemming = false; //start checkbox as false, if marked change to true
    public boolean checkbox_semantic = false; //start checkbox as false, if marked change to true

    public Model m;
    public QueryResult queryResult = new QueryResult();
    //</editor-fold>


    //checkbox for stemming, updates the boolean value accordingly
    public void checkBox_stemmimg(javafx.event.ActionEvent actionEvent) {
        checkbox_stemming = stemming_option.isSelected();
    }


    //parse function
    public void parse(javafx.event.ActionEvent actionEvent) {
        try {
            String workPath = work_path.getText();
            String savePath = save_path.getText();
            //if path left empty

            if (savePath.equals("") || workPath.equals("")) {
                doAlert("Error", "Please select a folder");
            } else {
                if (checkbox_stemming == false) {
                    new File(savePath + "\\without").mkdirs();
                    savePath = savePath + "\\without";
                } else {
                    new File(savePath + "\\with").mkdirs();
                    savePath = savePath + "\\with";
                }

                double t = System.currentTimeMillis();
                Controller.parse(workPath, savePath, checkbox_stemming);

                //Controller.writeLastDocsToDisk(savePath);
                //Controller.mergePartialPosting(workPath, savePath);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Done");

                alert.setHeaderText("total time to run program : " + ((System.currentTimeMillis() - t) / 1000) + " seconds. \n" +
                        "total number of parsed documents is : " + Controller.getNumOfFDocs() + " Docs. \n" + "total number of terms:" + Controller.getNumOfTerms() + "Terms");
                alert.showAndWait();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reset(javafx.event.ActionEvent actionEvent) {
        try {
            String savePath = save_path.getText();
            if (savePath.equals("")) {
                doAlert("Error", "Please select a folder in save and work path");
            } else {

                Controller.resetButton(savePath);
            }
        } catch (IOException e) {
            doAlert("Done", "Reset Succsefully");
        }
    }

    //update work path textfield
    public void Browse_working(javafx.event.ActionEvent actionEvent) {
        UpdateTextField(work_path);
    }

    //update save path textfield
    public void Browse_saving(javafx.event.ActionEvent actionEvent) {
        UpdateTextField(save_path);
    }

    //gets path from filechoser and updates the field text
    private void UpdateTextField(TextField Path) {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choose Folder");
            File selectedDirectory = chooser.showDialog(null);
            Path.setText(selectedDirectory.getPath());
        } catch (Exception e) {
            doAlert("Error", "Please select a folder");
        }
    }

    public void doAlert(String title, String headerText) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.showAndWait();
    }

    //show dictionary function
    public void show_dic(javafx.event.ActionEvent actionEvent) {
        try {
            String savePath = save_path.getText();
            if (savePath.equals("")) {
                doAlert("Error", "Save Path does not have Dictionary file");
            } else {
                if (checkbox_stemming == false) {
                    new File(savePath + "\\without").mkdirs();
                    savePath = savePath + "\\without";
                } else {
                    new File(savePath + "\\with").mkdirs();
                    savePath = savePath + "\\with";
                }
                Controller.show_dic(savePath, checkbox_stemming);
            }
        } catch (RuntimeException e) {
            doAlert("Fail", "Dictionary does not exist");
        } catch (IOException e) {
            doAlert("Fail", "Failed");

        }


    }

    //load dictionary
    public void load_dic(javafx.event.ActionEvent actionEvent) {
        if (AlertLoadDic() == true) {
            doAlert("Success", "Loaded dictionary");
        }
    }
    private boolean AlertLoadDic() {
        try {
            String savePath = this.save_path.getText();
            if (checkbox_stemming == false) {
                new File(savePath + "\\without").mkdirs();
                savePath = savePath + "\\without";
            } else {
                new File(savePath + "\\with").mkdirs();
                savePath = savePath + "\\with";
            }
            Controller.load_dic(savePath, checkbox_stemming);
            return true;

        } catch (IOException e) {
            doAlert("Error", e.getMessage());
            return false;

        }
    }

    public void Browse_query(javafx.event.ActionEvent actionEvent){
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choose Folder");
            File selectedDirectory = chooser.showOpenDialog(null);
            query_path.setText(selectedDirectory.getPath());
        } catch (Exception e) {
            doAlert("Error", "Please select a folder");
        }

    }
    public void run_query_file(javafx.event.ActionEvent actionEvent) throws IOException, JSONException {
        String q_text = query_path.getText();
        String save = save_path.getText();
        String workpath = work_path.getText();
        if(q_text.equals("")){
            doAlert("Error","Please fill the textfiled");
        }
        else {
            if (checkbox_stemming == false) {
                new File(save + "\\without").mkdirs();

                save = save + "\\without";
            } else {
                new File(save + "\\with").mkdirs();
                save = save + "\\with";
            }
            boolean tosave;
            String saveFolder = "";
            Alert alertConfirm = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to save the results?",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> result = alertConfirm.showAndWait();
            if (result.get() == ButtonType.YES) {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Choose Folder");
                File selectedDirectory = chooser.showDialog(null);
                saveFolder = (selectedDirectory.getPath());
                tosave = true;
            } else {
                tosave = false;
            }
            boolean check = AlertLoadDic();
            if (check == false)
                try {
                    throw new IOException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            Controller.runQueryFile(q_text, workpath, save, checkbox_semantic, checkbox_stemming, tosave, saveFolder);
            if (tosave == true) {
                doAlert("Done", "Query ran successfully.\n Open results.txt file to see them.");
            } else {
                doAlert("Done", "Done");
            }
        }
    }

    public void run_query(javafx.event.ActionEvent actionEvent) throws IOException, JSONException {
        String q_text = query_text.getText();
        String save = save_path.getText();
        String workpath = work_path.getText();
        if(q_text.equals("")){
            doAlert("Error","Please fill the textfiled");
        }
        else {
            if (checkbox_stemming == false) {
                new File(save_path + "\\without").mkdirs();

                save = save + "\\without";
            } else {
                new File(save_path + "\\with").mkdirs();
                save = save + "\\with";
            }
            boolean tosave;
            String saveFolder = "";
            Alert alertConfirm = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to save the results?",
                    ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> result = alertConfirm.showAndWait();
            if (result.get() == ButtonType.YES) {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Choose Folder");
                File selectedDirectory = chooser.showDialog(null);
                saveFolder = (selectedDirectory.getPath());
                tosave = true;
            } else {
                tosave = false;
            }
            boolean check = AlertLoadDic();
            if (check == false)
                try {
                    throw new IOException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            Controller.runQueryString(q_text, workpath, save, checkbox_semantic, checkbox_stemming, tosave, saveFolder);
            if (tosave == true) {
                doAlert("Done", "Query ran successfully.\n Open results.txt file to see them.");
            } else {
                doAlert("Done", "Done");
            }
        }
    }

    public void show_q(javafx.event.ActionEvent actionEvent) {
        try {
            String savePath = save_path.getText();
            if (savePath.equals("")) {
                doAlert("Error", "Save Path does not have Dictionary file");
            } else {
                if (checkbox_stemming == false) {
                    new File(savePath + "\\without").mkdirs();
                    savePath = savePath + "\\without";
                } else {
                    new File(savePath + "\\with").mkdirs();
                    savePath = savePath + "\\with";
                }
                Controller.show_q(savePath, checkbox_stemming);
            }
        } catch (RuntimeException e) {
            doAlert("Fail", "QueryResult does not exist");
        } catch (IOException e) {
            doAlert("Fail", "Failed");

        }


    }

    public void checkBox_semantics(javafx.event.ActionEvent actionEvent) {
        checkbox_semantic = semantic_option.isSelected();
//        if (checkbox_semantic == true) {
//            stemming_option.setSelected(true);
//            checkBox_stemmimg(actionEvent);
//        }
    }

    public void query_results(javafx.event.ActionEvent actionEvent) {
      //  Stage s = (Stage) showQR.getScene().getWindow();
//        s.close();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("QR.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.setTitle("Query Result");
            queryResult.start();
            HBox hbox = new HBox(queryResult.list);
            Scene scene = new Scene(hbox);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}