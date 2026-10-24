package com.aula.fixa.application.solicitacao;

import com.aula.fixa.domain.model.solicitacao.SolicitacaoGestor;
import com.aula.fixa.domain.repository.solicitacao.SolicitacaoGestorRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ListarSolicitacoesGestor {
    private final SolicitacaoGestorRepository repository;
    public ListarSolicitacoesGestor(SolicitacaoGestorRepository repository) { this.repository = repository; }

    public List<SolicitacaoGestor> executar() throws Exception {
        List<SolicitacaoGestor> pending = new ArrayList<>(repository.listarSolicitacoesDaUnidade());
        pending.removeIf(item -> item == null || !"PENDENTE".equalsIgnoreCase(item.status));
        Comparator<SolicitacaoGestor> byDate = Comparator.comparing(item -> item.dataCriacao,
                Comparator.nullsLast(Comparator.<String>reverseOrder()));
        pending.sort(byDate.thenComparing(Comparator.comparingLong((SolicitacaoGestor item) -> item.id).reversed()));
        return pending;
    }
}
