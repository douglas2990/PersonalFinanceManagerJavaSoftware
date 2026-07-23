package org.example.infrastructure.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.example.usecase.CadastroMetaUseCase;

import java.time.LocalDate;

public class CadastroMetaControllerAPI {

    // Componentes da sua interface FXML
    @FXML
    private ComboBox<CategoriaItem> comboCategoria;

    @FXML
    private TextField txtAno;

    @FXML
    private TextField txtValorMensal;

    @FXML
    private CheckBox chkDecimoTerceiro;

    @FXML
    private Button btnSalvar;

    // A nossa classe com a inteligência de negócio
    private CadastroMetaUseCase cadastroUseCase;

    @FXML
    public void initialize() {
        cadastroUseCase = new CadastroMetaUseCase();

        // Sugere o ano atual por defeito
        txtAno.setText(String.valueOf(java.time.Year.now().getValue()));

        // Chama o carregamento sem travar a tela
        carregarCategorias();
    }

    private void carregarCategorias() {
        /*
         * REGRA DE OURO DO JAVAFX: Nunca faça requisições à API ou ao Banco de Dados
         * diretamente na Thread Principal, senão a tela congela ao abrir!
         * Vamos usar uma nova Thread (Background) para buscar os dados.
         */
        new Thread(() -> {
            try {
                // 1. AQUI VOCÊ FAZ O TRABALHO PESADO (Fora da tela)
                // Ex: List<Categoria> categoriasDaApi = repository.buscarCategorias();

                // Simula um delay (remova isso no código real)
                Thread.sleep(200);

                // 2. QUANDO OS DADOS CHEGAREM, ATUALIZAMOS A TELA USANDO Platform.runLater
                Platform.runLater(() -> {
                    // Exemplo fictício. Substitua pelo loop da sua API
                    comboCategoria.getItems().add(new CategoriaItem(1, "Alimentação"));
                    comboCategoria.getItems().add(new CategoriaItem(2, "Lazer"));
                    comboCategoria.getItems().add(new CategoriaItem(3, "Salário"));
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível carregar as categorias."));
            }
        }).start();
    }

    @FXML
    public void onSalvarAction() {
        try {
            // 1. Capturar os dados da interface gráfica
            CategoriaItem categoriaSelecionada = comboCategoria.getValue();
            if (categoriaSelecionada == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Por favor, selecione uma categoria.");
                return;
            }

            int ano = Integer.parseInt(txtAno.getText());
            // Substitui a vírgula por ponto, caso o utilizador escreva "1000,50"
            double valor = Double.parseDouble(txtValorMensal.getText().replace(",", "."));
            boolean incluirDecimoTerceiro = chkDecimoTerceiro.isSelected();

            // Desativa o botão para evitar duplos cliques enquanto salva
            btnSalvar.setDisable(true);

            // 2. Chama a inteligência do nosso UseCase
            cadastroUseCase.salvarMetasDoAno(
                    categoriaSelecionada.getId(),
                    ano,
                    valor,
                    incluirDecimoTerceiro,
                    new CadastroMetaUseCase.Callback() {

                        @Override
                        public void onSucesso() {
                            // O JavaFX exige que as alterações na interface gráfica sejam feitas na Thread principal
                            Platform.runLater(() -> {
                                mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Metas configuradas para todo o ano de " + ano + "!");
                                btnSalvar.setDisable(false);
                                limparCampos();
                            });
                        }

                        @Override
                        public void onErro(String mensagem) {
                            Platform.runLater(() -> {
                                mostrarAlerta(Alert.AlertType.ERROR, "Erro", "Ocorreu um erro: " + mensagem);
                                btnSalvar.setDisable(false);
                            });
                        }
                    }
            );

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro de Validação", "Por favor, introduza números válidos para o Ano e Valor.");
        }
    }

    private void limparCampos() {
        txtValorMensal.clear();
        comboCategoria.getSelectionModel().clearSelection();
        chkDecimoTerceiro.setSelected(false);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    // Classe auxiliar simples para a ComboBox
    public static class CategoriaItem {
        private final int id;
        private final String nome;

        public CategoriaItem(int id, String nome) {
            this.id = id;
            this.nome = nome;
        }

        public int getId() { return id; }

        @Override
        public String toString() { return nome; }
    }
}