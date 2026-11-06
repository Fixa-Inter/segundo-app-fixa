package com.aula.fixa.application.auth;

import com.aula.fixa.domain.enums.TipoAcesso;
import com.aula.fixa.domain.repository.auth.SessaoRepository;

/** Regras de validade, roteamento e encerramento da sessão autenticada. */
public final class GerenciarSessao {
    private final SessaoRepository repository;

    public GerenciarSessao(SessaoRepository repository) { this.repository = repository; }

    public boolean temSessao() { return repository.temSessao(); }

    public TipoAcesso tipoAcesso() {
        String token = repository.accessToken();
        if (token == null || token.trim().isEmpty()) return null;
        try { return TipoAcesso.fromApi(repository.papelDoToken(token)); }
        catch (Exception ignored) { return null; }
    }

    public boolean tokenValido() {
        String token = repository.accessToken();
        if (token == null || token.trim().isEmpty()) return false;
        try {
            if (repository.expiracaoDoToken(token) > System.currentTimeMillis() / 1000L) return true;
        } catch (Exception ignored) { }
        String refresh = repository.refreshToken();
        return refresh != null && !refresh.isBlank();
    }

    public void encerrar() {
        long generation = repository.geracaoSessao();
        repository.removerTokenDispositivo(() -> {
            if (generation != repository.geracaoSessao()) return;
            String refresh = repository.refreshToken();
            if (refresh == null || refresh.isBlank()) {
                repository.limpar();
                return;
            }
            repository.encerrarSessaoRemota(refresh, () -> {
                if (generation == repository.geracaoSessao()) repository.limpar();
            });
        });
    }
}
