package com.aula.fixa.domain.model;

import java.util.List;

public final class ManagerHomeData {
    public final String nomeGestor;
    public final String fotoGestor;
    public final List<OrdemServicoHomeItem> ordens;
    public final List<SolicitacaoHomeItem> solicitacoes;

    public ManagerHomeData(String nomeGestor, String fotoGestor,
                           List<OrdemServicoHomeItem> ordens,
                           List<SolicitacaoHomeItem> solicitacoes) {
        this.nomeGestor = safe(nomeGestor);
        this.fotoGestor = safe(fotoGestor);
        this.ordens = ordens;
        this.solicitacoes = solicitacoes;
    }

    public static final class OrdemServicoHomeItem {
        public final String status;
        public OrdemServicoHomeItem(String status) { this.status = safe(status); }
    }

    public static final class SolicitacaoHomeItem {
        public final long id;
        public final String titulo;
        public final String local;
        public final String descricaoLocal;
        public final String solicitante;
        public final String dataCriacao;

        public SolicitacaoHomeItem(long id, String titulo, String local, String descricaoLocal,
                                   String solicitante, String dataCriacao) {
            this.id = id;
            this.titulo = safe(titulo);
            this.local = safe(local);
            this.descricaoLocal = safe(descricaoLocal);
            this.solicitante = safe(solicitante);
            this.dataCriacao = safe(dataCriacao);
        }
    }

    private static String safe(String value) { return value == null ? "" : value; }
}
