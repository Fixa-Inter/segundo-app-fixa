package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.api.EventoApi;
import com.aula.fixa.infrastructure.remote.dto.EventoResponse;
import com.aula.fixa.infrastructure.remote.ApiException;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import java.util.Collections;
import java.util.List;
import retrofit2.Response;

/** Acesso remoto exclusivo de eventos. */
public final class EventoRemoteRepository {
    private final EventoApi api;
    public EventoRemoteRepository(SessionStorage session) { api = ApiClient.eventos(session); }
    public List<EventoResponse> eventos() throws Exception {
        Response<List<EventoResponse>> response = api.eventos().execute();
        if (response.code() == 404 && response.errorBody() != null) {
            String body = response.errorBody().string();
            try {
                if (new org.json.JSONObject(body).optString("message", "").trim().equalsIgnoreCase("Evento não encontrado.")) return Collections.emptyList();
            } catch (org.json.JSONException ignored) { }
            throw new ApiException(response.code(), body);
        }
        return requireBody(response);
    }
    public EventoResponse evento(long id) throws Exception { return requireBody(api.evento(id).execute()); }
    private static <T> T requireBody(Response<T> response) throws Exception {
        if (!response.isSuccessful()) throw new ApiException(response.code(), response.errorBody() == null ? "" : response.errorBody().string());
        if (response.body() == null) throw new java.io.IOException("A API retornou uma resposta vazia.");
        return response.body();
    }
}
