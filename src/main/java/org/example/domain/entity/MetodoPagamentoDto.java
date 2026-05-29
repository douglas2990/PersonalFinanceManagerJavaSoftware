package org.example.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MetodoPagamentoDto {

    @JsonProperty("id")
    private int id;

    @JsonProperty("nome")
    private String nome;

    @JsonProperty("diaVencimento")
    private int diaVencimento;

    // Construtor vazio essencial para o Jackson desserializar (ler) dados da API
    public MetodoPagamentoDto() {}

    // Construtor completo para criar novos objetos
    public MetodoPagamentoDto(int id, String nome, int diaVencimento) {
        this.id = id;
        this.nome = nome;
        this.diaVencimento = diaVencimento;
    }

    // Getters para serializar (enviar)
    public int getId() { return id; }
    public String getNome() { return nome; }
    public int getDiaVencimento() { return diaVencimento; }

    // Setters para desserializar (receber)
    public void setId(int id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setDiaVencimento(int diaVencimento) { this.diaVencimento = diaVencimento; }
}