package com.aula.fixa.domain.model;

public final class SessaoUsuario {
    public final String email;
    public final String token;
    public SessaoUsuario(String email, String token) {
        this.email = email == null ? "" : email;
        this.token = token == null ? "" : token;
    }
}
