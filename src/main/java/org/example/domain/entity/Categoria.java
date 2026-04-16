package org.example.domain.entity;


public class Categoria {
    private Integer id;
    private String nome;

    // Construtor para quando buscamos do Banco (com ID)
    public Categoria(Integer id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    // Construtor para criar uma nova (ainda sem ID)
    public Categoria(String nome) {
        this.nome = nome;
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    //  O JavaFX usa o toString() para mostrar o texto no ComboBox
    @Override
    public String toString() {
        return nome;
    }
}
