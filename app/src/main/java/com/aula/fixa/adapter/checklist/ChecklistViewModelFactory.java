package com.aula.fixa.adapter.checklist;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.application.checklist.GerenciarChecklists;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.repository.ChecklistRepositoryImpl;

public final class ChecklistViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;
    public ChecklistViewModelFactory(Context context) { this.context = context.getApplicationContext(); }
    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> type) {
        if (!type.isAssignableFrom(ChecklistViewModel.class)) throw new IllegalArgumentException("ViewModel desconhecido");
        return type.cast(new ChecklistViewModel(new GerenciarChecklists(
                new ChecklistRepositoryImpl(new SessionStorage(context)))));
    }
}
