package com.aula.fixa.domain.model.tecnico;

public final class ResponsabilidadeTecnico {
    public final String titulo;
    public final String descricao;
    public final String local;
    public final String prazo;
    public final String prioridade;
    public final String status;

    public ResponsabilidadeTecnico(String titulo, String descricao, String local,
                                   String prazo, String prioridade, String status) {
        this.titulo = safe(titulo);
        this.descricao = safe(descricao);
        this.local = safe(local);
        this.prazo = safe(prazo);
        this.prioridade = safe(prioridade);
        this.status = safe(status);
    }

    private static String safe(String value) { return value == null ? "" : value; }
}
