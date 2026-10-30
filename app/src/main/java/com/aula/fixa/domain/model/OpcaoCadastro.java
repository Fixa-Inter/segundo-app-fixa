package com.aula.fixa.domain.model;

public final class OpcaoCadastro {
    private final long id;
    private final String nome;

    public OpcaoCadastro(long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return nome;
    }
}
