package com.aula.fixa.infrastructure.remote.api;

import com.aula.fixa.infrastructure.remote.dto.OrdemServicoResponse;
import com.aula.fixa.infrastructure.remote.dto.PlanejamentoOrdemRequest;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface OrdemServicoApi {
    @POST("api/v1/os")
    Call<OrdemServicoResponse> criar(@Body PlanejamentoOrdemRequest body);

    @GET("api/v1/os/minhas")
    Call<List<OrdemServicoResponse>> minhas(
            @Query("limite") Integer limite,
            @Query("status_ordem_servico") String status,
            @Query("campo_ordenacao") String campoOrdenacao,
            @Query("direcao_ordenacao") Integer direcaoOrdenacao);
}
