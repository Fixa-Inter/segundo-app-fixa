package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.api.EventoApi;
import com.aula.fixa.infrastructure.remote.dto.EventoResponse;
import com.aula.fixa.infrastructure.remote.ApiException;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import java.util.Collections;
import java.util.List;
import retrofit2.Response;
import com.aula.fixa.domain.model.FiltroBusca;
import java.util.LinkedHashMap;
import java.util.Map;

/** Acesso remoto exclusivo de eventos. */
public final class EventoRemoteRepository {
    private final EventoApi api;
    public EventoRemoteRepository(SessionStorage session) { api = ApiClient.eventos(session); }
    public List<EventoResponse> eventos() throws Exception {
        return eventos(null);
    }
    public List<EventoResponse> eventos(FiltroBusca filtros) throws Exception {
        Map<String,String> query=new LinkedHashMap<>();FiltroBusca f=filtros;
        if (f != null) {
            put(query,"titulo",f.termo);put(query,"local_unidade",f.local);
            put(query,"data_hora_inicio",f.prazoInicioIso());put(query,"data_hora_fim",f.prazoFimIso());
        }
        query.put("campo_ordenacao","data_hora_inicio");query.put("direcao_ordenacao","1");
        Response<List<EventoResponse>> response = api.eventosFiltrados(query).execute();
        if (response.code() == 404 && response.errorBody() != null) {
            String body = response.errorBody().string();
            try {
                if (new org.json.JSONObject(body).optString("message", "").trim().equalsIgnoreCase("Evento não encontrado.")) return Collections.emptyList();
            } catch (org.json.JSONException ignored) { }
            throw new ApiException(response.code(), body);
        }
        return requireBody(response);
    }
    private static void put(Map<String,String> query,String key,String value){if(value!=null&&!value.trim().isEmpty())query.put(key,value);}
    public EventoResponse evento(long id) throws Exception { return requireBody(api.evento(id).execute()); }
    private static <T> T requireBody(Response<T> response) throws Exception {
        if (!response.isSuccessful()) throw new ApiException(response.code(), response.errorBody() == null ? "" : response.errorBody().string());
        if (response.body() == null) throw new java.io.IOException("A API retornou uma resposta vazia.");
        return response.body();
    }
}
