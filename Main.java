import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) throws IOException {
        launch(args);

//         Parser p = new Parser("D:\\documents\\users\\itaymer\\Downloads\\corpus\\",false);
//        Indexer index = new Indexer("D:\\documents\\users\\itaymer\\Downloads\\corpus\\without\\out_1.txt");
//        // index.merge("D:\\documents\\users\\itaymer\\Downloads\\corpus\\without");
//        ReadFile rf = new ReadFile(p,index);
//       rf.listFiles("D:\\documents\\users\\itaymer\\Downloads\\corpus\\corpus","D:\\documents\\users\\itaymer\\Downloads\\corpus");

        //index.mergeFiles("C:\\Users\\Itaymer\\Downloads\\corpus",1);
    }

    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("MainPage.fxml"));
        primaryStage.setTitle("Search Engine");
        primaryStage.setScene(new Scene(root, 500, 500));
        primaryStage.setResizable(false);
        primaryStage.show();
        Controller controller = Controller.getInstance();
    }

}

