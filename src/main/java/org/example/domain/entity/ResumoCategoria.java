package org.example.domain.entity;

public class ResumoCategoria {
    private String categoria;
    private double planejado;
    private double realizado;
    private double saldo;
    private String status;
    private double saldoAcumulado;

    public ResumoCategoria(String categoria, double planejado, double realizado, double saldoAcumulado) {
        this.categoria = categoria;
        this.planejado = planejado;
        this.realizado = realizado;
        this.saldo = planejado - realizado;
        this.status = (this.saldo >= 0) ? "✅ No Limite" : "⚠️ Excedido";
        this.saldoAcumulado = saldoAcumulado;
    }

    public String getCategoria() { return categoria; }
    public double getPlanejado() { return planejado; }
    public double getRealizado() { return realizado; }
    public double getSaldo() { return saldo; }
    public String getStatus() { return status; }
    public double getSaldoAcumulado() { return saldoAcumulado; }
}