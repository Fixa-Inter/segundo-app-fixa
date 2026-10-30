package com.aula.fixa.domain.model.solicitacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Modelo de domínio de uma solicitação, independente de Retrofit e do formato JSON da API. */
public final class Problema {
    public final long id;
    public final String titulo;
    public final String descricao;
    public final String categoria;
    public final String local;
    public final String descricaoLocal;
    public final String status;
    public final String dataCriacao;
    public final List<FotoProblema> fotos;

    public Problema(
            long id,
            String titulo,
            String descricao,
            String categoria,
            String local,
            String descricaoLocal,
            String status,
            String dataCriacao,
            List<FotoProblema> fotos
    ) {
        this.id = id;
        this.titulo = titulo == null ? "" : titulo;
        this.descricao = descricao == null ? "" : descricao;
        this.categoria = categoria == null ? "" : categoria;
        this.local = local == null ? "" : local;
        this.descricaoLocal = descricaoLocal == null ? "" : descricaoLocal;
        this.status = status == null ? "" : status;
        this.dataCriacao = dataCriacao == null ? "" : dataCriacao;
        this.fotos = Collections.unmodifiableList(fotos == null ? new ArrayList<>() : new ArrayList<>(fotos));
    }
}
