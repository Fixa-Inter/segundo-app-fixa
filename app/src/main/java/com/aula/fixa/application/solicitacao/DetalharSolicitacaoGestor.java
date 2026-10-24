package com.aula.fixa.application.solicitacao;
import com.aula.fixa.domain.model.solicitacao.SolicitacaoGestor;
import com.aula.fixa.domain.repository.solicitacao.SolicitacaoGestorRepository;
public final class DetalharSolicitacaoGestor {
    private final SolicitacaoGestorRepository repository;
    public DetalharSolicitacaoGestor(SolicitacaoGestorRepository repository){this.repository=repository;}
    public SolicitacaoGestor executar(long id) throws Exception{return repository.detalhar(id);}
}
