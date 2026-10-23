package com.aula.fixa.application.solicitacao;

import com.aula.fixa.domain.model.Problema;
import com.aula.fixa.domain.repository.SolicitacaoRepository;

public final class DetalharSolicitacao {
    private final SolicitacaoRepository repository;
    public DetalharSolicitacao(SolicitacaoRepository repository) { this.repository = repository; }
    public Problema executar(long id) throws Exception { return repository.buscarDetalhes(id); }
}
