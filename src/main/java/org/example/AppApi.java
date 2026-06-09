package org.example;



import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class AppApi extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Aqui não precisamos do ConfigConnection,
        // pois a API não usa o caminho do arquivo SQLite local.
        System.out.println("🚀 Iniciando aplicação modo API...");

        // Carrega o FXML da sua tela principal "API"
        // Certifique-se de que o Controller no FXML seja o MainControllerAPI
        FXMLLoader fxmlLoader = new FXMLLoader(AppApi.class.getResource("/main_view_api.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("CleanFinance - Modo API");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
