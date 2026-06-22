package org.example.infrastructure.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
    @FXML private ComboBox<String> cbCategoria;
    @FXML private ComboBox<String> cbMetodo, cbFiltroMes;
    @FXML private Spinner<Integer> spFiltroAno;
    @FXML private CheckBox chkParcelado;

    // Novas CheckBoxes conectadas ao seu FXML atualizado
    @FXML private CheckBox chkCartaoCredito;
    @FXML private CheckBox chkCartaoNaoVirou;

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
        tableGastos.setEditable(false);

        colData.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getData().toString()));
        colDescricao.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescricao()));
        colValor.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("R$ %.2f", cellData.getValue().getValor())));

        colMetodo.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getMetodo() != null ? cellData.getValue().getMetodo().getNome() : "N/A"));

        colCategoria.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCategoria() != null ? cellData.getValue().getCategoria().getNome() : "N/A"));

        // Coluna de Ações
        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("✏️");
            private final Button btnExcluir = new Button("❌");
            private final HBox container = new HBox(5, btnEditar, btnExcluir);

            {
                btnEditar.setOnAction(event -> abrirJanelaEdicao(getTableView().getItems().get(getIndex())));
                btnExcluir.setOnAction(event -> confirmarExclusao(getTableView().getItems().get(getIndex())));
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
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
            String nomeCategoria = cbCategoria.getValue();
            String nomeMetodo = cbMetodo.getValue();

            System.out.println("--- INICIANDO SALVAMENTO ---");

            // Validação de segurança
            if (nomeCategoria == null || nomeCategoria.trim().isEmpty() || nomeMetodo == null || nomeMetodo.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Selecione categoria e método!");
                alert.showAndWait();
                return;
            }

            double valor = Double.parseDouble(txtValor.getText().replace(",", "."));
            int totalParcelas = chkParcelado.isSelected() ? Integer.parseInt(txtQtdParcelas.getText()) : 1;
            String descricaoBase = txtDescricao.getText().trim();
            LocalDate dataBase = dpData.getValue();

            // --- LÓGICA INTELIGENTE DO CARTÃO DE CRÉDITO ---
            // É cartão se: a caixa estiver marcada, se for parcelado, ou se o nome do método contiver a palavra "cartão"
            boolean isCartao = chkCartaoCredito.isSelected()
                    || chkParcelado.isSelected()
                    || nomeMetodo.toLowerCase().contains("cartão");

            boolean isCartaoNaoVirou = chkCartaoNaoVirou.isSelected();

            int avancoInicial = 0;
            if (isCartao && !isCartaoNaoVirou) {
                // Se for cartão e a fatura já virou, a primeira parcela pula 1 mês pra frente (Julho)
                avancoInicial = 1;
            }

            // LAÇO DE REPETIÇÃO: Gera um gasto para cada parcela
            for (int i = 1; i <= totalParcelas; i++) {

                // Adiciona a numeração da parcela no nome apenas se for parcelado (Ex: Barracuda 1/18)
                String descFinal = totalParcelas > 1 ? descricaoBase + " " + i + "/" + totalParcelas : descricaoBase;

                // Aplica a matemática do avanço inicial da fatura + o mês de cada parcela
                LocalDate dataFinal = dataBase.plusMonths(avancoInicial + (i - 1));

                Gasto gasto = new Gasto(
                        descFinal,
                        valor,
                        dataFinal,
                        new Categoria(nomeCategoria.trim()),
                        new MetodoPagamento(nomeMetodo.trim(), 0),
                        false,
                        totalParcelas,
                        i
                );

                System.out.println("Enviando parcela: " + descFinal + " para a data " + dataFinal);

                // Salva na API
                useCase.registrarGasto(gasto);
            }

            System.out.println("--- SALVAMENTO CONCLUÍDO ---");

            limparCampos();
            atualizarTabela();

        } catch (Exception e) {
            System.err.println("ERRO AO SALVAR:");
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erro: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void atualizarTabela() {
        int mes = cbFiltroMes.getSelectionModel().getSelectedIndex() + 1;
        int ano = spFiltroAno.getValue();
        List<Gasto> lista = repository.buscarPorMesEAno(mes, ano);
        lista.forEach(g -> System.out.println("Gasto: " + g.getDescricao() + " Cat: " + g.getCategoria()));
        tableGastos.getItems().setAll(lista);
        lblTotal.setText(String.format("R$ %.2f", lista.stream().mapToDouble(Gasto::getValor).sum()));
    }

    private void carregarCategoriasNoCombo() {
        cbCategoria.getItems().setAll(repository.buscarTodasCategorias().stream().map(Categoria::getNome).toList());
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
        chkCartaoCredito.setSelected(false);
        chkCartaoNaoVirou.setSelected(false);
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
        System.out.println("Tela principal updated com sucesso!");
    }

    private void abrirJanelaEdicao(Gasto gasto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edicao_gasto.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Editar Gasto: " + gasto.getDescricao());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));

            EdicaoGastoControllerAPI controller = loader.getController();
            controller.setGasto(gasto);

            stage.showAndWait();
            atualizarTabela();
        } catch (IOException e) { e.printStackTrace(); }
    }
}