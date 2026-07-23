package org.example.infrastructure.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.domain.entity.Gasto;
import org.example.domain.entity.MetodoPagamentoApi;
import org.example.domain.repository.GastoRepositoryAPI;
import org.example.domain.repository.RepositoryFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.time.format.DateTimeFormatter;

public class TabelaExpandidaControllerAPI {

    @FXML private TableView<Gasto> tableGastosFull;
    @FXML private TableColumn<Gasto, String> colData, colDescricao, colCategoria, colValor, colMetodo;
    @FXML private TableColumn<Gasto, Void> colAcoes;
    @FXML private ComboBox<String> cbFiltroMesFull;
    @FXML private Spinner<Integer> spFiltroAnoFull;
    @FXML private Label lblTotalFull;

    private final GastoRepositoryAPI repository = RepositoryFactory.getRepository();

    // Lista para guardar as cores dos métodos em memória
    private List<MetodoPagamentoApi> metodosDaApi;

    @FXML
    public void initialize() {
        cbFiltroMesFull.getItems().addAll("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro");

        // Carrega os métodos para sabermos as cores
        metodosDaApi = repository.buscarTodosMetodos();

        configurarColunas();

        cbFiltroMesFull.setOnAction(e -> atualizarDados());
        spFiltroAnoFull.valueProperty().addListener((obs, oldV, newV) -> atualizarDados());
    }

