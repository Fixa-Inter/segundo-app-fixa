package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.domain.repository.equipamento.CategoriaEquipamentoRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.api.CategoriaEquipamentoApi;
import com.aula.fixa.infrastructure.remote.ApiException;
import com.aula.fixa.infrastructure.remote.dto.CategoriaEquipamentoRequest;
import com.aula.fixa.infrastructure.remote.dto.CategoriaEquipamentoUpdateRequest;
import com.aula.fixa.infrastructure.remote.dto.OpcaoCadastroResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;

public final class CategoriaEquipamentoRepositoryImpl implements CategoriaEquipamentoRepository {
    private final CategoriaEquipamentoApi api;
    public CategoriaEquipamentoRepositoryImpl(SessionStorage session) { api = ApiClient.categorias(session); }
    @Override public List<OpcaoCadastro> listar() throws Exception {
        Response<List<OpcaoCadastroResponse>> response = api.categorias().execute();
        if (!response.isSuccessful()) throw apiError(response);
        if (response.body() == null) throw new java.io.IOException("A API retornou uma resposta vazia.");
        List<OpcaoCadastro> result = new ArrayList<>();
        for (OpcaoCadastroResponse item : response.body()) if (item != null) result.add(new OpcaoCadastro(item.id, item.nome));
        return result;
    }
    @Override public OpcaoCadastro criar(String nome, String descricao) throws Exception {
        return option(api.criar(new CategoriaEquipamentoRequest(nome, descricao)).execute());
    }
    @Override public OpcaoCadastro atualizar(long id, String nome) throws Exception {
        return option(api.atualizar(new CategoriaEquipamentoUpdateRequest(id, nome)).execute());
    }
    @Override public void excluir(long id) throws Exception {
        Response<OpcaoCadastroResponse> response = api.excluir(id).execute();
        if (!response.isSuccessful()) throw apiError(response);
    }
    private OpcaoCadastro option(Response<OpcaoCadastroResponse> response) throws Exception {
        if (!response.isSuccessful()) throw apiError(response);
        if (response.body() == null) throw new java.io.IOException("A API retornou uma resposta vazia.");
        return new OpcaoCadastro(response.body().id, response.body().nome);
    }
    private ApiException apiError(Response<?> response) throws Exception {
        return new ApiException(response.code(), response.errorBody() == null ? "" : response.errorBody().string());
    }
}
