package com.aula.fixa.infrastructure.remote.dto.jev;

import com.google.gson.annotations.SerializedName;

/** Corpo do endpoint de classificação JEV. */
public final class ClassificarProblemaRequest {
    public final String titulo;
    public final String descricao;
    @SerializedName("categoria_equipamento") public final String categoriaEquipamento;

    public ClassificarProblemaRequest(String titulo, String descricao, String categoriaEquipamento) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.categoriaEquipamento = categoriaEquipamento;
    }
}
