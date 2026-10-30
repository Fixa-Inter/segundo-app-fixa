package com.aula.fixa.infrastructure.remote.api;

import com.aula.fixa.infrastructure.remote.dto.TecnicoResponse;
import com.aula.fixa.infrastructure.remote.dto.OrdemServicoResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.PATCH;
import retrofit2.http.Body;

public interface TecnicoApi {
    @GET("api/v1/tecnicos")
    Call<List<TecnicoResponse>> listar(@Query("campos") String campos);

    @GET("api/v1/tecnicos/recomendar/{categoriaProblema}")
    Call<List<TecnicoResponse>> recomendar(@Path("categoriaProblema") String categoriaProblema);

    @GET("api/v1/tecnicos/competencias/{usuarioId}")
    Call<List<TecnicoResponse.AptidaoResponse>> competencias(@Path("usuarioId") long usuarioId);

    @PATCH("api/v1/tecnicos/competencias")
    Call<TecnicoResponse.AptidaoResponse> atualizarCompetencia(@Body CompetenciaAtualizarRequest request);

    @GET("api/v1/tecnicos/os/{usuarioId}")
    Call<List<OrdemServicoResponse>> ordensServico(@Path("usuarioId") long usuarioId);

}
