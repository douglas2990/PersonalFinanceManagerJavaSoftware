package org.example.infrastructure.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.example.domain.entity.MetodoPagamento;
import org.example.infrastructure.database.SqliteGastoRepository;

public class MetodosController {
    @FXML private TextField txtNomeMetodo;
    @FXML private TextField txtVencimento;

    private final SqliteGastoRepository repository = new SqliteGastoRepository();

    @FXML
    private void aoSalvar() {
        String nome = txtNomeMetodo.getText();
        int vencimento = Integer.parseInt(txtVencimento.getText());

        MetodoPagamento novoMetodo = new MetodoPagamento(nome, vencimento);
        repository.salvarMetodo(novoMetodo);

        System.out.println("✅ Método salvo: " + nome);

        // Limpa os campos
        txtNomeMetodo.clear();
        txtVencimento.clear();
    }
}
