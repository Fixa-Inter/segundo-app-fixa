package com.aula.fixa.infrastructure.remote.api;

import com.aula.fixa.infrastructure.remote.dto.chat.ChatConversationRequest;
import com.aula.fixa.infrastructure.remote.dto.chat.ChatConversationResponse;
import com.aula.fixa.infrastructure.remote.dto.chat.ChatDeleteResponse;
import com.aula.fixa.infrastructure.remote.dto.chat.ChatEndResponse;
import com.aula.fixa.infrastructure.remote.dto.chat.ChatListResponse;
import com.aula.fixa.infrastructure.remote.dto.chat.ChatMessageRequest;
import com.aula.fixa.infrastructure.remote.dto.chat.ChatMessageResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Headers;

public interface ChatApi {
    @Headers("Accept: application/json")
    @POST("api/v1/chat") Call<ChatMessageResponse> enviar(@Body ChatMessageRequest request);

    @GET("api/v1/sessoes") Call<ChatListResponse> listar();

    @POST("api/v1/sessoes/conversa") Call<ChatConversationResponse> conversa(@Body ChatConversationRequest request);

    @DELETE("api/v1/chat/sessions/{thread_id}") Call<ChatEndResponse> encerrar(@Path(value = "thread_id", encoded = true) String threadId);

    @DELETE("api/v1/sessoes/conversa/{session_id}") Call<ChatDeleteResponse> excluir(@Path(value = "session_id", encoded = true) String sessionId);
}
