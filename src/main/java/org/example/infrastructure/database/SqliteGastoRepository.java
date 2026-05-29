package org.example.infrastructure.database;

import org.example.domain.entity.Categoria;
import org.example.domain.entity.Gasto;
import org.example.domain.entity.MetodoPagamento;
import org.example.domain.repository.GastoRepository;
import org.example.domain.repository.MetodoRepository;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SqliteGastoRepository implements GastoRepository, MetodoRepository {

    // Método centralizado que descobre dinamicamente onde o banco está guardado
    private String obterUrlConexao() {
        java.util.Properties props = new java.util.Properties();
        File configFile = new File("config.properties");
        String caminhoDb = "";

        if (configFile.exists()) {
            try (java.io.FileInputStream in = new java.io.FileInputStream(configFile)) {
                props.load(in);
                caminhoDb = props.getProperty("database.path");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Fallback: se o arquivo não existir (ou antes do primeiro start), usa o padrão
        if (caminhoDb == null || caminhoDb.isEmpty()) {
            caminhoDb = System.getProperty("user.home") + File.separator + "Documents" + File.separator + "MinhasFinancas" + File.separator + "financas.db";
            new File(caminhoDb).getParentFile().mkdirs();
        }

        return "jdbc:sqlite:" + caminhoDb;
    }

    // Gerenciador oficial de conexões da classe
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(obterUrlConexao());
    }

    public SqliteGastoRepository() {
        initDatabase();
    }

    private void initDatabase() {
        // AJUSTADO: Agora usa o getConnection() dinâmico
        try (Connection conn = getConnection()) {
            if (conn != null) {
                Statement stmt = conn.createStatement();

                String sqlGastos = "CREATE TABLE IF NOT EXISTS gastos (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "descricao TEXT NOT NULL," +
                        "valor REAL," +
                        "data TEXT," +
                        "categoria TEXT," +
                        "metodo TEXT," +
                        "total_parcelas INTEGER," +
                        "parcela_atual INTEGER" +
                        ");";

                String sqlMetodos = "CREATE TABLE IF NOT EXISTS metodos_pagamento (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "nome TEXT NOT NULL UNIQUE," +
                        "dia_vencimento INTEGER" +
                        ");";

                String sqlCategorias = "CREATE TABLE IF NOT EXISTS categorias (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "nome TEXT NOT NULL UNIQUE" +
                        ");";

                String sqlMetas = "CREATE TABLE IF NOT EXISTS metas_categoria (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "nome_categoria TEXT NOT NULL," +
                        "mes INTEGER NOT NULL," +
                        "ano INTEGER NOT NULL," +
                        "valor_meta REAL NOT NULL," +
                        "UNIQUE(nome_categoria, mes, ano)" +
                        ");";

                String sqlMetaAnual = "CREATE TABLE IF NOT EXISTS metas_anuais (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "nome_categoria TEXT NOT NULL," +
                        "ano INTEGER NOT NULL," +
                        "valor_padrao REAL NOT NULL," +
                        "UNIQUE(nome_categoria, ano)" +
                        ");";

                stmt.execute(sqlGastos);
                stmt.execute(sqlMetodos);
                stmt.execute(sqlCategorias);
                stmt.execute(sqlMetas);
                stmt.execute(sqlMetaAnual);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao iniciar banco: " + e.getMessage());
        }
    }

    // --- MÉTODOS DE GASTOS ---

    @Override
    public void salvar(Gasto gasto) {
        String sql = "INSERT INTO gastos(descricao, valor, data, categoria, metodo, total_parcelas, parcela_atual) VALUES(?,?,?,?,?,?,?)";

        // AJUSTADO: Usando getConnection()
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, gasto.getDescricao());
            pstmt.setDouble(2, gasto.getValor());
            pstmt.setString(3, gasto.getData().toString());
            pstmt.setString(4, gasto.getCategoria().getNome());
            pstmt.setString(5, gasto.getMetodo().getNome());
            pstmt.setInt(6, gasto.getTotalParcelas());
            pstmt.setInt(7, gasto.getParcelaAtual());
            pstmt.executeUpdate();
            System.out.println("✅ Gasto salvo no banco!");
        } catch (SQLException e) {
            System.out.println("❌ Erro ao salvar gasto: " + e.getMessage());
        }
    }

    @Override
    public List<Gasto> buscarTodos() {
        List<Gasto> lista = new ArrayList<>();
        String sql = "SELECT * FROM gastos ORDER BY data DESC";

        // AJUSTADO: Usando getConnection()
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(converterParaGasto(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar gastos: " + e.getMessage());
        }
        return lista;
    }

    // --- MÉTODOS DE PAGAMENTO ---

    @Override
    public void salvarMetodo(MetodoPagamento metodo) {
        String sql = "INSERT INTO metodos_pagamento(nome, dia_vencimento) VALUES(?,?)";
        // AJUSTADO: Usando getConnection()
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, metodo.getNome());
            pstmt.setInt(2, metodo.getDiaVencimento());
            pstmt.executeUpdate();
            System.out.println("✅ Cartão/Método salvo: " + metodo.getNome());
        } catch (SQLException e) {
            System.out.println("❌ Erro ao salvar método: " + e.getMessage());
        }
    }

    @Override
    public List<MetodoPagamento> buscarTodosMetodos() {
        List<MetodoPagamento> lista = new ArrayList<>();
        String sql = "SELECT nome, dia_vencimento FROM metodos_pagamento";

        // AJUSTADO: Usando getConnection()
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new MetodoPagamento(
                        rs.getString("nome"),
                        rs.getInt("dia_vencimento")
                ));
            }
        } catch (SQLException e) {
            System.out.println("❌ Erro ao buscar métodos: " + e.getMessage());
        }
        return lista;
    }

    // --- MÉTODOS DE CATEGORIAS ---

    public void salvarCategoria(Categoria categoria) {
        String sql = "INSERT INTO categorias(nome) VALUES(?)";
        // AJUSTADO: Usando getConnection()
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoria.getNome());
            pstmt.executeUpdate();
            System.out.println("✅ Categoria salva: " + categoria.getNome());
        } catch (SQLException e) {
            System.out.println("Erro ao salvar categoria: " + e.getMessage());
        }
    }

    public List<Categoria> buscarTodasCategorias() {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT id, nome FROM categorias";
        // AJUSTADO: Usando getConnection()
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Categoria(rs.getInt("id"), rs.getString("nome")));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar categorias: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public List<Gasto> buscarPorMesEAno(int mes, int ano) {
        List<Gasto> lista = new ArrayList<>();
        String sql = "SELECT * FROM gastos WHERE strftime('%m', data) = ? AND strftime('%Y', data) = ? ORDER BY data DESC";

        // AJUSTADO: Usando getConnection()
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, String.format("%02d", mes));
            pstmt.setString(2, String.valueOf(ano));

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                lista.add(converterParaGasto(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao filtrar por mês: " + e.getMessage());
        }
        return lista;
    }

    private Gasto converterParaGasto(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String dataString = rs.getString("data");
        LocalDate dataFinal = LocalDate.parse(dataString);

        Categoria categoria = new Categoria(rs.getString("categoria"));
        MetodoPagamento metodo = new MetodoPagamento(rs.getString("metodo"), 0);

        return new Gasto(
                id,
                rs.getString("descricao"),
                rs.getDouble("valor"),
                dataFinal,
                categoria,
                metodo,
                false,
                rs.getInt("total_parcelas"),
                rs.getInt("parcela_atual")
        );
    }

    public void salvarOuAtualizarMeta(String nomeCategoria, int mes, int ano, double valor) {
        String sql = "INSERT OR REPLACE INTO metas_categoria(nome_categoria, mes, ano, valor_meta) VALUES(?,?,?,?)";

        // AJUSTADO: Usando getConnection()
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomeCategoria);
            pstmt.setInt(2, mes);
            pstmt.setInt(3, ano);
            pstmt.setDouble(4, valor);
            pstmt.executeUpdate();
            System.out.println("✅ Meta de " + nomeCategoria + " atualizada para R$ " + valor);
        } catch (SQLException e) {
            System.out.println("❌ Erro ao salvar meta: " + e.getMessage());
        }
    }

    public double buscarMetaPorCategoria(String nomeCategoria, int mes, int ano) {
        String sql = "SELECT valor_meta FROM metas_categoria WHERE nome_categoria = ? AND mes = ? AND ano = ?";
        // AJUSTADO: Usando getConnection()
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomeCategoria);
            pstmt.setInt(2, mes);
            pstmt.setInt(3, ano);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("valor_meta");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar meta: " + e.getMessage());
        }
        return 0.0;
    }

    public double buscarMetaFinal(String categoria, int mes, int ano) {
        double mensal = buscarMetaPorCategoria(categoria, mes, ano);
        if (mensal > 0) return mensal;
        return buscarMetaAnual(categoria, ano);
    }

    public double buscarMetaAnual(String nomeCategoria, int ano) {
        String sql = "SELECT valor_padrao FROM metas_anuais WHERE nome_categoria = ? AND ano = ?";
        // AJUSTADO: Usando getConnection()
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomeCategoria);
            pstmt.setInt(2, ano);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("valor_padrao");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar meta anual: " + e.getMessage());
        }
        return 0.0;
    }

    public void salvarOuAtualizarMetaAnual(String nomeCategoria, int ano, double valor) {
        String sql = "INSERT OR REPLACE INTO metas_anuais(nome_categoria, ano, valor_padrao) VALUES(?,?,?)";
        // AJUSTADO: Usando getConnection()
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomeCategoria);
            pstmt.setInt(2, ano);
            pstmt.setDouble(3, valor);
            pstmt.executeUpdate();
            System.out.println("✅ Meta ANUAL de " + nomeCategoria + " definida: R$ " + valor);
        } catch (SQLException e) {
            System.out.println("❌ Erro ao salvar meta anual: " + e.getMessage());
        }
    }

    public double buscarSomaGastosPorCategoria(String nomeCategoria, int mes, int ano) {
        String sql = "SELECT SUM(valor) as total FROM gastos WHERE categoria = ? AND strftime('%m', data) = ? AND strftime('%Y', data) = ?";
        // AJUSTADO: Usando getConnection()
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nomeCategoria);
            pstmt.setString(2, String.format("%02d", mes));
            pstmt.setString(3, String.valueOf(ano));

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao somar gastos: " + e.getMessage());
        }
        return 0.0;
    }

    public void removerGasto(int id) {
        String sql = "DELETE FROM gastos WHERE id = ?";
        // AJUSTADO: Usando getConnection()
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            System.out.println("✅ Gasto removido com sucesso!");
        } catch (SQLException e) {
            System.err.println("❌ Erro ao remover gasto: " + e.getMessage());
        }
    }

    public void atualizarGasto(Gasto gasto) {
        String sql = "UPDATE gastos SET descricao = ?, valor = ?, data = ? WHERE id = ?";
        // AJUSTADO: Usando getConnection()
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, gasto.getDescricao());
            pstmt.setDouble(2, gasto.getValor());
            pstmt.setString(3, gasto.getData().toString());
            pstmt.setInt(4, gasto.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar: " + e.getMessage());
        }
    }
}