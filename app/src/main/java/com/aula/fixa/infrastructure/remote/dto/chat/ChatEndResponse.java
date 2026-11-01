package com.aula.fixa.infrastructure.remote.dto.chat;

import com.google.gson.annotations.SerializedName;

public final class ChatEndResponse {
    @SerializedName("thread_id") public String threadId;
    public String status;
    public String mensagem;
    public String titulo;
    public String resumo;
}
