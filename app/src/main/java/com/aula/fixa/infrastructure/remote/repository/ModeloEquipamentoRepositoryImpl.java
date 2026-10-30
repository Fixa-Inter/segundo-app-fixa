package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.domain.model.equipamento.ModeloEquipamentoItem;
import com.aula.fixa.domain.repository.equipamento.ModeloEquipamentoRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.ApiException;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.api.CategoriaEquipamentoApi;
import com.aula.fixa.infrastructure.remote.api.MarcaEquipamentoApi;
import com.aula.fixa.infrastructure.remote.api.ModeloEquipamentoApi;
import com.aula.fixa.infrastructure.remote.dto.ModeloEquipamentoRequest;
import com.aula.fixa.infrastructure.remote.dto.ModeloEquipamentoResponse;
import com.aula.fixa.infrastructure.remote.dto.OpcaoCadastroResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;

public final class ModeloEquipamentoRepositoryImpl implements ModeloEquipamentoRepository {
    private final ModeloEquipamentoApi modelos;
    private final MarcaEquipamentoApi marcas;
    private final CategoriaEquipamentoApi categorias;
    public ModeloEquipamentoRepositoryImpl(SessionStorage session) {
        modelos = ApiClient.modelosEquipamento(session);
        marcas = ApiClient.marcasEquipamento(session);
        categorias = ApiClient.categorias(session);
    }
    @Override public List<ModeloEquipamentoItem> listar() throws Exception {
        Response<List<ModeloEquipamentoResponse>> response = modelos.listar().execute();
        if (response.code() == 404) return new ArrayList<>();
        if (!response.isSuccessful()) throw apiError(response);
        List<ModeloEquipamentoItem> result = new ArrayList<>();
        if (response.body() != null) for (ModeloEquipamentoResponse item : response.body()) if (item != null) result.add(map(item));
        return result;
    }
    @Override public List<OpcaoCadastro> categorias() throws Exception { return options(categorias.categorias().execute()); }
    @Override public List<OpcaoCadastro> marcas() throws Exception { return options(marcas.listar().execute()); }
    @Override public ModeloEquipamentoItem criar(String nome, String descricao, long categoriaId, long marcaId) throws Exception {
        return one(modelos.criar(new ModeloEquipamentoRequest(0L, marcaId, categoriaId, nome, descricao)).execute());
    }
    @Override public ModeloEquipamentoItem atualizar(long id, String nome, String descricao, long categoriaId, long marcaId) throws Exception {
        return one(modelos.atualizar(new ModeloEquipamentoRequest(id, marcaId, categoriaId, nome, descricao)).execute());
    }
    @Override public void excluir(long id) throws Exception {
        Response<ModeloEquipamentoResponse> response = modelos.excluir(id).execute();
        if (!response.isSuccessful()) throw apiError(response);
    }
    private List<OpcaoCadastro> options(Response<List<OpcaoCadastroResponse>> response) throws Exception {
        if (response.code() == 404) return new ArrayList<>();
        if (!response.isSuccessful()) throw apiError(response);
        List<OpcaoCadastro> result = new ArrayList<>();
        if (response.body() != null) for (OpcaoCadastroResponse value : response.body()) if (value != null) result.add(new OpcaoCadastro(value.id, value.nome));
        return result;
    }
    private ModeloEquipamentoItem one(Response<ModeloEquipamentoResponse> response) throws Exception {
        if (!response.isSuccessful()) throw apiError(response);
        if (response.body() == null) throw new java.io.IOException("A API retornou uma resposta vazia.");
        return map(response.body());
    }
    private ModeloEquipamentoItem map(ModeloEquipamentoResponse item) {
        return new ModeloEquipamentoItem(item.id == null ? 0 : item.id, item.nome, item.categoria, item.marca,
                item.quantidadeAparelhos == null ? 0 : item.quantidadeAparelhos, 0, 0, item.descricao);
    }
    private ApiException apiError(Response<?> response) throws Exception {
        return new ApiException(response.code(), response.errorBody() == null ? "" : response.errorBody().string());
    }
}
