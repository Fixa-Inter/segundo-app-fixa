package com.aula.fixa.domain.repository.auth;

/** Operações de persistência segura e comunicação remota necessárias ao gerenciamento da sessão. */
public interface SessaoRepository {
    interface Completion { void onComplete(); }

    boolean temSessao();
    String accessToken();
    String refreshToken();
    long geracaoSessao();
    String papelDoToken(String token) throws Exception;
    long expiracaoDoToken(String token) throws Exception;
    void limpar();
    void removerTokenDispositivo(Completion completion);
    void encerrarSessaoRemota(String refreshToken, Completion completion);
}
