package com.aula.fixa.adapter.equipamento;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.application.equipamento.GerenciarModelosEquipamento;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.repository.ModeloEquipamentoRepositoryImpl;

public final class ModeloEquipamentoViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;
    public ModeloEquipamentoViewModelFactory(Context context) { this.context = context.getApplicationContext(); }
    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> type) {
        if (!type.isAssignableFrom(ModeloEquipamentoViewModel.class)) throw new IllegalArgumentException("ViewModel desconhecido");
        return type.cast(new ModeloEquipamentoViewModel(new GerenciarModelosEquipamento(
                new ModeloEquipamentoRepositoryImpl(new SessionStorage(context)))));
    }
}
