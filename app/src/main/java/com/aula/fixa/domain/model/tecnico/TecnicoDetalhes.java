package com.aula.fixa.domain.model.tecnico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TecnicoDetalhes {
    public final int ordensAtivas;
    public final int ordensConcluidas;
    public final List<CompetenciaTecnico> competencias;
    public final List<ResponsabilidadeTecnico> responsabilidades;

    public TecnicoDetalhes(int ordensAtivas, int ordensConcluidas,
                           List<CompetenciaTecnico> competencias,
                           List<ResponsabilidadeTecnico> responsabilidades) {
        this.ordensAtivas = ordensAtivas;
        this.ordensConcluidas = ordensConcluidas;
        this.competencias = Collections.unmodifiableList(new ArrayList<>(competencias));
        this.responsabilidades = Collections.unmodifiableList(new ArrayList<>(responsabilidades));
    }
}
