package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.domain.model.tecnico.TecnicoDisponivel;
import com.aula.fixa.domain.repository.tecnico.TecnicoRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.ApiException;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.api.TecnicoApi;
import com.aula.fixa.infrastructure.remote.dto.TecnicoResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Response;

/** Implementa a busca de recomendação de técnicos pela API. */
public final class TecnicoRepositoryImpl implements TecnicoRepository {
    private final TecnicoApi api;
    public TecnicoRepositoryImpl(SessionStorage session) { api = ApiClient.tecnicos(session); }

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
            if (value.turnos != null && !value.turnos.isEmpty() && value.turnos.get(0) != null) {
                TecnicoResponse.TurnoResponse first = value.turnos.get(0);
                turno = first.nome == null || first.nome.trim().isEmpty()
                        ? formatHour(first.horaInicio) + " - " + formatHour(first.horaFim) : first.nome;
            }
            tecnicos.add(new TecnicoDisponivel(value.id, value.nomeCompleto, value.cargo,
                    value.tipoAcesso, value.urlFotoPerfil, turno,
                    aptidao == null ? "" : aptidao.categoriaProblema,
                    aptidao == null ? null : aptidao.nota,
                    value.tarefasEmAndamento == null ? 0 : value.tarefasEmAndamento));
        }
        return tecnicos;
    }

    private static String formatHour(Integer hour) {
        if (hour == null) return "--:--";
        return String.format(java.util.Locale.ROOT, "%02d:00", hour);
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
