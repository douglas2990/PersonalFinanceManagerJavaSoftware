package org.example.infrastructure.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.domain.entity.MetodoPagamento;
import org.example.infrastructure.database.SqliteGastoRepository;

public class MetodosController {
    @FXML private TextField txtNomeMetodo;
    @FXML private TextField txtVencimento;

    private final SqliteGastoRepository repository = new SqliteGastoRepository();

    @FXML
    private void aoSalvar() {
        try {
            String nome = txtNomeMetodo.getText();
            String vencimentoStr = txtVencimento.getText();

            if (nome.isEmpty() || vencimentoStr.isEmpty()) {
                exibirAlerta("Erro", "Por favor, preencha todos os campos.", Alert.AlertType.ERROR);
                return;
            }

            int vencimento = Integer.parseInt(vencimentoStr);
            MetodoPagamento novoMetodo = new MetodoPagamento(nome, vencimento);
            repository.salvarMetodo(novoMetodo);

            // Mensagem de sucesso (Tipo Toast/Snackbar)
            exibirAlerta("Sucesso", "Método de pagamento salvo com sucesso!", Alert.AlertType.INFORMATION);

            // Opcional: Fechar a janela após salvar
            Stage stage = (Stage) txtNomeMetodo.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            exibirAlerta("Erro", "O dia do vencimento deve ser um número.", Alert.AlertType.ERROR);
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
