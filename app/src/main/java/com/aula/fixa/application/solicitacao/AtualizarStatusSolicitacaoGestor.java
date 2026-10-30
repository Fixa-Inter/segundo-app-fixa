package com.aula.fixa.application.solicitacao;
import com.aula.fixa.domain.model.solicitacao.SolicitacaoGestor;
import com.aula.fixa.domain.repository.solicitacao.SolicitacaoGestorRepository;
import com.aula.fixa.domain.enums.StatusProblema;

public final class AtualizarStatusSolicitacaoGestor {
    private final SolicitacaoGestorRepository repository;
    public AtualizarStatusSolicitacaoGestor(SolicitacaoGestorRepository repository) { this.repository = repository; }

    public SolicitacaoGestor executar(long id, StatusProblema status, String motivo) throws Exception {
        if (id <= 0) throw new IllegalArgumentException("Solicitação inválida.");
        if (status != StatusProblema.APROVADO && status != StatusProblema.REPROVADO) {
            throw new IllegalArgumentException("Status inválido para análise da solicitação.");
        }
        boolean hasReason = motivo != null && !motivo.trim().isEmpty();
        if (status == StatusProblema.REPROVADO && !hasReason) {
            throw new IllegalArgumentException("Informe o motivo da reprovação.");
        }
        if (status == StatusProblema.APROVADO && hasReason) {
            throw new IllegalArgumentException("A aprovação não deve conter motivo de reprovação.");
        }
        return repository.atualizarStatus(id, status, hasReason ? motivo.trim() : null);
    }
}
