package com.aula.fixa.adapter.planejamento;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.application.planejamento.FinalizarPlanejamentoOS;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.repository.PlanejamentoOSRepositoryImpl;

public final class PlanejamentoOSViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;
    public PlanejamentoOSViewModelFactory(Context context) { this.context = context.getApplicationContext(); }

    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> type) {
        if (!type.isAssignableFrom(PlanejamentoOSViewModel.class)) {
            throw new IllegalArgumentException("ViewModel desconhecido");
        }
        PlanejamentoOSRepositoryImpl repository = new PlanejamentoOSRepositoryImpl(new SessionStorage(context));
        return type.cast(new PlanejamentoOSViewModel(new FinalizarPlanejamentoOS(repository)));
    }
}
