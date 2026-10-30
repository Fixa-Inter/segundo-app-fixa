package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.api.PerfilApi;
import com.aula.fixa.infrastructure.remote.dto.PerfilAtualizarRequest;
import com.aula.fixa.infrastructure.remote.dto.PerfilResponse;
import com.aula.fixa.infrastructure.remote.ApiException;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import retrofit2.Response;

/** Acesso remoto exclusivo do perfil. */
public final class PerfilRemoteRepository {
    private final PerfilApi api;
    public PerfilRemoteRepository(SessionStorage session) { api = ApiClient.perfil(session); }
    public PerfilResponse perfil() throws Exception { return requireBody(api.perfil().execute()); }
    public PerfilResponse atualizar(PerfilAtualizarRequest request) throws Exception { return requireBody(api.atualizar(request).execute()); }
    private static <T> T requireBody(Response<T> response) throws Exception {
        if (!response.isSuccessful()) throw new ApiException(response.code(), response.errorBody() == null ? "" : response.errorBody().string());
        if (response.body() == null) throw new java.io.IOException("A API retornou uma resposta vazia.");
        return response.body();
    }
}
