package com.aula.fixa.infrastructure.remote.api;

import com.aula.fixa.infrastructure.remote.dto.NotificacaoResponse;
import com.aula.fixa.infrastructure.remote.dto.TokenDispositivoRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface NotificacaoApi {
    @GET("api/v1/notificacao") Call<List<NotificacaoResponse>> listar();
    @PATCH("api/v1/notificacao/{id}/visualizada") Call<NotificacaoResponse> marcarVisualizada(@Path("id") String id);
    @DELETE("api/v1/notificacao/{id}") Call<Void> deletar(@Path("id") String id);
    @POST("api/v1/notificacao/dispositivo") Call<Void> registrarDispositivo(@Body TokenDispositivoRequest request);
    @DELETE("api/v1/notificacao/dispositivo") Call<Void> removerDispositivo(@Query("token") String token);
}
