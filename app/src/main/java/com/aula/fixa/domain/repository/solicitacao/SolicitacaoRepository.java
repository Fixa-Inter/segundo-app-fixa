package com.aula.fixa.domain.repository.solicitacao;

import com.aula.fixa.domain.model.solicitacao.NovaSolicitacao;
import com.aula.fixa.domain.model.solicitacao.Problema;
import com.aula.fixa.domain.model.solicitacao.AtualizacaoSolicitacao;
import java.util.List;

/** Porta do domínio para operações de solicitação. */
public interface SolicitacaoRepository {
    List<Problema> listarMinhas() throws Exception;
    Problema buscarDetalhes(long id) throws Exception;
    Problema criar(NovaSolicitacao solicitacao) throws Exception;
    Problema atualizar(AtualizacaoSolicitacao solicitacao) throws Exception;
    void excluir(long id) throws Exception;
}
