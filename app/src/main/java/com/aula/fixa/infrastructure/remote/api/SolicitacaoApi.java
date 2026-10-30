package com.aula.fixa.infrastructure.remote.api;

import com.aula.fixa.infrastructure.remote.dto.ProblemaAtualizarRequest;
import com.aula.fixa.infrastructure.remote.dto.ProblemaCriarRequest;
import com.aula.fixa.infrastructure.remote.dto.ProblemaResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import com.google.gson.JsonObject;

public interface SolicitacaoApi {
    @GET("api/v1/solicitacoes/minhas")
    Call<List<ProblemaResponse>> minhas();

    @GET("api/v1/solicitacoes")
    Call<List<ProblemaResponse>> listarUnidade(
            @Query("status_problema") String statusProblema,
            @Query("limite") Integer limite,
            @Query("campo_ordenacao") String campoOrdenacao,
            @Query("direcao_ordenacao") Integer direcaoOrdenacao);

    @GET("api/v1/solicitacoes")
    Call<List<JsonObject>> exportarUnidade(
            @Query("status_problema") String statusProblema,
            @Query("limite") Integer limite,
            @Query("campo_ordenacao") String campoOrdenacao,
            @Query("direcao_ordenacao") Integer direcaoOrdenacao);

    @GET("api/v1/solicitacoes/selecionar/{id}")
    Call<ProblemaResponse> detalhes(@Path("id") long id);

    @POST("api/v1/solicitacoes")
    Call<ProblemaResponse> criar(@Body ProblemaCriarRequest body);

    @PATCH("api/v1/solicitacoes")
    Call<ProblemaResponse> atualizar(@Body ProblemaAtualizarRequest body);

    @PATCH("api/v1/solicitacoes/status")
    Call<ProblemaResponse> atualizarStatus(@Body com.aula.fixa.infrastructure.remote.dto.ProblemaAtualizarStatusRequest body);

    @DELETE("api/v1/solicitacoes/{id}")
    Call<ProblemaResponse> excluir(@Path("id") long id);
}
