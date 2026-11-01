package com.aula.fixa.domain.model.chat;

import java.util.ArrayList;
import java.util.List;

public final class ChatHistory {
    public String sessionId, startedAt, updatedAt, title, summary;
    public final List<ChatEntry> messages = new ArrayList<>();
}
