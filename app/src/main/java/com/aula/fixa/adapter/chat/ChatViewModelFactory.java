package com.aula.fixa.adapter.chat;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.application.chat.ChatUseCase;
import com.aula.fixa.domain.repository.ChatRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.repository.ChatRepositoryImpl;

public final class ChatViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;
    public ChatViewModelFactory(Context context) { this.context=context.getApplicationContext(); }
    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (!modelClass.isAssignableFrom(ChatViewModel.class)) throw new IllegalArgumentException("ViewModel desconhecido");
        SessionStorage session = new SessionStorage(context);
        ChatRepository repository = new ChatRepositoryImpl(session);
        return modelClass.cast(new ChatViewModel(context, new ChatUseCase(repository), session));
    }
}
