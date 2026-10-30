package com.aula.fixa.adapter.equipamento;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.ViewModel;
import com.aula.fixa.application.equipamento.GerenciarModelosEquipamento;
import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.domain.model.equipamento.ModeloEquipamentoItem;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ModeloEquipamentoViewModel extends ViewModel {
    public interface Callback<T> { void success(T value); void failure(Throwable error); }
    private final GerenciarModelosEquipamento useCase;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    public ModeloEquipamentoViewModel(GerenciarModelosEquipamento useCase) { this.useCase = useCase; }
    public void listar(Callback<List<ModeloEquipamentoItem>> cb) { run(useCase::listar, cb); }
    public void categorias(Callback<List<OpcaoCadastro>> cb) { run(useCase::categorias, cb); }
    public void marcas(Callback<List<OpcaoCadastro>> cb) { run(useCase::marcas, cb); }
    public void criar(String nome, String descricao, long cat, long marca, Callback<ModeloEquipamentoItem> cb) { run(() -> useCase.criar(nome, descricao, cat, marca), cb); }
    public void atualizar(long id, String nome, String descricao, long cat, long marca, Callback<ModeloEquipamentoItem> cb) { run(() -> useCase.atualizar(id, nome, descricao, cat, marca), cb); }
    public void excluir(long id, Callback<Void> cb) { run(() -> { useCase.excluir(id); return null; }, cb); }
    private <T> void run(Work<T> work, Callback<T> cb) { io.execute(() -> { try { T value = work.get(); main.post(() -> cb.success(value)); } catch (Throwable e) { main.post(() -> cb.failure(e)); } }); }
    @Override protected void onCleared() { io.shutdownNow(); }
    private interface Work<T> { T get() throws Exception; }
}
