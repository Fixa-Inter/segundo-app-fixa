package com.aula.fixa.infrastructure.remote.api;

import com.aula.fixa.infrastructure.remote.dto.EquipamentoRequest;
import com.aula.fixa.infrastructure.remote.dto.EquipamentoResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface EquipamentoApi {
    @GET("api/v1/equipamentos/modelo/{modeloId}") Call<List<EquipamentoResponse>> listar(@Path("modeloId") long modeloId);
    @POST("api/v1/equipamentos") Call<EquipamentoResponse> criar(@Body EquipamentoRequest request);
    @PATCH("api/v1/equipamentos") Call<EquipamentoResponse> atualizar(@Body EquipamentoRequest request);
    @DELETE("api/v1/equipamentos/{id}") Call<EquipamentoResponse> excluir(@Path("id") long id);
}
