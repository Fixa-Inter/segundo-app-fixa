package com.aula.fixa.adapter.tecnico;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.application.tecnico.ListarTecnicosRecomendados;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.repository.TecnicoRepositoryImpl;

public final class TecnicoViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;
    public TecnicoViewModelFactory(Context context) { this.context = context.getApplicationContext(); }

    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> type) {
        if (!type.isAssignableFrom(TecnicoViewModel.class)) throw new IllegalArgumentException("ViewModel desconhecido");
        TecnicoRepositoryImpl repository = new TecnicoRepositoryImpl(new SessionStorage(context));
        return type.cast(new TecnicoViewModel(new ListarTecnicosRecomendados(repository)));
    }
}
