package com.aula.fixa.infrastructure.remote.dto.chat;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public final class ChatMessageRequest {
    public String mensagem;
    public String plataforma = "mobile";
    @SerializedName("thread_id") public String threadId;
    public List<String> imagens;
}
