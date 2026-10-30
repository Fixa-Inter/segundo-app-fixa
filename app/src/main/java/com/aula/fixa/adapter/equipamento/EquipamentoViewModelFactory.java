package com.aula.fixa.adapter.equipamento;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.application.equipamento.GerenciarEquipamentos;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.repository.EquipamentoRepositoryImpl;

public final class EquipamentoViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;
    public EquipamentoViewModelFactory(Context context) { this.context = context.getApplicationContext(); }
    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> type) {
        if (!type.isAssignableFrom(EquipamentoViewModel.class)) throw new IllegalArgumentException("ViewModel desconhecido");
        return type.cast(new EquipamentoViewModel(new GerenciarEquipamentos(
                new EquipamentoRepositoryImpl(new SessionStorage(context)))));
    }
}
