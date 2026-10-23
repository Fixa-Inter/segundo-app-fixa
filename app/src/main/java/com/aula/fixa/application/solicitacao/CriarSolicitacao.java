package com.aula.fixa.application.solicitacao;

import com.aula.fixa.domain.model.NovaSolicitacao;
import com.aula.fixa.domain.model.Problema;
import com.aula.fixa.domain.repository.SolicitacaoRepository;

public final class CriarSolicitacao {
    private final SolicitacaoRepository repository;
    public CriarSolicitacao(SolicitacaoRepository repository) { this.repository = repository; }
    public Problema executar(NovaSolicitacao solicitacao) throws Exception {
        if (solicitacao == null) throw new IllegalArgumentException("Informe os dados da solicitação.");
        if (solicitacao.titulo == null || solicitacao.titulo.trim().isEmpty()) throw new IllegalArgumentException("Informe o título da solicitação.");
        if (solicitacao.descricao == null || solicitacao.descricao.trim().isEmpty()) throw new IllegalArgumentException("Informe a descrição da solicitação.");
        if (solicitacao.localId <= 0 || solicitacao.categoriaId <= 0) throw new IllegalArgumentException("Selecione o local e a categoria.");
        if (solicitacao.fotos.isEmpty()) throw new IllegalArgumentException("Anexe pelo menos uma foto.");
        return repository.criar(solicitacao);
    }
}
