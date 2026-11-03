package com.aula.fixa.infrastructure.remote.dto.chat;

import com.google.gson.annotations.SerializedName;

public final class ChatConversationRequest {
    @SerializedName("session_id") public final String sessionId;
    public ChatConversationRequest(String sessionId) { this.sessionId = sessionId; }
}
