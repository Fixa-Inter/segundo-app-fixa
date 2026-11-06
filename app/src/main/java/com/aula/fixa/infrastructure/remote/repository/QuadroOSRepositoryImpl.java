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
import com.aula.fixa.domain.model.FiltroBusca;
import java.util.LinkedHashMap;
import java.util.Map;

public final class QuadroOSRepositoryImpl implements QuadroOSRepository {
    private final com.aula.fixa.infrastructure.remote.api.OrdemServicoApi api;
    private final boolean minhas;
    public QuadroOSRepositoryImpl(SessionStorage session){this(session, false);}
    public QuadroOSRepositoryImpl(SessionStorage session, boolean minhas){api=ApiClient.ordensServico(session);this.minhas=minhas;}
    @Override public List<OrdemServicoQuadro> listar()throws Exception{return listar(FiltroBusca.ultimosTresMeses());}
    @Override public List<OrdemServicoQuadro> listar(FiltroBusca filtros)throws Exception{
        Map<String,String> query=query(filtros);
        Response<List<OrdemServicoResponse>> response=minhas
                ? api.minhasFiltradas(query).execute()
                : api.listarFiltradas(query).execute();
        if(response.code()==404)return Collections.emptyList();
        if(!response.isSuccessful())throw new ApiException(response.code(),response.errorBody()==null?"":response.errorBody().string());
        if(response.body()==null)return Collections.emptyList();
        List<OrdemServicoQuadro> result=new ArrayList<>();
        for(OrdemServicoResponse dto:response.body())if(dto.id!=null)result.add(new OrdemServicoQuadro(dto.id,dto.titulo,dto.descricao,dto.categoriaProblema,dto.categoriaEquipamento,dto.prioridade,dto.dataPrevista,dto.status,dto.nomeUsuario,dto.dataCriacao,dto.localUnidade,dto.descricaoLocal,dto.solicitante));
        return result;
    }

    private Map<String,String> query(FiltroBusca filters){
        FiltroBusca f=filters==null?FiltroBusca.ultimosTresMeses():filters;Map<String,String> q=new LinkedHashMap<>();
        put(q,"titulo",f.termo);put(q,"categoria_equipamento",f.categoriaEquipamento);
        put(q,"categoria_problema",f.categoriaProblema);put(q,"prioridade",f.prioridade);
        put(q,"status_ordem_servico",statusCode(f.status));put(q,"nome_usuario",f.autor);
        put(q,"data_criacao_min",f.dataInicioIso());put(q,"data_criacao_max",f.dataFimIso());
        put(q,"data_prevista_min",f.prazoInicioIso());put(q,"data_prevista_max",f.prazoFimIso());
        q.put("campo_ordenacao","data_prevista");q.put("direcao_ordenacao","-1");return q;
    }
    private String statusCode(String value){if(value==null)return null;String n=value.trim().toLowerCase(java.util.Locale.ROOT);
        if(n.equals("não iniciada")||n.equals("nao iniciada"))return "NAO_INICIADA";
        if(n.equals("em andamento"))return "EM_ANDAMENTO";if(n.equals("concluída")||n.equals("concluida"))return "CONCLUIDA";
        if(n.equals("atrasada"))return "ATRASADA";return value;}
    private void put(Map<String,String> q,String k,String v){if(v!=null&&!v.trim().isEmpty())q.put(k,v);}
}
