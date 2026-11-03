package com.aula.fixa.infrastructure.remote.dto.chat;

import com.google.gson.annotations.SerializedName;

public final class ChatDeleteResponse {
    @SerializedName("session_id") public String sessionId;
    public String status;
    public String mensagem;
}
