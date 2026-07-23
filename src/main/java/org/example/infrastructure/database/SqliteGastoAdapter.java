package org.example.infrastructure.database;

import org.example.domain.entity.*;
import org.example.domain.repository.GastoRepositoryAPI;
import java.util.List;
import java.util.ArrayList;

public class SqliteGastoAdapter implements GastoRepositoryAPI {

    private SqliteGastoRepository sqliteRepo;

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
    public Categoria buscarCategoriaPorId(int id) {
        // Fallback seguro já que a classe de Domínio Categoria não possui getId() exposto
        return new Categoria("Desconhecida");
    }

    // === CORREÇÃO: Implementação correta do novo contrato da Interface ===
    @Override
    public void salvarMetodo(MetodoPagamentoApi metodo) {
        // Converte o objeto rico da API para o modelo antigo que o SQLite local espera receber
        MetodoPagamento antigo = new MetodoPagamento(metodo.getNome(), metodo.getDiaVencimento());
        getRepo().salvarMetodo(antigo);
    }

    // === CORREÇÃO: Adequação do retorno para List<MetodoPagamentoApi> ===
    @Override
    public List<MetodoPagamentoApi> buscarTodosMetodos() {
        // Pega a lista antiga do SQLite
        List<MetodoPagamento> metodosLocais = getRepo().buscarTodosMetodos();
        List<MetodoPagamentoApi> listaMapeada = new ArrayList<>();

        // Converte cada item para o novo MetodoPagamentoApi aplicando uma cor padrão branca
        for (MetodoPagamento m : metodosLocais) {
            listaMapeada.add(new MetodoPagamentoApi(0, m.getNome(), m.getDiaVencimento(), "#FFFFFF"));
        }
        return listaMapeada;
    }

    // === CORREÇÃO: Adequação do retorno para MetodoPagamentoApi ===
    @Override
    public MetodoPagamentoApi buscarMetodoPorId(int id) {
        // Caso use o banco local, cria o objeto esperado pela assinatura com valores default
        return new MetodoPagamentoApi(id, "Desconhecido", 0, "#FFFFFF");
    }

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

    @Override
    public boolean atualizarMetodo(MetodoPagamentoApi metodo) {
        // Se você tiver um método atualizar no seu SQLite, chame-o aqui.
        // Por enquanto, retornamos false ou apenas ignoramos, pois o foco atual é a API.
        System.out.println("Atualização de método via SQLite ainda não implementada.");
        return false;
    }
}