package com.aula.fixa.infrastructure.remote.dto.chat;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public final class ChatMessage {
    public String role;
    public String mensagem;
    public String emocao;
    public String confirmacao;
    public List<String> imagens;
}
