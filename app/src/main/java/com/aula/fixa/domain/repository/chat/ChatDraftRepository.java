package com.aula.fixa.domain.repository.chat;

import com.aula.fixa.domain.model.chat.ChatPendingDraft;

/** Persistência local do rascunho de mensagem cuja confirmação ainda não chegou. */
public interface ChatDraftRepository {
    ChatPendingDraft load();
    void save(ChatPendingDraft draft);
    void clear();
}
