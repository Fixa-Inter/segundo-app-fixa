package com.aula.fixa.data.repository;

import com.aula.fixa.data.local.preferences.SessionStorage;
import com.aula.fixa.data.remote.api.ApiClient;
import com.aula.fixa.data.remote.api.AuthApi;
import com.aula.fixa.data.remote.dto.CadastroOption;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public final class AuthRepository {
    private final AuthApi api = ApiClient.auth();
    private final SessionStorage session;

    public AuthRepository(SessionStorage session) {
        this.session = session;
    }

    public JSONObject login(String email, String password) throws Exception {
        JsonObject body = new JsonObject();

        body.addProperty("email", email);
        body.addProperty("senha", password);

        return execute(api.login(body));
    }

    public JSONObject loginGoogle(String idToken) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("idToken", idToken);

        return execute(api.loginGoogle(body));
    }

    public JSONObject register(
            String name,
            String email,
            String password,
            long unitId,
            String role,
            String birthDate
    ) throws Exception {
        JsonObject body = new JsonObject();

        body.addProperty("nomeCompleto", name);
        body.addProperty("email", email);
        body.addProperty("senha", password);
        body.addProperty("unidadeId", unitId);
        body.addProperty("cargo", role);
        body.addProperty("dataNascimento", birthDate);

        return execute(api.register(body));
    }

    public JSONObject requestPasswordRecovery(String email) throws Exception {
        return execute(api.requestPasswordRecovery(URLEncoder.encode(email, "UTF-8")));
    }

    public List<CadastroOption> institutions() throws Exception {
        return parseOptions(api.institutions().execute());
    }

    public List<CadastroOption> units(long institutionId) throws Exception {
        return parseOptions(api.units(institutionId).execute());
    }

    private List<CadastroOption> parseOptions(Response<com.google.gson.JsonArray> response) throws Exception {
        if (!response.isSuccessful() || response.body() == null) {
            throw new ApiException(
                    response.code(),
                    response.errorBody() == null ? "" : response.errorBody().string()
            );
        }
        List<CadastroOption> options = new ArrayList<>();
        for (com.google.gson.JsonElement element : response.body()) {
            com.google.gson.JsonObject object = element.getAsJsonObject();
            options.add(new CadastroOption(object.get("id").getAsLong(), object.get("nome").getAsString()));
        }
        return options;
    }


    private JSONObject execute(Call<JsonObject> call) throws Exception {
        Response<JsonObject> response;

        try {
            response = call.execute();
        } catch (IOException e) {
            throw new IOException("Não foi possível conectar à API", e);
        }

        if (!response.isSuccessful()) {
            throw new ApiException(
                    response.code(),
                    response.errorBody() == null ? "" : response.errorBody().string()
            );
        }

        JSONObject result = new JSONObject(response.body() == null ? "{}" : response.body().toString());
        String token = result.optString("token", "");

        if (!token.isBlank()) {
            session.saveToken(token);
        }

        return result;
    }

    public static final class ApiException extends Exception {
        public final int status;
        public final String body;

        public ApiException(int status, String body) {
            super("API returned HTTP " + status);
            this.status = status;
            this.body = body;
        }
    }

    public static String messageFrom(Throwable throwable) {
        if (throwable instanceof ApiException) {
            try {
                Object message = new JSONObject(((ApiException) throwable).body).opt("message");
                if (message instanceof org.json.JSONArray) return ((org.json.JSONArray) message).join("\\n").replace("\"", "");
                if (message != null && !message.toString().isBlank()) {
                    System.out.println(message.toString());
                    return message.toString();
                }
            } catch (Exception ignored) { }
        }
        return throwable.getMessage() == null ? "Não foi possível concluir a operação." : throwable.getMessage();
    }
}
