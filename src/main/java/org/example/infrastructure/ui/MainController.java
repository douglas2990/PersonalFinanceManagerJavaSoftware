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
import org.example.domain.entity.Categoria; // Import da nova classe
import org.example.domain.entity.Gasto;
import org.example.domain.entity.MetodoPagamento;
import org.example.infrastructure.database.SqliteGastoRepository;
import org.example.usecase.GerenciarGastoUseCase;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class MainController {
    @FXML private TextField txtDescricao, txtValor, txtQtdParcelas;
    @FXML private DatePicker dpData;

    // AJUSTE: ComboBox agora é de Categoria
    @FXML private ComboBox<Categoria> cbCategoria;
    @FXML private ComboBox<String> cbMetodo,cbFiltroMes;
    @FXML private Spinner<Integer> spFiltroAno;

    @FXML private CheckBox chkParcelado;
    @FXML private HBox containerParcelas;
    @FXML private TableView<Gasto> tableGastos;
    @FXML private TableColumn<Gasto, String> colData, colDescricao,colCategoria, colValor, colMetodo;
    @FXML private TableColumn<Gasto, Void> colAcoes;
    @FXML private Label lblTotal;

    private final SqliteGastoRepository repository = new SqliteGastoRepository();
    private final GerenciarGastoUseCase useCase = new GerenciarGastoUseCase(repository);

    @FXML
    public void initialize() {
        dpData.setValue(LocalDate.now());

        // Preenche meses
        cbFiltroMes.getItems().addAll(
                "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        );

        // Configura o ano (de 2020 a 2030, começando no ano atual)
        int anoAtual = LocalDate.now().getYear();
        spFiltroAno.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2020, 2030, anoAtual));

        // Seleciona o mês atual por padrão
        cbFiltroMes.getSelectionModel().select(LocalDate.now().getMonthValue() - 1);

        // ESCUTADORES (Listeners): Quando mudar o combo ou o ano, atualiza a tabela
        cbFiltroMes.setOnAction(e -> atualizarTabela());
        spFiltroAno.valueProperty().addListener((obs, oldVal, newVal) -> atualizarTabela());


        configurarTabela();
        carregarCategoriasNoCombo();
        carregarMetodosNoCombo();
        atualizarTabela();

    }

    private void configurarTabela() {
        tableGastos.setEditable(true);

        // 1. Configuração de Células e Fábricas de Valor
        colData.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getData().toString()));

        colDescricao.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescricao()));
        colDescricao.setCellFactory(TextFieldTableCell.forTableColumn());
        colDescricao.setOnEditCommit(event -> {
            Gasto g = event.getRowValue();
            g.setDescricao(event.getNewValue());
            repository.atualizarGasto(g);
        });

        colValor.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("R$ %.2f", cellData.getValue().getValor())));
        colValor.setStyle("-fx-alignment: CENTER-RIGHT;");

        colMetodo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMetodo().getNome()));

        colCategoria.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCategoria().getNome()));

        // 2. Configuração da Coluna de Ações (Usando a colAcoes do FXML)
        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("❌"); // Ou "Excluir"
            {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-cursor: hand; -fx-font-weight: bold;");
                btn.setOnAction(event -> {
                    Gasto gasto = getTableView().getItems().get(getIndex());
                    confirmarExclusao(gasto);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }

    private void confirmarExclusao(Gasto gasto) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Remover lançamento?");
        alert.setContentText("Deseja excluir: " + gasto.getDescricao() + "?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            repository.removerGasto(gasto.getId()); // Use o ID que vem do banco
            atualizarTabela();
        }
    }

    private void carregarCategoriasNoCombo() {
        cbCategoria.getItems().clear();
        // Busca a lista de objetos Categoria do banco
        List<Categoria> categorias = repository.buscarTodasCategorias();
        cbCategoria.getItems().addAll(categorias);
    }

    private void carregarMetodosNoCombo() {
        cbMetodo.getItems().clear();
        List<MetodoPagamento> metodos = repository.buscarTodosMetodos();
        for (MetodoPagamento m : metodos) {
            cbMetodo.getItems().add(m.getNome());
        }
    }

    @FXML
    protected void aoSalvar() {
        try {
            String desc = txtDescricao.getText();
            double valor = Double.parseDouble(txtValor.getText().replace(",", "."));
            LocalDate data = dpData.getValue();

            // AJUSTE: Pega o objeto Categoria selecionado
            Categoria cat = cbCategoria.getValue();
            String nomeMetodo = cbMetodo.getValue();

            if (desc.isEmpty() || cat == null || nomeMetodo == null) {
                System.out.println("Preencha todos os campos!");
                return;
            }

            MetodoPagamento metodo = new MetodoPagamento(nomeMetodo, 0);
            int parcelas = chkParcelado.isSelected() ? Integer.parseInt(txtQtdParcelas.getText()) : 1;

            // Criando o gasto com o objeto Categoria corrigido
            Gasto gasto = new Gasto(desc, valor, data, cat, metodo, false, parcelas, 1);
            useCase.registrarGasto(gasto);

            limparCampos();
            atualizarTabela();
        } catch (Exception e) {
            System.err.println("Erro ao salvar: " + e.getMessage());
        }
    }

    private void atualizarTabela() {
        tableGastos.getItems().clear();

        // Pegamos os valores dos filtros
        int mes = cbFiltroMes.getSelectionModel().getSelectedIndex() + 1;
        int ano = spFiltroAno.getValue();

        // Chamamos a função do banco que você já preparou (buscarPorMesEAno)
        List<Gasto> lista = repository.buscarPorMesEAno(mes, ano);

        tableGastos.getItems().addAll(lista);
        atualizarTotal(); // O total agora será apenas do mês filtrado!
    }

    private void atualizarTotal() {
        double total = tableGastos.getItems().stream()
                .mapToDouble(Gasto::getValor)
                .sum();
        lblTotal.setText(String.format("R$ %.2f", total));
    }

    @FXML
    private void aoToggleParcelas() {
        containerParcelas.setVisible(chkParcelado.isSelected());
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
        abrirJanela("/metodos_view.fxml", "Cadastro de Métodos");
        carregarMetodosNoCombo();
    }

    @FXML
    private void abrirCadastroCategorias() {
        abrirJanela("/categorias_view.fxml", "Cadastro de Categorias");
        carregarCategoriasNoCombo();
    }

    // Método auxiliar para evitar repetição de código ao abrir janelas
    private void abrirJanela(String fxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(titulo);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void abrirDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard_view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Dashboard de Metas e Balanço");
            stage.initModality(Modality.APPLICATION_MODAL); // Trava a tela de trás
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Quando fechar o dashboard, atualiza a tela principal por segurança
            atualizarTabela();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir o Dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void abrirTabelaExpandida() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tabela_expandida_view.fxml"));
            Parent root = loader.load();

            TabelaExpandidaController expandedController = loader.getController();

            // 1. Pegamos os valores atuais dos filtros da Main
            String mesSelecionado = cbFiltroMes.getValue();
            int anoSelecionado = spFiltroAno.getValue();

            // 2. Chamamos a nova função que configura os filtros e carrega os dados no banco
            // Note que mudamos de 'inicializarDados' para 'configurarInicial'
            expandedController.configurarInicial(mesSelecionado, anoSelecionado);

            Stage stage = new Stage();
            stage.setTitle("Relatório Detalhado - Oliveira");
            stage.setScene(new Scene(root));

            // Mantemos o modo tela cheia para o efeito de "zoom"
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao abrir a visualização expandida: " + e.getMessage());
        }
    }
}