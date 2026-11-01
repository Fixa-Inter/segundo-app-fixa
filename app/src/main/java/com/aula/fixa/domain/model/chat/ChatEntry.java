package com.aula.fixa.domain.model.chat;

import java.util.ArrayList;
import java.util.List;

public final class ChatEntry {
    public String role, text, emotion, confirmation;
    public final List<String> images = new ArrayList<>();
    /** Local-only timestamp for messages sent/received live; history has no per-message time. */
    public long localTimestamp;
    public String localStatus;
}
