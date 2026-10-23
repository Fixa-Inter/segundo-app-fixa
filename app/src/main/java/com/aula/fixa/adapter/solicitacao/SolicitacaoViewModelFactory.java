package com.aula.fixa.adapter.solicitacao;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.application.solicitacao.CriarSolicitacao;
import com.aula.fixa.application.solicitacao.ListarSolicitacoes;
import com.aula.fixa.application.solicitacao.DetalharSolicitacao;
import com.aula.fixa.application.solicitacao.AtualizarSolicitacao;
import com.aula.fixa.application.solicitacao.ExcluirSolicitacao;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.repository.SolicitacaoRepositoryImpl;

/** Montagem das dependências do fluxo de solicitações. */
public final class SolicitacaoViewModelFactory implements ViewModelProvider.Factory {
    private final Context applicationContext;
    public SolicitacaoViewModelFactory(Context context) { applicationContext = context.getApplicationContext(); }

    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (!modelClass.isAssignableFrom(SolicitacaoViewModel.class)) throw new IllegalArgumentException("ViewModel desconhecida");
        SolicitacaoRepositoryImpl repository = new SolicitacaoRepositoryImpl(new SessionStorage(applicationContext));
        return modelClass.cast(new SolicitacaoViewModel(new CriarSolicitacao(repository),
                new ListarSolicitacoes(repository), new DetalharSolicitacao(repository),
                new AtualizarSolicitacao(repository), new ExcluirSolicitacao(repository)));
    }
}
