package com.aula.fixa.adapter.tecnico;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.ViewModel;
import com.aula.fixa.application.tecnico.ListarTecnicosRecomendados;
import com.aula.fixa.domain.model.tecnico.TecnicoDisponivel;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class TecnicoViewModel extends ViewModel {
    public interface Callback<T> { void success(T value); void failure(Throwable error); }
    private final ListarTecnicosRecomendados listar;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    public TecnicoViewModel(ListarTecnicosRecomendados listar) { this.listar = listar; }

    public void recomendar(String categoria, Callback<List<TecnicoDisponivel>> callback) {
        executor.execute(() -> {
            try {
                List<TecnicoDisponivel> result = listar.executar(categoria);
                main.post(() -> callback.success(result));
            } catch (Throwable error) {
                main.post(() -> callback.failure(error));
            }
        });
    }

    public void listar(Callback<List<TecnicoDisponivel>> callback) {
        executor.execute(() -> {
            try {
                List<TecnicoDisponivel> result = listar.listar();
                main.post(() -> callback.success(result));
            } catch (Throwable error) {
                main.post(() -> callback.failure(error));
            }
        });
    }

    @Override protected void onCleared() { executor.shutdownNow(); }
}
