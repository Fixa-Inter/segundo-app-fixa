package com.aula.fixa.domain.repository.equipamento;

import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.domain.model.equipamento.EquipamentoItem;
import java.util.List;

public interface EquipamentoRepository {
    List<EquipamentoItem> listar(long modeloId) throws Exception;
    List<OpcaoCadastro> locais() throws Exception;
    EquipamentoItem criar(long modeloId, long localId, String codigo) throws Exception;
    EquipamentoItem atualizar(long id, long localId, String codigo) throws Exception;
    void excluir(long id) throws Exception;
}
