package com.aula.fixa.infrastructure.remote.dto.chat;

import com.google.gson.annotations.SerializedName;

public final class ChatMessageResponse {
    @SerializedName("thread_id") public String threadId;
    public String intencao;
    public String resposta;
    public String emocao;
    public String confirmacao;
}
