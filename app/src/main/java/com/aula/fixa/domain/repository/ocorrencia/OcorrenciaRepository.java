package com.aula.fixa.domain.repository.ocorrencia;

import com.aula.fixa.domain.model.ocorrencia.OcorrenciaItem;
import java.util.List;

public interface OcorrenciaRepository {
    List<OcorrenciaItem> listar() throws Exception;
    OcorrenciaItem detalhar(long id) throws Exception;
    void atualizar(long id, Long localId, Long equipmentId, com.aula.fixa.domain.enums.CategoriaProblema category,
            com.aula.fixa.domain.enums.Prioridade priority, List<Long> photosToDelete, List<String> photosToAdd) throws Exception;
    void excluir(long id) throws Exception;
}
