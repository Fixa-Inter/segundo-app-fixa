package com.aula.fixa.adapter.os;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.application.os.ListarOrdensDoQuadro;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.repository.QuadroOSRepositoryImpl;

public final class QuadroOSViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;
    public QuadroOSViewModelFactory(Context context){this.context=context.getApplicationContext();}
    @NonNull @Override public <T extends ViewModel>T create(@NonNull Class<T> c){return (T)new QuadroOSViewModel(new ListarOrdensDoQuadro(new QuadroOSRepositoryImpl(new SessionStorage(context))));}
}
