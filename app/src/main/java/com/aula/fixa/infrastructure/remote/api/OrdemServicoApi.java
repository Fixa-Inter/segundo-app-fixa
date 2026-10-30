package com.aula.fixa.infrastructure.remote.api;

import com.aula.fixa.infrastructure.remote.dto.OrdemServicoResponse;
import com.aula.fixa.infrastructure.remote.dto.PlanejamentoOrdemRequest;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Path;
import retrofit2.http.PATCH;
import retrofit2.http.DELETE;
import com.google.gson.JsonObject;

public interface OrdemServicoApi {
    @POST("api/v1/os")
    Call<OrdemServicoResponse> criar(@Body PlanejamentoOrdemRequest body);

    @GET("api/v1/os/minhas")
    Call<List<OrdemServicoResponse>> minhas(
            @Query("limite") Integer limite,
            @Query("status_ordem_servico") String status,
            @Query("campo_ordenacao") String campoOrdenacao,
            @Query("direcao_ordenacao") Integer direcaoOrdenacao);

    @GET("api/v1/os") Call<List<OrdemServicoResponse>> listar(
            @Query("titulo") String titulo, @Query("descricao") String descricao,
            @Query("categoria_problema") String categoriaProblema,
            @Query("categoria_equipamento") String categoriaEquipamento,
            @Query("prioridade") String prioridade,
            @Query("data_prevista_min") String dataPrevistaMin,
            @Query("data_prevista_max") String dataPrevistaMax,
            @Query("status_ordem_servico") String status,
            @Query("limite") Integer limite,
            @Query("campo_ordenacao") String campoOrdenacao,
            @Query("direcao_ordenacao") Integer direcaoOrdenacao);

    @GET("api/v1/os") Call<List<JsonObject>> exportar(
            @Query("titulo") String titulo, @Query("descricao") String descricao,
            @Query("categoria_problema") String categoriaProblema,
            @Query("categoria_equipamento") String categoriaEquipamento,
            @Query("prioridade") String prioridade,
            @Query("data_prevista_min") String dataPrevistaMin,
            @Query("data_prevista_max") String dataPrevistaMax,
            @Query("status_ordem_servico") String status,
            @Query("limite") Integer limite,
            @Query("campo_ordenacao") String campoOrdenacao,
            @Query("direcao_ordenacao") Integer direcaoOrdenacao);

    @GET("api/v1/os/{id}") Call<OrdemServicoResponse> selecionar(@Path("id") long id);
    @PATCH("api/v1/os") Call<OrdemServicoResponse> atualizar(@Body com.google.gson.JsonObject body);
    @DELETE("api/v1/os/{id}") Call<OrdemServicoResponse> excluir(@Path("id") long id);
}
