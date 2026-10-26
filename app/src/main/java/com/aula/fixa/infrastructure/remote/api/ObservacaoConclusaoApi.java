package com.aula.fixa.infrastructure.remote.api;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ObservacaoConclusaoApi {
    @POST("api/v1/observacoes-conclusao")
    Call<JsonObject> criar(@Body JsonObject body);
}
