package com.aula.fixa.domain.repository.tecnico;

import com.aula.fixa.domain.model.tecnico.TecnicoDisponivel;
import java.util.List;

public interface TecnicoRepository {
    List<TecnicoDisponivel> listar() throws Exception;
    List<TecnicoDisponivel> recomendar(String categoriaProblema) throws Exception;
}
