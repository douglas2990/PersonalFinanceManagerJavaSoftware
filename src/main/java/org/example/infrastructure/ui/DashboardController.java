package org.example.infrastructure.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.domain.entity.Categoria;
import org.example.domain.entity.ResumoCategoria;
import org.example.infrastructure.database.SqliteGastoRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML private ComboBox<String> cbMes;
    @FXML private Spinner<Integer> spAno;
    @FXML private Label lblStatusGeral;
    @FXML private TableView<ResumoCategoria> tableResumo;
    @FXML private TableColumn<ResumoCategoria, String> colCategoria, colMeta, colGasto, colSaldo, colStatus;

    private final SqliteGastoRepository repository = new SqliteGastoRepository();

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

        // Listeners para atualizar quando mudar o mês ou ano
        cbMes.setOnAction(e -> atualizarDashboard());
        spAno.valueProperty().addListener((obs, oldV, newV) -> atualizarDashboard());
    }

    private void configurarColunas() {
        colCategoria.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCategoria()));
        colMeta.setCellValueFactory(d -> new SimpleStringProperty(String.format("R$ %.2f", d.getValue().getPlanejado())));
        colGasto.setCellValueFactory(d -> new SimpleStringProperty(String.format("R$ %.2f", d.getValue().getRealizado())));
        colSaldo.setCellValueFactory(d -> new SimpleStringProperty(String.format("R$ %.2f", d.getValue().getSaldo())));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
    }

    private void atualizarDashboard() {
        int mes = cbMes.getSelectionModel().getSelectedIndex() + 1;
        int ano = spAno.getValue();

        List<ResumoCategoria> resumos = new ArrayList<>();
        double totalSaldoGeral = 0;

        // 1. Pegamos todas as categorias cadastradas
        List<Categoria> categorias = repository.buscarTodasCategorias();

        for (Categoria cat : categorias) {
            // 2. Buscamos a meta definida para essa categoria/mês
            double meta = repository.buscarMetaFinal(cat.getNome(), mes, ano);

            // 3. Calculamos o quanto já foi gasto (realizado)
            double realizado = repository.buscarTodos().stream()
                    .filter(g -> g.getCategoria().getNome().equals(cat.getNome()) &&
                            g.getData().getMonthValue() == mes &&
                            g.getData().getYear() == ano)
                    .mapToDouble(g -> g.getValor())
                    .sum();

            ResumoCategoria resumo = new ResumoCategoria(cat.getNome(), meta, realizado);
            resumos.add(resumo);
            totalSaldoGeral += resumo.getSaldo();
        }

        tableResumo.getItems().setAll(resumos);
        atualizarLabelStatus(totalSaldoGeral);
    }

    private void atualizarLabelStatus(double saldo) {
        lblStatusGeral.setText(String.format("R$ %.2f", saldo));
        if (saldo >= 0) {
            lblStatusGeral.setTextFill(Color.web("#27ae60")); // Verde
        } else {
            lblStatusGeral.setTextFill(Color.web("#e74c3c")); // Vermelho
        }
    }

    @FXML private void fecharJanela() {
        ((Stage) lblStatusGeral.getScene().getWindow()).close();
    }

    @FXML
    private void abrirDefinirMetas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/definir_metas_view.fxml"));
            Parent root = loader.load();

            // Passa o mês e ano atual do Dashboard para a tela de metas
            DefinirMetasController controller = loader.getController();
            controller.setPeriodo(cbMes.getSelectionModel().getSelectedIndex() + 1, spAno.getValue());

            Stage stage = new Stage();
            stage.setTitle("Configurar Orçamento");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            atualizarDashboard(); // Atualiza a tabela quando fechar a janelinha de metas

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void abrirPlanejamentoAnual() {
        // 1. Teste de sinal: Se isso não aparecer no console, o FXML não está ligado ao Controller
        System.out.println(">>> Clique detectado no método abrirPlanejamentoAnual!");

        try {
            java.net.URL fxmlLocation = getClass().getResource("/planejamento_anual_view.fxml");

            // 2. Teste de arquivo: Se for null, o Java não achou o arquivo na pasta resources
            if (fxmlLocation == null) {
                System.err.println(">>> ERRO: O arquivo planejamento_anual_view.fxml não foi encontrado!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Planejamento Orçamentário Anual");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            atualizarDashboard();

        } catch (Exception e) {
            // 3. Teste de erro interno: Mostra exatamente o que impediu a tela de abrir
            System.err.println(">>> FALHA AO CARREGAR A TELA:");
            e.printStackTrace();
        }
    }
}