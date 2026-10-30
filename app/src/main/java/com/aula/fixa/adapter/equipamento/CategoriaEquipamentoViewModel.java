package com.aula.fixa.adapter.equipamento;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.ViewModel;
import com.aula.fixa.application.equipamento.GerenciarCategoriasEquipamento;
import com.aula.fixa.domain.model.OpcaoCadastro;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CategoriaEquipamentoViewModel extends ViewModel {
    public interface Callback<T> { void success(T value); void failure(Throwable error); }
    private final GerenciarCategoriasEquipamento useCase;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    public CategoriaEquipamentoViewModel(GerenciarCategoriasEquipamento useCase) { this.useCase = useCase; }
    public void listar(Callback<List<OpcaoCadastro>> callback) { run(useCase::listar, callback); }
    public void criar(String nome, String descricao, Callback<OpcaoCadastro> callback) { run(() -> useCase.criar(nome, descricao), callback); }
    public void atualizar(long id, String nome, Callback<OpcaoCadastro> callback) { run(() -> useCase.atualizar(id, nome), callback); }
    public void excluir(long id, Callback<Void> callback) { run(() -> { useCase.excluir(id); return null; }, callback); }
    private <T> void run(Work<T> work, Callback<T> callback) { io.execute(() -> { try { T value = work.run(); main.post(() -> callback.success(value)); } catch (Throwable error) { main.post(() -> callback.failure(error)); } }); }
    @Override protected void onCleared() { io.shutdownNow(); }
    private interface Work<T> { T run() throws Exception; }
}
