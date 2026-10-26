package com.aula.fixa.application.ocorrencia;

import com.aula.fixa.domain.model.ocorrencia.OcorrenciaItem;
import com.aula.fixa.domain.repository.ocorrencia.OcorrenciaRepository;
import java.util.List;

public final class GerenciarOcorrencias {
    private final OcorrenciaRepository repository;
    public GerenciarOcorrencias(OcorrenciaRepository repository){this.repository=repository;}
    public List<OcorrenciaItem> listar() throws Exception{return repository.listar();}
    public OcorrenciaItem detalhar(long id) throws Exception{return repository.detalhar(id);}
    public void atualizar(long id,Long localId,Long equipmentId,com.aula.fixa.domain.enums.CategoriaProblema category,
            com.aula.fixa.domain.enums.Prioridade priority,List<Long> photosToDelete,List<String> photosToAdd)throws Exception{
        repository.atualizar(id,localId,equipmentId,category,priority,photosToDelete,photosToAdd);
    }
    public void excluir(long id)throws Exception{repository.excluir(id);}
}
