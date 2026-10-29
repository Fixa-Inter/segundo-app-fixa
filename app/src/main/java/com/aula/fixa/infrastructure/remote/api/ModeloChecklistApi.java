package com.aula.fixa.infrastructure.remote.api;

import com.aula.fixa.infrastructure.remote.dto.ModeloChecklistRequest;
import com.aula.fixa.infrastructure.remote.dto.ModeloChecklistResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ModeloChecklistApi {
    @GET("api/v1/modelo-checklist/listar") Call<List<ModeloChecklistResponse>> listar();
    @POST("api/v1/modelo-checklist/cadastrar") Call<ModeloChecklistResponse> cadastrar(@Body ModeloChecklistRequest request);
    @PATCH("api/v1/modelo-checklist/atualizar") Call<ModeloChecklistResponse> atualizar(@Body ModeloChecklistRequest request);
    @DELETE("api/v1/modelo-checklist/deletar/{id}") Call<ModeloChecklistResponse> excluir(@Path("id") String id);
}
