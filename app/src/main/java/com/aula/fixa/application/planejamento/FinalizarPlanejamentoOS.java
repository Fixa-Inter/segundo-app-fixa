package com.aula.fixa.application.planejamento;

import com.aula.fixa.domain.model.planejamento.TarefaPlanejada;
import com.aula.fixa.domain.repository.planejamento.PlanejamentoOSRepository;
import java.util.List;

/** Cria a OS e, em seguida, todas as tarefas do planejamento. */
public final class FinalizarPlanejamentoOS {
    private final PlanejamentoOSRepository repository;

    public FinalizarPlanejamentoOS(PlanejamentoOSRepository repository) {
        this.repository = repository;
    }

    public long executar(long solicitacaoId, long tecnicoId, int categoriaId,
                         int prioridadeId, String prazoIso, long ordemServicoExistenteId,
                         List<TarefaPlanejada> tarefas) throws Exception {
        if (solicitacaoId <= 0 || tecnicoId <= 0 || categoriaId <= 0 || prioridadeId < 0
                || prazoIso == null || prazoIso.trim().isEmpty()) {
            throw new IllegalArgumentException("Preencha todas as informações do planejamento.");
        }
        if (tarefas == null || tarefas.isEmpty()) {
            throw new IllegalArgumentException("Adicione ao menos uma tarefa antes de finalizar.");
        }
        for (TarefaPlanejada tarefa : tarefas) {
            if (tarefa == null || tarefa.titulo.trim().isEmpty() || tarefa.descricao.trim().isEmpty()) {
                throw new IllegalArgumentException("Todas as tarefas precisam de título e descrição.");
            }
        }

        long ordemServicoId = ordemServicoExistenteId > 0
                ? ordemServicoExistenteId
                : repository.criarOrdemServico(solicitacaoId, tecnicoId, categoriaId, prioridadeId, prazoIso);
        try {
            repository.criarTarefas(ordemServicoId, tarefas);
        } catch (Exception error) {
            if (ordemServicoExistenteId <= 0) {
                throw new TarefasPlanejamentoException(ordemServicoId, error);
            }
            throw error;
        }
        return ordemServicoId;
    }

    /** Identifica que a OS foi salva, mas o POST de tarefas falhou, permitindo retomar sem duplicar a OS. */
    public static final class TarefasPlanejamentoException extends Exception {
        public final long ordemServicoId;
        public TarefasPlanejamentoException(long ordemServicoId, Throwable cause) {
            super(cause == null ? null : cause.getMessage(), cause);
            this.ordemServicoId = ordemServicoId;
        }
    }
}
