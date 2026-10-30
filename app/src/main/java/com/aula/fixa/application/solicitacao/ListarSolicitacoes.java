package com.aula.fixa.application.solicitacao;

import com.aula.fixa.domain.model.solicitacao.Problema;
import com.aula.fixa.domain.repository.solicitacao.SolicitacaoRepository;
import java.util.List;

public final class ListarSolicitacoes {
    private final SolicitacaoRepository repository;
    public ListarSolicitacoes(SolicitacaoRepository repository) { this.repository = repository; }
    public List<Problema> executar() throws Exception { return repository.listarMinhas(); }
}
