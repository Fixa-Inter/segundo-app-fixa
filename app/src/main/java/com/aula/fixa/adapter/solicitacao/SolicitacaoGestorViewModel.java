package com.aula.fixa.adapter.solicitacao;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.ViewModel;
import com.aula.fixa.application.solicitacao.ListarSolicitacoesGestor;
import com.aula.fixa.application.solicitacao.DetalharSolicitacaoGestor;
import com.aula.fixa.application.solicitacao.AtualizarStatusSolicitacaoGestor;
import com.aula.fixa.domain.model.solicitacao.SolicitacaoGestor;
import com.aula.fixa.domain.enums.StatusProblema;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Remote actions available to managers for pending requests. */
public final class SolicitacaoGestorViewModel extends ViewModel {
    public interface Callback<T> { void success(T value); void failure(Throwable error); }
    private final ListarSolicitacoesGestor listar;
    private final DetalharSolicitacaoGestor detalhar;
    private final AtualizarStatusSolicitacaoGestor atualizarStatus;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    public SolicitacaoGestorViewModel(ListarSolicitacoesGestor listar, DetalharSolicitacaoGestor detalhar,
            AtualizarStatusSolicitacaoGestor atualizarStatus) {
        this.listar=listar; this.detalhar=detalhar; this.atualizarStatus=atualizarStatus;
    }

    public void load(Callback<List<SolicitacaoGestor>> callback) {
        executor.execute(() -> run(callback, listar::executar));
    }

    public void loadApproved(Callback<List<SolicitacaoGestor>> callback) {
        executor.execute(() -> run(callback, listar::executarAprovadas));
    }

    public void details(long id, Callback<SolicitacaoGestor> callback) {
        executor.execute(() -> run(callback, () -> detalhar.executar(id)));
    }

    public void updateStatus(long id, StatusProblema status, String reason, Callback<SolicitacaoGestor> callback) {
        executor.execute(() -> run(callback, () -> atualizarStatus.executar(id,status,reason)));
    }

    public List<SolicitacaoGestor> filter(List<SolicitacaoGestor> requests, String query,
            String period, String locationFilter, String categoryFilter, String authorFilter) {
        String term = normalized(query);
        List<SolicitacaoGestor> result = new java.util.ArrayList<>();
        for (SolicitacaoGestor item : requests) {
            String location = item.local.trim().isEmpty() ? item.descricaoLocal
                    : item.descricaoLocal.trim().isEmpty() ? item.local : item.local + " - " + item.descricaoLocal;
            String searchable = normalized(item.titulo + " " + location + " " + item.solicitante + " " + item.categoria);
            if (!searchable.contains(term) || !matches(location, locationFilter)
                    || !matches(item.categoria, categoryFilter) || !matches(item.solicitante, authorFilter)
                    || !matchesPeriod(item.dataCriacao, period)) continue;
            result.add(item);
        }
        return result;
    }

    private static String normalized(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private static boolean matches(String value, String filter) {
        String selected = normalized(filter);
        return selected.isEmpty() || selected.startsWith("todos") || normalized(value).contains(selected);
    }

    private static boolean matchesPeriod(String value, String filter) {
        String selected = normalized(filter);
        if (selected.isEmpty() || selected.startsWith("todos")) return true;
        try {
            LocalDate date = LocalDate.parse(value.substring(0, 10));
            long days = selected.startsWith("últimos 30") ? 30 : selected.startsWith("últimos 3") ? 90 : 365;
            return !date.isBefore(LocalDate.now().minusDays(days));
        } catch (RuntimeException ignored) {
            return true;
        }
    }
    private <T> void run(Callback<T> callback, Work<T> work) {
        try { T value = work.run(); main.post(() -> callback.success(value)); }
        catch (Throwable e) { main.post(() -> callback.failure(e)); }
    }
    private interface Work<T> { T run() throws Exception; }
    @Override protected void onCleared() { executor.shutdownNow(); }
}
