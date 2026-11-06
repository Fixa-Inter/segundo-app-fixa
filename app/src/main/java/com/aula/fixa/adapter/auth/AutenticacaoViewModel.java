package com.aula.fixa.adapter.auth;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.aula.fixa.application.auth.Autenticacao;
import com.aula.fixa.application.auth.MensagemErro;
import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.domain.model.ResultadoLoginGoogle;
import com.aula.fixa.domain.model.ResultadoRecuperacaoSenha;
import com.aula.fixa.domain.model.SessaoUsuario;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Adapta os casos de uso de autenticação às telas e entrega resultados no thread principal. */
public final class AutenticacaoViewModel extends ViewModel {
    public interface Listener<T> {
        void onResultado(T resultado);
        void onErro(Throwable erro);
    }

    private final Autenticacao autenticacao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    public AutenticacaoViewModel(Autenticacao autenticacao) { this.autenticacao = autenticacao; }

    public static AutenticacaoViewModel obter(ViewModelStoreOwner owner, Context context) {
        return new ViewModelProvider(owner, new AutenticacaoViewModelFactory(context))
                .get(AutenticacaoViewModel.class);
    }

    // Este metodo síncrono ainda atende fluxos fora de ui/view/auth que já executam em executor
    // próprio.
    public ResultadoRecuperacaoSenha requestPasswordRecovery(String email) throws Exception { return autenticacao.solicitarRecuperacao(email); }

    public void login(String email, String senha, Listener<SessaoUsuario> listener) {
        executar(listener, () -> autenticacao.login(email, senha));
    }
    public void loginGoogle(String token, Listener<ResultadoLoginGoogle> listener) {
        executar(listener, () -> autenticacao.loginGoogle(token));
    }
    public void register(String nome, String email, String senha, long unidadeId, String cargo,
                         String nascimento, Listener<SessaoUsuario> listener) {
        executar(listener, () -> autenticacao.cadastrar(nome, email, senha, unidadeId, cargo, nascimento));
    }
    public void requestPasswordRecovery(String email, Listener<ResultadoRecuperacaoSenha> listener) {
        executar(listener, () -> autenticacao.solicitarRecuperacao(email));
    }
    public void validateRecoveryCode(String email, int codigo, Listener<Boolean> listener) {
        executar(listener, () -> autenticacao.validarCodigo(email, codigo));
    }
    public void resetPassword(String email, int codigo, String senha, Listener<String> listener) {
        executar(listener, () -> autenticacao.redefinirSenha(email, codigo, senha));
    }
    public void institutions(Listener<List<OpcaoCadastro>> listener) {
        executar(listener, autenticacao::instituicoes);
    }
    public void units(long institutionId, Listener<List<OpcaoCadastro>> listener) {
        executar(listener, () -> autenticacao.unidades(institutionId));
    }

    public static String messageFrom(Throwable error) { return MensagemErro.from(error); }

    private <T> void executar(Listener<T> listener, Operacao<T> operacao) {
        executor.execute(() -> {
            try {
                T resultado = operacao.executar();
                main.post(() -> listener.onResultado(resultado));
            } catch (Throwable erro) {
                main.post(() -> listener.onErro(erro));
            }
        });
    }

    @Override protected void onCleared() { executor.shutdownNow(); }

    private interface Operacao<T> { T executar() throws Exception; }
}
