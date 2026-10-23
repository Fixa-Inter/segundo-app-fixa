package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.infrastructure.remote.dto.ProblemaCriarRequest;
import com.aula.fixa.infrastructure.remote.dto.ProblemaResponse;
import com.aula.fixa.infrastructure.remote.repository.SolicitacaoRemoteRepository;
import com.aula.fixa.domain.model.NovaSolicitacao;
import com.aula.fixa.domain.model.Problema;
import com.aula.fixa.domain.model.AtualizacaoSolicitacao;
import com.aula.fixa.domain.model.FotoProblema;
import com.aula.fixa.domain.repository.SolicitacaoRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Implementação da porta de domínio usando a API REST. */
public final class SolicitacaoRepositoryImpl implements SolicitacaoRepository {
    private final SolicitacaoRemoteRepository remoto;

    public SolicitacaoRepositoryImpl(SessionStorage session) {
        remoto = new SolicitacaoRemoteRepository(session);
    }

    @Override public List<Problema> listarMinhas() throws Exception {
        List<Problema> result = new ArrayList<>();
        for (ProblemaResponse response : remoto.minhas()) result.add(map(response));
        return result;
    }

    @Override public Problema buscarDetalhes(long id) throws Exception { return map(remoto.detalhes(id)); }

    @Override public Problema criar(NovaSolicitacao solicitacao) throws Exception {
        ProblemaCriarRequest request = new ProblemaCriarRequest();
        request.titulo = solicitacao.titulo;
        request.descricaoProblema = solicitacao.descricao;
        request.localUnidadeID = solicitacao.localId;
        request.descricaoLocal = solicitacao.descricaoLocal;
        request.categoriaEquipamentoId = solicitacao.categoriaId;
        request.urlsFoto = solicitacao.fotos;
        return map(remoto.criar(request));
    }

    @Override public void excluir(long id) throws Exception { remoto.excluir(id); }

    @Override public Problema atualizar(AtualizacaoSolicitacao solicitacao) throws Exception {
        com.aula.fixa.infrastructure.remote.dto.ProblemaAtualizarRequest request =
                new com.aula.fixa.infrastructure.remote.dto.ProblemaAtualizarRequest();
        request.problemaId = solicitacao.id;
        request.titulo = solicitacao.titulo;
        request.descricaoProblema = solicitacao.descricao;
        request.localUnidadeId = solicitacao.localId;
        request.descricaoLocal = solicitacao.descricaoLocal;
        request.categoriaEquipamentoId = solicitacao.categoriaId;
        request.fotosAdicionar = Collections.emptyList();
        request.fotosExcluir = solicitacao.fotosExcluir;
        return map(remoto.atualizar(request));
    }

    private static Problema map(ProblemaResponse response) {
        List<FotoProblema> photos = new ArrayList<>();
        if (response.fotos != null) {
            response.fotos.stream().filter(photo -> photo != null && photo.url != null)
                    .forEach(photo -> photos.add(new FotoProblema(photo.id == null ? 0 : photo.id, photo.url)));
        }
        return new Problema(response.id == null ? 0 : response.id, response.titulo,
                response.descricaoProblema, response.categoriaEquipamento, response.localUnidade,
                response.descricaoLocal, response.status, response.dataCriacao,
                photos.isEmpty() ? Collections.emptyList() : photos);
    }
}
