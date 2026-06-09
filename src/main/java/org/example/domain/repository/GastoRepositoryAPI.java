package org.example.domain.repository;

import org.example.domain.entity.Categoria;
import org.example.domain.entity.Gasto;
import org.example.domain.entity.MetodoPagamento;
import java.util.List;

public interface GastoRepositoryAPI {
    // Gastos
    void salvar(Gasto gasto);
    void atualizarGasto(Gasto gasto); // ADICIONE ISSO (O erro apontava falta disso)
    void removerGasto(int id);
    List<Gasto> buscarTodos();
    List<Gasto> buscarPorMesEAno(int mes, int ano);

    // Categorias
    void salvarCategoria(Categoria categoria);
    List<Categoria> buscarTodasCategorias();

    // Métodos de Pagamento
    void salvarMetodo(MetodoPagamento metodo);
    List<MetodoPagamento> buscarTodosMetodos(); // ADICIONE ISSO (Estava faltando)

    // Metas
    void salvarOuAtualizarMeta(String categoria, int mes, int ano, double valor);
    void salvarOuAtualizarMetaAnual(String categoria, int ano, double valor);
    double buscarMetaFinal(String categoria, int mes, int ano);
    double buscarSomaGastosPorCategoria(String categoria, int mes, int ano);
    double buscarMetaAnual(String categoria, int ano);
    double buscarMetaPorCategoria(String categoria, int mes, int ano);
}