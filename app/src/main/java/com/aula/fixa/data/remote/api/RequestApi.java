package com.aula.fixa.data.remote.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PATCH;
import retrofit2.http.DELETE;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RequestApi {
    @GET("api/v1/solicitacoes/minhas") Call<JsonArray> minhas();
    @GET("api/v1/solicitacoes/selecionar/{id}") Call<JsonObject> detalhes(@Path("id") long id);
    @POST("api/v1/solicitacoes") Call<JsonObject> criar(@Body JsonObject body);
    @PATCH("api/v1/solicitacoes") Call<JsonObject> atualizar(@Body JsonObject body);
    @DELETE("api/v1/solicitacoes/{id}") Call<JsonObject> excluir(@Path("id") long id);
    @GET("api/v1/local-unidade") Call<JsonArray> locais();
    @GET("api/v1/equipamentos/categorias") Call<JsonArray> categorias();
    @GET("api/v1/perfil") Call<JsonObject> perfil();
    @PATCH("api/v1/perfil") Call<JsonObject> atualizarPerfil(@Body JsonObject body);
    @GET("api/v1/eventos") Call<JsonArray> eventos();
    @GET("api/v1/eventos/{id}") Call<JsonObject> evento(@Path("id") long id);
}
