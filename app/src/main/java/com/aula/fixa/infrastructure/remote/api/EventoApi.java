package com.aula.fixa.infrastructure.remote.api;

import com.aula.fixa.infrastructure.remote.dto.EventoResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface EventoApi {
    @GET("api/v1/eventos") Call<List<EventoResponse>> eventos();
    @GET("api/v1/eventos/{id}") Call<EventoResponse> evento(@Path("id") long id);
}
