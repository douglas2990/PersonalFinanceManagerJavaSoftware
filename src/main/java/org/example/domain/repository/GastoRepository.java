package org.example.domain.repository;


import org.example.domain.entity.Gasto;
import java.util.List;

public interface GastoRepository {
    void salvar(Gasto gasto);
    List<Gasto> buscarTodos();
    List<Gasto> buscarPorMesEAno(int mes, int ano);
}
