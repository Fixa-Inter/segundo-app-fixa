package com.aula.fixa.domain.enums;

/** Prioridades aceitas pela API para o planejamento da OS. */
public enum Prioridade {
    ALTA("Alta", 0),
    MEDIA("Média", 1),
    BAIXA("Baixa", 2);

    private final String nome;
    private final int id;

    Prioridade(String nome, int id) { this.nome = nome; this.id = id; }
    public String getNome() { return nome; }
    public int getId() { return id; }
}
