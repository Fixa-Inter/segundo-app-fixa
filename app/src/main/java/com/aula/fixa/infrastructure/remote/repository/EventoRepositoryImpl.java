package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.domain.model.Evento;
import com.aula.fixa.domain.repository.EventoRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.dto.EventoResponse;
import java.util.ArrayList;
import java.util.List;

public final class EventoRepositoryImpl implements EventoRepository {
    private final EventoRemoteRepository remoto;
    public EventoRepositoryImpl(SessionStorage session) { remoto = new EventoRemoteRepository(session); }

    @Override public List<Evento> listar() throws Exception {
        List<Evento> result = new ArrayList<>();
        for (EventoResponse item : remoto.eventos()) result.add(map(item));
        return result;
    }

    @Override public Evento buscar(long id) throws Exception { return map(remoto.evento(id)); }

    private static Evento map(EventoResponse item) {
        return new Evento(item.id == null ? 0 : item.id, item.titulo, item.descricao,
                item.localUnidade, item.descricaoLocal, item.dataHoraInicio, item.dataHoraFim,
                item.observacao);
    }
}
