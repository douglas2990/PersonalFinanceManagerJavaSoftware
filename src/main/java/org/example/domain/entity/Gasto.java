package org.example.domain.entity;

import java.time.LocalDate;

public class Gasto {
    private String descricao;
    private double valor;
    private LocalDate data;
    private String categoria;
    private MetodoPagamento metodo;
    private boolean isMensal;
    private int totalParcelas;
    private int parcelaAtual;

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public MetodoPagamento getMetodo() {
        return metodo;
    }

    public void setMetodo(MetodoPagamento metodo) {
        this.metodo = metodo;
    }

    public boolean isMensal() {
        return isMensal;
    }

    public void setMensal(boolean mensal) {
        isMensal = mensal;
    }

    public int getTotalParcelas() {
        return totalParcelas;
    }

    public void setTotalParcelas(int totalParcelas) {
        this.totalParcelas = totalParcelas;
    }

    public int getParcelaAtual() {
        return parcelaAtual;
    }

    public void setParcelaAtual(int parcelaAtual) {
        this.parcelaAtual = parcelaAtual;
    }

    public Gasto(String descricao, double valor, LocalDate data, String categoria,
                 MetodoPagamento metodo, boolean isMensal, int totalParcelas, int parcelaAtual) {
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.categoria = categoria;
        this.metodo = metodo;
        this.isMensal = isMensal;
        this.totalParcelas = totalParcelas;
        this.parcelaAtual = parcelaAtual;
    }
}
