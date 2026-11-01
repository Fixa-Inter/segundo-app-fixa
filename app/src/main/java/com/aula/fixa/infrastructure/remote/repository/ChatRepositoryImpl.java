package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.domain.model.chat.ChatEntry;
import com.aula.fixa.domain.model.chat.ChatHistory;
import com.aula.fixa.domain.model.chat.ChatTurn;
import com.aula.fixa.domain.model.chat.ChatException;
import com.aula.fixa.domain.repository.ChatRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.api.ChatApi;
import com.aula.fixa.infrastructure.remote.dto.chat.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import retrofit2.Response;

/** Integração mobile com o contrato público da IA; DTOs projetam somente os campos permitidos. */
public final class ChatRepositoryImpl implements ChatRepository {
    private final ChatApi api;
    public ChatRepositoryImpl(SessionStorage session) { api = ApiClient.chat(session); }

    @Override public List<ChatHistory> listar() throws Exception {
        ChatListResponse result = require(api.listar().execute());
        if (result.conversas == null) throw new IOException("A lista de conversas retornou um formato inválido.");
        List<ChatHistory> histories = new ArrayList<>();
        for (ChatConversation c : result.conversas) histories.add(map(c));
        return histories;
    }

    @Override public ChatHistory recuperar(String sessionId) throws Exception {
        ChatConversationResponse result = require(api.conversa(new ChatConversationRequest(validateId(sessionId))).execute());
        if (result.conversa == null) throw new IOException("A conversa não foi retornada pela IA.");
        return map(result.conversa);
    }

    @Override public ChatTurn enviar(String message, String threadId, List<String> imageUrls) throws Exception {
        String text = message == null ? "" : message.trim();
        if (text.isEmpty() || text.length() > 4000) throw new IllegalArgumentException("Digite uma mensagem de até 4.000 caracteres.");
        List<String> urls = imageUrls == null ? Collections.emptyList() : imageUrls;
        if (urls.size() > 3) throw new IllegalArgumentException("Envie no máximo 3 imagens por mensagem.");
        for (String url : urls) validateImageUrl(url);
        ChatMessageRequest request = new ChatMessageRequest();
        request.mensagem = text;
        request.threadId = threadId == null || threadId.trim().isEmpty() ? null : validateId(threadId);
        request.imagens = new ArrayList<>(urls);
        ChatMessageResponse response = require(api.enviar(request).execute());
        if (response.threadId == null || response.threadId.trim().isEmpty() || response.resposta == null || response.resposta.trim().isEmpty()) {
            throw new IOException("A resposta da Manu não veio completa. Atualize a conversa antes de tentar novamente.");
        }
        if (request.threadId != null && !request.threadId.equals(response.threadId))
            throw new IOException("A resposta recebida pertence a outra conversa. Atualize o histórico antes de continuar.");
        ChatEntry entry = new ChatEntry();
        entry.role = "assistant"; entry.text = response.resposta;
        entry.emotion = response.emocao; entry.confirmation = response.confirmacao;
        entry.localTimestamp = System.currentTimeMillis();
        ChatTurn turn = new ChatTurn(); turn.threadId = response.threadId; turn.assistantMessage = entry;
        return turn;
    }

    @Override public void encerrar(String threadId) throws Exception {
        require(api.encerrar(encodePathSegment(validateId(threadId))).execute());
    }

    @Override public void excluir(String sessionId) throws Exception {
        require(api.excluir(encodePathSegment(validateId(sessionId))).execute());
    }

    private static ChatHistory map(ChatConversation c) {
        ChatHistory history = new ChatHistory();
        history.sessionId = c.sessionId; history.startedAt = c.iniciadaEm; history.updatedAt = c.atualizadaEm;
        history.title = c.titulo; history.summary = c.resumo;
        if (c.mensagens == null) throw new IllegalStateException("O histórico retornou um formato inválido.");
        for (ChatMessage source : c.mensagens) {
            ChatEntry m = new ChatEntry(); m.role = source.role; m.text = source.mensagem;
            m.emotion = source.emocao; m.confirmation = source.confirmacao;
            if (source.imagens != null) m.images.addAll(source.imagens);
            history.messages.add(m);
        }
        return history;
    }

    private static String validateId(String id) {
        String value = id == null ? "" : id.trim();
        if (value.isEmpty() || value.length() > 100) throw new IllegalArgumentException("Identificador de conversa inválido.");
        return value;
    }
    private static String encodePathSegment(String value) {
        try { return URLEncoder.encode(value, "UTF-8").replace("+", "%20"); }
        catch (java.io.UnsupportedEncodingException impossible) { throw new IllegalStateException(impossible); }
    }

    private static void validateImageUrl(String value) {
        if (value == null) throw new IllegalArgumentException("URL de imagem inválida.");
        String url = value.trim();
        if (url.isEmpty() || url.length() > 255 || url.contains(" ") || url.indexOf('\\') >= 0 || url.indexOf('#') >= 0
                || !(url.startsWith("https://") || url.startsWith("http://")) || url.matches(".*[\\p{Cntrl}\\p{javaWhitespace}].*"))
            throw new IllegalArgumentException("Uma imagem possui URL inválida ou maior que 255 caracteres.");
        try {
            java.net.URI uri = java.net.URI.create(url);
            String host = uri.getHost();
            if (host == null || uri.getUserInfo() != null || !host.contains(".") || host.endsWith(".local") || host.endsWith(".localhost") || host.endsWith(".internal") || host.endsWith(".test") || host.endsWith(".invalid"))
                throw new IllegalArgumentException("A URL da imagem precisa ser pública.");
            if (host.matches("\\d{1,3}(\\.\\d{1,3}){3}")) {
                String[] octets = host.split("\\."); int a=Integer.parseInt(octets[0]), b=Integer.parseInt(octets[1]);
                if (a==0 || a==10 || a==127 || a>=224 || (a==169&&b==254) || (a==172&&b>=16&&b<=31) || (a==192&&b==168))
                    throw new IllegalArgumentException("A URL da imagem precisa ser pública.");
            }
        } catch (IllegalArgumentException e) { throw new IllegalArgumentException("URL de imagem inválida."); }
    }

    private static <T> T require(Response<T> response) throws Exception {
        if (!response.isSuccessful()) {
            if (response.errorBody() != null) response.errorBody().close();
            throw new ChatException(response.code(), errorMessage(response.code()));
        }
        if (response.body() == null) throw new IOException("A IA retornou uma resposta vazia.");
        return response.body();
    }

    private static String errorMessage(int status) {
        if (status == 401) return "Sua sessão expirou. Entre novamente.";
        if (status == 403) return "Seu perfil não tem acesso a esta conversa.";
        if (status == 404) return "Esta conversa não está mais disponível.";
        if (status == 422) return "Não foi possível validar os dados da conversa.";
        if (status >= 500) return "A Manu está indisponível no momento. Tente novamente mais tarde.";
        return "Não foi possível concluir esta ação.";
    }
}
