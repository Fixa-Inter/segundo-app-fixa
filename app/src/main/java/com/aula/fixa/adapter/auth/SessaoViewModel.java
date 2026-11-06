package com.aula.fixa.adapter.auth;

import androidx.lifecycle.ViewModel;

import com.aula.fixa.application.auth.GerenciarSessao;
import com.aula.fixa.domain.enums.TipoAcesso;

public final class SessaoViewModel extends ViewModel {
    private final GerenciarSessao sessao;

    public SessaoViewModel(GerenciarSessao sessao) { this.sessao = sessao; }

    public boolean temSessao() { return sessao.temSessao(); }
    public TipoAcesso tipoAcesso() { return sessao.tipoAcesso(); }
    public boolean tokenValido() { return sessao.tokenValido(); }
    public void encerrar() { sessao.encerrar(); }
}
