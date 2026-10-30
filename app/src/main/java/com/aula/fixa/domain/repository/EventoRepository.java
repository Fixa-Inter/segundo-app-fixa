package com.aula.fixa.domain.repository;

import com.aula.fixa.domain.model.Evento;
import java.util.List;

public interface EventoRepository {
    List<Evento> listar() throws Exception;
    Evento buscar(long id) throws Exception;
}
