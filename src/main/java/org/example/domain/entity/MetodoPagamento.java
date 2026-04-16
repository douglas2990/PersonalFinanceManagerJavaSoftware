package org.example.domain.entity;

public class MetodoPagamento {
    private String nome; // Ex: "Nubank", "Santander"
    private int diaVencimento; // Para o sistema te avisar quando pagar

    public MetodoPagamento(String nome, int diaVencimento) {
        this.nome = nome;
        this.diaVencimento = diaVencimento;
    }

    // Getters
    public String getNome() { return nome; }
}
