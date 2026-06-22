package org.example.infrastructure.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.domain.entity.Categoria;
import org.example.domain.entity.Gasto;
import org.example.domain.entity.MetodoPagamento;
import org.example.domain.repository.GastoRepositoryAPI;
import org.example.domain.repository.RepositoryFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class TabelaExpandidaControllerAPI {

    @FXML private TableView<Gasto> tableGastosFull;
    @FXML private TableColumn<Gasto, String> colData, colDescricao, colCategoria, colValor, colMetodo;
    @FXML private TableColumn<Gasto, Void> colAcoes;
    @FXML private ComboBox<String> cbFiltroMesFull;
    @FXML private Spinner<Integer> spFiltroAnoFull;
    @FXML private Label lblTotalFull;

    // A Fábrica decide se usa o Adapter do SQLite ou a ApiGastoRepository
    private final GastoRepositoryAPI repository = RepositoryFactory.getRepository();

    @FXML
    public void initialize() {
        cbFiltroMesFull.getItems().addAll("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro");

        configurarColunas();

        cbFiltroMesFull.setOnAction(e -> atualizarDados());
        spFiltroAnoFull.valueProperty().addListener((obs, oldV, newV) -> atualizarDados());
    }

    private void configurarColunas() {
        tableGastosFull.setEditable(false); // Agora a edição é via Modal

        colData.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getData().toString()));
        colDescricao.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescricao()));
        colValor.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("R$ %.2f", cellData.getValue().getValor())));
        colMetodo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMetodo().getNome()));
        colCategoria.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoria().getNome()));

        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("✏️");
            private final Button btnExcluir = new Button("❌");
            private final HBox container = new HBox(5, btnEditar, btnExcluir);

            {
                btnEditar.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                btnEditar.setOnAction(event -> abrirJanelaEdicao(getTableView().getItems().get(getIndex())));

                btnExcluir.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-cursor: hand;");
                btnExcluir.setOnAction(event -> removerGasto(getTableView().getItems().get(getIndex())));
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void atualizarDados() {
        int mes = cbFiltroMesFull.getSelectionModel().getSelectedIndex() + 1;
        int ano = spFiltroAnoFull.getValue();
        List<Gasto> lista = repository.buscarPorMesEAno(mes, ano);

        tableGastosFull.getItems().setAll(lista);

        double total = lista.stream().mapToDouble(Gasto::getValor).sum();
        lblTotalFull.setText(String.format("R$ %.2f", total));
    }

    private void removerGasto(Gasto gasto) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Excluir " + gasto.getDescricao() + "?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().get() == ButtonType.YES) {
            // Este método agora existe na interface e no seu Adapter!
            repository.removerGasto(gasto.getId());
            atualizarDados();
        }
    }

    @FXML
    private void exportarParaExcel() {
        List<Gasto> lista = tableGastosFull.getItems();
        if (lista.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Não há dados para exportar!");
            alert.showAndWait();
            return;
        }

        // 1. Criar o seletor de pasta
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecionar pasta para salvar o relatório");
        java.io.File selectedDirectory = directoryChooser.showDialog(tableGastosFull.getScene().getWindow());

        if (selectedDirectory != null) {
            // 2. Definir o nome do arquivo automaticamente
            String nomeArquivo = "Gastos_" + cbFiltroMesFull.getValue() + "_" + spFiltroAnoFull.getValue() + ".xlsx";
            java.io.File file = new java.io.File(selectedDirectory, nomeArquivo);

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Gastos");

                // Estilo para o cabeçalho
                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);

                Row header = sheet.createRow(0);
                String[] cols = {"Data", "Descrição", "Categoria", "Valor", "Pagamento"};
                for (int i = 0; i < cols.length; i++) {
                    Cell cell = header.createCell(i);
                    cell.setCellValue(cols[i]);
                    cell.setCellStyle(headerStyle);
                }

                int r = 1;
                for (Gasto g : lista) {
                    Row row = sheet.createRow(r++);
                    row.createCell(0).setCellValue(g.getData().toString());
                    row.createCell(1).setCellValue(g.getDescricao());
                    row.createCell(2).setCellValue(g.getCategoria().getNome());
                    row.createCell(3).setCellValue(g.getValor());
                    row.createCell(4).setCellValue(g.getMetodo().getNome());
                }

                // Ajustar largura das colunas
                for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

                try (FileOutputStream out = new FileOutputStream(file)) {
                    workbook.write(out);
                }

                // Feedback de sucesso
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Arquivo salvo em: " + file.getAbsolutePath());
                alert.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erro ao gerar Excel: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void abrirDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard_view.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            atualizarDados();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void configurarInicial(String mes, int ano) {
        cbFiltroMesFull.getSelectionModel().select(mes);
        spFiltroAnoFull.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2020, 2030, ano));
        atualizarDados();
    }
    @FXML
    private void aoAtualizar() {
        atualizarDados(); // Recarrega a lista expandida com base no mês/ano selecionados
        System.out.println("Tabela expandida atualizada!");
    }

    private void abrirJanelaEdicao(Gasto gasto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edicao_gasto.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Editar Gasto");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));

            EdicaoGastoControllerAPI controller = loader.getController();
            controller.setGasto(gasto);

            stage.showAndWait();
            atualizarDados(); // Atualiza a tabela ao fechar o modal
        } catch (IOException e) { e.printStackTrace(); }
    }

}