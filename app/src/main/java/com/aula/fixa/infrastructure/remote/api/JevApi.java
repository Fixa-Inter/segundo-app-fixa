package com.aula.fixa.infrastructure.remote.api;

import com.aula.fixa.infrastructure.remote.dto.jev.ClassificarProblemaRequest;
import com.aula.fixa.infrastructure.remote.dto.jev.ClassificarProblemaResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/** Chamadas ao serviço de IA JEV, independente da API de conversação. */
public interface JevApi {
    @Headers("Accept: application/json")
    @POST("api/v1/classificar/classificar-problema")
    Call<ClassificarProblemaResponse> classificar(@Body ClassificarProblemaRequest request);
}
