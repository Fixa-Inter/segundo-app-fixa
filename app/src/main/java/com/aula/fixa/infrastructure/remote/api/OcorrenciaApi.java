package com.aula.fixa.infrastructure.remote.api;

import com.aula.fixa.infrastructure.remote.dto.OcorrenciaAtualizarRequest;
import com.aula.fixa.infrastructure.remote.dto.OcorrenciaResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;

public interface OcorrenciaApi {
    @GET("api/v1/ocorrencias") Call<List<OcorrenciaResponse>> listar();
    @GET("api/v1/ocorrencias/{id}") Call<OcorrenciaResponse> detalhar(@Path("id") long id);
    @PATCH("api/v1/ocorrencias") Call<OcorrenciaResponse> atualizar(@Body OcorrenciaAtualizarRequest body);
    @DELETE("api/v1/ocorrencias/{id}") Call<OcorrenciaResponse> excluir(@Path("id") long id);
}
