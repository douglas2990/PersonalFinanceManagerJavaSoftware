package org.example.infrastructure.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.domain.entity.Categoria;

import org.example.domain.repository.GastoRepositoryAPI;
import org.example.domain.repository.RepositoryFactory;


public class CategoriasControllerAPI  {
    @FXML
    private TextField txtNomeCategoria;
    private final GastoRepositoryAPI repository = RepositoryFactory.getRepository();

    @FXML
    private void aoSalvar() {
        String nome = txtNomeCategoria.getText();
        if (!nome.isEmpty()) {
            Categoria novaCategoria = new org.example.domain.entity.Categoria(nome);

            repository.salvarCategoria(novaCategoria); // Agora o tipo bate com o Repository

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Categoria salva!");
            alert.showAndWait();
            ((Stage) txtNomeCategoria.getScene().getWindow()).close();
        }
    }

    @FXML
    private void abrirListaCategorias() {
        try {
            // Vamos criar esse arquivo FXML no próximo passo!
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lista_categorias.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Gerenciar Categorias");
            stage.setScene(new Scene(loader.load()));

            // Supondo que seu TextField se chame txtNomeCategoria
            Stage stageAtual = (Stage) txtNomeCategoria.getScene().getWindow();
            stageAtual.close();

            stage.show();
        } catch (Exception e) {
            // Supondo que você tenha o método exibirAlerta nesta classe também
            exibirAlerta("Erro", "Não foi possível abrir a lista de categorias: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void exibirAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}

