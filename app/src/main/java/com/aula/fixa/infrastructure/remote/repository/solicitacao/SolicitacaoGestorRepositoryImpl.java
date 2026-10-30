package com.aula.fixa.infrastructure.remote.repository.solicitacao;

import com.aula.fixa.domain.enums.StatusProblema;
import com.aula.fixa.domain.model.solicitacao.FotoProblema;
import com.aula.fixa.domain.model.solicitacao.SolicitacaoGestor;
import com.aula.fixa.domain.repository.solicitacao.SolicitacaoGestorRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.dto.FotoResponse;
import com.aula.fixa.infrastructure.remote.dto.ProblemaResponse;
import com.aula.fixa.infrastructure.remote.repository.SolicitacaoRemoteRepository;

import java.util.ArrayList;
import java.util.List;

/** API adapter for the manager's pending request workflow. */
public final class SolicitacaoGestorRepositoryImpl implements SolicitacaoGestorRepository {
    private final SolicitacaoRemoteRepository remote;

    public SolicitacaoGestorRepositoryImpl(SessionStorage session) {
        remote = new SolicitacaoRemoteRepository(session);
    }

    @Override public List<SolicitacaoGestor> listarSolicitacoesDaUnidade() throws Exception {
        return mapAll(remote.listarUnidade());
    }

    @Override public List<SolicitacaoGestor> listarSolicitacoesAprovadasDaUnidade() throws Exception {
        return mapAll(remote.listarUnidade("APROVADO"));
    }

    private static List<SolicitacaoGestor> mapAll(List<ProblemaResponse> values) {
        List<SolicitacaoGestor> result = new ArrayList<>();
        for (ProblemaResponse value : values) {
            if (value != null) result.add(map(value));
        }
        return result;
    }

    @Override public SolicitacaoGestor detalhar(long id) throws Exception {
        return map(remote.detalhes(id));
    }

    @Override public SolicitacaoGestor atualizarStatus(long id, StatusProblema status, String motivo) throws Exception {
        return map(remote.atualizarStatus(id, status.name(), motivo));
    }

    private static SolicitacaoGestor map(ProblemaResponse value) {
        List<FotoProblema> photos = new ArrayList<>();
        if (value.fotos != null) {
            for (FotoResponse photo : value.fotos) {
                if (photo != null && photo.url != null) {
                    photos.add(new FotoProblema(photo.id == null ? 0 : photo.id, photo.url));
                }
            }
        }
        return new SolicitacaoGestor(value.id == null ? 0 : value.id, value.titulo, value.descricaoProblema,
                value.categoriaEquipamento, value.localUnidade, value.descricaoLocal, value.status,
                value.dataCriacao, value.usuario, photos);
    }
}
