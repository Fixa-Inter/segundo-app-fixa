package com.aula.fixa.domain.repository;

import com.aula.fixa.domain.model.chat.ChatHistory;
import com.aula.fixa.domain.model.chat.ChatTurn;
import java.util.List;

public interface ChatRepository {
    List<ChatHistory> listar() throws Exception;
    ChatHistory recuperar(String sessionId) throws Exception;
    ChatTurn enviar(String message, String threadId, List<String> imageUrls) throws Exception;
    void encerrar(String threadId) throws Exception;
    void excluir(String sessionId) throws Exception;
}
