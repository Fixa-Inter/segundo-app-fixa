package com.aula.fixa.adapter.auth;

import android.content.Context;
import android.util.Base64;
import androidx.lifecycle.ViewModel;
import com.aula.fixa.domain.enums.TipoAcesso;
import com.aula.fixa.infrastructure.auth.JwtAccessReader;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.notifications.FcmTokenRegistrar;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.dto.RefreshTokenRequest;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

public final class SessaoViewModel extends ViewModel {
    private final SessionStorage storage;
    public SessaoViewModel(Context context) { storage = new SessionStorage(context.getApplicationContext()); }
    public boolean temSessao() { return storage.hasSession(); }
    public TipoAcesso tipoAcesso() {
        try { return JwtAccessReader.read(storage.getToken()); }
        catch (Exception ignored) { return null; }
    }
    public boolean tokenValido() {
        String token = storage.getToken();
        if (token == null || token.trim().isEmpty()) return false;
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;
            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP), StandardCharsets.UTF_8);
            if (new JSONObject(payload).optLong("exp", 0L) > System.currentTimeMillis() / 1000L) return true;
        } catch (Exception ignored) { }
        String refreshToken = storage.getRefreshToken();
        return refreshToken != null && !refreshToken.isBlank();
    }
    public void encerrar() {
        long generation = storage.sessionGeneration();
        FcmTokenRegistrar.removerTokenAtual(storage, () -> {
            if (generation != storage.sessionGeneration()) return;
            String refreshToken = storage.getRefreshToken();
            if (refreshToken == null || refreshToken.isBlank()) {
                storage.clear();
                return;
            }
            ApiClient.auth().logout(new RefreshTokenRequest(refreshToken)).enqueue(new retrofit2.Callback<>() {
                private void clearCurrentSession() {
                    if (generation == storage.sessionGeneration()) storage.clear();
                }
                @Override public void onResponse(retrofit2.Call<Void> call,
                                                 retrofit2.Response<Void> response) {
                    clearCurrentSession();
                }
                @Override public void onFailure(retrofit2.Call<Void> call, Throwable error) {
                    clearCurrentSession();
                }
            });
        });
    }
}
