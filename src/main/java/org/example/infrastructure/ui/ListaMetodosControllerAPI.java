package org.example.infrastructure.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.example.domain.entity.MetodoPagamentoApi;
import org.example.domain.repository.GastoRepositoryAPI;
import org.example.domain.repository.RepositoryFactory;

import java.io.IOException;
import java.util.List;

public class ListaMetodosControllerAPI {

    @FXML private TableView<MetodoPagamentoApi> tableMetodos;
    @FXML private TableColumn<MetodoPagamentoApi, String> colNome, colVencimento, colCor;
    @FXML private TableColumn<MetodoPagamentoApi, Void> colAcoes;

    // Conectando com a sua API através do Factory
    private final GastoRepositoryAPI repository = RepositoryFactory.getRepository();

    @FXML
    public void initialize() {
        configurarColunas();
        carregarDados();
    }

    private void configurarColunas() {
        colNome.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNome()));

        colVencimento.setCellValueFactory(cellData -> {
            Integer v = cellData.getValue().getDiaVencimento();
            return new SimpleStringProperty(v != null ? v.toString() : "-");
        });

        colCor.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCor()));

        // Configurando a coluna de botões
        colAcoes.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("✏️");
            private final Button btnExcluir = new Button("❌");
            private final HBox container = new HBox(10, btnEditar, btnExcluir);

            {
                btnEditar.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");
                btnEditar.setOnAction(e -> {
                    MetodoPagamentoApi metodo = getTableView().getItems().get(getIndex());
                    editarMetodo(metodo);
                });

                btnExcluir.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-cursor: hand;");
                btnExcluir.setOnAction(e -> {
                    MetodoPagamentoApi metodo = getTableView().getItems().get(getIndex());
                    excluirMetodo(metodo);
                });

                container.setStyle("-fx-alignment: CENTER;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void carregarDados() {
        // Busca os métodos direto do seu backend em C#
        List<MetodoPagamentoApi> lista = repository.buscarTodosMetodos();
        tableMetodos.getItems().setAll(lista);
    }

    private void editarMetodo(MetodoPagamentoApi metodo) {
        Dialog<MetodoPagamentoApi> dialog = new Dialog<>();
        dialog.setTitle("Editar Método");
        dialog.setHeaderText("Editando: " + metodo.getNome());

        ButtonType btnSalvar = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSalvar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        // Campos de Nome e Vencimento
        TextField txtNome = new TextField(metodo.getNome());
        TextField txtVencimento = new TextField(metodo.getDiaVencimento() > 0 ? String.valueOf(metodo.getDiaVencimento()) : "");

        // 1. Criando o ColorPicker no lugar do TextField
        ColorPicker colorPicker = new ColorPicker();

        // 2. Tentando carregar a cor atual do banco para preencher a paleta
        try {
            if (metodo.getCor() != null && !metodo.getCor().trim().isEmpty()) {
                colorPicker.setValue(javafx.scene.paint.Color.web(metodo.getCor()));
            } else {
                colorPicker.setValue(javafx.scene.paint.Color.WHITE); // Branco por padrão
            }
        } catch (IllegalArgumentException e) {
            colorPicker.setValue(javafx.scene.paint.Color.WHITE); // Se a cor no banco estiver inválida, usa branco
        }

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(txtNome, 1, 0);
        grid.add(new Label("Dia Vencimento:"), 0, 1);
        grid.add(txtVencimento, 1, 1);
        grid.add(new Label("Cor:"), 0, 2);
        grid.add(colorPicker, 1, 2); // Adicionando o ColorPicker na tela

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnSalvar) {
                metodo.setNome(txtNome.getText());

                // 3. Convertendo a cor escolhida na paleta para o formato Hexadecimal (#RRGGBB)
                javafx.scene.paint.Color corSelecionada = colorPicker.getValue();
                String corHex = String.format("#%02X%02X%02X",
                        (int) (corSelecionada.getRed() * 255),
                        (int) (corSelecionada.getGreen() * 255),
                        (int) (corSelecionada.getBlue() * 255));

                metodo.setCor(corHex);

                try {
                    metodo.setDiaVencimento(Integer.parseInt(txtVencimento.getText()));
                } catch (NumberFormatException e) {
                    metodo.setDiaVencimento(0);
                }
                return metodo;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(metodoAtualizado -> {
            boolean sucesso = repository.atualizarMetodo(metodoAtualizado);
            if (sucesso) {
                exibirAlerta("Sucesso", "Método atualizado com sucesso!", Alert.AlertType.INFORMATION);
                carregarDados();
            } else {
                exibirAlerta("Erro", "Falha ao atualizar o método na API.", Alert.AlertType.ERROR);
            }
        });
    }

    private void excluirMetodo(MetodoPagamentoApi metodo) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Tem certeza que deseja excluir o método '" + metodo.getNome() + "'?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            // Deixando o aviso pronto para quando formos implementar a exclusão
            exibirAlerta("Em breve", "Vamos conectar isso ao DELETE da API no próximo passo!", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void voltar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/metodos_view_api.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Cadastro de Métodos");
            stage.setScene(new Scene(loader.load()));

            Stage stageAtual = (Stage) tableMetodos.getScene().getWindow();
            stageAtual.close();

            stage.show();
        } catch (IOException e) {
            exibirAlerta("Erro", "Erro ao voltar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void exibirAlerta(String titulo, String mensagem, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}