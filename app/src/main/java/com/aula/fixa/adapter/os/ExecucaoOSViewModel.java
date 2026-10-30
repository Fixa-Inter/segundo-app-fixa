package com.aula.fixa.adapter.os;
import android.os.Handler;import android.os.Looper;import androidx.lifecycle.ViewModel;
import com.aula.fixa.application.os.GerenciarExecucaoOS;import com.aula.fixa.domain.model.os.OrdemServicoQuadro;import com.aula.fixa.domain.model.os.TarefaOS;
import java.util.List;import java.util.concurrent.ExecutorService;import java.util.concurrent.Executors;
public final class ExecucaoOSViewModel extends ViewModel {
 public interface Callback<T>{void success(T value);void failure(Throwable error);} private final GerenciarExecucaoOS useCase;private final ExecutorService io=Executors.newSingleThreadExecutor();private final Handler main=new Handler(Looper.getMainLooper());
 public ExecucaoOSViewModel(GerenciarExecucaoOS useCase){this.useCase=useCase;}
 public void detalhes(long id,Callback<OrdemServicoQuadro> cb){run(()->useCase.detalhes(id),cb);}
 public void tarefas(long id,Callback<List<TarefaOS>> cb){run(()->useCase.tarefas(id),cb);}
 public void atualizar(TarefaOS t,Callback<Void> cb){run(()->{useCase.atualizar(t);return null;},cb);}
 public void criar(long id,String t,String d,Callback<Void> cb){run(()->{useCase.criar(id,t,d);return null;},cb);}
 public void excluirTarefa(long id,Callback<Void> cb){run(()->{useCase.excluirTarefa(id);return null;},cb);}
 public void excluirOS(long id,Callback<Void> cb){run(()->{useCase.excluirOS(id);return null;},cb);}
 public void finalizar(long id,String dificuldade,String observacao,List<String> fotos,Callback<Void> cb){run(()->{useCase.finalizar(id,dificuldade,observacao,fotos);return null;},cb);}
 public void atualizarOS(long id,Long tecnico,String categoria,String data,String prioridade,Callback<Void> cb){run(()->{useCase.atualizarOS(id,tecnico,categoria,data,prioridade);return null;},cb);}
 private <T>void run(Operacao<T> op,Callback<T> cb){io.execute(()->{try{T v=op.call();main.post(()->cb.success(v));}catch(Throwable e){main.post(()->cb.failure(e));}});}
 @Override protected void onCleared(){io.shutdownNow();} private interface Operacao<T>{T call()throws Exception;}
}
