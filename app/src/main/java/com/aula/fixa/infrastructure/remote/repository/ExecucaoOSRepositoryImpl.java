package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.domain.model.os.OrdemServicoQuadro;
import com.aula.fixa.domain.model.os.TarefaOS;
import com.aula.fixa.domain.repository.os.ExecucaoOSRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.ApiException;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.dto.OrdemServicoResponse;
import com.aula.fixa.infrastructure.remote.dto.TarefaPlanejamentoRequest;
import com.aula.fixa.infrastructure.remote.dto.TarefaResponse;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import retrofit2.Response;

public final class ExecucaoOSRepositoryImpl implements ExecucaoOSRepository {
    private final com.aula.fixa.infrastructure.remote.api.OrdemServicoApi osApi;
    private final com.aula.fixa.infrastructure.remote.api.TarefaApi taskApi;
    private final com.aula.fixa.infrastructure.remote.api.ObservacaoConclusaoApi observacaoApi;
    public ExecucaoOSRepositoryImpl(SessionStorage s){osApi=ApiClient.ordensServico(s);taskApi=ApiClient.tarefas(s);observacaoApi=ApiClient.observacoesConclusao(s);}
    @Override public OrdemServicoQuadro detalhes(long id)throws Exception{Response<OrdemServicoResponse> r=osApi.selecionar(id).execute();check(r);OrdemServicoResponse x=r.body();if(x==null)throw new java.io.IOException("OS não encontrada");return new OrdemServicoQuadro(x.id==null?id:x.id,x.titulo,x.descricao,x.categoriaProblema,x.categoriaEquipamento,x.prioridade,x.dataPrevista,x.status,x.nomeUsuario);}
    @Override public List<TarefaOS> tarefas(long id)throws Exception{Response<List<TarefaResponse>> r=taskApi.listar(id).execute();check(r);if(r.body()==null)return Collections.emptyList();List<TarefaOS> out=new ArrayList<>();for(TarefaResponse x:r.body())if(x.id!=null)out.add(new TarefaOS(x.id,x.titulo,x.descricao,statusCode(x.status)));return out;}
    @Override public void atualizarTarefa(TarefaOS t)throws Exception{JsonObject b=new JsonObject();b.addProperty("id",t.id);b.addProperty("titulo",t.titulo);b.addProperty("descricao",t.descricao);b.addProperty("statusOrdemServico",t.status);check(taskApi.atualizar(b).execute());}
    @Override public void criarTarefa(long osId,String titulo,String descricao)throws Exception{check(taskApi.criar(Collections.singletonList(new TarefaPlanejamentoRequest(osId,titulo,descricao))).execute());}
    @Override public void excluirTarefa(long id)throws Exception{check(taskApi.excluir(id).execute());}
    @Override public void excluirOS(long id)throws Exception{check(osApi.excluir(id).execute());}
    @Override public void finalizarOS(long id,String dificuldade,String observacao,List<String> fotos)throws Exception{
        JsonObject body=new JsonObject();body.addProperty("ordemServicoId",id);body.addProperty("dificuldade",dificuldade);body.addProperty("observacao",observacao);
        com.google.gson.JsonArray images=new com.google.gson.JsonArray();for(String foto:fotos)images.add(foto);body.add("fotos",images);
        check(observacaoApi.criar(body).execute());
    }
    @Override public void atualizarOS(long id,Long tecnicoId,String categoria,String data,String prioridade)throws Exception{
        JsonObject body=new JsonObject();body.addProperty("id",id);body.addProperty("usuarioId",tecnicoId);
        body.addProperty("categoriaProblema",categoria);body.addProperty("dataPrevista",data);body.addProperty("prioridade",prioridade);
        check(osApi.atualizar(body).execute());
    }
    private String statusCode(String s){if(s==null)return "NAO_INICIADA";String n=s.toLowerCase(java.util.Locale.ROOT);if(n.contains("conclu"))return "CONCLUIDA";if(n.contains("andamento"))return "EM_ANDAMENTO";if(n.contains("atras"))return "ATRASADA";return "NAO_INICIADA";}
    private void check(Response<?> r)throws Exception{if(!r.isSuccessful())throw new ApiException(r.code(),r.errorBody()==null?"":r.errorBody().string());}
}
