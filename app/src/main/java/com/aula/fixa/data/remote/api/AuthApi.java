package com.aula.fixa.data.remote.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import okhttp3.ResponseBody;

public interface AuthApi {

    @POST("api/v1/auth/login")
    Call<JsonObject> login(@Body JsonObject body);

    @POST("api/v1/auth/login/google")
    Call<JsonObject> loginGoogle(@Body JsonObject body);

    @POST("api/v1/auth/cadastro")
    Call<JsonObject> register(@Body JsonObject body);

    @POST("api/v1/auth/esqueceu-senha/{email}")
    Call<JsonObject> requestPasswordRecovery(@Path(value = "email", encoded = true) String email);

    @POST("api/v1/auth/validar-codigo")
    Call<Boolean> validateRecoveryCode(@Body JsonObject body);

    @POST("api/v1/auth/redefinir-senha")
    Call<ResponseBody> resetPassword(@Body JsonObject body);

    @GET("api/v1/auth/instituicoes")
    Call<com.google.gson.JsonArray> institutions();

    @GET("api/v1/auth/instituicoes/{instituicaoId}/unidades")
    Call<com.google.gson.JsonArray> units(@Path("instituicaoId") long institutionId);
}
