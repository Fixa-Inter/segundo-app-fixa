package com.aula.fixa.application.solicitacao;

import com.aula.fixa.domain.repository.solicitacao.SolicitacaoRepository;

public final class ExcluirSolicitacao {
    private final SolicitacaoRepository repository;
    public ExcluirSolicitacao(SolicitacaoRepository repository) { this.repository = repository; }
    public void executar(long id) throws Exception {
        if (id <= 0) throw new IllegalArgumentException("Solicitação inválida.");
        repository.excluir(id);
    }
}
