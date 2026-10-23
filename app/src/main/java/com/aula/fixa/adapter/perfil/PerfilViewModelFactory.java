package com.aula.fixa.adapter.perfil;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.application.perfil.AtualizarPerfil;
import com.aula.fixa.application.perfil.ObterPerfil;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.repository.PerfilRepositoryImpl;

public final class PerfilViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;
    public PerfilViewModelFactory(Context context) { this.context = context.getApplicationContext(); }

    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (!modelClass.isAssignableFrom(PerfilViewModel.class)) {
            throw new IllegalArgumentException("ViewModel desconhecido: " + modelClass.getName());
        }
        PerfilRepositoryImpl repository = new PerfilRepositoryImpl(new SessionStorage(context));
        return modelClass.cast(new PerfilViewModel(new ObterPerfil(repository), new AtualizarPerfil(repository)));
    }
}
