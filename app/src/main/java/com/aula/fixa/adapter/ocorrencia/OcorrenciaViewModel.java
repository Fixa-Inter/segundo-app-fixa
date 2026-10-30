package com.aula.fixa.adapter.ocorrencia;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.ViewModel;
import com.aula.fixa.application.ocorrencia.GerenciarOcorrencias;
import com.aula.fixa.domain.model.ocorrencia.OcorrenciaItem;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class OcorrenciaViewModel extends ViewModel {
    public interface Callback<T>{void success(T value);void failure(Throwable error);}
    private final GerenciarOcorrencias useCase;
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    private final Handler main=new Handler(Looper.getMainLooper());
    public OcorrenciaViewModel(GerenciarOcorrencias useCase){this.useCase=useCase;}
    public void listar(Callback<List<OcorrenciaItem>> cb){run(useCase::listar,cb);}
    public void listarMinhas(Callback<List<OcorrenciaItem>> cb){run(useCase::listarMinhas,cb);}
    public void cadastrar(Long localId,Long equipmentId,com.aula.fixa.domain.enums.CategoriaProblema category,
            com.aula.fixa.domain.enums.Prioridade priority,String title,String description,String locationDescription,
            List<String> photos,Callback<Void> cb){run(()->{useCase.cadastrar(localId,equipmentId,category,priority,title,description,locationDescription,photos);return null;},cb);}
    public void detalhar(long id,Callback<OcorrenciaItem> cb){run(()->useCase.detalhar(id),cb);}
    public void atualizar(long id,Long localId,Long equipmentId,com.aula.fixa.domain.enums.CategoriaProblema category,
            com.aula.fixa.domain.enums.Prioridade priority,List<Long> photosToDelete,List<String> photosToAdd,Callback<Void> cb){
        run(()->{useCase.atualizar(id,localId,equipmentId,category,priority,photosToDelete,photosToAdd);return null;},cb);
    }
    public void excluir(long id,Callback<Void> cb){run(()->{useCase.excluir(id);return null;},cb);}
    private <T>void run(Work<T> work,Callback<T> cb){io.execute(()->{try{T value=work.run();main.post(()->cb.success(value));}catch(Throwable error){main.post(()->cb.failure(error));}});}
    @Override protected void onCleared(){io.shutdownNow();}
    private interface Work<T>{T run()throws Exception;}
}
