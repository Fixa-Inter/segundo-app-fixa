package com.aula.fixa.adapter.auth;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.application.auth.Autenticacao;
import com.aula.fixa.infrastructure.auth.AuthRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;

public final class AutenticacaoViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;
    public AutenticacaoViewModelFactory(Context context) { this.context = context.getApplicationContext(); }
    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (!modelClass.isAssignableFrom(AutenticacaoViewModel.class)) throw new IllegalArgumentException("ViewModel desconhecido");
        AuthRepository repository = new AuthRepository(new SessionStorage(context));
        return modelClass.cast(new AutenticacaoViewModel(new Autenticacao(repository)));
    }
}
