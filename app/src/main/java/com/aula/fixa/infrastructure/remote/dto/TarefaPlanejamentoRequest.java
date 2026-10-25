package com.aula.fixa.infrastructure.remote.dto;

/** Corpo de criação em lote das tarefas de uma OS. */
public final class TarefaPlanejamentoRequest {
    public final long ordemServicoId;
    public final String statusOrdemServico = "NAO_INICIADA";
    public final String titulo;
    public final String descricao;

    public TarefaPlanejamentoRequest(long ordemServicoId, String titulo, String descricao) {
        this.ordemServicoId = ordemServicoId;
        this.titulo = titulo;
        this.descricao = descricao;
    }
}
