package com.aula.fixa.infrastructure.remote.dto;

import java.util.List;

public final class OcorrenciaCriarRequest {
    public final Long localUnidadeId;
    public final Long categoriaEquipamentoId;
    public final com.aula.fixa.domain.enums.CategoriaProblema categoriaProblemaId;
    public final String titulo;
    public final String descricaoOcorrencia;
    public final String descricaoLocal;
    public final com.aula.fixa.domain.enums.Prioridade prioridade;
    public final List<String> urlsFoto;

    public OcorrenciaCriarRequest(Long localUnidadeId, Long categoriaEquipamentoId,
            com.aula.fixa.domain.enums.CategoriaProblema categoriaProblemaId, String titulo,
            String descricaoOcorrencia, String descricaoLocal,
            com.aula.fixa.domain.enums.Prioridade prioridade, List<String> urlsFoto) {
        this.localUnidadeId = localUnidadeId;
        this.categoriaEquipamentoId = categoriaEquipamentoId;
        this.categoriaProblemaId = categoriaProblemaId;
        this.titulo = titulo;
        this.descricaoOcorrencia = descricaoOcorrencia;
        this.descricaoLocal = descricaoLocal;
        this.prioridade = prioridade;
        this.urlsFoto = urlsFoto;
    }
}
