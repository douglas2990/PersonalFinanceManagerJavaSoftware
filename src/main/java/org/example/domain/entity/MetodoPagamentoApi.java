package org.example.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MetodoPagamentoApi {

    @JsonProperty("id")
    private int id;

    @JsonProperty("nome")
    private String nome;

    @JsonProperty("diaVencimento")
    private int diaVencimento;

    // NOVO CAMPO: Cor do cartão/método
    @JsonProperty("cor")
    private String cor;

    // CONSTRUTOR VAZIO OBRIGATÓRIO PARA O JACKSON
    public MetodoPagamentoApi() {}

    // Construtor atualizado com parâmetros
    public MetodoPagamentoApi(int id, String nome, int diaVencimento, String cor) {
        this.id = id;
        this.nome = nome;
        this.diaVencimento = diaVencimento;
        this.cor = cor;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getDiaVencimento() { return diaVencimento; }
    public void setDiaVencimento(int diaVencimento) { this.diaVencimento = diaVencimento; }

    // Getters e Setters da nova propriedade 'cor'
    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }
}