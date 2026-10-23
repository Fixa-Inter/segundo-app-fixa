package com.aula.fixa.domain.model;

import com.aula.fixa.domain.enums.TipoAcesso;

public final class SessaoUsuario {
    public final String email;
    public final String token;
    public final TipoAcesso tipoAcesso;
    public SessaoUsuario(String email, String token, TipoAcesso tipoAcesso) {
        this.email = email == null ? "" : email;
        this.token = token == null ? "" : token;
        this.tipoAcesso = tipoAcesso;
    }
}
