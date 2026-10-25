package com.aula.fixa.infrastructure.remote.dto;

/** Corpo de criação da OS. Os enums de entrada da API são enviados pelo nome Java. */
public final class PlanejamentoOrdemRequest {
    public final long problemaId;
    public final long usuarioId;
    public final String statusOrdemServico = "NAO_INICIADA";
    public final String categoriaProblema;
    public final String dataPrevista;
    public final String prioridade;

    public PlanejamentoOrdemRequest(long problemaId, long usuarioId, String categoriaEnum,
                                    String prioridadeEnum, String dataPrevista) {
        this.problemaId = problemaId;
        this.usuarioId = usuarioId;
        this.categoriaProblema = categoriaEnum;
        this.prioridade = prioridadeEnum;
        this.dataPrevista = dataPrevista;
    }
}
