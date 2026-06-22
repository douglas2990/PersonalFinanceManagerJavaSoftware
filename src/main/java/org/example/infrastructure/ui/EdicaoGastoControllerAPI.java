package org.example.infrastructure.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.example.domain.entity.Categoria;
import org.example.domain.entity.Gasto;
import org.example.domain.entity.MetodoPagamento;
import org.example.domain.repository.GastoRepositoryAPI;
import org.example.domain.repository.RepositoryFactory;
import java.time.LocalDate;

public class EdicaoGastoControllerAPI {
    @FXML private TextField txtDescricao, txtValor;
    @FXML private DatePicker dpData;
    @FXML private TextField txtParcelaAtual, txtTotalParcelas;
    @FXML private ComboBox<Categoria> cbCategoria;
    @FXML private ComboBox<String> cbMetodo;

    private Gasto gasto;
    private final GastoRepositoryAPI repository = RepositoryFactory.getRepository();

    @FXML
    public void initialize() {
        cbCategoria.getItems().addAll(repository.buscarTodasCategorias());
        cbCategoria.setConverter(new StringConverter<Categoria>() {
            public String toString(Categoria c) { return c == null ? "" : c.getNome(); }
            public Categoria fromString(String string) { return null; }
        });

        cbMetodo.getItems().addAll(repository.buscarTodosMetodos().stream()
                .map(MetodoPagamento::getNome).toList());
    }

    public void setGasto(Gasto gasto) {
        this.gasto = gasto;

        txtDescricao.setText(gasto.getDescricao());
        txtValor.setText(String.valueOf(gasto.getValor()));

        // Novos campos carregados
        if (dpData != null) dpData.setValue(gasto.getData());
        if (txtParcelaAtual != null) txtParcelaAtual.setText(String.valueOf(gasto.getParcelaAtual()));
        if (txtTotalParcelas != null) txtTotalParcelas.setText(String.valueOf(gasto.getTotalParcelas()));

        cbCategoria.getSelectionModel().select(gasto.getCategoria());
        cbMetodo.getSelectionModel().select(gasto.getMetodo().getNome());
    }

    @FXML
    private void salvarEdicao() {
        try {
            gasto.setDescricao(txtDescricao.getText());
            gasto.setValor(Double.parseDouble(txtValor.getText().replace(",", ".")));

            // Novos campos salvos
            if (dpData != null) gasto.setData(dpData.getValue());
            if (txtParcelaAtual != null) gasto.setParcelaAtual(Integer.parseInt(txtParcelaAtual.getText()));
            if (txtTotalParcelas != null) gasto.setTotalParcelas(Integer.parseInt(txtTotalParcelas.getText()));

            gasto.setCategoria(cbCategoria.getValue());
            gasto.setMetodo(new MetodoPagamento(cbMetodo.getValue(), 0));

            // Dispara o PUT para a API
            repository.salvar(gasto); // Seu repository já faz o PUT se o ID > 0

            Stage stage = (Stage) txtDescricao.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erro ao salvar: " + e.getMessage());
            alert.showAndWait();
        }
    }
}