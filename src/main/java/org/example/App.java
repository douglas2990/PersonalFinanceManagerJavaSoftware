package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.infrastructure.database.ConfigConnection;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // 1. FORÇA O DIALOG: Abre a janela do Windows Explorer para o usuário escolher a pasta do banco.
        // Se já tiver escolhido antes, ele apenas lê silenciosamente o config.properties.
        String caminhoBanco = ConfigConnection.getDatabasePath(stage);
        System.out.println("🚀 Banco de dados configurado em: " + caminhoBanco);

        // 2. Carrega o arquivo FXML da pasta resources normalmente
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/main_view.fxml"));

        // Se a sua tela principal for maximizada, o tamanho (400, 300) será ignorado automaticamente.
        Scene scene = new Scene(fxmlLoader.load(), 400, 300);
        stage.setTitle("CleanFinance - Oliveira");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}