package com.aula.fixa.domain.repository;

import com.aula.fixa.domain.model.Evento;
import java.util.List;
import com.aula.fixa.domain.model.FiltroBusca;

public interface EventoRepository {
    List<Evento> listar() throws Exception;
    List<Evento> listar(FiltroBusca filtros) throws Exception;
    Evento buscar(long id) throws Exception;
}
