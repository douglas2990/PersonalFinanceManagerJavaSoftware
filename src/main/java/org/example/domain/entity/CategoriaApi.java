package org.example.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CategoriaApi {

    @JsonProperty("id")
    private int id;

    @JsonProperty("nome")
    private String nome;

    // CONSTRUTOR VAZIO OBRIGATÓRIO PARA O JACKSON
    public CategoriaApi() {}

    // Seu construtor atual com parâmetros
    public CategoriaApi(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    // Getters e Setters obrigatórios
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}