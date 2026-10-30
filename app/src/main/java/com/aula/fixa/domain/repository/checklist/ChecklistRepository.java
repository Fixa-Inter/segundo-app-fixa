package com.aula.fixa.domain.repository.checklist;

import com.aula.fixa.domain.model.checklist.ChecklistEquipmentModel;
import com.aula.fixa.domain.model.checklist.ChecklistTemplate;
import com.aula.fixa.domain.model.checklist.EquipmentModelOption;
import java.util.List;
import java.util.Map;

public interface ChecklistRepository {
    List<ChecklistTemplate> listar() throws Exception;
    List<EquipmentModelOption> modelosDisponiveis(List<ChecklistTemplate> existentes, String checklistEditadoId) throws Exception;
    List<ChecklistEquipmentModel> modelosDoChecklist(String checklistId) throws Exception;
    ChecklistTemplate cadastrar(ChecklistTemplate checklist) throws Exception;
    ChecklistTemplate atualizar(ChecklistTemplate checklist) throws Exception;
    void excluir(String checklistId) throws Exception;
    void cadastrarInspecao(String checklistId, long equipamentoId, Map<String, Object> dados) throws Exception;
}
