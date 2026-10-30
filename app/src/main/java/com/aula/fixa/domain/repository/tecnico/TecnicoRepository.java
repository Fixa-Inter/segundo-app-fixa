package com.aula.fixa.domain.repository.tecnico;

import com.aula.fixa.domain.model.tecnico.TecnicoDisponivel;
import com.aula.fixa.domain.model.tecnico.TecnicoDetalhes;
import com.aula.fixa.domain.model.tecnico.CompetenciaTecnico;
import java.math.BigDecimal;
import java.util.List;

public interface TecnicoRepository {
    List<TecnicoDisponivel> listar() throws Exception;
    List<TecnicoDisponivel> recomendar(String categoriaProblema) throws Exception;
    TecnicoDetalhes detalhes(long tecnicoId) throws Exception;
    List<CompetenciaTecnico> competencias(long tecnicoId) throws Exception;
    CompetenciaTecnico atualizarCompetencia(long competenciaId, BigDecimal nota) throws Exception;
}
