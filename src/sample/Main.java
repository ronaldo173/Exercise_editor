package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        File file = new File("resources/main2.png");
        Image image = new Image(new FileInputStream(file));
        primaryStage.getIcons().add(image);
        primaryStage.setTitle("Exercise editor");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }
}
