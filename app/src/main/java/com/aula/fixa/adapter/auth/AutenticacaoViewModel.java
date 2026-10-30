package com.aula.fixa.adapter.auth;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import android.content.Context;
import com.aula.fixa.application.auth.Autenticacao;
import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.domain.model.ResultadoLoginGoogle;
import com.aula.fixa.domain.model.ResultadoRecuperacaoSenha;
import com.aula.fixa.domain.model.SessaoUsuario;
import java.util.List;

/** Adapta os casos de uso de autenticação às telas. Operações de rede são chamadas pelas telas em executor. */
public final class AutenticacaoViewModel extends ViewModel {
    private final Autenticacao autenticacao;
    public AutenticacaoViewModel(Autenticacao autenticacao) { this.autenticacao = autenticacao; }
    public static AutenticacaoViewModel obter(ViewModelStoreOwner owner, Context context) {
        return new ViewModelProvider(owner, new AutenticacaoViewModelFactory(context))
                .get(AutenticacaoViewModel.class);
    }
    public SessaoUsuario login(String email, String senha) throws Exception { return autenticacao.login(email, senha); }
    public ResultadoLoginGoogle loginGoogle(String token) throws Exception { return autenticacao.loginGoogle(token); }
    public SessaoUsuario register(String nome, String email, String senha, long unidadeId, String cargo, String nascimento) throws Exception {
        return autenticacao.cadastrar(nome, email, senha, unidadeId, cargo, nascimento);
    }
    public ResultadoRecuperacaoSenha requestPasswordRecovery(String email) throws Exception { return autenticacao.solicitarRecuperacao(email); }
    public boolean validateRecoveryCode(String email, int codigo) throws Exception { return autenticacao.validarCodigo(email, codigo); }
    public String resetPassword(String email, int codigo, String senha) throws Exception { return autenticacao.redefinirSenha(email, codigo, senha); }
    public List<OpcaoCadastro> institutions() throws Exception { return autenticacao.instituicoes(); }
    public List<OpcaoCadastro> units(long institutionId) throws Exception { return autenticacao.unidades(institutionId); }
    public static String messageFrom(Throwable error) { return com.aula.fixa.infrastructure.auth.AuthRepository.messageFrom(error); }
}
