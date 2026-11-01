package com.aula.fixa.infrastructure.remote.dto.chat;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public final class ChatConversation {
    @SerializedName("session_id") public String sessionId;
    @SerializedName("iniciada_em") public String iniciadaEm;
    @SerializedName("atualizada_em") public String atualizadaEm;
    public String titulo;
    public String resumo;
    public List<ChatMessage> mensagens;
}
