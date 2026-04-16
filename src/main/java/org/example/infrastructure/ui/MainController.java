package org.example.infrastructure.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.example.domain.entity.Gasto;
import org.example.domain.entity.MetodoPagamento;
import org.example.infrastructure.database.SqliteGastoRepository;
import org.example.usecase.GerenciarGastoUseCase;

import java.time.LocalDate;

public class MainController {
    @FXML
    private TextField txtDescricao;
    @FXML
    private TextField txtValor;

    // Instanciando as camadas (isso depois pode evoluir para Injeção de Dependência)
    private final SqliteGastoRepository repository = new SqliteGastoRepository();
    private final GerenciarGastoUseCase useCase = new GerenciarGastoUseCase(repository);

    @FXML
    protected void aoSalvar() {
        String descricao = txtDescricao.getText();
        double valor = Double.parseDouble(txtValor.getText());

        // Criando um método padrão por enquanto (ex: Nubank)
        MetodoPagamento metodoPadrao = new MetodoPagamento("NUBANK", 10);

        // Criando o objeto Gasto
        Gasto novoGasto = new Gasto(
                descricao,
                valor,
                LocalDate.now(),
                "Geral",
                metodoPadrao,
                false,
                1,
                1
        );

        // Chamando o UseCase para salvar no SQLite
        useCase.registrarGasto(novoGasto);

        System.out.println("✅ Gasto salvo com sucesso: " + descricao);

        // Limpando os campos após salvar (igual no Android)
        txtDescricao.clear();
        txtValor.clear();
    }
}
