package com.aula.fixa.domain.model;

/** Modelo de domínio de evento. */
public final class Evento {
    public final long id;
    public final String titulo;
    public final String descricao;
    public final String local;
    public final String descricaoLocal;
    public final String inicio;
    public final String fim;
    public final String observacao;

    public Evento(long id, String titulo, String descricao, String local, String descricaoLocal,
                  String inicio, String fim, String observacao) {
        this.id = id;
        this.titulo = titulo == null ? "" : titulo;
        this.descricao = descricao == null ? "" : descricao;
        this.local = local == null ? "" : local;
        this.descricaoLocal = descricaoLocal == null ? "" : descricaoLocal;
        this.inicio = inicio == null ? "" : inicio;
        this.fim = fim == null ? "" : fim;
        this.observacao = observacao == null ? "" : observacao;
    }
}
