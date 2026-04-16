package org.example.infrastructure.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import org.example.infrastructure.database.SqliteGastoRepository;

public class PlanejamentoAnualController {

    @FXML private TextField txtValorMassa;
    @FXML private TableView<PlanejamentoRow> tablePlanilha;
    @FXML private TableColumn<PlanejamentoRow, String> colCategoria;
    @FXML private TableColumn<PlanejamentoRow, Double> colMetaAnual;
    @FXML private Spinner<Integer> spAno;
    @FXML private ComboBox<String> cbCategoriaMassa;

    // Mapeamento das colunas dos meses do FXML
    @FXML private TableColumn<PlanejamentoRow, Double> colJan, colFev, colMar, colAbr, colMai, colJun,
            colJul, colAgo, colSet, colOut, colNov, colDez;

    private final SqliteGastoRepository repository = new SqliteGastoRepository();
    private final ObservableList<PlanejamentoRow> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Inicializa o Spinner de ano
        spAno.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2020, 2030, 2026));
        spAno.valueProperty().addListener((obs, oldV, newV) -> carregarDados());

        configurarColunasEditaveis();
        carregarDados();

        repository.buscarTodasCategorias().forEach(cat ->
                cbCategoriaMassa.getItems().add(cat.getNome())
        );
    }

    private void configurarColunasEditaveis() {
        tablePlanilha.setEditable(true);

        // Categoria (Fixa)
        colCategoria.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategoria()));

        // Meta Padrão (Editável)
        configurarColunaMes(colMetaAnual, -1); // -1 para identificar a meta anual

        // Mapear todos os meses (Editáveis)
        TableColumn[] colunasMeses = {colJan, colFev, colMar, colAbr, colMai, colJun,
                colJul, colAgo, colSet, colOut, colNov, colDez};

        for (int i = 0; i < 12; i++) {
            configurarColunaMes(colunasMeses[i], i);
        }
    }

    private void configurarColunaMes(TableColumn<PlanejamentoRow, Double> coluna, int mesIndex) {
        coluna.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        coluna.setCellValueFactory(cellData -> {
            if (mesIndex == -1) return cellData.getValue().metaAnualProperty().asObject();
            return cellData.getValue().mesProperty(mesIndex).asObject();
        });

        // ADICIONE ISSO: Faz o valor digitado ser salvo no objeto PlanejamentoRow
        coluna.setOnEditCommit(event -> {
            PlanejamentoRow row = event.getRowValue();
            if (mesIndex == -1) {
                row.metaAnualProperty().set(event.getNewValue());
            } else {
                row.mesProperty(mesIndex).set(event.getNewValue());
            }
        });
    }

    private void carregarDados() {
        masterData.clear();
        int ano = spAno.getValue();

        repository.buscarTodasCategorias().forEach(cat -> {
            PlanejamentoRow row = new PlanejamentoRow(cat.getNome());
            row.metaAnualProperty().set(repository.buscarMetaAnual(cat.getNome(), ano));

            for (int i = 0; i < 12; i++) {
                row.mesProperty(i).set(repository.buscarMetaPorCategoria(cat.getNome(), i + 1, ano));
            }
            masterData.add(row);
        });
        tablePlanilha.setItems(masterData);
    }

    @FXML
    private void salvarPlanejamento() {
        int ano = spAno.getValue();
        for (PlanejamentoRow row : masterData) {
            repository.salvarOuAtualizarMetaAnual(row.getCategoria(), ano, row.metaAnualProperty().get());

            for (int i = 0; i < 12; i++) {
                double valorMes = row.mesProperty(i).get();
                // Salva se for maior que 0. Se for 0, poderíamos deletar para seguir a meta anual
                repository.salvarOuAtualizarMeta(row.getCategoria(), i + 1, ano, valorMes);
            }
        }
        fechar(); // Fecha após salvar
    }


    @FXML
    private void aplicarValorEmMassa() {
        String categoriaSelecionada = cbCategoriaMassa.getValue();
        String valorTexto = txtValorMassa.getText().replace(",", ".");

        if (categoriaSelecionada != null && !valorTexto.isEmpty()) {
            try {
                double valor = Double.parseDouble(valorTexto);

                // Procura a linha correspondente na tabela
                for (PlanejamentoRow row : masterData) {
                    if (row.getCategoria().equals(categoriaSelecionada)) {
                        // ATUALIZA O OBJETO
                        row.metaAnualProperty().set(valor);
                        for (int i = 0; i < 12; i++) {
                            row.mesProperty(i).set(valor);
                        }
                        break;
                    }
                }

                // ESSENCIAL: Avisa a tabela que os dados mudaram visualmente
                tablePlanilha.refresh();
                System.out.println("Meta aplicada para: " + categoriaSelecionada);

            } catch (NumberFormatException e) {
                System.err.println("Erro: Digite um valor numérico válido.");
            }
        }
    }


    @FXML
    private void fechar() {
        Stage stage = (Stage) tablePlanilha.getScene().getWindow();
        stage.close();
    }
}