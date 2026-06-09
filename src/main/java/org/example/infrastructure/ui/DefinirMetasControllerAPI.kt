package org.example.infrastructure.ui

import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.stage.Stage
import org.example.domain.entity.Categoria
import org.example.domain.repository.GastoRepositoryAPI
import org.example.domain.repository.RepositoryFactory
import java.time.LocalDate

// IMPORTANTE: Importar sua interface e a fábrica

class DefinirMetasControllerAPI {
    @FXML
    private val cbCategoria: ComboBox<Categoria?>? = null

    @FXML
    private val txtValorMeta: TextField? = null

    // A MUDANÇA ESTÁ AQUI: Usa a interface, a Factory fornece a instância correta
    private val repository: GastoRepositoryAPI = RepositoryFactory.getRepository()

    private var mesAtual = 0
    private var anoAtual = 0

    @FXML
    fun initialize() {
        carregarCategorias()
        this.mesAtual = LocalDate.now().getMonthValue()
        this.anoAtual = LocalDate.now().getYear()
    }

    private fun carregarCategorias() {
        // Como o 'repository' é um GastoRepositoryAPI, ele pode ser o Adapter ou a API
        cbCategoria!!.getItems().setAll(repository.buscarTodasCategorias())
    }

    fun setPeriodo(mes: Int, ano: Int) {
        this.mesAtual = mes
        this.anoAtual = ano
    }

    @FXML
    private fun aoSalvar() {
        try {
            val selecionada = cbCategoria!!.getValue()
            val valor = txtValorMeta!!.getText().replace(",", ".").toDouble()

            if (selecionada != null) {
                // Chama o método definido na sua interface GastoRepositoryAPI
                repository.salvarOuAtualizarMeta(selecionada.getNome(), mesAtual, anoAtual, valor)

                val alert = Alert(Alert.AlertType.INFORMATION, "Meta definida com sucesso!")
                alert.showAndWait()
                aoCancelar()
            }
        } catch (e: Exception) {
            System.err.println("Erro ao salvar meta: " + e.message)
        }
    }

    @FXML
    private fun aoCancelar() {
        (txtValorMeta!!.getScene().getWindow() as Stage).close()
    }
}