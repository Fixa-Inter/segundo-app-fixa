package com.aula.fixa.adapter.solicitacao;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import com.aula.fixa.application.solicitacao.CriarSolicitacao;
import com.aula.fixa.application.solicitacao.ListarSolicitacoes;
import com.aula.fixa.application.solicitacao.DetalharSolicitacao;
import com.aula.fixa.application.solicitacao.AtualizarSolicitacao;
import com.aula.fixa.application.solicitacao.ExcluirSolicitacao;
import com.aula.fixa.domain.model.solicitacao.AtualizacaoSolicitacao;
import com.aula.fixa.domain.model.solicitacao.NovaSolicitacao;
import com.aula.fixa.domain.model.solicitacao.Problema;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Adapta operações de solicitação ao ciclo de vida e aos eventos da tela. */
public final class SolicitacaoViewModel extends ViewModel {
    public interface Callback<T> { void aoConcluir(T resultado); void aoFalhar(Throwable erro); }
    public interface Listener {
        void aoCarregar(boolean carregando);
        void aoCriar(Problema problema);
        void aoListar(List<Problema> problemas);
        void aoFalhar(Throwable erro);
    }

    private final CriarSolicitacao criar;
    private final ListarSolicitacoes listar;
    private final DetalharSolicitacao detalhar;
    private final AtualizarSolicitacao atualizar;
    private final ExcluirSolicitacao excluir;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    public SolicitacaoViewModel(CriarSolicitacao criar, ListarSolicitacoes listar,
                                DetalharSolicitacao detalhar, AtualizarSolicitacao atualizar,
                                ExcluirSolicitacao excluir) {
        this.criar = criar;
        this.listar = listar;
        this.detalhar = detalhar;
        this.atualizar = atualizar;
        this.excluir = excluir;
    }

    public void criar(@NonNull NovaSolicitacao solicitacao, @NonNull Listener listener) {
        listener.aoCarregar(true);
        executor.execute(() -> {
            try {
                Problema problema = criar.executar(solicitacao);
                main.post(() -> { listener.aoCarregar(false); listener.aoCriar(problema); });
            } catch (Throwable error) {
                main.post(() -> { listener.aoCarregar(false); listener.aoFalhar(error); });
            }
        });
    }

    public void listar(@NonNull Listener listener) {
        listener.aoCarregar(true);
        executor.execute(() -> {
            try {
                List<Problema> problemas = listar.executar();
                main.post(() -> { listener.aoCarregar(false); listener.aoListar(problemas); });
            } catch (Throwable error) {
                main.post(() -> { listener.aoCarregar(false); listener.aoFalhar(error); });
            }
        });
    }

    public void detalhar(long id, @NonNull Callback<Problema> callback) {
        executor.execute(() -> executar(callback, () -> detalhar.executar(id)));
    }

    public void atualizar(@NonNull AtualizacaoSolicitacao solicitacao, @NonNull Callback<Problema> callback) {
        executor.execute(() -> executar(callback, () -> atualizar.executar(solicitacao)));
    }

    public void excluir(long id, @NonNull Callback<Void> callback) {
        executor.execute(() -> executar(callback, () -> { excluir.executar(id); return null; }));
    }

    private <T> void executar(Callback<T> callback, Operacao<T> operacao) {
        try { T result = operacao.executar(); main.post(() -> callback.aoConcluir(result)); }
        catch (Throwable error) { main.post(() -> callback.aoFalhar(error)); }
    }

    @Override protected void onCleared() {
        executor.shutdownNow();
    }
    private interface Operacao<T> { T executar() throws Exception; }
}
