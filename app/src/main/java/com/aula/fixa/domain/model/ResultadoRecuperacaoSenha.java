package com.aula.fixa.domain.model;

public final class ResultadoRecuperacaoSenha {
    public final String nome;
    public final String email;
    public final String mensagem;
    public ResultadoRecuperacaoSenha(String nome, String email, String mensagem) {
        this.nome = nome == null ? "" : nome;
        this.email = email == null ? "" : email;
        this.mensagem = mensagem == null ? "" : mensagem;
    }
}
