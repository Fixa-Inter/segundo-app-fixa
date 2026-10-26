package com.aula.fixa.application.os;
import com.aula.fixa.domain.model.os.OrdemServicoQuadro;
import com.aula.fixa.domain.model.os.TarefaOS;
import com.aula.fixa.domain.repository.os.ExecucaoOSRepository;
import java.util.List;
public final class GerenciarExecucaoOS {
    private final ExecucaoOSRepository repository; public GerenciarExecucaoOS(ExecucaoOSRepository r){repository=r;}
    public OrdemServicoQuadro detalhes(long id)throws Exception{return repository.detalhes(id);}
    public List<TarefaOS> tarefas(long id)throws Exception{return repository.tarefas(id);}
    public void atualizar(TarefaOS t)throws Exception{repository.atualizarTarefa(t);}
    public void criar(long id,String t,String d)throws Exception{repository.criarTarefa(id,t,d);}
    public void excluirTarefa(long id)throws Exception{repository.excluirTarefa(id);}
    public void excluirOS(long id)throws Exception{repository.excluirOS(id);}
    public void finalizar(long id,String dificuldade,String observacao,java.util.List<String> fotos)throws Exception{
        if(id<=0)throw new IllegalArgumentException("OS inválida.");
        if(dificuldade==null||dificuldade.trim().isEmpty())throw new IllegalArgumentException("Informe o grau de dificuldade.");
        if(fotos==null||fotos.isEmpty())throw new IllegalArgumentException("Adicione ao menos uma foto.");
        repository.finalizarOS(id,dificuldade,observacao,fotos);
    }
    public void atualizarOS(long id,Long tecnico,String categoria,String data,String prioridade)throws Exception{
        if(id<=0||tecnico==null||categoria==null||data==null||prioridade==null)throw new IllegalArgumentException("Preencha os dados da OS.");
        repository.atualizarOS(id,tecnico,categoria,data,prioridade);
    }
}
