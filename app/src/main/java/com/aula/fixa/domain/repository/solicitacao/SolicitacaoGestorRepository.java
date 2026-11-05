package com.aula.fixa.domain.repository.solicitacao;

import com.aula.fixa.domain.model.solicitacao.SolicitacaoGestor;
import com.aula.fixa.domain.enums.StatusProblema;
import java.util.List;
import com.aula.fixa.domain.model.FiltroBusca;

public interface SolicitacaoGestorRepository {
    List<SolicitacaoGestor> listarSolicitacoesDaUnidade() throws Exception;
    List<SolicitacaoGestor> listarSolicitacoesDaUnidade(FiltroBusca filtros) throws Exception;
    List<SolicitacaoGestor> listarSolicitacoesAprovadasDaUnidade() throws Exception;
    List<SolicitacaoGestor> listarSolicitacoesAprovadasDaUnidade(FiltroBusca filtros) throws Exception;
    SolicitacaoGestor detalhar(long id) throws Exception;
    SolicitacaoGestor atualizarStatus(long id, StatusProblema status, String motivo) throws Exception;
}
