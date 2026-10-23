package com.aula.fixa.adapter.cadastro;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.ViewModel;
import com.aula.fixa.application.cadastro.ListarCategorias;
import com.aula.fixa.application.cadastro.ListarLocais;
import com.aula.fixa.domain.model.OpcaoCadastro;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CadastroViewModel extends ViewModel {
    public interface Callback<T> { void aoConcluir(T valor); void aoFalhar(Throwable erro); }
    private final ListarLocais locais;
    private final ListarCategorias categorias;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    public CadastroViewModel(ListarLocais locais, ListarCategorias categorias) {
        this.locais = locais; this.categorias = categorias;
    }
    public void listarLocais(Callback<List<OpcaoCadastro>> callback) { executar(callback, locais::executar); }
    public void listarCategorias(Callback<List<OpcaoCadastro>> callback) { executar(callback, categorias::executar); }
    private <T> void executar(Callback<T> callback, Operacao<T> operacao) {
        executor.execute(() -> { try { T valor = operacao.executar(); main.post(() -> callback.aoConcluir(valor)); }
            catch (Throwable erro) { main.post(() -> callback.aoFalhar(erro)); } });
    }
    @Override protected void onCleared() { executor.shutdownNow(); }
    private interface Operacao<T> { T executar() throws Exception; }
}
