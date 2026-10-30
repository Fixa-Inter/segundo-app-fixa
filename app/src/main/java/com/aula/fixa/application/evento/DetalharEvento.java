package com.aula.fixa.application.evento;

import com.aula.fixa.domain.model.Evento;
import com.aula.fixa.domain.repository.EventoRepository;

public final class DetalharEvento {
    private final EventoRepository repository;
    public DetalharEvento(EventoRepository repository) { this.repository = repository; }
    public Evento executar(long id) throws Exception {
        if (id <= 0) throw new IllegalArgumentException("Evento inválido.");

        return repository.buscar(id);
    }
}
