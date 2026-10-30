package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.infrastructure.remote.dto.OpcaoCadastroResponse;
import com.aula.fixa.infrastructure.remote.ApiException;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;

import java.util.List;

import retrofit2.Response;

/**
 * Opções remotas usadas nos formulários de solicitação.
 */
public final class CadastroRemoteRepository {
    private final com.aula.fixa.infrastructure.remote.api.LocalUnidadeApi locaisApi;
    private final com.aula.fixa.infrastructure.remote.api.CategoriaEquipamentoApi categoriasApi;

    public CadastroRemoteRepository(SessionStorage session) {
        locaisApi = ApiClient.locais(session);
        categoriasApi = ApiClient.categorias(session);
    }

    public List<OpcaoCadastro> locais() throws Exception {
        return map(requireBody(locaisApi.locais().execute()));
    }

    public List<OpcaoCadastro> categorias() throws Exception {
        return map(requireBody(categoriasApi.categorias().execute()));
    }

    private static List<OpcaoCadastro> map(List<OpcaoCadastroResponse> values) {
        java.util.ArrayList<OpcaoCadastro> result = new java.util.ArrayList<>();
        for (OpcaoCadastroResponse value : values) {
            result.add(new OpcaoCadastro(value.id, value.nome));
        }
        return result;
    }

    private static <T> T requireBody(Response<T> response) throws Exception {
        if (!response.isSuccessful()) {
            throw new ApiException(response.code(), response.errorBody() == null ? "" : response.errorBody().string());
        }
        if (response.body() == null) {
            throw new java.io.IOException("A API retornou uma resposta vazia.");
        }
        return response.body();
    }
}
