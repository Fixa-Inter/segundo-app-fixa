package com.aula.fixa.adapter.planejamento;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.application.planejamento.RecomendarCategoriaProblema;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.repository.planejamento.RecomendacaoCategoriaRepositoryImpl;

/** Monta as dependências da recomendação JEV. */
public final class RecomendacaoCategoriaViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;
    public RecomendacaoCategoriaViewModelFactory(Context context) { this.context = context.getApplicationContext(); }

    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> type) {
        if (!type.isAssignableFrom(RecomendacaoCategoriaViewModel.class)) {
            throw new IllegalArgumentException("ViewModel desconhecido");
        }
        RecomendacaoCategoriaRepositoryImpl repository =
                new RecomendacaoCategoriaRepositoryImpl(new SessionStorage(context));
        return type.cast(new RecomendacaoCategoriaViewModel(new RecomendarCategoriaProblema(repository)));
    }
}
