package com.aula.fixa.application.perfil;

import com.aula.fixa.domain.model.Perfil;
import com.aula.fixa.domain.repository.PerfilRepository;

public final class ObterPerfil {
    private final PerfilRepository repository;
    public ObterPerfil(PerfilRepository repository) { this.repository = repository; }
    public Perfil executar() throws Exception { return repository.obter(); }
}
