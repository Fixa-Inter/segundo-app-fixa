package com.aula.fixa.adapter.os;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.ViewModel;
import com.aula.fixa.application.os.ListarOrdensDoQuadro;
import com.aula.fixa.domain.model.os.OrdemServicoQuadro;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class QuadroOSViewModel extends ViewModel {
    public interface Callback{void success(List<OrdemServicoQuadro> data);void failure(Throwable error);}
    private final ListarOrdensDoQuadro listar;
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    private final Handler main=new Handler(Looper.getMainLooper());
    public QuadroOSViewModel(ListarOrdensDoQuadro listar){this.listar=listar;}
    public void carregar(Callback cb){io.execute(()->{try{List<OrdemServicoQuadro> data=listar.executar();main.post(()->cb.success(data));}catch(Throwable e){main.post(()->cb.failure(e));}});}
    @Override protected void onCleared(){io.shutdownNow();}
}
