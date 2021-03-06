package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import ui.controllers.MainController;

import java.io.IOException;

public class Main extends Application {

    public void start(Stage primaryStage){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/ui/layouts/layout1.fxml"));
            Pane root = fxmlLoader.load();
            Scene scene = new Scene(root);
            MainController controller = fxmlLoader.getController();
            controller.setCrtScene(scene);
            primaryStage.setScene(scene);
            primaryStage.show();

            primaryStage.setResizable(false);
            primaryStage.setMaxWidth(1250);
            primaryStage.setMaxHeight(650);
            primaryStage.setOnHiding(event -> controller.cleanUp());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}