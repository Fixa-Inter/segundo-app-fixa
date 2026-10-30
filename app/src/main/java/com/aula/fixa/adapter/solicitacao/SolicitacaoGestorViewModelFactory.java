package com.aula.fixa.adapter.solicitacao;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.repository.solicitacao.SolicitacaoGestorRepositoryImpl;
import com.aula.fixa.application.solicitacao.ListarSolicitacoesGestor;
import com.aula.fixa.application.solicitacao.DetalharSolicitacaoGestor;
import com.aula.fixa.application.solicitacao.AtualizarStatusSolicitacaoGestor;

public final class SolicitacaoGestorViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;
    public SolicitacaoGestorViewModelFactory(Context context) { this.context = context.getApplicationContext(); }
    @NonNull @Override public <T extends ViewModel> T create(@NonNull Class<T> type) {
        if (!type.isAssignableFrom(SolicitacaoGestorViewModel.class)) throw new IllegalArgumentException("ViewModel desconhecido");
        SolicitacaoGestorRepositoryImpl repository = new SolicitacaoGestorRepositoryImpl(new SessionStorage(context));
        return type.cast(new SolicitacaoGestorViewModel(new ListarSolicitacoesGestor(repository),
                new DetalharSolicitacaoGestor(repository), new AtualizarStatusSolicitacaoGestor(repository)));
    }
}
