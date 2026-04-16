package org.example.infrastructure.database;

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

                // Tabela de Gastos com colunas de parcelamento
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

                // Tabela de Métodos de Pagamento
                String sqlMetodos = "CREATE TABLE IF NOT EXISTS metodos_pagamento (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "nome TEXT NOT NULL UNIQUE," +
                        "dia_vencimento INTEGER" +
                        ");";

                stmt.execute(sqlGastos);
                stmt.execute(sqlMetodos);
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
            pstmt.setString(4, gasto.getCategoria());
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
        // Fazemos um JOIN ou apenas pegamos os dados? Por enquanto, vamos buscar os gastos:
        String sql = "SELECT * FROM gastos ORDER BY data DESC";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String dataString = rs.getString("data");
                LocalDate dataFinal = LocalDate.parse(dataString); // Garante que a String vire objeto data

                Gasto gasto = new Gasto(
                        rs.getString("descricao"), // Verifique se o nome da coluna no banco é exatamente 'descricao'
                        rs.getDouble("valor"),
                        dataFinal,
                        rs.getString("categoria"),
                        new MetodoPagamento(rs.getString("metodo"), 0),
                        false,
                        rs.getInt("total_parcelas"),
                        rs.getInt("parcela_atual")
                );
                lista.add(gasto);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar gastos: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public List<Gasto> buscarPorMesEAno(int mes, int ano) {
        return new ArrayList<>();
    }

    // --- MÉTODOS DE PAGAMENTO (MetodoRepository) ---

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
}