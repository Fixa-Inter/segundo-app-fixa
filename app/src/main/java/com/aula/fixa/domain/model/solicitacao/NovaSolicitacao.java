package com.aula.fixa.domain.model.solicitacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dados necessários para cadastrar uma solicitação.
 */
public final class NovaSolicitacao {
    public final String titulo;
    public final String descricao;
    public final long localId;
    public final String descricaoLocal;
    public final long categoriaId;
    public final List<String> fotos;

    public NovaSolicitacao(
            String titulo,
            String descricao,
            long localId,
            String descricaoLocal,
            long categoriaId,
            List<String> fotos
    ) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.localId = localId;
        this.descricaoLocal = descricaoLocal;
        this.categoriaId = categoriaId;
        this.fotos = Collections.unmodifiableList(fotos == null ? new ArrayList<>() : new ArrayList<>(fotos));
    }
}
