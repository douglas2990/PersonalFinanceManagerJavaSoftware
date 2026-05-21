package org.example.infrastructure.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.domain.entity.Gasto;
import org.example.infrastructure.database.SqliteGastoRepository;

import java.io.IOException;
import java.util.List;

public class TabelaExpandidaController {

    @FXML private TableView<Gasto> tableGastosFull;
    @FXML private TableColumn<Gasto, String> colData, colDescricao, colCategoria, colValor, colMetodo;
    @FXML private TableColumn<Gasto, Void> colAcoes;
    @FXML private ComboBox<String> cbFiltroMesFull;
    @FXML private Spinner<Integer> spFiltroAnoFull;
    @FXML private Label lblTotalFull;

    private final SqliteGastoRepository repository = new SqliteGastoRepository();

    @FXML
    public void initialize() {
        cbFiltroMesFull.getItems().addAll("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro");

        configurarColunas();

        // Listeners para atualizar ao trocar mês/ano na tela cheia
        cbFiltroMesFull.setOnAction(e -> atualizarDadosLocal());
        spFiltroAnoFull.valueProperty().addListener((obs, oldV, newV) -> atualizarDadosLocal());
    }

    private void configurarColunas() {
        tableGastosFull.setEditable(true);

        colData.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getData().toString()));

        // Permite editar a descrição direto na tela cheia
        colDescricao.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescricao()));
        colDescricao.setCellFactory(TextFieldTableCell.forTableColumn());
        colDescricao.setOnEditCommit(event -> {
            Gasto g = event.getRowValue();
            g.setDescricao(event.getNewValue());
            repository.atualizarGasto(g); //
        });

        colCategoria.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoria().getNome()));
        colMetodo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMetodo().getNome()));

        colValor.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("R$ %.2f", cellData.getValue().getValor())));
        colValor.setStyle("-fx-alignment: CENTER-RIGHT;");

        // Botão de Excluir espelhado
        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("❌");
            {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-cursor: hand; -fx-font-weight: bold;");
                btn.setOnAction(event -> {
                    Gasto gasto = getTableView().getItems().get(getIndex());
                    removerGastoLocal(gasto);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    @FXML
    private void exportarParaExcel() {
        try {
            List<Gasto> listaParaExportar = tableGastosFull.getItems();

            if (listaParaExportar.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Não há dados para exportar!", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Salvar Relatório Excel");
            fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Arquivo Excel (*.xlsx)", "*.xlsx"));
            fileChooser.setInitialFileName("Relatorio_Financas.xlsx");

            java.io.File file = fileChooser.showSaveDialog(tableGastosFull.getScene().getWindow());

            if (file != null) {
                // Criação do Excel usando Apache POI diretamente
                org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Gastos");

                // Cabeçalho
                org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
                String[] colunas = {"Data", "Descrição", "Categoria", "Valor", "Pagamento"};
                for (int i = 0; i < colunas.length; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                    cell.setCellValue(colunas[i]);
                }

                // Dados
                int rowNum = 1;
                for (Gasto g : listaParaExportar) {
                    org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(g.getData().toString());
                    row.createCell(1).setCellValue(g.getDescricao());
                    row.createCell(2).setCellValue(g.getCategoria().getNome());
                    row.createCell(3).setCellValue(g.getValor());
                    row.createCell(4).setCellValue(g.getMetodo().getNome());
                }

                // Auto-ajuste de colunas
                for (int i = 0; i < colunas.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (java.io.FileOutputStream fileOut = new java.io.FileOutputStream(file)) {
                    workbook.write(fileOut);
                }
                workbook.close();

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "✅ Excel gerado com sucesso!", ButtonType.OK);
                alert.showAndWait();
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao exportar para Excel: " + e.getMessage());
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
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Atualiza a tabela caso algo tenha mudado no dashboard
            atualizarDadosLocal();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void configurarInicial(String mes, int ano) {
        cbFiltroMesFull.getSelectionModel().select(mes);
        spFiltroAnoFull.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2020, 2030, ano));
        atualizarDadosLocal();
    }

    private void atualizarDadosLocal() {
        int mes = cbFiltroMesFull.getSelectionModel().getSelectedIndex() + 1;
        int ano = spFiltroAnoFull.getValue();
        List<Gasto> lista = repository.buscarPorMesEAno(mes, ano);
        tableGastosFull.getItems().setAll(lista);

        double total = lista.stream().mapToDouble(Gasto::getValor).sum();
        lblTotalFull.setText(String.format("R$ %.2f", total));
    }

    private void removerGastoLocal(Gasto gasto) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Excluir " + gasto.getDescricao() + "?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().get() == ButtonType.YES) {
            repository.removerGasto(gasto.getId()); //
            atualizarDadosLocal();
        }
    }
}