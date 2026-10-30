package com.aula.fixa.adapter.equipamento;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.ViewModel;
import com.aula.fixa.application.equipamento.GerenciarEquipamentos;
import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.domain.model.equipamento.EquipamentoItem;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class EquipamentoViewModel extends ViewModel {
    public interface Callback<T> { void success(T value); void failure(Throwable error); }
    private final GerenciarEquipamentos useCase;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    public EquipamentoViewModel(GerenciarEquipamentos useCase) { this.useCase = useCase; }
    public void listar(long modeloId, Callback<List<EquipamentoItem>> cb) { run(() -> useCase.listar(modeloId), cb); }
    public void locais(Callback<List<OpcaoCadastro>> cb) { run(useCase::locais, cb); }
    public void criar(long modeloId, long localId, String codigo, Callback<EquipamentoItem> cb) { run(() -> useCase.criar(modeloId, localId, codigo), cb); }
    public void atualizar(long id, long localId, String codigo, Callback<EquipamentoItem> cb) { run(() -> useCase.atualizar(id, localId, codigo), cb); }
    public void excluir(long id, Callback<Void> cb) { run(() -> { useCase.excluir(id); return null; }, cb); }
    private <T> void run(Work<T> work, Callback<T> cb) {
        io.execute(() -> { try { T value = work.get(); main.post(() -> cb.success(value)); }
            catch (Throwable error) { main.post(() -> cb.failure(error)); } });
    }
    @Override protected void onCleared() { io.shutdownNow(); }
    private interface Work<T> { T get() throws Exception; }
}
