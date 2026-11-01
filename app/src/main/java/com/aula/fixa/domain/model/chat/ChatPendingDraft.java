package com.aula.fixa.domain.model.chat;

import java.util.ArrayList;
import java.util.List;

/** Estado local de uma mensagem cuja resposta ainda não foi confirmada pela origem. */
public final class ChatPendingDraft {
    public String threadId, message;
    public List<String> images = new ArrayList<>();
    public long timestamp;
}
