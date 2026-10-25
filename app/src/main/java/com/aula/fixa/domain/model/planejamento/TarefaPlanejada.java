package com.aula.fixa.domain.model.planejamento;

/** Tarefa mantida no rascunho local até a confirmação da criação da OS. */
public final class TarefaPlanejada {
    public final String titulo;
    public final String descricao;

    public TarefaPlanejada(String titulo, String descricao) {
        this.titulo = titulo == null ? "" : titulo;
        this.descricao = descricao == null ? "" : descricao;
    }
}
