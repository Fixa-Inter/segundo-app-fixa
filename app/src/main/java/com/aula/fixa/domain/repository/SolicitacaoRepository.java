package com.aula.fixa.domain.repository;

import com.aula.fixa.domain.model.NovaSolicitacao;
import com.aula.fixa.domain.model.Problema;
import com.aula.fixa.domain.model.AtualizacaoSolicitacao;
import java.util.List;

/** Porta do domínio para operações de solicitação. */
public interface SolicitacaoRepository {
    List<Problema> listarMinhas() throws Exception;
    Problema buscarDetalhes(long id) throws Exception;
    Problema criar(NovaSolicitacao solicitacao) throws Exception;
    Problema atualizar(AtualizacaoSolicitacao solicitacao) throws Exception;
    void excluir(long id) throws Exception;
}
