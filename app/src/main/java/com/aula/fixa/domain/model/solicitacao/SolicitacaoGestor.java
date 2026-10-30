package com.aula.fixa.domain.model.solicitacao;

import com.aula.fixa.domain.model.solicitacao.FotoProblema;

import java.util.Collections;
import java.util.List;

/** Solicitação da unidade exibida e analisada no fluxo do gestor. */
public final class SolicitacaoGestor {
    public final long id;
    public final String titulo, descricao, categoria, local, descricaoLocal, status, dataCriacao, solicitante;
    public final List<FotoProblema> fotos;
    public SolicitacaoGestor(long id, String titulo, String descricao, String categoria, String local,
            String descricaoLocal, String status, String dataCriacao, String solicitante, List<FotoProblema> fotos) {
        this.id=id; this.titulo=text(titulo); this.descricao=text(descricao); this.categoria=text(categoria);
        this.local=text(local); this.descricaoLocal=text(descricaoLocal); this.status=text(status);
        this.dataCriacao=text(dataCriacao); this.solicitante=text(solicitante);
        this.fotos=Collections.unmodifiableList(fotos==null?Collections.emptyList():fotos);
    }
    private static String text(String value) { return value==null?"":value; }
}
