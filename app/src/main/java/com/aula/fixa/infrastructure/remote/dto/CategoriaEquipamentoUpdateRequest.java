package com.aula.fixa.infrastructure.remote.dto;

public final class CategoriaEquipamentoUpdateRequest {
    public final long id;
    public final String nome;
    public CategoriaEquipamentoUpdateRequest(long id, String nome) {
        this.id = id;
        this.nome = nome;
    }
}
