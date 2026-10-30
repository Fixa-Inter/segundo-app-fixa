package com.aula.fixa.adapter.os;
import android.content.Context;import androidx.annotation.NonNull;import androidx.lifecycle.ViewModel;import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.application.os.GerenciarExecucaoOS;import com.aula.fixa.infrastructure.auth.session.SessionStorage;import com.aula.fixa.infrastructure.remote.repository.ExecucaoOSRepositoryImpl;
public final class ExecucaoOSViewModelFactory implements ViewModelProvider.Factory {private final Context context;public ExecucaoOSViewModelFactory(Context c){context=c.getApplicationContext();}@NonNull @Override public <T extends ViewModel>T create(@NonNull Class<T> type){return (T)new ExecucaoOSViewModel(new GerenciarExecucaoOS(new ExecucaoOSRepositoryImpl(new SessionStorage(context))));}}
