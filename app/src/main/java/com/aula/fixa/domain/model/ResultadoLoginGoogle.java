package com.aula.fixa.domain.model;

import com.aula.fixa.domain.enums.TipoAcesso;

public final class ResultadoLoginGoogle {
    public final String status;
    public final String email;
    public final String nome;
    public final String token;
    public final TipoAcesso tipoAcesso;
    public ResultadoLoginGoogle(String status, String email, String nome, String token, TipoAcesso tipoAcesso) {
        this.status = status == null ? "" : status;
        this.email = email == null ? "" : email;
        this.nome = nome == null ? "" : nome;
        this.token = token == null ? "" : token;
        this.tipoAcesso = tipoAcesso;
    }
}
