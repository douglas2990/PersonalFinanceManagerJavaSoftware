package org.example.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public class GastoResponseDto {

    @JsonProperty("id")
    private int id;

    @JsonProperty("descricao")
    private String descricao;

    @JsonProperty("valor")
    private double valor;

    @JsonProperty("data")
    private OffsetDateTime data;

    @JsonProperty("categoria")
    private String categoria;

    @JsonProperty("metodo")
    private String metodo;

    @JsonProperty("totalParcelas")
    private int totalParcelas;

    @JsonProperty("parcelaAtual")
    private int parcelaAtual;

    // Construtor vazio obrigatório para o Jackson
    public GastoResponseDto() {}

    // Construtor completo atualizado para OffsetDateTime
    public GastoResponseDto(int id, String descricao, double valor, OffsetDateTime data,
                            String categoria, String metodo, int totalParcelas, int parcelaAtual) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.categoria = categoria;
        this.metodo = metodo;
        this.totalParcelas = totalParcelas;
        this.parcelaAtual = parcelaAtual;
    }

    // Getters
    public int getId() { return id; }
    public String getDescricao() { return descricao; }
    public double getValor() { return valor; }
    public OffsetDateTime getData() { return data; }
    public String getCategoria() { return categoria; }
    public String getMetodo() { return metodo; }
    public int getTotalParcelas() { return totalParcelas; }
    public int getParcelaAtual() { return parcelaAtual; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setValor(double valor) { this.valor = valor; }
    public void setData(OffsetDateTime data) { this.data = data; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public void setTotalParcelas(int totalParcelas) { this.totalParcelas = totalParcelas; }
    public void setParcelaAtual(int parcelaAtual) { this.parcelaAtual = parcelaAtual; }
}