package com.aula.fixa.domain.repository;

import com.aula.fixa.domain.model.AtualizacaoPerfil;
import com.aula.fixa.domain.model.Perfil;

public interface PerfilRepository {
    Perfil obter() throws Exception;
    Perfil atualizar(AtualizacaoPerfil atualizacao) throws Exception;
}
