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
import org.example.domain.entity.MetodoPagamento; // Importado para construir o Gasto original
import org.example.domain.entity.MetodoPagamentoApi; // Importado para ler o Repositório e as Cores
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

    // Lista local para fazer a ponte de cores entre MetodoPagamento e MetodoPagamentoApi
    private List<MetodoPagamentoApi> metodosDaApi;

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
        carregarMetodosNoCombo(); // Carrega os métodos e popula a lista 'metodosDaApi' antes da tabela renderizar
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

// LÓGICA DE CORES AUTOMÁTICA (Fundo + Texto Claro/Escuro)
        tableGastos.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Gasto gasto, boolean empty) {
                super.updateItem(gasto, empty);

                if (empty || gasto == null || gasto.getMetodo() == null) {
                    // Reseta para o padrão se a linha for vazia
                    setStyle("-fx-background-color: transparent; -fx-text-background-color: black; -fx-font-weight: normal;");
                } else {
                    String corFundo = buscarCorPorNome(gasto.getMetodo().getNome());
                    boolean fundoEscuro = false;

                    try {
                        // Converte o Hexadecimal para a classe Color do JavaFX
                        javafx.scene.paint.Color cor = javafx.scene.paint.Color.web(corFundo);

                        // Fórmula padrão de Luminância para saber se a cor é escura
                        double luminancia = 0.2126 * cor.getRed() + 0.7152 * cor.getGreen() + 0.0722 * cor.getBlue();

                        // Se a luminância for menor que 0.5, a cor é considerada escura
                        fundoEscuro = luminancia < 0.5;
                    } catch (Exception e) {
                        fundoEscuro = false; // Em caso de erro, assume que é clara
                    }

                    // Define a cor da letra e o negrito com base no fundo
                    String corTexto = fundoEscuro ? "white" : "black";
                    String pesoFonte = fundoEscuro ? "bold" : "normal";

                    // Aplica os estilos
                    // Dica: no JavaFX, para pintar o texto de toda a linha na tabela, usamos -fx-text-background-color
                    setStyle("-fx-background-color: " + corFundo + "; " +
                            "-fx-text-background-color: " + corTexto + "; " +
                            "-fx-font-weight: " + pesoFonte + ";");
                }
            }
        });

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

            if (nomeCategoria == null || nomeCategoria.trim().isEmpty() || nomeMetodo == null || nomeMetodo.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Selecione categoria e método!");
                alert.showAndWait();
                return;
            }

            double valor = Double.parseDouble(txtValor.getText().replace(",", "."));
            int totalParcelas = chkParcelado.isSelected() ? Integer.parseInt(txtQtdParcelas.getText()) : 1;
            String descricaoBase = txtDescricao.getText().trim();
            LocalDate dataBase = dpData.getValue();

            boolean isCartao = chkCartaoCredito.isSelected()
                    || chkParcelado.isSelected()
                    || nomeMetodo.toLowerCase().contains("cartão");

            boolean isCartaoNaoVirou = chkCartaoNaoVirou.isSelected();

            int avancoInicial = 0;
            if (isCartao && !isCartaoNaoVirou) {
                avancoInicial = 1;
            }

            for (int i = 1; i <= totalParcelas; i++) {
                String descFinal = totalParcelas > 1 ? descricaoBase + " " + i + "/" + totalParcelas : descricaoBase;
                LocalDate dataFinal = dataBase.plusMonths(avancoInicial + (i - 1));

                // CORREÇÃO DA LINHA 163: Instancia o MetodoPagamento esperado pelo domínio do Gasto
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

    // CORREÇÃO DA LINHA 201: Adaptação à nova assinatura List<MetodoPagamentoApi> do repositório
    private void carregarMetodosNoCombo() {
        metodosDaApi = repository.buscarTodosMetodos();
        cbMetodo.getItems().setAll(metodosDaApi.stream().map(MetodoPagamentoApi::getNome).toList());
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
    private void abrirCadastroMetodos() {
        abrirJanela("/metodos_view_api.fxml", "Cadastro de Métodos"); carregarMetodosNoCombo();
        carregarMetodosNoCombo(); // Atualiza a lista com a nova cor
        atualizarTabela();
    }

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
        atualizarTabela();
        carregarCategoriasNoCombo();
        carregarMetodosNoCombo();
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

    private String buscarCorPorNome(String nomeMetodo) {
        if (metodosDaApi != null && nomeMetodo != null) {
            for (MetodoPagamentoApi m : metodosDaApi) {
                if (m.getNome().trim().equalsIgnoreCase(nomeMetodo.trim())) {
                    return m.getCor(); // Ex: "#ff0000"
                }
            }
        }
        System.out.println("Cor não encontrada para " + nomeMetodo + ", usando branco.");
        return "#FFFFFF"; // Cor padrão branco
    }
}