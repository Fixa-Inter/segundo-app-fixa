package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.domain.model.planejamento.TarefaPlanejada;
import com.aula.fixa.domain.repository.planejamento.PlanejamentoOSRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.ApiException;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.dto.OrdemServicoResponse;
import com.aula.fixa.infrastructure.remote.dto.PlanejamentoOrdemRequest;
import com.aula.fixa.infrastructure.remote.dto.TarefaPlanejamentoRequest;
import com.aula.fixa.infrastructure.remote.dto.TarefasCriarRequest;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;

/** Executa as chamadas externas de criação da OS e de suas tarefas. */
public final class PlanejamentoOSRepositoryImpl implements PlanejamentoOSRepository {
    private final com.aula.fixa.infrastructure.remote.api.OrdemServicoApi ordens;
    private final com.aula.fixa.infrastructure.remote.api.TarefaApi tarefasApi;

    public PlanejamentoOSRepositoryImpl(SessionStorage session) {
        ordens = ApiClient.ordensServico(session);
        tarefasApi = ApiClient.tarefas(session);
    }

    @Override public long criarOrdemServico(long solicitacaoId, long tecnicoId, int categoriaId,
                                             int prioridadeId, String prazoIso) throws Exception {
        String categoriaEnum = categoryEnum(categoriaId);
        String prioridadeEnum = priorityEnum(prioridadeId);
        Response<OrdemServicoResponse> response = ordens.criar(new PlanejamentoOrdemRequest(
                solicitacaoId, tecnicoId, categoriaEnum, prioridadeEnum, prazoIso)).execute();
        if (!response.isSuccessful()) throw apiError(response.code(), response.errorBody());
        OrdemServicoResponse body = response.body();
        if (body == null || body.id == null || body.id <= 0) {
            throw new IOException("A API criou a OS sem retornar o identificador.");
        }
        return body.id;
    }

    @Override public void criarTarefas(long ordemServicoId, List<TarefaPlanejada> tarefas) throws Exception {
        List<TarefaPlanejamentoRequest> body = new ArrayList<>();
        for (TarefaPlanejada tarefa : tarefas) {
            body.add(new TarefaPlanejamentoRequest(ordemServicoId, tarefa.titulo, tarefa.descricao));
        }
        Response<List<JsonObject>> response = tarefasApi.criar(new TarefasCriarRequest(body)).execute();
        if (!response.isSuccessful()) throw apiError(response.code(), response.errorBody());
    }

    private Exception apiError(int status, okhttp3.ResponseBody errorBody) throws IOException {
        return new ApiException(status, errorBody == null ? "" : errorBody.string());
    }

    private String categoryEnum(int id) {
        for (com.aula.fixa.domain.enums.CategoriaProblema category :
                com.aula.fixa.domain.enums.CategoriaProblema.values()) {
            if (category.getId() == id) return category.name();
        }
        throw new IllegalArgumentException("Competência inválida.");
    }

    private String priorityEnum(int id) {
        for (com.aula.fixa.domain.enums.Prioridade priority : com.aula.fixa.domain.enums.Prioridade.values()) {
            if (priority.getId() == id) return priority.name();
        }
        throw new IllegalArgumentException("Prioridade inválida.");
    }
}
