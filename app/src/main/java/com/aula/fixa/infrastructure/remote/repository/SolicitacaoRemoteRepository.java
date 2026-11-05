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
import java.util.LinkedHashMap;
import java.util.Map;
import com.aula.fixa.domain.model.FiltroBusca;
import retrofit2.Response;

/** Acesso remoto exclusivo do fluxo de solicitações. */
public final class SolicitacaoRemoteRepository {
    private final SolicitacaoApi api;

    public SolicitacaoRemoteRepository(SessionStorage session) {
        api = ApiClient.solicitacoes(session);
    }

    public List<ProblemaResponse> minhas() throws Exception {
        return minhas(FiltroBusca.ultimosTresMeses());
    }

    public List<ProblemaResponse> minhas(FiltroBusca filtros) throws Exception {
        Response<List<ProblemaResponse>> response = api.minhasFiltradas(requestFilters(filtros)).execute();
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
        return listarUnidade(FiltroBusca.ultimosTresMeses());
    }

    public List<ProblemaResponse> listarUnidade(String statusProblema) throws Exception {
        return listarUnidade(FiltroBusca.periodo(null, null, statusProblema,
                null, null, null, null, null));
    }

    public List<ProblemaResponse> listarUnidade(FiltroBusca filtros) throws Exception {
        Response<List<ProblemaResponse>> response = api.listarUnidadeFiltrada(requestFilters(filtros)).execute();
        if (!response.isSuccessful()) {
            String body = response.errorBody() == null ? "" : response.errorBody().string();
            throw new ApiException(response.code(), body);
        }
        if (response.body() == null) throw new IOException("A API retornou uma resposta vazia.");
        return response.body();
    }

    private static Map<String, String> requestFilters(FiltroBusca filtros) {
        FiltroBusca value = filtros == null ? FiltroBusca.ultimosTresMeses() : filtros;
        Map<String, String> query = new LinkedHashMap<>();
        put(query, "titulo", value.termo);
        put(query, "status_problema", value.status);
        put(query, "local_unidade", value.local);
        put(query, "categoria_equipamento", value.categoriaEquipamento);
        put(query, "nome_usuario", value.autor);
        put(query, "data_criacao_min", value.dataInicioIso());
        put(query, "data_criacao_max", value.dataFimIso());
        query.put("campo_ordenacao", "dataCriacao");
        query.put("direcao_ordenacao", "-1");
        return query;
    }

    private static void put(Map<String, String> target, String key, String value) {
        if (value != null && !value.trim().isEmpty()) target.put(key, value);
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
