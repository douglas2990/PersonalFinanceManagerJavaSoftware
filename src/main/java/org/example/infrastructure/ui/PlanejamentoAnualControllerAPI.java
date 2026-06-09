package org.example.infrastructure.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
// IMPORTANTE: Importar a interface e a fábrica
import org.example.domain.repository.GastoRepositoryAPI;
import org.example.domain.repository.RepositoryFactory;

public class PlanejamentoAnualControllerAPI {

    @FXML private TextField txtValorMassa;
    @FXML private TableView<PlanejamentoRow> tablePlanilha;
    @FXML private TableColumn<PlanejamentoRow, String> colCategoria;
    @FXML private TableColumn<PlanejamentoRow, Double> colMetaAnual;
    @FXML private Spinner<Integer> spAno;
    @FXML private ComboBox<String> cbCategoriaMassa;

    @FXML private TableColumn<PlanejamentoRow, Double> colJan, colFev, colMar, colAbr, colMai, colJun,
            colJul, colAgo, colSet, colOut, colNov, colDez;

    // A MUDANÇA ESTÁ AQUI: O repositório agora vem da Factory
    private final GastoRepositoryAPI repository = RepositoryFactory.getRepository();

    private final ObservableList<PlanejamentoRow> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
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

        colCategoria.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCategoria()));

        configurarColunaMes(colMetaAnual, -1);

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

        // O repository agora é a interface GastoRepositoryAPI, então funciona tanto para API quanto SQLite
        repository.buscarTodasCategorias().forEach(cat -> {
            PlanejamentoRow row = new PlanejamentoRow(cat.getNome());
            // Nota: Certifique-se de que esses métodos existem na sua interface GastoRepositoryAPI
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
                repository.salvarOuAtualizarMeta(row.getCategoria(), i + 1, ano, valorMes);
            }
        }
        fechar();
    }

    @FXML
    private void aplicarValorEmMassa() {
        String categoriaSelecionada = cbCategoriaMassa.getValue();
        String valorTexto = txtValorMassa.getText().replace(",", ".");

        if (categoriaSelecionada != null && !valorTexto.isEmpty()) {
            try {
                double valor = Double.parseDouble(valorTexto);
                for (PlanejamentoRow row : masterData) {
                    if (row.getCategoria().equals(categoriaSelecionada)) {
                        row.metaAnualProperty().set(valor);
                        for (int i = 0; i < 12; i++) {
                            row.mesProperty(i).set(valor);
                        }
                        break;
                    }
                }
                tablePlanilha.refresh();
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