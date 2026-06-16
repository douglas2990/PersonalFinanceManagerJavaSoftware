package org.example.infrastructure.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.domain.entity.Categoria;
import org.example.domain.entity.Gasto;
import org.example.domain.entity.MetodoPagamento;
import org.example.domain.repository.GastoRepositoryAPI;
import org.example.domain.repository.RepositoryFactory;
import org.example.usecase.GerenciarGastoApiUseCase;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class MainControllerAPI {
    @FXML private TextField txtDescricao, txtValor, txtQtdParcelas;
    @FXML private DatePicker dpData;
    @FXML private ComboBox<Categoria> cbCategoria;
    @FXML private ComboBox<String> cbMetodo, cbFiltroMes;
    @FXML private Spinner<Integer> spFiltroAno;
    @FXML private CheckBox chkParcelado;
    @FXML private HBox containerParcelas;
    @FXML private TableView<Gasto> tableGastos;
    @FXML private TableColumn<Gasto, String> colData, colDescricao, colCategoria, colValor, colMetodo;
    @FXML private TableColumn<Gasto, Void> colAcoes;
    @FXML private Label lblTotal;

    // Repositório via Factory e UseCase específico para API
    private final GastoRepositoryAPI repository = RepositoryFactory.getRepository();
    private final GerenciarGastoApiUseCase useCase = new GerenciarGastoApiUseCase(repository);

    @FXML
    public void initialize() {
        dpData.setValue(LocalDate.now());
        cbFiltroMes.getItems().addAll("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro");

        int anoAtual = LocalDate.now().getYear();
        spFiltroAno.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2020, 2030, anoAtual));
        cbFiltroMes.getSelectionModel().select(LocalDate.now().getMonthValue() - 1);

        cbFiltroMes.setOnAction(e -> atualizarTabela());
        spFiltroAno.valueProperty().addListener((obs, oldVal, newVal) -> atualizarTabela());

        configurarTabela();
        carregarCategoriasNoCombo();
        carregarMetodosNoCombo();
        atualizarTabela();
    }

    private void configurarTabela() {
        tableGastos.setEditable(true);
        colData.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getData().toString()));
        colDescricao.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescricao()));
        colDescricao.setCellFactory(TextFieldTableCell.forTableColumn());
        colDescricao.setOnEditCommit(event -> {
            Gasto g = event.getRowValue();
            g.setDescricao(event.getNewValue());
            repository.salvar(g);
        });

        colValor.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("R$ %.2f", cellData.getValue().getValor())));
        colValor.setStyle("-fx-alignment: CENTER-RIGHT;");
        colMetodo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMetodo().getNome()));
        colCategoria.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoria().getNome()));

        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("❌");
            {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-cursor: hand; -fx-font-weight: bold;");
                btn.setOnAction(event -> confirmarExclusao(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void confirmarExclusao(Gasto gasto) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Excluir " + gasto.getDescricao() + "?", ButtonType.OK, ButtonType.CANCEL);
        if (alert.showAndWait().get() == ButtonType.OK) {
            repository.removerGasto(gasto.getId());
            atualizarTabela();
        }
    }

    @FXML
    protected void aoSalvar() {
        try {
            double valor = Double.parseDouble(txtValor.getText().replace(",", "."));
            Gasto gasto = new Gasto(txtDescricao.getText(), valor, dpData.getValue(),
                    cbCategoria.getValue(), new MetodoPagamento(cbMetodo.getValue(), 0),
                    false, chkParcelado.isSelected() ? Integer.parseInt(txtQtdParcelas.getText()) : 1, 1);
            useCase.registrarGasto(gasto);
            limparCampos();
            atualizarTabela();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void atualizarTabela() {
        int mes = cbFiltroMes.getSelectionModel().getSelectedIndex() + 1;
        int ano = spFiltroAno.getValue();
        List<Gasto> lista = repository.buscarPorMesEAno(mes, ano);
        tableGastos.getItems().setAll(lista);
        lblTotal.setText(String.format("R$ %.2f", lista.stream().mapToDouble(Gasto::getValor).sum()));
    }

    private void carregarCategoriasNoCombo() {
        cbCategoria.getItems().setAll(repository.buscarTodasCategorias());
    }

    private void carregarMetodosNoCombo() {
        cbMetodo.getItems().setAll(repository.buscarTodosMetodos().stream().map(MetodoPagamento::getNome).toList());
    }

    @FXML
    private void aoToggleParcelas() { containerParcelas.setVisible(chkParcelado.isSelected()); }

    private void limparCampos() {
        txtDescricao.clear();
        txtValor.clear();
        chkParcelado.setSelected(false);
        containerParcelas.setVisible(false);
        txtQtdParcelas.setText("1");
    }

    @FXML
    private void abrirCadastroMetodos() { abrirJanela("/metodos_view_api.fxml", "Cadastro de Métodos"); carregarMetodosNoCombo(); }

    @FXML
    private void abrirCadastroCategorias() { abrirJanela("/categorias_view_api.fxml", "Cadastro de Categorias"); carregarCategoriasNoCombo(); }

    private void abrirJanela(String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));
            stage.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void abrirDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard_view_api.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Dashboard de Metas e Balanço");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));
            stage.showAndWait();
            atualizarTabela();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void abrirTabelaExpandida() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tabela_expandida_view_api.fxml"));
            Parent root = loader.load();
            TabelaExpandidaControllerAPI controller = loader.getController();
            controller.configurarInicial(cbFiltroMes.getValue(), spFiltroAno.getValue());
            Stage stage = new Stage();
            stage.setTitle("Relatório Detalhado");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    protected void aoAtualizar() {
        atualizarTabela();            // Recarrega a lista de gastos da API
        carregarCategoriasNoCombo();  // Recarrega as categorias da API
        carregarMetodosNoCombo();     // Recarrega os métodos de pagamento da API
        System.out.println("Tela principal atualizada com sucesso!");
    }
}