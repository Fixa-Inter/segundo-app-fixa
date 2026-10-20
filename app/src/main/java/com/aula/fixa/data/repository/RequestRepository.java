package com.aula.fixa.data.repository;

import com.aula.fixa.data.local.preferences.SessionStorage;
import com.aula.fixa.data.remote.api.ApiClient;
import com.aula.fixa.data.remote.api.RequestApi;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.aula.fixa.data.remote.dto.CadastroOption;

import retrofit2.Response;

public final class RequestRepository {
    private final RequestApi api;

    public RequestRepository(SessionStorage session) {
        api = ApiClient.requests(session);
    }

    public JsonArray minhas() throws Exception {
        return array(api.minhas().execute());
    }

    public JsonObject detalhes(long id) throws Exception {
        return object(api.detalhes(id).execute());
    }

    public JsonObject criar(String title, String description, long localId, String localDescription, long categoryId, List<String> photos) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("titulo", title);
        body.addProperty("descricaoProblema", description);
        body.addProperty("localUnidadeID", localId);
        body.addProperty("descricaoLocal", localDescription);
        body.addProperty("categoriaEquipamentoId", categoryId);
        JsonArray urls = new JsonArray();
        photos.forEach(urls::add);
        body.add("urlsFoto", urls);
        return object(api.criar(body).execute());
    }

    public JsonObject atualizar(long id, String title, String description, Long localId,
                                String localDescription, Long categoryId,
                                List<String> photosToAdd, List<Long> photosToDelete) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("problemaId", id);
        if (title != null) body.addProperty("titulo", title);
        if (description != null) body.addProperty("descricaoProblema", description);
        if (localId != null) body.addProperty("localUnidadeId", localId);
        if (localDescription != null) body.addProperty("descricaoLocal", localDescription);
        if (categoryId != null) body.addProperty("categoriaEquipamentoId", categoryId);
        JsonArray add = new JsonArray();
        if (photosToAdd != null) photosToAdd.forEach(add::add);
        body.add("fotosAdicionar", add);
        JsonArray remove = new JsonArray();
        if (photosToDelete != null) photosToDelete.forEach(remove::add);
        body.add("fotosExcluir", remove);
        return object(api.atualizar(body).execute());
    }

    public JsonObject excluir(long id) throws Exception {
        return object(api.excluir(id).execute());
    }

    public JsonArray locais() throws Exception {
        return array(api.locais().execute());
    }

    public JsonArray categorias() throws Exception {
        return array(api.categorias().execute());
    }

    public JsonObject perfil() throws Exception {
        return object(api.perfil().execute());
    }

    public JsonObject atualizarPerfil(String nomeCompleto, String cargo, String urlFotoPerfil) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("nomeCompleto", nomeCompleto);
        body.addProperty("cargo", cargo);
        if (urlFotoPerfil != null) body.addProperty("urlFotoPerfil", urlFotoPerfil);
        return object(api.atualizarPerfil(body).execute());
    }

    public JsonArray eventos() throws Exception {
        return array(api.eventos().execute());
    }

    public JsonObject evento(long id) throws Exception {
        return object(api.evento(id).execute());
    }

    public List<CadastroOption> options(JsonArray values) {
        List<CadastroOption> result = new ArrayList<>();
        for (var value : values) {
            JsonObject item = value.getAsJsonObject();
            result.add(new CadastroOption(item.get("id").getAsLong(), item.get("nome").getAsString()));
        }
        return result;
    }

    private JsonArray array(Response<JsonArray> r) throws Exception {
        if (!r.isSuccessful()) {
            throw new Exception(AuthRepository.messageFrom(new AuthRepository.ApiException(r.code(), r.errorBody() == null ? "" : r.errorBody().string())));
        }
        return r.body();
    }

    private JsonObject object(Response<JsonObject> r) throws Exception {
        if (!r.isSuccessful()) {
            throw new Exception(AuthRepository.messageFrom(new AuthRepository.ApiException(r.code(), r.errorBody() == null ? "" : r.errorBody().string())));
        }
        return r.body();
    }
}
