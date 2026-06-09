package org.example.infrastructure.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import org.example.domain.entity.Categoria;
import org.example.domain.entity.ResumoCategoria;
import org.example.domain.repository.GastoRepositoryAPI;
import org.example.domain.repository.RepositoryFactory;

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

    // A fábrica decide se usa ApiGastoRepository ou o SqliteGastoAdapter
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

        // Exemplo de atualização de status (opcional, se você tiver esse método)
        // double saldoTotal = resumos.stream().mapToDouble(ResumoCategoria::getSaldo).sum();
        // atualizarLabelStatus(saldoTotal);
    }

    private double calcularGastoMes(String categoria, int mes, int ano) {
        return repository.buscarSomaGastosPorCategoria(categoria, mes, ano);
    }
}