package org.example.infrastructure.ui;


import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class MainController {
    @FXML
    private TextField txtDescricao;
    @FXML
    private TextField txtValor;

    @FXML
    protected void aoSalvar() {
        System.out.println("Botão clicado! Gasto: " + txtDescricao.getText());
        // Aqui depois chamaremos o nosso UseCase
    }
}
