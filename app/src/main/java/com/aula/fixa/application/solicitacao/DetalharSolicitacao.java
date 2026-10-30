package com.aula.fixa.application.solicitacao;

import com.aula.fixa.domain.model.solicitacao.Problema;
import com.aula.fixa.domain.repository.solicitacao.SolicitacaoRepository;

public final class DetalharSolicitacao {
    private final SolicitacaoRepository repository;
    public DetalharSolicitacao(SolicitacaoRepository repository) { this.repository = repository; }
    public Problema executar(long id) throws Exception { return repository.buscarDetalhes(id); }
}
