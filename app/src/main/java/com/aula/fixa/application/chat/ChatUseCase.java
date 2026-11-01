package com.aula.fixa.application.chat;

import com.aula.fixa.domain.model.chat.ChatHistory;
import com.aula.fixa.domain.model.chat.ChatTurn;
import com.aula.fixa.domain.repository.ChatRepository;
import java.util.List;

/** Orquestra as operações conversacionais e mantém as regras de entrada do mobile. */
public final class ChatUseCase {
    private final ChatRepository repository;
    public ChatUseCase(ChatRepository repository) { this.repository = repository; }

    public List<ChatHistory> listar() throws Exception { return repository.listar(); }
    public ChatHistory recuperar(String sessionId) throws Exception { return repository.recuperar(sessionId); }
    public ChatTurn enviar(String mensagem, String threadId, List<String> imagens) throws Exception {
        String text = mensagem == null ? "" : mensagem.trim();
        if (text.isEmpty() || text.length() > 4000) throw new IllegalArgumentException("Digite uma mensagem de até 4.000 caracteres.");
        if (imagens != null && imagens.size() > 3) throw new IllegalArgumentException("Envie no máximo 3 imagens por mensagem.");
        return repository.enviar(text, threadId, imagens);
    }
    public void encerrar(String threadId) throws Exception { repository.encerrar(threadId); }
    public void excluir(String sessionId) throws Exception { repository.excluir(sessionId); }
}
