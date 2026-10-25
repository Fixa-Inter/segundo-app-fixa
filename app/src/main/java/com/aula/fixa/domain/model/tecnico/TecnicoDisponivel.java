package com.aula.fixa.domain.model.tecnico;

import java.math.BigDecimal;

public final class TecnicoDisponivel {
    public final long id;
    public final String nome;
    public final String cargo;
    public final String urlFoto;
    public final String competencia;
    public final BigDecimal notaCompetencia;
    public final long tarefasEmAndamento;

    public TecnicoDisponivel(long id, String nome, String cargo, String urlFoto,
                             String competencia, BigDecimal notaCompetencia, long tarefasEmAndamento) {
        this.id = id;
        this.nome = text(nome);
        this.cargo = text(cargo);
        this.urlFoto = text(urlFoto);
        this.competencia = text(competencia);
        this.notaCompetencia = notaCompetencia;
        this.tarefasEmAndamento = tarefasEmAndamento;
    }

    private static String text(String value) { return value == null ? "" : value; }
}
