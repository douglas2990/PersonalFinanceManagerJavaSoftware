package org.example.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public class GastoDto {
    private int id;

    @JsonProperty("descricao")
    private String descricao;

    private double valor;
    private LocalDate data;

    @JsonProperty("categoriaId")
    private int categoriaId;

    @JsonProperty("metodoId")
    private int metodoId;

    private boolean isMensal;
    private int totalParcelas;
    private int parcelaAtual;

    // Construtor vazio (Necessário para alguns frameworks de desserialização)
    public GastoDto() {}

    // Construtor completo
    public GastoDto(int id, String descricao, double valor, LocalDate data,
                    int categoriaId, int metodoId, boolean isMensal,
                    int totalParcelas, int parcelaAtual) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.categoriaId = categoriaId;
        this.metodoId = metodoId;
        this.isMensal = isMensal;
        this.totalParcelas = totalParcelas;
        this.parcelaAtual = parcelaAtual;
    }

    // Getters
    public int getId() { return id; }
    public String getDescricao() { return descricao; }
    public double getValor() { return valor; }
    public LocalDate getData() { return data; }
    public int getCategoriaId() { return categoriaId; }
    public int getMetodoId() { return metodoId; }
    public boolean isMensal() { return isMensal; }
    public int getTotalParcelas() { return totalParcelas; }
    public int getParcelaAtual() { return parcelaAtual; }

    // Setters (Adicionados para permitir que o Jackson preencha os objetos ao receber dados da API)
    public void setId(int id) { this.id = id; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setValor(double valor) { this.valor = valor; }
    public void setData(LocalDate data) { this.data = data; }
    public void setCategoriaId(int categoriaId) { this.categoriaId = categoriaId; }
    public void setMetodoId(int metodoId) { this.metodoId = metodoId; }
    public void setMensal(boolean mensal) { isMensal = mensal; }
    public void setTotalParcelas(int totalParcelas) { this.totalParcelas = totalParcelas; }
    public void setParcelaAtual(int parcelaAtual) { this.parcelaAtual = parcelaAtual; }
}