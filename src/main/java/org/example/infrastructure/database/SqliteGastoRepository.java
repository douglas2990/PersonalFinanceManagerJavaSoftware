package org.example.infrastructure.database;

import org.example.domain.entity.Categoria; // Importante importar a nova Entity
import org.example.domain.entity.Gasto;
import org.example.domain.entity.MetodoPagamento;
import org.example.domain.repository.GastoRepository;
import org.example.domain.repository.MetodoRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SqliteGastoRepository implements GastoRepository, MetodoRepository {
    private static final String URL = "jdbc:sqlite:financas.db";

    public SqliteGastoRepository() {
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(URL)) {
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

                // Tabela de Metas por Categoria e Mês
                String sqlMetas = "CREATE TABLE IF NOT EXISTS metas_categoria (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "nome_categoria TEXT NOT NULL," + // Usando o nome para simplificar o JOIN com seus gastos atuais
                        "mes INTEGER NOT NULL," +
                        "ano INTEGER NOT NULL," +
                        "valor_meta REAL NOT NULL," +
                        "UNIQUE(nome_categoria, mes, ano)" + // Impede metas duplicadas para o mesmo mês/categoria
                        ");";



                stmt.execute(sqlGastos);
                stmt.execute(sqlMetodos);
                stmt.execute(sqlCategorias);
                stmt.execute(sqlMetas);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao iniciar banco: " + e.getMessage());
        }
    }

    // --- MÉTODOS DE GASTOS ---

    @Override
    public void salvar(Gasto gasto) {
        String sql = "INSERT INTO gastos(descricao, valor, data, categoria, metodo, total_parcelas, parcela_atual) VALUES(?,?,?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, gasto.getDescricao());
            pstmt.setDouble(2, gasto.getValor());
            pstmt.setString(3, gasto.getData().toString());

            // AJUSTE: Pegamos o nome do objeto Categoria
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

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(converterParaGasto(rs)); // Apenas uma linha aqui!
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
        try (Connection conn = DriverManager.getConnection(URL);
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

        try (Connection conn = DriverManager.getConnection(URL);
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
        try (Connection conn = DriverManager.getConnection(URL);
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
        try (Connection conn = DriverManager.getConnection(URL);
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
        // Filtro usando funções de data do SQLite
        String sql = "SELECT * FROM gastos WHERE strftime('%m', data) = ? AND strftime('%Y', data) = ? ORDER BY data DESC";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, String.format("%02d", mes)); // Garante "04" em vez de "4"
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
        String dataString = rs.getString("data");
        LocalDate dataFinal = LocalDate.parse(dataString);

        // Criamos os objetos de valor a partir das strings/dados do banco
        Categoria categoria = new Categoria(rs.getString("categoria"));

        // Para o método de pagamento, inicializamos com o nome vindo do banco
        MetodoPagamento metodo = new MetodoPagamento(rs.getString("metodo"), 0);

        return new Gasto(
                rs.getString("descricao"),
                rs.getDouble("valor"),
                dataFinal,
                categoria,
                metodo,
                false, // isMensal (pode ser ajustado se você tiver essa coluna)
                rs.getInt("total_parcelas"),
                rs.getInt("parcela_atual")
        );
    }
    public void definirMeta(int categoriaId, int mes, int ano, double valor) {
        String sql = "INSERT OR REPLACE INTO metas_categoria(categoria_id, mes, ano, valor_meta) VALUES(?,?,?,?)";
        // ... lógica do PreparedStatement ...
    }

    public void salvarOuAtualizarMeta(String nomeCategoria, int mes, int ano, double valor) {
        // O "INSERT OR REPLACE" é o segredo aqui: se já existir meta para esse mês/categoria, ele apenas atualiza o valor
        String sql = "INSERT OR REPLACE INTO metas_categoria(nome_categoria, mes, ano, valor_meta) VALUES(?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(URL);
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
        try (Connection conn = DriverManager.getConnection(URL);
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
        return 0.0; // Se não houver meta definida, retorna zero
    }
}