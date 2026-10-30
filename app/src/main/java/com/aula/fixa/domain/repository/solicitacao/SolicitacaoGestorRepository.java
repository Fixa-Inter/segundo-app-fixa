package com.aula.fixa.domain.repository.solicitacao;

import com.aula.fixa.domain.model.solicitacao.SolicitacaoGestor;
import com.aula.fixa.domain.enums.StatusProblema;
import java.util.List;

public interface SolicitacaoGestorRepository {
    List<SolicitacaoGestor> listarSolicitacoesDaUnidade() throws Exception;
    List<SolicitacaoGestor> listarSolicitacoesAprovadasDaUnidade() throws Exception;
    SolicitacaoGestor detalhar(long id) throws Exception;
    SolicitacaoGestor atualizarStatus(long id, StatusProblema status, String motivo) throws Exception;
}
