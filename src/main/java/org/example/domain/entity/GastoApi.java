package org.example.domain.entity;

import java.time.LocalDate;

public class GastoApi {
    private int id;
    private String descricao;
    private double valor;
    private LocalDate data;
    private Categoria categoria;

    // ALTERAÇÃO CRÍTICA: Usando MetodoPagamentoApi (que possui o campo 'cor')
    private MetodoPagamentoApi metodo;

    private boolean isMensal;
    private int totalParcelas;
    private int parcelaAtual;

    // Construtor completo
    public GastoApi(int id, String descricao, double valor, LocalDate data, Categoria categoria,
                    MetodoPagamentoApi metodo, boolean isMensal, int totalParcelas, int parcelaAtual) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.categoria = categoria;
        this.metodo = metodo;
        this.isMensal = isMensal;
        this.totalParcelas = totalParcelas;
        this.parcelaAtual = parcelaAtual;
    }

    // Construtor para NOVOS gastos (sem ID)
    public GastoApi(String descricao, double valor, LocalDate data, Categoria categoria,
                    MetodoPagamentoApi metodo, boolean isMensal, int totalParcelas, int parcelaAtual) {
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.categoria = categoria;
        this.metodo = metodo;
        this.isMensal = isMensal;
        this.totalParcelas = totalParcelas;
        this.parcelaAtual = parcelaAtual;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    // Retorna o tipo com 'cor'
    public MetodoPagamentoApi getMetodo() { return metodo; }
    public void setMetodo(MetodoPagamentoApi metodo) { this.metodo = metodo; }

    public boolean isMensal() { return isMensal; }
    public void setMensal(boolean mensal) { this.isMensal = mensal; }

    public int getTotalParcelas() { return totalParcelas; }
    public void setTotalParcelas(int totalParcelas) { this.totalParcelas = totalParcelas; }

    public int getParcelaAtual() { return parcelaAtual; }
    public void setParcelaAtual(int parcelaAtual) { this.parcelaAtual = parcelaAtual; }
}