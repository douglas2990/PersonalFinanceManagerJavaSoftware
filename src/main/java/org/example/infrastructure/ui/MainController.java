package org.example.infrastructure.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.domain.entity.Gasto;
import org.example.domain.entity.MetodoPagamento;
import org.example.infrastructure.database.SqliteGastoRepository;
import org.example.usecase.GerenciarGastoUseCase;

import java.io.IOException;
import java.time.LocalDate;

public class MainController {
    @FXML private TextField txtDescricao, txtValor, txtQtdParcelas;
    @FXML private DatePicker dpData;
    @FXML private ComboBox<String> cbCategoria, cbMetodo;
    @FXML private CheckBox chkParcelado;
    @FXML private HBox containerParcelas;

    private final SqliteGastoRepository repository = new SqliteGastoRepository();
    private final GerenciarGastoUseCase useCase = new GerenciarGastoUseCase(repository);

    @FXML
    public void initialize() {
        // Inicializa valores padrão (No Android seria o onCreate)
        dpData.setValue(LocalDate.now());
        cbCategoria.getItems().addAll("Alimentação", "Lazer", "Contas Fixas", "Saúde");
        cbMetodo.getItems().addAll("Nubank", "Santander", "Dinheiro");
    }

    @FXML
    private void aoToggleParcelas() {
        // Mostra ou esconde o campo de parcelas
        containerParcelas.setVisible(chkParcelado.isSelected());
    }

    @FXML
    protected void aoSalvar() {
        try {
            String desc = txtDescricao.getText();
            double valor = Double.parseDouble(txtValor.getText());
            LocalDate data = dpData.getValue();
            String cat = cbCategoria.getValue();
            MetodoPagamento metodo = new MetodoPagamento(cbMetodo.getValue(), 1);

            int parcelas = chkParcelado.isSelected() ? Integer.parseInt(txtQtdParcelas.getText()) : 1;

            Gasto gasto = new Gasto(desc, valor, data, cat, metodo, false, parcelas, 1);
            useCase.registrarGasto(gasto);

            System.out.println("✅ Gasto registrado!");
            limparCampos();
        } catch (Exception e) {
            System.err.println("Erro ao salvar: " + e.getMessage());
        }
    }

    private void limparCampos() {
        txtDescricao.clear();
        txtValor.clear();
        chkParcelado.setSelected(false);
        containerParcelas.setVisible(false);
        txtQtdParcelas.setText("1");
    }
    @FXML
    private void abrirCadastroMetodos() {
        try {
            // 1. Carrega o FXML da nova tela
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metodos_view.fxml"));
            Parent root = loader.load();

            // 2. Cria um novo "Palco" (Janela)
            Stage stage = new Stage();
            stage.setTitle("Cadastro de Métodos de Pagamento");

            // 3. Define como Modal (bloqueia a janela de trás, igual um Dialog no Android)
            stage.initModality(Modality.APPLICATION_MODAL);

            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void abrirCadastroCategorias() {
        System.out.println("Abrindo tela de Categorias...");
    }
}