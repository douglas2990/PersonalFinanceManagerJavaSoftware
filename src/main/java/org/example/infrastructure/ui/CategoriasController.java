package org.example.infrastructure.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.domain.entity.Categoria;
import org.example.infrastructure.database.SqliteGastoRepository;



public class CategoriasController {
    @FXML
    private TextField txtNomeCategoria;
    private final SqliteGastoRepository repository = new SqliteGastoRepository();

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
}
