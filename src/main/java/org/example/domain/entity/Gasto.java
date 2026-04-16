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
