package com.aula.fixa.infrastructure.remote.dto;

public final class CategoriaEquipamentoRequest {
    public final String nome;
    public final String descricao;
    public CategoriaEquipamentoRequest(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }
}
