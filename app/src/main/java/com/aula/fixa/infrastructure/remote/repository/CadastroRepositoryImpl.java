package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.domain.repository.CadastroRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import java.util.List;

public final class CadastroRepositoryImpl implements CadastroRepository {
    private final CadastroRemoteRepository remote;
    public CadastroRepositoryImpl(SessionStorage session) { remote = new CadastroRemoteRepository(session); }
    @Override public List<OpcaoCadastro> listarLocais() throws Exception { return remote.locais(); }
    @Override public List<OpcaoCadastro> listarCategorias() throws Exception { return remote.categorias(); }
}
