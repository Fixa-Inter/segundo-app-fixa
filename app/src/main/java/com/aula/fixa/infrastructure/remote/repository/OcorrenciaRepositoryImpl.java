package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.domain.model.ocorrencia.OcorrenciaItem;
import com.aula.fixa.domain.model.ocorrencia.OcorrenciaFoto;
import com.aula.fixa.domain.repository.ocorrencia.OcorrenciaRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.ApiException;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.api.OcorrenciaApi;
import com.aula.fixa.infrastructure.remote.dto.FotoResponse;
import com.aula.fixa.infrastructure.remote.dto.OcorrenciaAtualizarRequest;
import com.aula.fixa.infrastructure.remote.dto.OcorrenciaResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import retrofit2.Response;

public final class OcorrenciaRepositoryImpl implements OcorrenciaRepository {
    private final OcorrenciaApi api;
    public OcorrenciaRepositoryImpl(SessionStorage session){api=ApiClient.ocorrencias(session);}
    @Override public List<OcorrenciaItem> listar()throws Exception{
        Response<List<OcorrenciaResponse>> response=api.listar().execute();check(response);
        if(response.body()==null)return Collections.emptyList();
        List<OcorrenciaItem> result=new ArrayList<>();
        for(OcorrenciaResponse dto:response.body())if(dto!=null&&dto.id!=null)result.add(map(dto));
        return result;
    }
    @Override public OcorrenciaItem detalhar(long id)throws Exception{
        Response<OcorrenciaResponse> response=api.detalhar(id).execute();check(response);
        if(response.body()==null)throw new IOException("A API não retornou a ocorrência.");
        return map(response.body());
    }
    @Override public void atualizar(long id,Long localId,Long equipmentId,com.aula.fixa.domain.enums.CategoriaProblema category,
            com.aula.fixa.domain.enums.Prioridade priority,List<Long> photosToDelete,List<String> photosToAdd)throws Exception{
        check(api.atualizar(new OcorrenciaAtualizarRequest(id,localId,equipmentId,category,priority,null,null,null,photosToDelete,photosToAdd)).execute());
    }
    @Override public void excluir(long id)throws Exception{check(api.excluir(id).execute());}
    private static OcorrenciaItem map(OcorrenciaResponse dto){
        List<OcorrenciaFoto> photos=new ArrayList<>();
        if(dto.fotos!=null)for(FotoResponse photo:dto.fotos)if(photo!=null&&photo.url!=null&&!photo.url.trim().isEmpty())photos.add(new OcorrenciaFoto(photo.id==null?0:photo.id,photo.url));
        return new OcorrenciaItem(dto.id==null?0:dto.id,dto.titulo,dto.descricaoOcorrencia,dto.categoriaProblema,
                dto.prioridade,dto.localUnidade,dto.descricaoLocal,dto.dataCriacao,dto.categoriaEquipamento,dto.nomeUsuario,dto.localUnidadeId,dto.categoriaEquipamentoId,photos);
    }
    private static void check(Response<?> response)throws Exception{
        if(response.isSuccessful())return;
        String message=response.errorBody()==null?"":response.errorBody().string();
        throw new ApiException(response.code(),message);
    }
}
