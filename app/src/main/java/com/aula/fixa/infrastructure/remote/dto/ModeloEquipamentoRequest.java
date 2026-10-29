package com.aula.fixa.infrastructure.remote.dto;

public final class ModeloEquipamentoRequest {
    public final Long id;
    public final Long marcaEquipamentoId;
    public final Long categoriaEquipamentoId;
    public final String nome;
    public final String descricao;

    public ModeloEquipamentoRequest(Long id, Long marcaEquipamentoId, Long categoriaEquipamentoId,
                                    String nome, String descricao) {
        this.id = id;
        this.marcaEquipamentoId = marcaEquipamentoId;
        this.categoriaEquipamentoId = categoriaEquipamentoId;
        this.nome = nome;
        this.descricao = descricao;
    }
}
