package com.aula.fixa.infrastructure.remote.dto;

public final class ProblemaAtualizarStatusRequest {
    public long problemaId;
    public String statusProblema;
    public String motivoRecusa;

    public ProblemaAtualizarStatusRequest(long problemaId, String statusProblema, String motivoRecusa) {
        this.problemaId = problemaId;
        this.statusProblema = statusProblema;
        this.motivoRecusa = motivoRecusa == null || motivoRecusa.trim().isEmpty() ? null : motivoRecusa.trim();
    }
}
