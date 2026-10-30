package com.aula.fixa.domain.repository.equipamento;

import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.domain.model.equipamento.ModeloEquipamentoItem;
import java.util.List;

public interface ModeloEquipamentoRepository {
    List<ModeloEquipamentoItem> listar() throws Exception;
    List<OpcaoCadastro> categorias() throws Exception;
    List<OpcaoCadastro> marcas() throws Exception;
    ModeloEquipamentoItem criar(String nome, String descricao, long categoriaId, long marcaId) throws Exception;
    ModeloEquipamentoItem atualizar(long id, String nome, String descricao, long categoriaId, long marcaId) throws Exception;
    void excluir(long id) throws Exception;
}
