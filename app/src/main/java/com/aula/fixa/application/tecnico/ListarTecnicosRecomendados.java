package com.aula.fixa.application.tecnico;

import com.aula.fixa.domain.model.tecnico.TecnicoDisponivel;
import com.aula.fixa.domain.model.tecnico.TecnicoDetalhes;
import com.aula.fixa.domain.model.tecnico.CompetenciaTecnico;
import java.math.BigDecimal;
import com.aula.fixa.domain.repository.tecnico.TecnicoRepository;
import java.util.List;

public final class ListarTecnicosRecomendados {
    private final TecnicoRepository repository;
    public ListarTecnicosRecomendados(TecnicoRepository repository) { this.repository = repository; }
    public List<TecnicoDisponivel> executar(String categoriaProblema) throws Exception {
        return repository.recomendar(categoriaProblema);
    }
    public List<TecnicoDisponivel> listar() throws Exception { return repository.listar(); }
    public TecnicoDetalhes detalhes(long tecnicoId) throws Exception {
        if (tecnicoId <= 0) throw new IllegalArgumentException("Técnico inválido.");
        return repository.detalhes(tecnicoId);
    }
    public List<CompetenciaTecnico> competencias(long tecnicoId) throws Exception {
        if (tecnicoId <= 0) throw new IllegalArgumentException("Técnico inválido.");
        return repository.competencias(tecnicoId);
    }
    public CompetenciaTecnico atualizarCompetencia(long competenciaId, BigDecimal nota) throws Exception {
        if (competenciaId <= 0 || nota == null) throw new IllegalArgumentException("Informe uma nota válida.");
        return repository.atualizarCompetencia(competenciaId, nota);
    }
}
