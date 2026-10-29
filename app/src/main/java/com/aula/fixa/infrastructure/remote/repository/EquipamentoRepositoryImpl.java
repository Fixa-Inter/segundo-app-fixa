package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.domain.model.equipamento.EquipamentoItem;
import com.aula.fixa.domain.repository.equipamento.EquipamentoRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.ApiException;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.api.EquipamentoApi;
import com.aula.fixa.infrastructure.remote.api.LocalUnidadeApi;
import com.aula.fixa.infrastructure.remote.api.PerfilApi;
import com.aula.fixa.infrastructure.remote.dto.EquipamentoRequest;
import com.aula.fixa.infrastructure.remote.dto.EquipamentoResponse;
import com.aula.fixa.infrastructure.remote.dto.OpcaoCadastroResponse;
import com.aula.fixa.infrastructure.remote.dto.PerfilResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;

public final class EquipamentoRepositoryImpl implements EquipamentoRepository {
    private final EquipamentoApi api;
    private final LocalUnidadeApi locaisApi;
    private final PerfilApi perfilApi;
    public EquipamentoRepositoryImpl(SessionStorage session) {
        api = ApiClient.equipamentos(session);
        locaisApi = ApiClient.locais(session);
        perfilApi = ApiClient.perfil(session);
    }

    @Override public List<EquipamentoItem> listar(long modeloId) throws Exception {
        Response<List<EquipamentoResponse>> response = api.listar(modeloId).execute();
        if (response.code() == 404) return new ArrayList<>();
        if (!response.isSuccessful()) throw apiError(response);
        List<EquipamentoItem> values = new ArrayList<>();
        if (response.body() != null) for (EquipamentoResponse value : response.body()) if (value != null) values.add(map(value));
        return values;
    }

    @Override public List<OpcaoCadastro> locais() throws Exception {
        Response<List<OpcaoCadastroResponse>> response = locaisApi.locais().execute();
        if (!response.isSuccessful()) throw apiError(response);
        List<OpcaoCadastro> values = new ArrayList<>();
        if (response.body() != null) for (OpcaoCadastroResponse value : response.body()) if (value != null) values.add(new OpcaoCadastro(value.id, value.nome));
        return values;
    }

    @Override public EquipamentoItem criar(long modeloId, long localId, String codigo) throws Exception {
        Response<PerfilResponse> profile = perfilApi.perfil().execute();
        if (!profile.isSuccessful()) throw apiError(profile);
        if (profile.body() == null || profile.body().id == null) throw new java.io.IOException("Não foi possível identificar o usuário atual.");
        return one(api.criar(EquipamentoRequest.criar(profile.body().id, modeloId, localId, codigo)).execute());
    }

    @Override public EquipamentoItem atualizar(long id, long localId, String codigo) throws Exception {
        return one(api.atualizar(EquipamentoRequest.atualizar(id, localId, codigo)).execute());
    }

    @Override public void excluir(long id) throws Exception {
        Response<EquipamentoResponse> response = api.excluir(id).execute();
        if (!response.isSuccessful()) throw apiError(response);
    }

    private EquipamentoItem one(Response<EquipamentoResponse> response) throws Exception {
        if (!response.isSuccessful()) throw apiError(response);
        if (response.body() == null) throw new java.io.IOException("A API retornou uma resposta vazia.");
        return map(response.body());
    }
    private EquipamentoItem map(EquipamentoResponse value) {
        return new EquipamentoItem(value.id == null ? 0 : value.id, value.codigo, value.localUnidade);
    }
    private ApiException apiError(Response<?> response) throws Exception {
        return new ApiException(response.code(), response.errorBody() == null ? "" : response.errorBody().string());
    }
}
