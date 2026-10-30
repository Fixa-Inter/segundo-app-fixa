package com.aula.fixa.infrastructure.remote.api;

import com.aula.fixa.infrastructure.remote.dto.PerfilAtualizarRequest;
import com.aula.fixa.infrastructure.remote.dto.PerfilResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;

public interface PerfilApi {
    @GET("api/v1/perfil") Call<PerfilResponse> perfil();
    @PATCH("api/v1/perfil") Call<PerfilResponse> atualizar(@Body PerfilAtualizarRequest body);
}
