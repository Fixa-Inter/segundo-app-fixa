package com.aula.fixa.adapter.planejamento;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.ViewModel;
import com.aula.fixa.application.planejamento.FinalizarPlanejamentoOS;
import com.aula.fixa.domain.model.planejamento.TarefaPlanejada;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Adaptador assíncrono para finalizar o planejamento da OS. */
public final class PlanejamentoOSViewModel extends ViewModel {
    public interface Callback { void success(long ordemServicoId); void failure(Throwable error); }

    private final FinalizarPlanejamentoOS finalizar;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    public PlanejamentoOSViewModel(FinalizarPlanejamentoOS finalizar) { this.finalizar = finalizar; }

    public void finalizar(long solicitacaoId, long tecnicoId, int categoriaId, int prioridadeId,
                          String prazoIso, long ordemServicoExistenteId,
                          List<TarefaPlanejada> tarefas, Callback callback) {
        executor.execute(() -> {
            try {
                long id = finalizar.executar(solicitacaoId, tecnicoId, categoriaId,
                        prioridadeId, prazoIso, ordemServicoExistenteId, tarefas);
                main.post(() -> callback.success(id));
            } catch (Throwable error) {
                main.post(() -> callback.failure(error));
            }
        });
    }

    @Override protected void onCleared() { executor.shutdownNow(); }
}
