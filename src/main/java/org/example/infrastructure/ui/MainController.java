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
        colData.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getData().toString()));
        colDescricao.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescricao()));

        // Valor formatado com R$
        colValor.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("R$ %.2f", cellData.getValue().getValor())));

        colMetodo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMetodo().getNome()));

        colCategoria.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCategoria().getNome()));
        // Alinhamento profissional para valores financeiros
        colValor.setStyle("-fx-alignment: CENTER-RIGHT;");
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
}