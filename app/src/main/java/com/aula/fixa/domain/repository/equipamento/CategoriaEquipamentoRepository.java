package com.aula.fixa.domain.repository.equipamento;

import com.aula.fixa.domain.model.OpcaoCadastro;
import java.util.List;

public interface CategoriaEquipamentoRepository {
    List<OpcaoCadastro> listar() throws Exception;
    OpcaoCadastro criar(String nome, String descricao) throws Exception;
    OpcaoCadastro atualizar(long id, String nome) throws Exception;
    void excluir(long id) throws Exception;
}
