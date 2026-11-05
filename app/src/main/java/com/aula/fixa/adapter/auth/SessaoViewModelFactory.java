package com.aula.fixa.adapter.auth;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.application.auth.GerenciarSessao;
import com.aula.fixa.infrastructure.auth.SessaoRepositoryImpl;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;

public final class SessaoViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;
    public SessaoViewModelFactory(Context context) { this.context = context.getApplicationContext(); }
    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (!modelClass.isAssignableFrom(SessaoViewModel.class)) throw new IllegalArgumentException("ViewModel desconhecido");
        SessaoRepositoryImpl repository = new SessaoRepositoryImpl(new SessionStorage(context));
        return modelClass.cast(new SessaoViewModel(new GerenciarSessao(repository)));
    }
}
