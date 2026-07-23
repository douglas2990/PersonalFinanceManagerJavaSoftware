package org.example.infrastructure.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.domain.entity.Categoria;
import org.example.domain.entity.ResumoCategoria;
import org.example.domain.repository.GastoRepositoryAPI;
import org.example.domain.repository.RepositoryFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DashboardControllerAPI {

    @FXML private ComboBox<String> cbMes;
    @FXML private Spinner<Integer> spAno;
    @FXML private Label lblStatusGeral;
    @FXML private TableView<ResumoCategoria> tableResumo;
    @FXML private TableColumn<ResumoCategoria, String> colAcumulado;
    @FXML private TableColumn<ResumoCategoria, String> colCategoria, colMeta, colGasto, colSaldo, colStatus;

    // A fábrica decide se utiliza o ApiGastoRepository ou o SqliteGastoAdapter
    private final GastoRepositoryAPI repository = RepositoryFactory.getRepository();

    @FXML
    public void initialize() {
        configurarFiltros();
        configurarColunas();
        atualizarDashboard();
    }

    private void configurarFiltros() {
        cbMes.getItems().addAll("Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro");
        cbMes.getSelectionModel().select(LocalDate.now().getMonthValue() - 1);

        int anoAtual = LocalDate.now().getYear();
        spAno.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2020, 2030, anoAtual));

        cbMes.setOnAction(e -> atualizarDashboard());
        spAno.valueProperty().addListener((obs, oldV, newV) -> atualizarDashboard());
    }

    private void configurarColunas() {
        colCategoria.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategoria()));
        colMeta.setCellValueFactory(d -> new SimpleStringProperty(String.format("R$ %.2f", d.getValue().getPlanejado())));
        colGasto.setCellValueFactory(d -> new SimpleStringProperty(String.format("R$ %.2f", d.getValue().getRealizado())));
        colSaldo.setCellValueFactory(d -> new SimpleStringProperty(String.format("R$ %.2f", d.getValue().getSaldo())));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        colAcumulado.setCellValueFactory(d -> new SimpleStringProperty(String.format("R$ %.2f", d.getValue().getSaldoAcumulado())));
    }

    @FXML
    private void atualizarDashboard() {
        int mesSelecionado = cbMes.getSelectionModel().getSelectedIndex() + 1;
        int ano = spAno.getValue();
        List<ResumoCategoria> resumos = new ArrayList<>();

        List<Categoria> categorias = repository.buscarTodasCategorias();

        for (Categoria cat : categorias) {
            double metaAtual = repository.buscarMetaFinal(cat.getNome(), mesSelecionado, ano);
            double realizadoAtual = calcularGastoMes(cat.getNome(), mesSelecionado, ano);

            double saldoAcumulado = 0;
            for (int m = 1; m <= mesSelecionado; m++) {
                double metaM = repository.buscarMetaFinal(cat.getNome(), m, ano);
                double gastoM = calcularGastoMes(cat.getNome(), m, ano);
                saldoAcumulado += (metaM - gastoM);
            }

            ResumoCategoria resumo = new ResumoCategoria(cat.getNome(), metaAtual, realizadoAtual, saldoAcumulado);
            resumos.add(resumo);
        }
        tableResumo.getItems().setAll(resumos);

        // Atualização do Balanço Geral Dinâmico
        atualizarStatusGeral(resumos);
    }

    /**
     * Calcula o balanço geral baseado no acumulado e estiliza o texto (verde para positivo, vermelho para negativo)
     */
    private void atualizarStatusGeral(List<ResumoCategoria> resumos) {
        double saldoTotal = resumos.stream()
                .mapToDouble(ResumoCategoria::getSaldoAcumulado)
                .sum();

        lblStatusGeral.setText(String.format("R$ %.2f", saldoTotal));

        if (saldoTotal >= 0) {
            lblStatusGeral.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: #27ae60;");
        } else {
            lblStatusGeral.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: #e74c3c;");
        }
    }

    private double calcularGastoMes(String categoria, int mes, int ano) {
        return repository.buscarSomaGastosPorCategoria(categoria, mes, ano);
    }

    @FXML
    private void exportarGeralExcel() {
        System.out.println("Botão de Exportar para Excel clicado!");
    }

    @FXML
    private void abrirPlanejamentoAnual() {
        try {
            // Procura o ficheiro FXML do Planeamento Anual de forma robusta
            URL fxmlLocation = getClass().getResource("/planejamento_anual_view_api.fxml");

            if (fxmlLocation == null) {
                fxmlLocation = getClass().getResource("planejamento_anual_view_api.fxml");
            }

            if (fxmlLocation == null) {
                throw new IOException("O ficheiro 'planejamento_anual_view_api.fxml' não foi encontrado na pasta de recursos (resources).");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Configura a nova janela (Stage) para a planilha anual
            Stage stage = new Stage();
            stage.setTitle("Planeamento Anual de Metas - API C#");
            stage.setScene(new Scene(root));

            // showAndWait() pausa esta execução até que a janela aberta seja fechada.
            // Quando fechar, o dashboard atualiza automaticamente!
            stage.showAndWait();
            atualizarDashboard();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlertaErro("Erro de Carregamento",
                    "Não foi possível abrir o ecrã de planeamento anual.\n\n" +
                            "Motivo: " + e.getMessage() + "\n\n" +
                            "Verifique se o ficheiro fxml está na pasta src/main/resources com o nome correto.");
        }
    }

    @FXML
    private void abrirDefinirMetas() {
        try {
            // Procura o ficheiro FXML de forma robusta
            URL fxmlLocation = getClass().getResource("/cadastro_meta_view_api.fxml");

            if (fxmlLocation == null) {
                fxmlLocation = getClass().getResource("cadastro_meta_view_api.fxml");
            }

            if (fxmlLocation == null) {
                throw new IOException("O ficheiro 'cadastro_meta_view_api.fxml' não foi encontrado na pasta de recursos (resources).");
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // Configura a nova janela (Stage)
            Stage stage = new Stage();
            stage.setTitle("Registo de Metas - API C#");
            stage.setScene(new Scene(root));

            // Aguarda fechar e depois atualiza o dashboard automaticamente
            stage.showAndWait();
            atualizarDashboard();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlertaErro("Erro de Carregamento",
                    "Não foi possível abrir o ecrã de metas.\n\n" +
                            "Motivo: " + e.getMessage() + "\n\n" +
                            "Verifique se o ficheiro fxml está na pasta src/main/resources com o nome correto.");
        }
    }

    @FXML
    private void handleAtualizar() {
        atualizarDashboard();
    }

    @FXML
    private void fecharJanela() {
        Stage stage = (Stage) tableResumo.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlertaErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}