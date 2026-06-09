package org.example.infrastructure.database;

import org.example.domain.entity.*;
import org.example.domain.repository.GastoRepositoryAPI;
import java.util.List;

public class SqliteGastoAdapter implements GastoRepositoryAPI {

    private SqliteGastoRepository sqliteRepo;

    // Método privado que garante a instância preguiçosa (Lazy Loading)
    private SqliteGastoRepository getRepo() {
        if (sqliteRepo == null) {
            sqliteRepo = new SqliteGastoRepository();
        }
        return sqliteRepo;
    }

    @Override
    public void salvar(Gasto gasto) { getRepo().salvar(gasto); }

    @Override
    public void atualizarGasto(Gasto gasto) { getRepo().atualizarGasto(gasto); }

    @Override
    public void removerGasto(int id) { getRepo().removerGasto(id); }

    @Override
    public List<Gasto> buscarTodos() { return getRepo().buscarTodos(); }

    @Override
    public List<Gasto> buscarPorMesEAno(int mes, int ano) { return getRepo().buscarPorMesEAno(mes, ano); }

    @Override
    public void salvarCategoria(Categoria cat) { getRepo().salvarCategoria(cat); }

    @Override
    public List<Categoria> buscarTodasCategorias() { return getRepo().buscarTodasCategorias(); }

    @Override
    public void salvarMetodo(MetodoPagamento metodo) { getRepo().salvarMetodo(metodo); }

    @Override
    public List<MetodoPagamento> buscarTodosMetodos() { return getRepo().buscarTodosMetodos(); }

    @Override
    public void salvarOuAtualizarMeta(String categoria, int mes, int ano, double valor) {
        getRepo().salvarOuAtualizarMeta(categoria, mes, ano, valor);
    }

    @Override
    public void salvarOuAtualizarMetaAnual(String categoria, int ano, double valor) {
        getRepo().salvarOuAtualizarMetaAnual(categoria, ano, valor);
    }

    @Override
    public double buscarMetaFinal(String categoria, int mes, int ano) {
        return getRepo().buscarMetaFinal(categoria, mes, ano);
    }

    @Override
    public double buscarSomaGastosPorCategoria(String categoria, int mes, int ano) {
        return getRepo().buscarSomaGastosPorCategoria(categoria, mes, ano);
    }

    @Override
    public double buscarMetaAnual(String categoria, int ano) {
        return getRepo().buscarMetaAnual(categoria, ano);
    }

    @Override
    public double buscarMetaPorCategoria(String categoria, int mes, int ano) {
        return getRepo().buscarMetaPorCategoria(categoria, mes, ano);
    }
}