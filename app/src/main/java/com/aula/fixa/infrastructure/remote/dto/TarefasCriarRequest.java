package com.aula.fixa.infrastructure.remote.dto;

import java.util.List;

/** Objeto enviado no POST em lote de tarefas. */
public final class TarefasCriarRequest {
    public final List<TarefaPlanejamentoRequest> tarefas;

    public TarefasCriarRequest(List<TarefaPlanejamentoRequest> tarefas) {
        this.tarefas = tarefas;
    }
}
