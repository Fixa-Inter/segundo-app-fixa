package com.aula.fixa.adapter.ocorrencia;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.application.ocorrencia.GerenciarOcorrencias;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.repository.OcorrenciaRepositoryImpl;

public final class OcorrenciaViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;
    public OcorrenciaViewModelFactory(Context context){this.context=context.getApplicationContext();}
    @NonNull @Override public <T extends ViewModel>T create(@NonNull Class<T> type){
        if(!type.isAssignableFrom(OcorrenciaViewModel.class))throw new IllegalArgumentException("ViewModel desconhecido");
        return type.cast(new OcorrenciaViewModel(new GerenciarOcorrencias(new OcorrenciaRepositoryImpl(new SessionStorage(context)))));
    }
}
