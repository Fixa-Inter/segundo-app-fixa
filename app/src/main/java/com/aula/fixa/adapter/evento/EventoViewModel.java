package com.aula.fixa.adapter.evento;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import com.aula.fixa.application.evento.DetalharEvento;
import com.aula.fixa.application.evento.ListarEventos;
import com.aula.fixa.domain.model.Evento;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class EventoViewModel extends ViewModel {
    public interface Listener {
        void aoCarregar(boolean carregando);
        void aoListar(List<Evento> eventos);
        void aoDetalhar(Evento evento);
        void aoFalhar(Throwable erro);
    }

    private final ListarEventos listar;
    private final DetalharEvento detalhar;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    public EventoViewModel(ListarEventos listar, DetalharEvento detalhar) {
        this.listar = listar;
        this.detalhar = detalhar;
    }

    public void listar(@NonNull Listener listener) {
        listener.aoCarregar(true);
        executor.execute(() -> {
            try {
                List<Evento> result = listar.executar();
                main.post(() -> { listener.aoCarregar(false); listener.aoListar(result); });
            } catch (Throwable error) {
                main.post(() -> { listener.aoCarregar(false); listener.aoFalhar(error); });
            }
        });
    }

    public void detalhar(long id, @NonNull Listener listener) {
        listener.aoCarregar(true);
        executor.execute(() -> {
            try {
                Evento result = detalhar.executar(id);
                main.post(() -> { listener.aoCarregar(false); listener.aoDetalhar(result); });
            } catch (Throwable error) {
                main.post(() -> { listener.aoCarregar(false); listener.aoFalhar(error); });
            }
        });
    }

    @Override protected void onCleared() { executor.shutdownNow(); }
}
