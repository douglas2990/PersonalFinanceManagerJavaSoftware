package org.example.domain.entity;

public class MetodoPagamento {
    private String nome;
    private int diaVencimento;

    public MetodoPagamento(String nome, int diaVencimento) {
        this.nome = nome;
        this.diaVencimento = diaVencimento;
    }

    // Getters
    public String getNome() {
        return nome;
    }

    public int getDiaVencimento() {
        return diaVencimento;
    }
}