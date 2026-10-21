package com.aula.fixa.data.remote.dto;

public final class CadastroOption {
    private final long id;
    private final String nome;

    public CadastroOption(long id, String nome) {
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
