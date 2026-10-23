package com.aula.fixa.adapter.auth;

import android.content.Context;
import android.util.Base64;
import androidx.lifecycle.ViewModel;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

public final class SessaoViewModel extends ViewModel {
    private final SessionStorage storage;
    public SessaoViewModel(Context context) { storage = new SessionStorage(context.getApplicationContext()); }
    public boolean temSessao() { return storage.hasSession(); }
    public boolean tokenValido() {
        String token = storage.getToken();
        if (token == null || token.trim().isEmpty()) return false;
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;
            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP), StandardCharsets.UTF_8);
            return new JSONObject(payload).optLong("exp", 0L) > System.currentTimeMillis() / 1000L;
        } catch (Exception ignored) { return false; }
    }
    public void encerrar() { storage.clear(); }
}
