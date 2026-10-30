package com.aula.fixa.application.foto;

import com.aula.fixa.domain.repository.FotoRepository;

public final class GerenciarFotos {
    private final FotoRepository repository;

    public GerenciarFotos(FotoRepository repository) {
        this.repository = repository;
    }

    public void enviarSolicitacao(String uri, FotoRepository.Callback callback) {
        repository.enviarSolicitacao(uri, callback);
    }

    public void enviarPerfil(String uri, FotoRepository.Callback callback) {
        repository.enviarPerfil(uri, callback);
    }

    public void excluir(String token, FotoRepository.Callback callback) {
        repository.excluir(token, callback);
    }
}
