package com.aula.fixa.domain.model;

/** Perfil autenticado, sem dependência do formato de transporte da API. */
public final class Perfil {
    public final Long id;
    public final String nomeCompleto;
    public final String email;
    public final String cargo;
    public final String tipoAcesso;
    public final String urlFotoPerfil;
    public final String dataNascimento;
    public final String cnpjUnidade;

    public Perfil(Long id, String nomeCompleto, String email, String cargo,
                  String tipoAcesso, String urlFotoPerfil, String dataNascimento, String cnpjUnidade) {
        this.id = id;
        this.nomeCompleto = safe(nomeCompleto);
        this.email = safe(email);
        this.cargo = safe(cargo);
        this.tipoAcesso = safe(tipoAcesso);
        this.urlFotoPerfil = safe(urlFotoPerfil);
        this.dataNascimento = safe(dataNascimento);
        this.cnpjUnidade = safe(cnpjUnidade);
    }

    private static String safe(String value) { return value == null ? "" : value; }
}
