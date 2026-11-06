package com.aula.fixa.infrastructure.auth;

import com.aula.fixa.domain.repository.auth.SessaoRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.dto.RefreshTokenRequest;
import com.aula.fixa.infrastructure.remote.notifications.FcmTokenRegistrar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Adapta o armazenamento criptografado e os endpoints de sessão ao contrato do domínio. */
public final class SessaoRepositoryImpl implements SessaoRepository {
    private final SessionStorage storage;

    public SessaoRepositoryImpl(SessionStorage storage) { this.storage = storage; }

    @Override public boolean temSessao() { return storage.hasSession(); }
    @Override public String accessToken() { return storage.getToken(); }
    @Override public String refreshToken() { return storage.getRefreshToken(); }
    @Override public long geracaoSessao() { return storage.sessionGeneration(); }
    @Override public String papelDoToken(String token) throws Exception { return JwtAccessReader.readRole(token); }
    @Override public long expiracaoDoToken(String token) throws Exception { return JwtAccessReader.readExpiration(token); }
    @Override public void limpar() { storage.clear(); }

    @Override public void removerTokenDispositivo(Completion completion) {
        FcmTokenRegistrar.removerTokenAtual(storage, completion::onComplete);
    }

    @Override public void encerrarSessaoRemota(String refreshToken, Completion completion) {
        ApiClient.auth().logout(new RefreshTokenRequest(refreshToken)).enqueue(new Callback<>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                completion.onComplete();
            }
            @Override public void onFailure(Call<Void> call, Throwable error) {
                completion.onComplete();
            }
        });
    }
}
