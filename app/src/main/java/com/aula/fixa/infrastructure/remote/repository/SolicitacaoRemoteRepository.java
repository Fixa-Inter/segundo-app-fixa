package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.api.SolicitacaoApi;
import com.aula.fixa.infrastructure.remote.dto.ProblemaAtualizarRequest;
import com.aula.fixa.infrastructure.remote.dto.ProblemaAtualizarStatusRequest;
import com.aula.fixa.infrastructure.remote.dto.ProblemaCriarRequest;
import com.aula.fixa.infrastructure.remote.dto.ProblemaResponse;
import com.aula.fixa.infrastructure.remote.ApiException;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import retrofit2.Response;

/** Acesso remoto exclusivo do fluxo de solicitações. */
public final class SolicitacaoRemoteRepository {
    private final SolicitacaoApi api;

    public SolicitacaoRemoteRepository(SessionStorage session) {
        api = ApiClient.solicitacoes(session);
    }

    public List<ProblemaResponse> minhas() throws Exception {
        Response<List<ProblemaResponse>> response = api.minhas().execute();
        if (!response.isSuccessful()) {
            String body = response.errorBody() == null ? "" : response.errorBody().string();
            if (response.code() == 404 && expectedNotFound(body, "Solicitação não encontrada.")) {
                return Collections.emptyList();
            }
            throw new ApiException(response.code(), body);
        }
        if (response.body() == null) throw new IOException("A API retornou uma resposta vazia.");
        return response.body();
    }

    public List<ProblemaResponse> listarUnidade() throws Exception {
        return listarUnidade(null);
    }

    public List<ProblemaResponse> listarUnidade(String statusProblema) throws Exception {
        Response<List<ProblemaResponse>> response = api.listarUnidade(statusProblema, null, "dataCriacao", -1).execute();
        if (!response.isSuccessful()) {
            String body = response.errorBody() == null ? "" : response.errorBody().string();
            throw new ApiException(response.code(), body);
        }
        if (response.body() == null) throw new IOException("A API retornou uma resposta vazia.");
        return response.body();
    }

    public ProblemaResponse detalhes(long id) throws Exception {
        return requireBody(api.detalhes(id).execute());
    }

    public ProblemaResponse criar(ProblemaCriarRequest request) throws Exception {
        return requireBody(api.criar(request).execute());
    }

    public ProblemaResponse atualizar(ProblemaAtualizarRequest request) throws Exception {
        return requireBody(api.atualizar(request).execute());
    }

    public ProblemaResponse atualizarStatus(long id, String status, String motivo) throws Exception {
        return requireBody(api.atualizarStatus(new ProblemaAtualizarStatusRequest(id,status, motivo)).execute());
    }

    public ProblemaResponse excluir(long id) throws Exception {
        return requireBody(api.excluir(id).execute());
    }

    private static boolean expectedNotFound(String body, String expected) {
        try {
            return new org.json.JSONObject(body).optString("message", "").trim().equalsIgnoreCase(expected);
        } catch (org.json.JSONException ignored) {
            return false;
        }
    }

    private static <T> T requireBody(Response<T> response) throws Exception {
        if (!response.isSuccessful()) {
            String body = response.errorBody() == null ? "" : response.errorBody().string();
            throw new ApiException(response.code(), body);
        }
        if (response.body() == null) throw new IOException("A API retornou uma resposta vazia.");
        return response.body();
    }
}
