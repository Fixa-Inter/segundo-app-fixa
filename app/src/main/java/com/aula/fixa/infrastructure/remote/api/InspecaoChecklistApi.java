package com.aula.fixa.infrastructure.remote.api;

import com.aula.fixa.infrastructure.remote.dto.InspecaoChecklistRequest;
import com.aula.fixa.infrastructure.remote.dto.InspecaoChecklistResponse;
import com.aula.fixa.infrastructure.remote.dto.ModeloEquipamentoChecklistResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface InspecaoChecklistApi {
    @GET("api/v1/inspecao-checklist/modelos/{checklistId}") Call<List<ModeloEquipamentoChecklistResponse>> listarModelos(@Path("checklistId") String checklistId);
    @POST("api/v1/inspecao-checklist/cadastrar") Call<InspecaoChecklistResponse> cadastrar(@Body InspecaoChecklistRequest request);
}
