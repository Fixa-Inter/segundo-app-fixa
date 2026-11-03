package com.aula.fixa.domain.model.chat;

import java.io.IOException;

/** Falha segura do fluxo de chat, sem incluir body bruto da origem. */
public final class ChatException extends IOException {
    public final int status;
    public ChatException(int status, String message) { super(message); this.status=status; }
}
