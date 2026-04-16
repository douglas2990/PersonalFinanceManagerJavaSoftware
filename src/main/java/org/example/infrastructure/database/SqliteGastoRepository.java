package org.example.infrastructure.database;

import org.example.domain.entity.Gasto;
import org.example.domain.repository.GastoRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteGastoRepository implements GastoRepository {
    private static final String URL = "jdbc:sqlite:financas.db";

    public SqliteGastoRepository() {
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(URL)) {
            if (conn != null) {
                String sql = "CREATE TABLE IF NOT EXISTS gastos (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "descricao TEXT NOT NULL," +
                        "valor REAL NOT NULL," +
                        "data TEXT NOT NULL," +
                        "categoria TEXT," +
                        "metodo TEXT," +
                        "total_parcelas INTEGER," +
                        "parcela_atual INTEGER" +
                        ");";
                Statement stmt = conn.createStatement();
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

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
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Gasto> buscarTodos() {
        // Implementaremos a lógica de busca depois
        return new ArrayList<>();
    }

    @Override
    public List<Gasto> buscarPorMesEAno(int mes, int ano) {
        return new ArrayList<>();
    }
}
