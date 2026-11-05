package com.aula.fixa.adapter.chat;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.ViewModel;

import com.aula.fixa.application.chat.ChatUseCase;
import com.aula.fixa.domain.model.chat.ChatHistory;
import com.aula.fixa.domain.model.chat.ChatPendingDraft;
import com.aula.fixa.domain.model.chat.ChatTurn;
import com.aula.fixa.domain.repository.auth.SessaoRepository;
import com.aula.fixa.domain.repository.chat.ChatDraftRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Coordena os casos de uso e entrega seus resultados à interface na thread principal. */
public final class ChatViewModel extends ViewModel {
    public interface Listener<T> {
        void onResultado(T resultado);
        void onErro(Throwable erro);
    }

    private final ChatUseCase useCase;
    private final SessaoRepository sessao;
    private final ChatDraftRepository drafts;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    public ChatViewModel(ChatUseCase useCase, SessaoRepository sessao, ChatDraftRepository drafts) {
        this.useCase = useCase;
        this.sessao = sessao;
        this.drafts = drafts;
    }

    public boolean hasSession() { return sessao.temSessao(); }
    public void loadDraft(Listener<ChatPendingDraft> listener) {
        executar(listener, drafts::load);
    }
    public void saveDraft(ChatPendingDraft draft, Listener<Void> listener) {
        executar(listener, () -> { drafts.save(draft); return null; });
    }
    public void clearDraft(Listener<Void> listener) {
        executar(listener, () -> { drafts.clear(); return null; });
    }

    public void listar(Listener<List<ChatHistory>> listener) {
        executar(listener, useCase::listar);
    }
    public void recuperar(String id, Listener<ChatHistory> listener) {
        executar(listener, () -> useCase.recuperar(id));
    }
    public void enviar(String text, String id, List<String> images, Listener<ChatTurn> listener) {
        executar(listener, () -> useCase.enviar(text, id, images));
    }
    public void encerrar(String id, Listener<Void> listener) {
        executar(listener, () -> { useCase.encerrar(id); return null; });
    }
    public void excluir(String id, Listener<Void> listener) {
        executar(listener, () -> { useCase.excluir(id); return null; });
    }

    private <T> void executar(Listener<T> listener, Operacao<T> operacao) {
        executor.execute(() -> {
            try {
                T resultado = operacao.executar();
                main.post(() -> listener.onResultado(resultado));
            } catch (Exception erro) {
                main.post(() -> listener.onErro(erro));
            }
        });
    }

    @Override protected void onCleared() { executor.shutdownNow(); }

    private interface Operacao<T> { T executar() throws Exception; }
}
