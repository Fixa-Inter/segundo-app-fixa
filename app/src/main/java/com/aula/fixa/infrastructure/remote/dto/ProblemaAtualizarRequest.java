package com.aula.fixa.infrastructure.remote.dto;

import java.util.List;

/** Contrato de atualização equivalente a ProblemaAtualizarInputDTO da API. */
public final class ProblemaAtualizarRequest {
    public long problemaId;
    public String titulo;
    public String descricaoProblema;
    public Long localUnidadeId;
    public String descricaoLocal;
    public Long categoriaEquipamentoId;
    public List<String> fotosAdicionar;
    public List<Long> fotosExcluir;
}
