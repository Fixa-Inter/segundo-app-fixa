package com.aula.fixa.infrastructure.auth;

import android.util.Base64;
import com.aula.fixa.domain.enums.TipoAcesso;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

/** Lê o claim de roteamento do JWT. Autorização continua sendo responsabilidade da API. */
public final class JwtAccessReader {
    private JwtAccessReader() { }

    public static TipoAcesso read(String token) throws Exception {
        if (token == null) throw new IllegalArgumentException("Token ausente");
        String[] parts = token.split("\\.");

        if (parts.length != 3) throw new IllegalArgumentException("Token inválido");
        byte[] payload = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);

        String json = new String(payload, StandardCharsets.UTF_8);
        return TipoAcesso.fromApi(new JSONObject(json).getString("role"));
    }
}
