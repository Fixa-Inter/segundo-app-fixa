package com.aula.fixa.infrastructure.remote.repository.planejamento;

import com.aula.fixa.domain.repository.planejamento.RecomendacaoCategoriaRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.dto.jev.ClassificarProblemaRequest;
import com.aula.fixa.infrastructure.remote.dto.jev.ClassificarProblemaResponse;
import java.io.IOException;
import retrofit2.Response;

/** Implementa a integração externa com o classificador JEV. */
public final class RecomendacaoCategoriaRepositoryImpl implements RecomendacaoCategoriaRepository {
    private final com.aula.fixa.infrastructure.remote.api.JevApi api;

    public RecomendacaoCategoriaRepositoryImpl(SessionStorage session) {
        api = ApiClient.jev(session);
    }

    @Override public String classificar(String titulo, String descricao, String categoriaEquipamento) throws Exception {
        Response<ClassificarProblemaResponse> response = api.classificar(
                new ClassificarProblemaRequest(titulo, descricao, categoriaEquipamento)).execute();
        if (!response.isSuccessful()) {
            String body = response.errorBody() == null ? "" : response.errorBody().string();
            throw new IOException("Falha ao classificar a solicitação (HTTP " + response.code() + "): " + body);
        }
        ClassificarProblemaResponse body = response.body();
        // Algumas versões do classificador retornam a classificação sem o indicador booleano.
        // O valor da categoria é suficiente para aplicar a recomendação quando estiver presente.
        return body == null || body.classificacao == null || body.classificacao.trim().isEmpty()
                ? null : body.classificacao.trim();
    }
}
