package com.aula.fixa.adapter.planejamento;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.ViewModel;
import com.aula.fixa.application.planejamento.RecomendarCategoriaProblema;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Executa a recomendação JEV fora da thread de interface. */
public final class RecomendacaoCategoriaViewModel extends ViewModel {
    public interface Callback { void success(String classificacao); void failure(Throwable error); }
    private final RecomendarCategoriaProblema recomendar;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    public RecomendacaoCategoriaViewModel(RecomendarCategoriaProblema recomendar) {
        this.recomendar = recomendar;
    }

    public void recomendar(String titulo, String descricao, String categoriaEquipamento, Callback callback) {
        executor.execute(() -> {
            try {
                String result = recomendar.executar(titulo, descricao, categoriaEquipamento);
                main.post(() -> callback.success(result));
            } catch (Throwable error) {
                main.post(() -> callback.failure(error));
            }
        });
    }

    @Override protected void onCleared() { executor.shutdownNow(); }
}
