package com.aula.fixa.adapter.evento;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.application.evento.DetalharEvento;
import com.aula.fixa.application.evento.ListarEventos;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.repository.EventoRepositoryImpl;

public final class EventoViewModelFactory implements ViewModelProvider.Factory {
    private final Context applicationContext;
    public EventoViewModelFactory(Context context) { applicationContext = context.getApplicationContext(); }
    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (!modelClass.isAssignableFrom(EventoViewModel.class)) throw new IllegalArgumentException("ViewModel desconhecida");
        EventoRepositoryImpl repository = new EventoRepositoryImpl(new SessionStorage(applicationContext));
        return modelClass.cast(new EventoViewModel(new ListarEventos(repository), new DetalharEvento(repository)));
    }
}
