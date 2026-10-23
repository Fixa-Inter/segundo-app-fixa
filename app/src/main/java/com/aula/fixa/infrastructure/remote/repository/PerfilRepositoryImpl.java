package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.domain.model.AtualizacaoPerfil;
import com.aula.fixa.domain.model.Perfil;
import com.aula.fixa.domain.repository.PerfilRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.dto.PerfilAtualizarRequest;
import com.aula.fixa.infrastructure.remote.dto.PerfilResponse;

public final class PerfilRepositoryImpl implements PerfilRepository {
    private final PerfilRemoteRepository remote;
    public PerfilRepositoryImpl(SessionStorage session) { remote = new PerfilRemoteRepository(session); }

    @Override public Perfil obter() throws Exception { return map(remote.perfil()); }

    @Override public Perfil atualizar(AtualizacaoPerfil atualizacao) throws Exception {
        PerfilAtualizarRequest request = new PerfilAtualizarRequest();
        request.nomeCompleto = atualizacao.nomeCompleto;
        request.cargo = atualizacao.cargo;
        request.urlFotoPerfil = atualizacao.urlFotoPerfil;
        return map(remote.atualizar(request));
    }

    private static Perfil map(PerfilResponse response) {
        return new Perfil(response.id, response.nomeCompleto, response.email,
                response.cargo, response.tipoAcesso, response.urlFotoPerfil,
                response.dataNascimento, response.cnpjUnidade);
    }
}
