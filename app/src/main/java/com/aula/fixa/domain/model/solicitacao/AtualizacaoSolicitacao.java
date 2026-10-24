package com.aula.fixa.domain.model.solicitacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AtualizacaoSolicitacao {
    public final long id;
    public final String titulo;
    public final String descricao;
    public final Long localId;
    public final String descricaoLocal;
    public final Long categoriaId;
    public final List<Long> fotosExcluir;

    public AtualizacaoSolicitacao(long id, String titulo, String descricao, Long localId,
                                  String descricaoLocal, Long categoriaId, List<Long> fotosExcluir) {
        if (id <= 0) throw new IllegalArgumentException("Solicitação inválida.");
        this.id = id;
        this.titulo = titulo == null ? "" : titulo.trim();
        this.descricao = descricao == null ? "" : descricao.trim();
        this.localId = localId;
        this.descricaoLocal = descricaoLocal;
        this.categoriaId = categoriaId;
        this.fotosExcluir = Collections.unmodifiableList(fotosExcluir == null
                ? new ArrayList<>() : new ArrayList<>(fotosExcluir));
    }
}
