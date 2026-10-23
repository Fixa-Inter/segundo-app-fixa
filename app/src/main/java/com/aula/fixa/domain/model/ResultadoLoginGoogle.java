package com.aula.fixa.domain.model;

public final class ResultadoLoginGoogle {
    public final String status;
    public final String email;
    public final String nome;
    public final String token;
    public ResultadoLoginGoogle(String status, String email, String nome, String token) {
        this.status = status == null ? "" : status;
        this.email = email == null ? "" : email;
        this.nome = nome == null ? "" : nome;
        this.token = token == null ? "" : token;
    }
}
