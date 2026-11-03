package com.aula.fixa.adapter.chat;

import android.content.Context;
import androidx.lifecycle.ViewModel;
import com.aula.fixa.application.chat.ChatUseCase;
import com.aula.fixa.domain.model.chat.ChatHistory;
import com.aula.fixa.domain.model.chat.ChatTurn;
import com.aula.fixa.domain.model.chat.ChatPendingDraft;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.local.ChatDraftStorage;
import java.util.List;

/** Faz a ponte entre o controller da tela e os casos de uso do chat. */
public final class ChatViewModel extends ViewModel {
    private final ChatUseCase useCase;
    private final SessionStorage session;
    private final ChatDraftStorage drafts;
    public ChatViewModel(Context context, ChatUseCase useCase, SessionStorage session) {
        this.useCase=useCase; this.session=session; this.drafts=new ChatDraftStorage(context, session);
    }
    public boolean hasSession() { return session.hasSession(); }
    public ChatPendingDraft loadDraft() { return drafts.load(); }
    public void saveDraft(ChatPendingDraft draft) { drafts.save(draft); }
    public void clearDraft() { drafts.clear(); }
    public List<ChatHistory> listar() throws Exception { return useCase.listar(); }
    public ChatHistory recuperar(String id) throws Exception { return useCase.recuperar(id); }
    public ChatTurn enviar(String text, String id, List<String> images) throws Exception { return useCase.enviar(text,id,images); }
    public void encerrar(String id) throws Exception { useCase.encerrar(id); }
    public void excluir(String id) throws Exception { useCase.excluir(id); }
}
