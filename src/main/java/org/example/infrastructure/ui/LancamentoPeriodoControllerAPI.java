package org.example.infrastructure.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.domain.entity.Categoria;
import org.example.domain.entity.Gasto;
import org.example.domain.entity.MetodoPagamento;
import org.example.domain.entity.MetodoPagamentoApi;
import org.example.domain.repository.GastoRepositoryAPI;
import org.example.domain.repository.RepositoryFactory;
import org.example.usecase.GerenciarGastoApiUseCase;

import java.time.LocalDate;
import java.util.*;

public class LancamentoPeriodoControllerAPI {

    @FXML private TextField txtDescricao, txtValor;
    @FXML private Spinner<Integer> spAno;
    @FXML private ComboBox<String> cbCategoria, cbMetodo;

    // Checkboxes dos meses
    @FXML private CheckBox chkJan, chkFev, chkMar, chkAbr, chkMai, chkJun;
    @FXML private CheckBox chkJul, chkAgo, chkSet, chkOut, chkNov, chkDez;

    private final GastoRepositoryAPI repository = RepositoryFactory.getRepository();
    private final GerenciarGastoApiUseCase useCase = new GerenciarGastoApiUseCase(repository);

    private Map<Integer, CheckBox> mapaMeses;

    @FXML
    public void initialize() {
        // Configura o Spinner com o ano atual
        int anoAtual = LocalDate.now().getYear();
        spAno.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2020, 2035, anoAtual));

        // Carrega os combos seguindo a lógica que você usa na Main
        cbCategoria.getItems().setAll(repository.buscarTodasCategorias().stream().map(Categoria::getNome).toList());
        cbMetodo.getItems().setAll(repository.buscarTodosMetodos().stream().map(MetodoPagamentoApi::getNome).toList());

        // Mapeia o número do mês ao respectivo CheckBox para facilitar o loop depois
        mapaMeses = new HashMap<>();
        mapaMeses.put(1, chkJan);   mapaMeses.put(2, chkFev);   mapaMeses.put(3, chkMar);
        mapaMeses.put(4, chkAbr);   mapaMeses.put(5, chkMai);   mapaMeses.put(6, chkJun);
        mapaMeses.put(7, chkJul);   mapaMeses.put(8, chkAgo);   mapaMeses.put(9, chkSet);
        mapaMeses.put(10, chkOut);  mapaMeses.put(11, chkNov);  mapaMeses.put(12, chkDez);
    }

    @FXML
    protected void aoSalvarPeriodo() {
        try {
            String descricao = txtDescricao.getText().trim();
            String nomeCategoria = cbCategoria.getValue();
            String nomeMetodo = cbMetodo.getValue();

            if (descricao.isEmpty() || txtValor.getText().isEmpty() || nomeCategoria == null || nomeMetodo == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Por favor, preencha todos os campos obrigatórios!");
                return;
            }

            double valor = Double.parseDouble(txtValor.getText().replace(",", "."));
            int ano = spAno.getValue();

            // Verifica se pelo menos um mês foi selecionado
            boolean algumSelecionado = mapaMeses.values().stream().anyMatch(CheckBox::isSelected);
            if (!algumSelecionado) {
                mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Selecione pelo menos um mês para realizar o lançamento!");
                return;
            }

            System.out.println("--- INICIANDO LANÇAMENTO EM MASSA ---");

            // Percorre o mapa e gera um registro para cada mês selecionado
            for (Map.Entry<Integer, CheckBox> entry : mapaMeses.entrySet()) {
                if (entry.getValue().isSelected()) {
                    int mes = entry.getKey();

                    // Define a data como o dia 1 daquele mês/ano selecionado
                    LocalDate dataGasto = LocalDate.of(ano, mes, 1);

                    Gasto gasto = new Gasto(
                            descricao,
                            valor,
                            dataGasto,
                            new Categoria(nomeCategoria),
                            new MetodoPagamento(nomeMetodo, 0),
                            false,
                            1, // Considerado como parcela única por mês
                            1
                    );

                    System.out.println("Salvando no período: " + descricao + " | Mês: " + mes + "/" + ano);
                    useCase.registrarGasto(gasto);
                }
            }

            System.out.println("--- LANÇAMENTO EM MASSA CONCLUÍDO ---");
            fecharJanela();

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Digite um valor numérico válido!");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Falha ao salvar lançamentos: " + e.getMessage());
        }
    }

    @FXML
    protected void aoCancelar() {
        fecharJanela();
    }

    private void fecharJanela() {
        Stage stage = (Stage) txtDescricao.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo, mensagem);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}