package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.domain.model.tecnico.TecnicoDisponivel;
import com.aula.fixa.domain.model.tecnico.CompetenciaTecnico;
import com.aula.fixa.domain.model.tecnico.ResponsabilidadeTecnico;
import com.aula.fixa.domain.model.tecnico.TecnicoDetalhes;
import com.aula.fixa.domain.repository.tecnico.TecnicoRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.ApiException;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.api.TecnicoApi;
import com.aula.fixa.infrastructure.remote.api.CompetenciaAtualizarRequest;
import com.aula.fixa.infrastructure.remote.dto.TecnicoResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.Locale;
import java.text.Normalizer;
import retrofit2.Response;

/** Implementa a busca de recomendação de técnicos pela API. */
public final class TecnicoRepositoryImpl implements TecnicoRepository {
    private final TecnicoApi api;
    public TecnicoRepositoryImpl(SessionStorage session) {
        api = ApiClient.tecnicos(session);
    }

    @Override public List<TecnicoDisponivel> listar() throws Exception {
        Response<List<TecnicoResponse>> response = api.listar(null).execute();
        if (!response.isSuccessful()) {
            throw new ApiException(response.code(), response.errorBody() == null ? "" : response.errorBody().string());
        }
        List<TecnicoResponse> body = response.body();
        if (body == null) throw new IOException("A API retornou uma resposta vazia.");
        List<TecnicoDisponivel> tecnicos = new ArrayList<>();
        for (TecnicoResponse value : body) {
            if (value == null || value.id == null) continue;
            TecnicoResponse.AptidaoResponse aptidao = value.competenciaVencedora;
            String turno = "";
            String horarioTurno = "";
            if (value.turnos != null && !value.turnos.isEmpty() && value.turnos.get(0) != null) {
                TecnicoResponse.TurnoResponse first = value.turnos.get(0);
                turno = first.nome == null ? "" : first.nome;
                if (first.horaInicio != null && first.horaFim != null) {
                    horarioTurno = formatHour(first.horaInicio) + " - " + formatHour(first.horaFim);
                }
            }
            tecnicos.add(new TecnicoDisponivel(value.id, value.nomeCompleto, value.cargo,
                    value.tipoAcesso, value.urlFotoPerfil, turno, horarioTurno,
                    aptidao == null ? "" : aptidao.categoriaProblema,
                    aptidao == null ? null : aptidao.nota,
                    value.tarefasEmAndamento == null ? 0 : value.tarefasEmAndamento));
        }
        return tecnicos;
    }

    private static String formatHour(Integer hour) {
        if (hour == null) return "--:--";
        return String.format(java.util.Locale.ROOT, "%02d:00", hour / 3600);
    }

    @Override public TecnicoDetalhes detalhes(long tecnicoId) throws Exception {
        Response<List<TecnicoResponse.AptidaoResponse>> competenceResponse = api.competencias(tecnicoId).execute();
        List<TecnicoResponse.AptidaoResponse> competenceBody = bodyOrEmptyWhenNotFound(competenceResponse);
        List<CompetenciaTecnico> competences = new ArrayList<>();
        for (TecnicoResponse.AptidaoResponse value : competenceBody) {
            if (value != null) competences.add(new CompetenciaTecnico(value.categoriaProblema, value.nota));
        }
        competences.sort(Comparator.comparing((CompetenciaTecnico item) -> item.nota,
                Comparator.nullsLast(Comparator.reverseOrder())));

        Response<List<com.aula.fixa.infrastructure.remote.dto.OrdemServicoResponse>> orderResponse = api.ordensServico(tecnicoId).execute();
        List<com.aula.fixa.infrastructure.remote.dto.OrdemServicoResponse> orders = bodyOrEmptyWhenNotFound(orderResponse);
        int activeOrders = 0;
        int completedOrders = 0;
        List<ResponsabilidadeTecnico> responsibilities = new ArrayList<>();
        for (com.aula.fixa.infrastructure.remote.dto.OrdemServicoResponse order : orders) {
            if (order == null || order.id == null) continue;
            boolean orderCompleted = isCompleted(order.status);
            if (orderCompleted) completedOrders++;
            else activeOrders++;

            if (!orderCompleted) {
                responsibilities.add(new ResponsabilidadeTecnico(order.titulo, order.descricao, order.localUnidade,
                        order.dataPrevista, order.prioridade, order.status));
            }
        }
        return new TecnicoDetalhes(activeOrders, completedOrders, competences, responsibilities);
    }

    @Override public List<CompetenciaTecnico> competencias(long tecnicoId) throws Exception {
        Response<List<TecnicoResponse.AptidaoResponse>> response = api.competencias(tecnicoId).execute();
        List<TecnicoResponse.AptidaoResponse> body = bodyOrEmptyWhenNotFound(response);
        List<CompetenciaTecnico> result = new ArrayList<>();
        for (TecnicoResponse.AptidaoResponse value : body) {
            if (value != null) result.add(new CompetenciaTecnico(value.id, value.categoriaProblema, value.nota));
        }
        result.sort(Comparator.comparing((CompetenciaTecnico item) -> item.nota,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    @Override public CompetenciaTecnico atualizarCompetencia(long competenciaId, java.math.BigDecimal nota) throws Exception {
        Response<TecnicoResponse.AptidaoResponse> response = api.atualizarCompetencia(
                new CompetenciaAtualizarRequest(competenciaId, nota)).execute();
        if (!response.isSuccessful()) throw new ApiException(response.code(),
                response.errorBody() == null ? "" : response.errorBody().string());
        TecnicoResponse.AptidaoResponse value = response.body();
        if (value == null) throw new IOException("A API retornou uma resposta vazia.");
        return new CompetenciaTecnico(value.id, value.categoriaProblema, value.nota);
    }

    private static boolean isCompleted(String status) {
        return normalize(status).equals("CONCLUIDA");
    }

    private static String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toUpperCase(Locale.ROOT);
    }

    private void check(Response<?> response) throws Exception {
        if (!response.isSuccessful()) throw new ApiException(response.code(),
                response.errorBody() == null ? "" : response.errorBody().string());
    }

    private <T> List<T> bodyOrEmptyWhenNotFound(Response<List<T>> response) throws Exception {
        if (response.code() == 404) return java.util.Collections.emptyList();
        check(response);
        return response.body() == null ? java.util.Collections.emptyList() : response.body();
    }

    @Override public List<TecnicoDisponivel> recomendar(String categoriaProblema) throws Exception {
        Response<List<TecnicoResponse>> response = api.recomendar(categoriaProblema).execute();
        if (!response.isSuccessful()) {
            throw new ApiException(response.code(), response.errorBody() == null ? "" : response.errorBody().string());
        }
        List<TecnicoResponse> body = response.body();
        if (body == null) throw new IOException("A API retornou uma resposta vazia.");
        List<TecnicoDisponivel> tecnicos = new ArrayList<>();
        for (TecnicoResponse value : body) {
            if (value == null || value.id == null) continue;
            TecnicoResponse.AptidaoResponse aptidao = value.competenciaVencedora;
            tecnicos.add(new TecnicoDisponivel(
                    value.id,
                    value.nomeCompleto,
                    value.cargo,
                    value.urlFotoPerfil,
                    aptidao == null ? "" : aptidao.categoriaProblema,
                    aptidao == null ? null : aptidao.nota,
                    value.tarefasEmAndamento == null ? 0 : value.tarefasEmAndamento
            ));
        }
        return tecnicos;
    }
}
