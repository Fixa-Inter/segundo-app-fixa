package com.aula.fixa.adapter.equipamento;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.application.equipamento.GerenciarCategoriasEquipamento;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.repository.CategoriaEquipamentoRepositoryImpl;

public final class CategoriaEquipamentoViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;
    public CategoriaEquipamentoViewModelFactory(Context context) { this.context = context.getApplicationContext(); }
    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (!modelClass.isAssignableFrom(CategoriaEquipamentoViewModel.class)) throw new IllegalArgumentException("ViewModel desconhecido");
        return modelClass.cast(new CategoriaEquipamentoViewModel(new GerenciarCategoriasEquipamento(
                new CategoriaEquipamentoRepositoryImpl(new SessionStorage(context)))));
    }
}
