package org.example.infrastructure.ui;


import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.domain.entity.MetodoPagamento;
// IMPORTANTE: Importar a interface e a fábrica
import org.example.domain.repository.GastoRepositoryAPI;
import org.example.domain.repository.RepositoryFactory;

public class MetodosControllerAPI {
    @FXML private TextField txtNomeMetodo;
    @FXML private TextField txtVencimento;

    // A MUDANÇA ESTÁ AQUI: Usa a interface, a Factory fornece a instância (API ou Adaptador SQLite)
    private final GastoRepositoryAPI repository = RepositoryFactory.getRepository();

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

            // O repositório agora gerencia a persistência (API ou SQLite via Adaptador)
            repository.salvarMetodo(novoMetodo);

            exibirAlerta("Sucesso", "Método de pagamento salvo com sucesso!", Alert.AlertType.INFORMATION);

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