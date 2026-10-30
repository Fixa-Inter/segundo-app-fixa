package com.aula.fixa.infrastructure.remote.dto;

import java.util.List;

public final class OcorrenciaAtualizarRequest {
    public final long id;
    public final Long localUnidadeId, categoriaEquipamentoId;
    public final com.aula.fixa.domain.enums.CategoriaProblema categoriaProblemaId;
    public final com.aula.fixa.domain.enums.Prioridade prioridade;
    public final String titulo, descricaoOcorrencia, descricaoLocal;
    public final List<Long> fotosExcluir;
    public final List<String> fotosAdicionar;
    public OcorrenciaAtualizarRequest(long id,Long localUnidadeId,Long categoriaEquipamentoId,
            com.aula.fixa.domain.enums.CategoriaProblema categoriaProblemaId,
            com.aula.fixa.domain.enums.Prioridade prioridade,String titulo,String descricaoOcorrencia,
            String descricaoLocal,List<Long> fotosExcluir,List<String> fotosAdicionar){
        this.id=id;this.localUnidadeId=localUnidadeId;this.categoriaEquipamentoId=categoriaEquipamentoId;
        this.categoriaProblemaId=categoriaProblemaId;this.prioridade=prioridade;this.titulo=titulo;
        this.descricaoOcorrencia=descricaoOcorrencia;this.descricaoLocal=descricaoLocal;
        this.fotosExcluir=fotosExcluir;this.fotosAdicionar=fotosAdicionar;
    }
}
