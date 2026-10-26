package com.aula.fixa.infrastructure.remote.api;

import com.aula.fixa.infrastructure.remote.dto.TarefaPlanejamentoRequest;
import com.aula.fixa.infrastructure.remote.dto.TarefaResponse;
import com.google.gson.JsonObject;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.DELETE;
import retrofit2.http.Path;

public interface TarefaApi {
    @POST("api/v1/tarefas")
    Call<List<JsonObject>> criar(@Body List<TarefaPlanejamentoRequest> tarefas);

    @GET("api/v1/tarefas/{ordemServicoId}") Call<List<TarefaResponse>> listar(@Path("ordemServicoId") long ordemServicoId);
    @PATCH("api/v1/tarefas") Call<TarefaResponse> atualizar(@Body JsonObject tarefa);
    @DELETE("api/v1/tarefas/{tarefaId}") Call<TarefaResponse> excluir(@Path("tarefaId") long tarefaId);
}
