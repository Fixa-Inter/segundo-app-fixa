package com.aula.fixa.domain.model.tecnico;

import java.math.BigDecimal;

public final class CompetenciaTecnico {
    public final Long id;
    public final String nome;
    public final BigDecimal nota;

    public CompetenciaTecnico(String nome, BigDecimal nota) {
        this(null, nome, nota);
    }

    public CompetenciaTecnico(Long id, String nome, BigDecimal nota) {
        this.id = id;
        this.nome = nome == null ? "" : nome;
        this.nota = nota;
    }
}
