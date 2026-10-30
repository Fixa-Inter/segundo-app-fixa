package com.aula.fixa.domain.model;

public final class AtualizacaoPerfil {
    public final String nomeCompleto;
    public final String cargo;
    public final String urlFotoPerfil;

    public AtualizacaoPerfil(String nomeCompleto, String cargo, String urlFotoPerfil) {
        this.nomeCompleto = nomeCompleto == null ? "" : nomeCompleto.trim();
        this.cargo = cargo == null ? "" : cargo.trim();
        this.urlFotoPerfil = urlFotoPerfil;
    }
}
