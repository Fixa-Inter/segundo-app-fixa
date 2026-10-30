package com.aula.fixa.application.perfil;

import com.aula.fixa.domain.model.AtualizacaoPerfil;
import com.aula.fixa.domain.model.Perfil;
import com.aula.fixa.domain.repository.PerfilRepository;

public final class AtualizarPerfil {
    private final PerfilRepository repository;
    public AtualizarPerfil(PerfilRepository repository) { this.repository = repository; }
    public Perfil executar(AtualizacaoPerfil atualizacao) throws Exception {
        return repository.atualizar(atualizacao);
    }
}
