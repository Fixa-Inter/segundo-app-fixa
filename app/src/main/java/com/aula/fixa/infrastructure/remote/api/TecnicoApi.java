package com.aula.fixa.infrastructure.remote.api;

import com.aula.fixa.infrastructure.remote.dto.TecnicoResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TecnicoApi {
    @GET("api/v1/tecnicos")
    Call<List<TecnicoResponse>> listar(@Query("campos") String campos);

    @GET("api/v1/tecnicos/recomendar/{categoriaProblema}")
    Call<List<TecnicoResponse>> recomendar(@Path("categoriaProblema") String categoriaProblema);
}
