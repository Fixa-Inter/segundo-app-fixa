package com.aula.fixa.application.evento;

import com.aula.fixa.domain.model.Evento;
import com.aula.fixa.domain.repository.EventoRepository;
import java.util.List;

public final class ListarEventos {
    private final EventoRepository repository;
    public ListarEventos(EventoRepository repository) { this.repository = repository; }
    public List<Evento> executar() throws Exception { return repository.listar(); }
}
