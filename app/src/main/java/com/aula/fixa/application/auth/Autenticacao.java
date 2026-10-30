package com.aula.fixa.application.auth;

import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.domain.model.ResultadoLoginGoogle;
import com.aula.fixa.domain.model.SessaoUsuario;
import com.aula.fixa.domain.model.ResultadoRecuperacaoSenha;
import com.aula.fixa.domain.repository.AutenticacaoRepository;
import java.util.List;

/** Casos de uso da autenticação disponíveis à camada de adapters. */
public final class Autenticacao {
    private final AutenticacaoRepository repository;
    public Autenticacao(AutenticacaoRepository repository) { this.repository = repository; }
    public SessaoUsuario login(String email, String senha) throws Exception { return repository.login(email, senha); }
    public ResultadoLoginGoogle loginGoogle(String token) throws Exception { return repository.loginGoogle(token); }
    public SessaoUsuario cadastrar(String nome, String email, String senha, long unidadeId, String cargo, String nascimento) throws Exception {
        return repository.cadastrar(nome, email, senha, unidadeId, cargo, nascimento);
    }
    public ResultadoRecuperacaoSenha solicitarRecuperacao(String email) throws Exception { return repository.solicitarRecuperacao(email); }
    public boolean validarCodigo(String email, int codigo) throws Exception { return repository.validarCodigo(email, codigo); }
    public String redefinirSenha(String email, int codigo, String senha) throws Exception { return repository.redefinirSenha(email, codigo, senha); }
    public List<OpcaoCadastro> instituicoes() throws Exception { return repository.instituicoes(); }
    public List<OpcaoCadastro> unidades(long instituicaoId) throws Exception { return repository.unidades(instituicaoId); }
}
