package org.example.domain.repository;

import org.example.domain.entity.MetodoPagamento;
import java.util.List;

public interface MetodoRepository {
    void salvarMetodo(MetodoPagamento metodo); // Mudei o nome aqui para combinar com seu código
    List<MetodoPagamento> buscarTodosMetodos();
}