    private void configurarColunas() {
        tableGastosFull.setEditable(false);

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

        // 1. APLICANDO AS CORES NA TABELA EXPANDIDA (Igual à tela principal)
        tableGastosFull.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Gasto gasto, boolean empty) {
                super.updateItem(gasto, empty);

                if (empty || gasto == null || gasto.getMetodo() == null) {
                    setStyle("-fx-background-color: transparent; -fx-text-background-color: black; -fx-font-weight: normal;");
                } else {
                    String corFundo = buscarCorPorNome(gasto.getMetodo().getNome());
                    boolean fundoEscuro = false;

                    try {
                        javafx.scene.paint.Color cor = javafx.scene.paint.Color.web(corFundo);
                        double luminancia = 0.2126 * cor.getRed() + 0.7152 * cor.getGreen() + 0.0722 * cor.getBlue();
                        fundoEscuro = luminancia < 0.5;
                    } catch (Exception e) {
                        fundoEscuro = false;
                    }

                    String corTexto = fundoEscuro ? "white" : "black";
                    String pesoFonte = fundoEscuro ? "bold" : "normal";

                    setStyle("-fx-background-color: " + corFundo + "; " +
                            "-fx-text-background-color: " + corTexto + "; " +
                            "-fx-font-weight: " + pesoFonte + ";");
                }
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
            repository.removerGasto(gasto.getId());
            atualizarDados();
        }
    }



    // Auxiliar: Busca a cor salva na API com base no nome do método
    private String buscarCorPorNome(String nomeMetodo) {
        if (nomeMetodo == null || metodosDaApi == null) return "#FFFFFF";
        for (MetodoPagamentoApi m : metodosDaApi) {
            if (m.getNome().trim().equalsIgnoreCase(nomeMetodo.trim())) {
                return m.getCor() != null ? m.getCor() : "#FFFFFF";
            }
        }
        return "#FFFFFF";
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
        atualizarDados();
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
            atualizarDados();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    private void exportarParaExcel3() {
        List<Gasto> lista = tableGastosFull.getItems();
        if (lista.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Não há dados para exportar!");
            alert.showAndWait();
            return;
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecionar pasta para salvar o relatório");
        java.io.File selectedDirectory = directoryChooser.showDialog(tableGastosFull.getScene().getWindow());

        if (selectedDirectory != null) {
            String nomeArquivo = "Gastos_" + cbFiltroMesFull.getValue() + "_" + spFiltroAnoFull.getValue() + ".xlsx";
            java.io.File file = new java.io.File(selectedDirectory, nomeArquivo);

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Gastos");

                // Estilo padrão para o cabeçalho
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);

                Row header = sheet.createRow(0);

                // NOVA ORDEM DO CABEÇALHO
                String[] cols = {"Descrição", "Valor", "Data", "Pagamento", "Categoria"};
                for (int i = 0; i < cols.length; i++) {
                    Cell cell = header.createCell(i);
                    cell.setCellValue(cols[i]);
                    cell.setCellStyle(headerStyle);
                }

                // CACHE DE ESTILOS DE CORES
                Map<String, CellStyle> styleCache = new HashMap<>();

                // FORMATADOR DE DATA NO PADRÃO BRASILEIRO
                DateTimeFormatter formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                int r = 1;
                for (Gasto g : lista) {
                    Row row = sheet.createRow(r++);

                    // Descobre a cor e calcula o claro/escuro
                    String corHex = buscarCorPorNome(g.getMetodo().getNome());
                    CellStyle rowStyle = styleCache.get(corHex);

                    if (rowStyle == null) {
                        rowStyle = workbook.createCellStyle();
                        try {
                            // Converte HEX para a cor nativa do Java (AWT)
                            java.awt.Color awtColor = java.awt.Color.decode(corHex);

                            // Aplica o fundo customizado no Excel
                            XSSFColor xssfColor = new XSSFColor(awtColor, new DefaultIndexedColorMap());
                            ((XSSFCellStyle) rowStyle).setFillForegroundColor(xssfColor);
                            rowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                            // Matemática de luminância
                            double luminancia = 0.2126 * (awtColor.getRed() / 255.0) +
                                    0.7152 * (awtColor.getGreen() / 255.0) +
                                    0.0722 * (awtColor.getBlue() / 255.0);

                            Font rowFont = workbook.createFont();
                            if (luminancia < 0.5) {
                                rowFont.setColor(IndexedColors.WHITE.getIndex());
                                rowFont.setBold(true);
                            } else {
                                rowFont.setColor(IndexedColors.BLACK.getIndex());
                            }
                            rowStyle.setFont(rowFont);

                        } catch (Exception e) {
                            // Se a conversão de cor falhar, deixa em branco
                        }
                        styleCache.put(corHex, rowStyle);
                    }

                    // PREENCHE AS CÉLULAS NA NOVA ORDEM E COM A DATA FORMATADA
                    Cell[] cells = new Cell[5];
                    cells[0] = row.createCell(0); cells[0].setCellValue(g.getDescricao());
                    cells[1] = row.createCell(1); cells[1].setCellValue(g.getValor());

                    String dataFormatada = g.getData().format(formatadorData);
                    cells[2] = row.createCell(2); cells[2].setCellValue(dataFormatada);

                    cells[3] = row.createCell(3); cells[3].setCellValue(g.getMetodo().getNome());
                    cells[4] = row.createCell(4); cells[4].setCellValue(g.getCategoria().getNome());

                    for (Cell c : cells) {
                        c.setCellStyle(rowStyle);
                    }
                }

                for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

                try (FileOutputStream out = new FileOutputStream(file)) {
                    workbook.write(out);
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Arquivo salvo com sucesso!");
                alert.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erro ao gerar Excel: " + e.getMessage());
                alert.showAndWait();
            }
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

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecionar pasta para salvar o relatório");
        java.io.File selectedDirectory = directoryChooser.showDialog(tableGastosFull.getScene().getWindow());

        if (selectedDirectory != null) {
            String nomeArquivo = "Gastos_" + cbFiltroMesFull.getValue() + "_" + spFiltroAnoFull.getValue() + ".xlsx";
            java.io.File file = new java.io.File(selectedDirectory, nomeArquivo);

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Gastos");

                // Estilo padrão para o cabeçalho
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);

                Row header = sheet.createRow(0);

                // Ordem do cabeçalho solicitada
                String[] cols = {"Descrição", "Valor", "Data", "Pagamento", "Categoria"};
                for (int i = 0; i < cols.length; i++) {
                    Cell cell = header.createCell(i);
                    cell.setCellValue(cols[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Cache de estilos de cores
                Map<String, CellStyle> styleCache = new HashMap<>();

                // Formatador de data no padrão brasileiro
                DateTimeFormatter formatadorData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                int r = 1;
                for (Gasto g : lista) {
                    Row row = sheet.createRow(r++);

                    // Descobre a cor e calcula o claro/escuro
                    String corHex = buscarCorPorNome(g.getMetodo().getNome());
                    CellStyle rowStyle = styleCache.get(corHex);

                    if (rowStyle == null) {
                        rowStyle = workbook.createCellStyle();
                        try {
                            // Converte HEX para a cor nativa do Java (AWT)
                            java.awt.Color awtColor = java.awt.Color.decode(corHex);

                            // Aplica o fundo customizado no Excel
                            XSSFColor xssfColor = new XSSFColor(awtColor, new DefaultIndexedColorMap());
                            ((XSSFCellStyle) rowStyle).setFillForegroundColor(xssfColor);
                            rowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                            // Matemática de luminância
                            double luminancia = 0.2126 * (awtColor.getRed() / 255.0) +
                                    0.7152 * (awtColor.getGreen() / 255.0) +
                                    0.0722 * (awtColor.getBlue() / 255.0);

                            Font rowFont = workbook.createFont();
                            if (luminancia < 0.5) {
                                rowFont.setColor(IndexedColors.WHITE.getIndex());
                                rowFont.setBold(true);
                            } else {
                                rowFont.setColor(IndexedColors.BLACK.getIndex());
                            }
                            rowStyle.setFont(rowFont);

                        } catch (Exception e) {
                            // Se a conversão falhar, mantém padrão
                        }
                        styleCache.put(corHex, rowStyle);
                    }

                    // Preenche as células na ordem: Descrição, Valor, Data, Pagamento, Categoria
                    Cell[] cells = new Cell[5];

                    // 1. Descrição
                    cells[0] = row.createCell(0);
                    cells[0].setCellValue(g.getDescricao());

                    // 2. Valor formatado em Reais (ex: R$ 100,00)
                    String valorFormatado = String.format(new java.util.Locale("pt", "BR"), "R$ %.2f", g.getValor());
                    cells[1] = row.createCell(1);
                    cells[1].setCellValue(valorFormatado);

                    // 3. Data formatada (ex: 11/11/2026)
                    String dataFormatada = g.getData().format(formatadorData);
                    cells[2] = row.createCell(2);
                    cells[2].setCellValue(dataFormatada);

                    // 4. Pagamento
                    cells[3] = row.createCell(3);
                    cells[3].setCellValue(g.getMetodo().getNome());

                    // 5. Categoria
                    cells[4] = row.createCell(4);
                    cells[4].setCellValue(g.getCategoria().getNome());

                    for (Cell c : cells) {
                        c.setCellStyle(rowStyle);
                    }
                }

                for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);

                try (FileOutputStream out = new FileOutputStream(file)) {
                    workbook.write(out);
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Arquivo salvo com sucesso!");
                alert.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erro ao gerar Excel: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
}