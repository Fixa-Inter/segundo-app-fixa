package com.aula.fixa.adapter.foto;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.application.foto.GerenciarFotos;
import com.aula.fixa.infrastructure.cloudinary.FotoRepositoryImpl;

public final class FotoViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;
    public FotoViewModelFactory(Context context) { this.context = context.getApplicationContext(); }
    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (!modelClass.isAssignableFrom(FotoViewModel.class)) throw new IllegalArgumentException("ViewModel desconhecido");
        return modelClass.cast(new FotoViewModel(new GerenciarFotos(new FotoRepositoryImpl(context))));
    }
}
