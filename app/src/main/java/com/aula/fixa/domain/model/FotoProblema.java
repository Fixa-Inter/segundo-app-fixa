package com.aula.fixa.domain.model;

public final class FotoProblema {
    public final long id;
    public final String url;
    public FotoProblema(long id, String url) { this.id = id; this.url = url == null ? "" : url; }
}
