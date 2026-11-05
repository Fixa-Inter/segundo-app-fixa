package com.aula.fixa.infrastructure.remote.api;

import com.aula.fixa.infrastructure.remote.dto.EventoResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import java.util.Map;

public interface EventoApi {
    @GET("api/v1/eventos") Call<List<EventoResponse>> eventos();
    @GET("api/v1/eventos") Call<List<EventoResponse>> eventosFiltrados(@QueryMap Map<String, String> filtros);
    @GET("api/v1/eventos/{id}") Call<EventoResponse> evento(@Path("id") long id);
}
