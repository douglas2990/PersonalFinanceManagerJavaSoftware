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
    @FXML private TextField txtDescricao, txtValor, txtQtdParcelas;
    @FXML private DatePicker dpData;
    @FXML private ComboBox<String> cbCategoria, cbMetodo;
    @FXML private CheckBox chkParcelado;
    @FXML private HBox containerParcelas;
    @FXML private TableView<Gasto> tableGastos;
    @FXML private TableColumn<Gasto, String> colData;
    @FXML private TableColumn<Gasto, String> colDescricao;
    @FXML private TableColumn<Gasto, Double> colValor;
    @FXML private TableColumn<Gasto, String> colMetodo;

    private final SqliteGastoRepository repository = new SqliteGastoRepository();
    private final GerenciarGastoUseCase useCase = new GerenciarGastoUseCase(repository);

    @FXML
    public void initialize() {
        // Inicializa valores padrão (No Android seria o onCreate)
        dpData.setValue(LocalDate.now());

        // Categorias fixas por enquanto
        cbCategoria.getItems().addAll("Alimentação", "Lazer", "Contas Fixas", "Saúde");

        // BUSCA DINÂMICA: Carrega o que estiver no SQLite
        carregarMetodosNoCombo();

        configurarTabela();
        atualizarTabela();
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

            // Quando você fechar a janela de métodos, ele continua para a linha de baixo
            stage.showAndWait();

            // Atualiza o ComboBox para mostrar o cartão que acabou de ser criado!
            carregarMetodosNoCombo();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void abrirCadastroCategorias() {
        System.out.println("Abrindo tela de Categorias...");
    }

    private void carregarMetodosNoCombo() {
        cbMetodo.getItems().clear();

        // Buscamos a lista do banco através do repositório
        List<MetodoPagamento> metodosDoBanco = repository.buscarTodosMetodos();

        if (metodosDoBanco.isEmpty()) {
            cbMetodo.setPromptText("Cadastre um cartão no menu");
        } else {
            for (MetodoPagamento m : metodosDoBanco) {
                cbMetodo.getItems().add(m.getNome());
            }
        }
    }

    private void configurarTabela() {
        // Diz para a coluna qual atributo da classe Gasto ela deve olhar
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
        colValor.setCellValueFactory(new PropertyValueFactory<>("valor"));

        // Como o método é um objeto, precisamos de uma lógica extra para pegar só o nome
        colMetodo.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMetodo().getNome()));
    }

    private void atualizarTabela() {
        tableGastos.getItems().clear();
        List<Gasto> gastos = repository.buscarTodos(); // Precisaremos implementar isso no Repository!
        tableGastos.getItems().addAll(gastos);
    }
}