package org.example.infrastructure.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.domain.entity.Gasto;
import org.example.domain.entity.MetodoPagamento;
import org.example.infrastructure.database.SqliteGastoRepository;
import org.example.usecase.GerenciarGastoUseCase;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class MainController {
    // Campos de Entrada
    @FXML private TextField txtDescricao, txtValor, txtQtdParcelas;
    @FXML private DatePicker dpData;
    @FXML private ComboBox<String> cbCategoria, cbMetodo;
    @FXML private CheckBox chkParcelado;
    @FXML private HBox containerParcelas;

    // Componentes da Tabela
    @FXML private TableView<Gasto> tableGastos;
    @FXML private TableColumn<Gasto, String> colData;
    @FXML private TableColumn<Gasto, String> colDescricao;
    @FXML private TableColumn<Gasto, String> colValor;
    @FXML private TableColumn<Gasto, String> colMetodo;

    private final SqliteGastoRepository repository = new SqliteGastoRepository();
    private final GerenciarGastoUseCase useCase = new GerenciarGastoUseCase(repository);

    @FXML
    public void initialize() {
        dpData.setValue(LocalDate.now());

        // Configurações iniciais
        cbCategoria.getItems().addAll("Alimentação", "Lazer", "Contas Fixas", "Saúde", "Transporte");

        configurarTabela();
        carregarMetodosNoCombo();
        atualizarTabela();
    }

    private void configurarTabela() {
        // Data formatada
        colData.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getData().toString()));

        // Descrição
        colDescricao.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDescricao()));

        // Valor em Reais (R$) - Agora como String para aceitar a formatação
        colValor.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("R$ %.2f", cellData.getValue().getValor())));

        // Método de Pagamento
        colMetodo.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMetodo().getNome()));
    }

    private void atualizarTabela() {
        tableGastos.getItems().clear();
        List<Gasto> lista = repository.buscarTodos();
        tableGastos.getItems().addAll(lista);
    }

    private void carregarMetodosNoCombo() {
        cbMetodo.getItems().clear();
        List<MetodoPagamento> metodos = repository.buscarTodosMetodos();
        for (MetodoPagamento m : metodos) {
            cbMetodo.getItems().add(m.getNome());
        }
    }

    @FXML
    private void aoToggleParcelas() {
        containerParcelas.setVisible(chkParcelado.isSelected());
    }

    @FXML
    protected void aoSalvar() {
        try {
            String desc = txtDescricao.getText();
            double valor = Double.parseDouble(txtValor.getText().replace(",", "."));
            LocalDate data = dpData.getValue();
            String cat = cbCategoria.getValue();
            String nomeMetodo = cbMetodo.getValue();

            if (desc.isEmpty() || nomeMetodo == null) {
                System.out.println("Preencha os campos obrigatórios!");
                return;
            }

            MetodoPagamento metodo = new MetodoPagamento(nomeMetodo, 0);
            int parcelas = chkParcelado.isSelected() ? Integer.parseInt(txtQtdParcelas.getText()) : 1;

            Gasto gasto = new Gasto(desc, valor, data, cat, metodo, false, parcelas, 1);
            useCase.registrarGasto(gasto);

            System.out.println("✅ Gasto registrado!");

            limparCampos();
            atualizarTabela(); // Atualiza a lista na hora

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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metodos_view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Cadastro de Métodos de Pagamento");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));

            // Espera fechar para atualizar o combo e a tabela
            stage.showAndWait();

            carregarMetodosNoCombo();
            atualizarTabela();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void abrirCadastroCategorias() {
        System.out.println("Abrindo tela de Categorias...");
    }
}