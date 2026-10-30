package com.aula.fixa.domain.repository;

import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.domain.model.ResultadoLoginGoogle;
import com.aula.fixa.domain.model.SessaoUsuario;
import com.aula.fixa.domain.model.ResultadoRecuperacaoSenha;
import java.util.List;

public interface AutenticacaoRepository {
    SessaoUsuario login(String email, String senha) throws Exception;
    ResultadoLoginGoogle loginGoogle(String token) throws Exception;
    SessaoUsuario cadastrar(String nome, String email, String senha, long unidadeId, String cargo, String nascimento) throws Exception;
    ResultadoRecuperacaoSenha solicitarRecuperacao(String email) throws Exception;
    boolean validarCodigo(String email, int codigo) throws Exception;
    String redefinirSenha(String email, int codigo, String senha) throws Exception;
    List<OpcaoCadastro> instituicoes() throws Exception;
    List<OpcaoCadastro> unidades(long instituicaoId) throws Exception;
}
