package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.domain.model.os.OrdemServicoQuadro;
import com.aula.fixa.domain.repository.os.QuadroOSRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.ApiException;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.dto.OrdemServicoResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import retrofit2.Response;

public final class QuadroOSRepositoryImpl implements QuadroOSRepository {
    private final com.aula.fixa.infrastructure.remote.api.OrdemServicoApi api;
    public QuadroOSRepositoryImpl(SessionStorage session){api=ApiClient.ordensServico(session);}
    @Override public List<OrdemServicoQuadro> listar()throws Exception{
        Response<List<OrdemServicoResponse>> response=api.listar(null,null,null,null,null,null,null,null,null,null,null).execute();
        if(response.code()==404)return Collections.emptyList();
        if(!response.isSuccessful())throw new ApiException(response.code(),response.errorBody()==null?"":response.errorBody().string());
        if(response.body()==null)return Collections.emptyList();
        List<OrdemServicoQuadro> result=new ArrayList<>();
        for(OrdemServicoResponse dto:response.body())if(dto.id!=null)result.add(new OrdemServicoQuadro(dto.id,dto.titulo,dto.descricao,dto.categoriaProblema,dto.categoriaEquipamento,dto.prioridade,dto.dataPrevista,dto.status,dto.nomeUsuario));
        return result;
    }
}
