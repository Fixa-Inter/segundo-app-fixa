package com.aula.fixa.adapter.operations;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.application.operations.LoadManagerHome;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.repository.ManagerHomeRepositoryImpl;

public final class ManagerHomeViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;
    public ManagerHomeViewModelFactory(Context context) { this.context = context.getApplicationContext(); }

    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (!modelClass.isAssignableFrom(ManagerHomeViewModel.class))
            throw new IllegalArgumentException("ViewModel desconhecido: " + modelClass.getName());
        ManagerHomeRepositoryImpl repository = new ManagerHomeRepositoryImpl(new SessionStorage(context));
        return modelClass.cast(new ManagerHomeViewModel(new LoadManagerHome(repository)));
    }
}
