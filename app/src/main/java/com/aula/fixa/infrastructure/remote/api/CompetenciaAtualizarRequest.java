package com.aula.fixa.infrastructure.remote.api;

import java.math.BigDecimal;

/** Corpo do PATCH de atualização de nota de aptidão. */
public final class CompetenciaAtualizarRequest {
    public final Long id;
    public final BigDecimal nota;

    public CompetenciaAtualizarRequest(Long id, BigDecimal nota) {
        this.id = id;
        this.nota = nota;
    }
}
