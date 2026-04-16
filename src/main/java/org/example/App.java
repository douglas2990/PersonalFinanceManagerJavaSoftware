package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Carrega o arquivo FXML da pasta resources
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/main_view.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 400, 300);
        stage.setTitle("CleanFinance - Oliveira");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
