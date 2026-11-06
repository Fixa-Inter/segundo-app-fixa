package com.aula.fixa.application.solicitacao;

import com.aula.fixa.domain.model.solicitacao.Problema;
import com.aula.fixa.domain.repository.solicitacao.SolicitacaoRepository;
import java.util.List;
import com.aula.fixa.domain.model.FiltroBusca;

public final class ListarSolicitacoes {
    private final SolicitacaoRepository repository;
    public ListarSolicitacoes(SolicitacaoRepository repository) { this.repository = repository; }
    public List<Problema> executar() throws Exception { return repository.listarMinhas(); }
    public List<Problema> executar(FiltroBusca filtros) throws Exception { return repository.listarMinhas(filtros); }
}
