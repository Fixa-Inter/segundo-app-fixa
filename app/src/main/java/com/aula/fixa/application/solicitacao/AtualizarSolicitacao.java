package com.aula.fixa.application.solicitacao;

import com.aula.fixa.domain.model.solicitacao.AtualizacaoSolicitacao;
import com.aula.fixa.domain.model.solicitacao.Problema;
import com.aula.fixa.domain.repository.solicitacao.SolicitacaoRepository;

public final class AtualizarSolicitacao {
    private final SolicitacaoRepository repository;
    public AtualizarSolicitacao(SolicitacaoRepository repository) { this.repository = repository; }
    public Problema executar(AtualizacaoSolicitacao solicitacao) throws Exception {
        if (solicitacao.titulo.trim().isEmpty()) throw new IllegalArgumentException("Informe o título da solicitação.");
        if (solicitacao.descricao.trim().isEmpty()) throw new IllegalArgumentException("Informe a descrição da solicitação.");
        return repository.atualizar(solicitacao);
    }
}
