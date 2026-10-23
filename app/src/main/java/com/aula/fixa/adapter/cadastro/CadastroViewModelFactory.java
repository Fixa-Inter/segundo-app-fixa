package com.aula.fixa.adapter.cadastro;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.application.cadastro.ListarCategorias;
import com.aula.fixa.application.cadastro.ListarLocais;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.repository.CadastroRepositoryImpl;

public final class CadastroViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;
    public CadastroViewModelFactory(Context context) { this.context = context.getApplicationContext(); }
    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (!modelClass.isAssignableFrom(CadastroViewModel.class)) throw new IllegalArgumentException("ViewModel desconhecido");
        CadastroRepositoryImpl repository = new CadastroRepositoryImpl(new SessionStorage(context));
        return modelClass.cast(new CadastroViewModel(new ListarLocais(repository), new ListarCategorias(repository)));
    }
}
