package org.example.domain.repository;

import org.example.domain.entity.Categoria;
import org.example.domain.entity.Gasto;
import org.example.domain.entity.MetodoPagamentoApi; // Importante: classe atualizada
import java.util.List;

public interface GastoRepositoryAPI {

    // Gastos
    void salvar(Gasto gasto);
    void atualizarGasto(Gasto gasto);
    void removerGasto(int id);
    List<Gasto> buscarTodos();
    List<Gasto> buscarPorMesEAno(int mes, int ano);

    // Categorias
    void salvarCategoria(Categoria categoria);
    List<Categoria> buscarTodasCategorias();
    Categoria buscarCategoriaPorId(int id);

    // Métodos de Pagamento - Atualizado para usar MetodoPagamentoApi
    void salvarMetodo(MetodoPagamentoApi metodo);
    List<MetodoPagamentoApi> buscarTodosMetodos();
    MetodoPagamentoApi buscarMetodoPorId(int id);

    // Metas
    void salvarOuAtualizarMeta(String categoria, int mes, int ano, double valor);
    void salvarOuAtualizarMetaAnual(String categoria, int ano, double valor);
    double buscarMetaFinal(String categoria, int mes, int ano);
    double buscarSomaGastosPorCategoria(String categoria, int mes, int ano);
    double buscarMetaAnual(String categoria, int ano);
    double buscarMetaPorCategoria(String categoria, int mes, int ano);
    // Adicione esta linha na sua interface
    boolean atualizarMetodo(MetodoPagamentoApi metodo);
}