package com.aula.fixa.infrastructure.remote.dto;

import java.util.List;

/** Contrato de entrada equivalente a ProblemaCriarInputDTO da API. */
public final class ProblemaCriarRequest {
    public String titulo;
    public String descricaoProblema;
    public long localUnidadeID;
    public String descricaoLocal;
    public long categoriaEquipamentoId;
    public List<String> urlsFoto;
}
