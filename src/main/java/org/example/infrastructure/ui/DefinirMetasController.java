package org.example.infrastructure.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.domain.entity.Categoria;
import org.example.infrastructure.database.SqliteGastoRepository;

import java.time.LocalDate;

public class DefinirMetasController {

    @FXML private ComboBox<Categoria> cbCategoria;
    @FXML private TextField txtValorMeta;

    private final SqliteGastoRepository repository = new SqliteGastoRepository();

    // Variáveis para saber em qual mês/ano estamos definindo a meta
    private int mesAtual;
    private int anoAtual;

    @FXML
    public void initialize() {
        carregarCategorias();
        // Padrão para o mês/ano atual, caso não venha do Dashboard
        this.mesAtual = LocalDate.now().getMonthValue();
        this.anoAtual = LocalDate.now().getYear();
    }

    private void carregarCategorias() {
        cbCategoria.getItems().setAll(repository.buscarTodasCategorias());
    }

    public void setPeriodo(int mes, int ano) {
        this.mesAtual = mes;
        this.anoAtual = ano;
    }

    @FXML
    private void aoSalvar() {
        try {
            Categoria selecionada = cbCategoria.getValue();
            double valor = Double.parseDouble(txtValorMeta.getText().replace(",", "."));

            if (selecionada != null) {
                repository.salvarOuAtualizarMeta(selecionada.getNome(), mesAtual, anoAtual, valor);

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Meta definida com sucesso!");
                alert.showAndWait();
                aoCancelar(); // Fecha a janela
            }
        } catch (Exception e) {
            System.err.println("Erro ao salvar meta: " + e.getMessage());
        }
    }

    @FXML
    private void aoCancelar() {
        ((Stage) txtValorMeta.getScene().getWindow()).close();
    }
}