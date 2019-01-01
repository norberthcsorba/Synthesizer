import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import ui.controllers.MainController;

import java.io.IOException;

public class Main extends Application {

    public void start(Stage primaryStage) throws Exception {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/layout/main_layout.fxml"));
            Pane root = fxmlLoader.load();
            Scene scene = new Scene(root);
            MainController controller = (MainController) fxmlLoader.getController();
            controller.setCrtScene(scene);
            primaryStage.setScene(scene);
            primaryStage.show();

            primaryStage.setOnHiding(event -> controller.cleanUp());

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}