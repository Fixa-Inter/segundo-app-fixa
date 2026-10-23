package com.aula.fixa.adapter.operations;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.ViewModel;
import com.aula.fixa.application.operations.LoadManagerHome;
import com.aula.fixa.domain.model.ManagerHomeData;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ManagerHomeViewModel extends ViewModel {
    public interface Callback {
        void aoCarregar(ManagerHomeData data);
        void aoFalhar(Throwable erro);
    }
    private final LoadManagerHome carregarHome;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    public ManagerHomeViewModel(LoadManagerHome carregarHome) { this.carregarHome = carregarHome; }

    public void carregar(Callback callback) {
        executor.execute(() -> {
            try {
                ManagerHomeData data = carregarHome.executar();
                main.post(() -> callback.aoCarregar(data));
            } catch (Throwable erro) { main.post(() -> callback.aoFalhar(erro)); }
        });
    }

    @Override protected void onCleared() { executor.shutdownNow(); }
}
