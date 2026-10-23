package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.domain.model.ManagerHomeData;
import com.aula.fixa.domain.repository.ManagerHomeRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.ApiException;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.dto.OrdemServicoResponse;
import com.aula.fixa.infrastructure.remote.dto.PerfilResponse;
import com.aula.fixa.infrastructure.remote.dto.ProblemaResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import retrofit2.Response;

/** Carrega os dados remotos usados exclusivamente pela homepage do gestor. */
public final class ManagerHomeRepositoryImpl implements ManagerHomeRepository {
    private final SessionStorage session;
    public ManagerHomeRepositoryImpl(SessionStorage session) { this.session = session; }

    @Override public ManagerHomeData carregar() throws Exception {
        PerfilResponse perfil = requireBody(ApiClient.perfil(session).perfil().execute());
        List<OrdemServicoResponse> ordens = optionalList(
                ApiClient.ordensServico(session).minhas(null, null, null, null).execute());
        List<ProblemaResponse> solicitacoes = new ArrayList<>(optionalList(
                ApiClient.solicitacoes(session).listarUnidade(null, "dataCriacao", -1).execute()));

        solicitacoes.removeIf(item -> item == null || !"PENDENTE".equals(normalize(item.status)));
        solicitacoes.sort(Comparator
                .comparing((ProblemaResponse item) -> item.dataCriacao == null ? "" : item.dataCriacao)
                .reversed()
                .thenComparing(item -> item.id == null ? 0L : item.id, Comparator.reverseOrder()));
        if (solicitacoes.size() > 5) solicitacoes = new ArrayList<>(solicitacoes.subList(0, 5));

        List<ManagerHomeData.OrdemServicoHomeItem> ordemItems = new ArrayList<>();
        for (OrdemServicoResponse ordem : ordens) {
            if (ordem != null) ordemItems.add(new ManagerHomeData.OrdemServicoHomeItem(ordem.status));
        }
        List<ManagerHomeData.SolicitacaoHomeItem> requestItems = new ArrayList<>();
        for (ProblemaResponse problema : solicitacoes) {
            requestItems.add(new ManagerHomeData.SolicitacaoHomeItem(
                    problema.id == null ? 0 : problema.id, problema.titulo, problema.localUnidade,
                    problema.descricaoLocal, problema.usuario, problema.dataCriacao));
        }
        return new ManagerHomeData(perfil.nomeCompleto, perfil.urlFotoPerfil, ordemItems, requestItems);
    }

    private static String normalize(String value) {
        return value == null ? "" : java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").trim().toUpperCase(Locale.ROOT);
    }

    private static <T> List<T> optionalList(Response<List<T>> response) throws Exception {
        if (!response.isSuccessful()) {
            String body = response.errorBody() == null ? "" : response.errorBody().string();
            if (response.code() == 404) return Collections.emptyList();
            throw new ApiException(response.code(), body);
        }
        if (response.body() == null) throw new IOException("A API retornou uma resposta vazia.");
        return response.body();
    }

    private static <T> T requireBody(Response<T> response) throws Exception {
        if (!response.isSuccessful()) {
            String body = response.errorBody() == null ? "" : response.errorBody().string();
            throw new ApiException(response.code(), body);
        }
        if (response.body() == null) throw new IOException("A API retornou uma resposta vazia.");
        return response.body();
    }
}
