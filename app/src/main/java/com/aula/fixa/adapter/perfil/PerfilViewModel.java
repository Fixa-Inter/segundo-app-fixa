package com.aula.fixa.adapter.perfil;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.ViewModel;
import com.aula.fixa.application.perfil.AtualizarPerfil;
import com.aula.fixa.application.perfil.ObterPerfil;
import com.aula.fixa.domain.model.AtualizacaoPerfil;
import com.aula.fixa.domain.model.Perfil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PerfilViewModel extends ViewModel {
    public interface Callback<T> {
        void aoConcluir(T resultado);
        void aoFalhar(Throwable erro);
    }

    private final ObterPerfil obterPerfil;
    private final AtualizarPerfil atualizarPerfil;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    public PerfilViewModel(ObterPerfil obterPerfil, AtualizarPerfil atualizarPerfil) {
        this.obterPerfil = obterPerfil;
        this.atualizarPerfil = atualizarPerfil;
    }

    public void obter(Callback<Perfil> callback) {
        executor.execute(() -> executar(callback, obterPerfil::executar));
    }

    public void atualizar(AtualizacaoPerfil perfil, Callback<Perfil> callback) {
        executor.execute(() -> executar(callback, () -> atualizarPerfil.executar(perfil)));
    }

    private <T> void executar(Callback<T> callback, Operacao<T> operacao) {
        try {
            T resultado = operacao.executar();
            main.post(() -> callback.aoConcluir(resultado));
        } catch (Throwable erro) {
            main.post(() -> callback.aoFalhar(erro));
        }
    }

    @Override protected void onCleared() { executor.shutdownNow(); }

    private interface Operacao<T> { T executar() throws Exception; }
}
