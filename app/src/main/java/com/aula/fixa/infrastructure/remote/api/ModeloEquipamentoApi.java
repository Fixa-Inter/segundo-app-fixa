package com.aula.fixa.infrastructure.remote.api;

import com.aula.fixa.infrastructure.remote.dto.ModeloEquipamentoRequest;
import com.aula.fixa.infrastructure.remote.dto.ModeloEquipamentoResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ModeloEquipamentoApi {
    @GET("api/v1/equipamentos/modelos") Call<List<ModeloEquipamentoResponse>> listar();
    @POST("api/v1/equipamentos/modelos") Call<ModeloEquipamentoResponse> criar(@Body ModeloEquipamentoRequest request);
    @PATCH("api/v1/equipamentos/modelos") Call<ModeloEquipamentoResponse> atualizar(@Body ModeloEquipamentoRequest request);
    @DELETE("api/v1/equipamentos/modelos/{modeloId}") Call<ModeloEquipamentoResponse> excluir(@Path("modeloId") long id);
}
