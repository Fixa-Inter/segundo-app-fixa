package com.aula.fixa.domain.repository.planejamento;

import com.aula.fixa.domain.model.planejamento.TarefaPlanejada;
import java.util.List;

/** Contrato para persistir uma OS e as tarefas planejadas para ela. */
public interface PlanejamentoOSRepository {
    long criarOrdemServico(long solicitacaoId, long tecnicoId, int categoriaId,
                           int prioridadeId, String prazoIso) throws Exception;
    void criarTarefas(long ordemServicoId, List<TarefaPlanejada> tarefas) throws Exception;
}
