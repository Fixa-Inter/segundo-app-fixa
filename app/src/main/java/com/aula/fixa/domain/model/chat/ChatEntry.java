package com.aula.fixa.domain.model.chat;

import java.util.ArrayList;
import java.util.List;

public final class ChatEntry {
    public String role, text, emotion, confirmation;
    public final List<String> images = new ArrayList<>();
    /** Epoch millis do timestamp retornado pela API ou, para mensagens novas, do horário local. */
    public long localTimestamp;
    public String localStatus;
}
