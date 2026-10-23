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

public interface SolicitacaoApi {
    @GET("api/v1/solicitacoes/minhas") Call<List<ProblemaResponse>> minhas();
    @GET("api/v1/solicitacoes/selecionar/{id}") Call<ProblemaResponse> detalhes(@Path("id") long id);
    @POST("api/v1/solicitacoes") Call<ProblemaResponse> criar(@Body ProblemaCriarRequest body);
    @PATCH("api/v1/solicitacoes") Call<ProblemaResponse> atualizar(@Body ProblemaAtualizarRequest body);
    @DELETE("api/v1/solicitacoes/{id}") Call<ProblemaResponse> excluir(@Path("id") long id);
}
