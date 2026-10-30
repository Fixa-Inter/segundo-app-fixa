package com.aula.fixa.adapter.foto;

import android.net.Uri;
import androidx.lifecycle.ViewModel;
import com.aula.fixa.application.foto.GerenciarFotos;
import com.aula.fixa.domain.repository.FotoRepository;

public final class FotoViewModel extends ViewModel {
    public interface Callback {
        void aoSucesso(String url, String publicId);
        void aoReceberTokenExclusao(String token);
        void aoFalhar(String mensagem);
    }

    private final GerenciarFotos fotos;
    public FotoViewModel(GerenciarFotos fotos) { this.fotos = fotos; }

    public void enviarSolicitacao(Uri arquivo, Callback callback) {
        fotos.enviarSolicitacao(arquivo.toString(), adaptar(callback));
    }
    public void enviarPerfil(Uri arquivo, Callback callback) {
        fotos.enviarPerfil(arquivo.toString(), adaptar(callback));
    }
    public void excluir(String token) {
        fotos.excluir(token, new FotoRepository.Callback() {
            @Override public void aoConcluir(String url, String identificadorExclusao) { }
            @Override public void aoReceberTokenExclusao(String valor) { }
            @Override public void aoFalhar(String mensagem) { }
        });
    }
    private FotoRepository.Callback adaptar(Callback callback) {
        return new FotoRepository.Callback() {
            @Override public void aoConcluir(String url, String id) { callback.aoSucesso(url, id); }
            @Override public void aoReceberTokenExclusao(String token) { callback.aoReceberTokenExclusao(token); }
            @Override public void aoFalhar(String mensagem) { callback.aoFalhar(mensagem); }
        };
    }
}
