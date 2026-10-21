package com.aula.fixa.data.remote.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AuthApi {

    @POST("api/v1/auth/login")
    Call<JsonObject> login(@Body JsonObject body);

    @POST("api/v1/auth/login/google")
    Call<JsonObject> loginGoogle(@Body JsonObject body);

    @POST("api/v1/auth/cadastro")
    Call<JsonObject> register(@Body JsonObject body);

    @POST("api/v1/auth/esqueceu-senha/{email}")
    Call<JsonObject> requestPasswordRecovery(@Path("email") String email);

    @GET("api/v1/auth/instituicoes")
    Call<com.google.gson.JsonArray> institutions();

    @GET("api/v1/auth/instituicoes/{instituicaoId}/unidades")
    Call<com.google.gson.JsonArray> units(@Path("instituicaoId") long institutionId);
}
