package org.example.infrastructure.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.domain.entity.MetodoPagamentoApi; // Importante usar a classe com o campo 'cor'
import org.example.domain.repository.GastoRepositoryAPI;
import org.example.domain.repository.RepositoryFactory;

public class MetodosControllerAPI {
    @FXML private TextField txtNomeMetodo;
    @FXML private TextField txtVencimento;
    @FXML private ColorPicker cpCor; // NOVO COMPONENTE

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

            // CONVERSÃO DA COR: Pega a cor do ColorPicker e converte para Hexadecimal
            String corHex = "#" + cpCor.getValue().toString().substring(2, 8);

            // Cria o objeto passando a cor escolhida
            MetodoPagamentoApi novoMetodo = new MetodoPagamentoApi(0, nome, vencimento, corHex);

            // Salva no repositório (que enviará o JSON com o campo 'cor' para a API)
            repository.salvarMetodo(novoMetodo);

            exibirAlerta("Sucesso", "Método de pagamento salvo com sucesso!", Alert.AlertType.INFORMATION);

            Stage stage = (Stage) txtNomeMetodo.getScene().getWindow();
            stage.close();

        } catch (NumberFormatException e) {
            exibirAlerta("Erro", "O dia do vencimento deve ser um número.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            exibirAlerta("Erro", "Erro ao salvar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void exibirAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    @FXML
    private void abrirListaMetodos() {
        try {
            // Vamos criar esse arquivo FXML no próximo passo!
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/lista_metodos.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Gerenciar Métodos de Pagamento");
            stage.setScene(new Scene(loader.load()));

            // Opcional: Fecha a janelinha de cadastro atual para não acumular janelas abertas
            Stage stageAtual = (Stage) txtNomeMetodo.getScene().getWindow();
            stageAtual.close();

            stage.show();
        } catch (Exception e) {
            exibirAlerta("Erro", "Não foi possível abrir a lista de métodos: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}