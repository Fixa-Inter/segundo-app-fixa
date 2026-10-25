package com.aula.fixa.infrastructure.remote.api;

import com.aula.fixa.infrastructure.remote.dto.TarefaPlanejamentoRequest;
import com.google.gson.JsonObject;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TarefaApi {
    @POST("api/v1/tarefas")
    Call<List<JsonObject>> criar(@Body List<TarefaPlanejamentoRequest> tarefas);
}
