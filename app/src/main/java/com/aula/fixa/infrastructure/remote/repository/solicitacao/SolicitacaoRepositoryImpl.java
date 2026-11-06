package com.aula.fixa.infrastructure.remote.repository.solicitacao;

import com.aula.fixa.infrastructure.remote.dto.ProblemaCriarRequest;
import com.aula.fixa.infrastructure.remote.dto.ProblemaResponse;
import com.aula.fixa.infrastructure.remote.repository.SolicitacaoRemoteRepository;
import com.aula.fixa.domain.model.solicitacao.NovaSolicitacao;
import com.aula.fixa.domain.model.solicitacao.Problema;
import com.aula.fixa.domain.model.solicitacao.AtualizacaoSolicitacao;
import com.aula.fixa.domain.model.solicitacao.FotoProblema;
import com.aula.fixa.domain.repository.solicitacao.SolicitacaoRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.local.NetworkAvailability;
import com.aula.fixa.infrastructure.local.OfflineDatabase;
import com.aula.fixa.infrastructure.local.OfflineSolicitacaoSyncScheduler;
import com.aula.fixa.infrastructure.local.PendingSolicitacao;
import com.aula.fixa.infrastructure.cloudinary.CloudinaryUploadService;
import com.google.gson.Gson;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import com.aula.fixa.domain.model.FiltroBusca;

/** Implementação da porta de domínio usando a API REST. */
public final class SolicitacaoRepositoryImpl implements SolicitacaoRepository {
    private final SolicitacaoRemoteRepository remoto;
    private final SessionStorage session;

    public SolicitacaoRepositoryImpl(SessionStorage session) {
        this.session = session;
        remoto = new SolicitacaoRemoteRepository(session);
    }

    @Override public List<Problema> listarMinhas() throws Exception {
        return listarMinhas(FiltroBusca.ultimosTresMeses());
    }

    @Override public List<Problema> listarMinhas(FiltroBusca filtros) throws Exception {
        List<Problema> result = new ArrayList<>();
        for (ProblemaResponse response : remoto.minhas(filtros)) result.add(map(response));
        return result;
    }

    @Override public Problema buscarDetalhes(long id) throws Exception { return map(remoto.detalhes(id)); }

    @Override public Problema criar(NovaSolicitacao solicitacao) throws Exception {
        if (!NetworkAvailability.isOnline(session.applicationContext())) return enfileirar(solicitacao);
        ProblemaCriarRequest request = new ProblemaCriarRequest();
        request.titulo = solicitacao.titulo;
        request.descricaoProblema = solicitacao.descricao;
        request.localUnidadeID = solicitacao.localId;
        request.descricaoLocal = solicitacao.descricaoLocal;
        request.categoriaEquipamentoId = solicitacao.categoriaId;
        request.urlsFoto = new ArrayList<>(solicitacao.fotos);
        try {
            for (String path : solicitacao.fotosLocais) request.urlsFoto.add(CloudinaryUploadService.salvarBloqueante(session.applicationContext(), path));
        } catch (Exception error) {
            if (!NetworkAvailability.isOnline(session.applicationContext())) return enfileirar(solicitacao);
            throw error;
        }
        return map(remoto.criar(request));
    }

    private Problema enfileirar(NovaSolicitacao solicitacao) throws Exception {
        String id = UUID.randomUUID().toString();
        OfflineDatabase.get(session.applicationContext()).enqueueRequest(id, session.localDataOwnerKey(),
                session.encryptLocalData(new Gson().toJson(PendingSolicitacao.from(solicitacao)).getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        OfflineSolicitacaoSyncScheduler.schedule(session.applicationContext());
        List<FotoProblema> photos = new ArrayList<>();
        for (String url : solicitacao.fotos) photos.add(new FotoProblema(0, url));
        return new Problema(-System.currentTimeMillis(), solicitacao.titulo, solicitacao.descricao,
                "", solicitacao.descricaoLocal, solicitacao.descricaoLocal, "AGUARDANDO_ENVIO",
                java.time.Instant.now().toString(), photos);
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
        request.fotosAdicionar = solicitacao.fotosAdicionar;
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
