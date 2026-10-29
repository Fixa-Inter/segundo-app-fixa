package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Busca registros completos da API para geração de planilhas. */
public final class ExportacaoRepository {
    public static final String SOLICITACOES = "Solicitações";
    public static final String ORDENS_SERVICO = "Ordens de serviço";
    public static final String OCORRENCIAS = "Ocorrências";

    private final SessionStorage session;

    public ExportacaoRepository(SessionStorage session) {
        this.session = session;
    }

    public List<Map<String, String>> listar(String tipo) throws Exception {
        if (SOLICITACOES.equals(tipo)) {
            Response<List<JsonObject>> response = ApiClient.solicitacoes(session)
                    .exportarUnidade(null, null, "dataCriacao", -1).execute();
            return rows(requireBody(response));
        }
        if (ORDENS_SERVICO.equals(tipo)) {
            Response<List<JsonObject>> response = ApiClient.ordensServico(session)
                    .exportar(null, null, null, null, null, null, null, null,
                            null, "dataCriacao", -1).execute();
            return rows(requireBody(response));
        }
        if (OCORRENCIAS.equals(tipo)) {
            Response<List<JsonObject>> response = ApiClient.ocorrencias(session).exportar().execute();
            return rows(requireBody(response));
        }
        throw new IllegalArgumentException("Exportação indisponível para: " + tipo);
    }

    private List<Map<String, String>> rows(List<JsonObject> items) {
        List<Map<String, String>> result = new ArrayList<>();
        for (JsonObject item : items) {
            if (item == null) continue;
            Map<String, String> row = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> field : item.entrySet()) {
                JsonElement value = field.getValue();
                row.put(field.getKey(), value == null || value.isJsonNull() ? "" :
                        value.isJsonPrimitive() ? value.getAsString() : value.toString());
            }
            result.add(row);
        }
        return result;
    }

    private static <T> T requireBody(Response<T> response) throws Exception {
        if (!response.isSuccessful()) {
            String body = response.errorBody() == null ? "" : response.errorBody().string();
            throw new com.aula.fixa.infrastructure.remote.ApiException(response.code(), body);
        }
        if (response.body() == null) throw new java.io.IOException("A API retornou uma resposta vazia.");
        return response.body();
    }
}
