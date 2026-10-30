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
        return ordenar(repository.listarSolicitacoesDaUnidade(), "PENDENTE");
    }

    public List<SolicitacaoGestor> executarAprovadas() throws Exception {
        return ordenar(repository.listarSolicitacoesAprovadasDaUnidade(), "APROVADO");
    }

    private static List<SolicitacaoGestor> ordenar(List<SolicitacaoGestor> source, String status) {
        List<SolicitacaoGestor> result = new ArrayList<>(source);
        result.removeIf(item -> item == null || !status.equalsIgnoreCase(item.status));
        Comparator<SolicitacaoGestor> byDate = Comparator.comparing(item -> item.dataCriacao,
                Comparator.nullsLast(Comparator.<String>reverseOrder()));
        result.sort(byDate.thenComparing(Comparator.comparingLong((SolicitacaoGestor item) -> item.id).reversed()));
        return result;
    }
}
