package com.aula.fixa.application.evento;

import com.aula.fixa.domain.model.Evento;
import com.aula.fixa.domain.repository.EventoRepository;
import java.util.List;
import com.aula.fixa.domain.model.FiltroBusca;

public final class ListarEventos {
    private final EventoRepository repository;
    public ListarEventos(EventoRepository repository) { this.repository = repository; }
    public List<Evento> executar() throws Exception { return repository.listar(); }
    public List<Evento> executar(FiltroBusca filtros) throws Exception { return repository.listar(filtros); }
}
