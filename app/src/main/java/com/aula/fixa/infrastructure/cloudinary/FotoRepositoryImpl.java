package com.aula.fixa.infrastructure.cloudinary;

import android.content.Context;
import android.net.Uri;
import com.aula.fixa.domain.repository.FotoRepository;

public final class FotoRepositoryImpl implements FotoRepository {
    private final Context context;
    public FotoRepositoryImpl(Context context) { this.context = context.getApplicationContext(); }

    @Override public void enviarSolicitacao(String uri, Callback callback) {
        CloudinaryUploadService.salvar(context, Uri.parse(uri), CloudinaryUploadService.callback(adaptar(callback)));
    }
    @Override public void enviarPerfil(String uri, Callback callback) {
        CloudinaryUploadService.salvarPerfil(context, Uri.parse(uri), adaptar(callback));
    }
    @Override public void excluir(String token, Callback callback) {
        CloudinaryUploadService.excluir(token, new CloudinaryUploadService.ResultadoCallback() {
            @Override public void onSuccess(String url, String publicId) { callback.aoConcluir(url, publicId); }
            @Override public void onError(String message) { callback.aoFalhar(message); }
        });
    }
    private CloudinaryUploadService.ResultadoCallback adaptar(Callback callback) {
        return new CloudinaryUploadService.ResultadoCallback() {
            @Override public void onSuccess(String url, String publicId) { callback.aoConcluir(url, publicId); }
            @Override public void onDeleteToken(String token) { callback.aoReceberTokenExclusao(token); }
            @Override public void onError(String message) { callback.aoFalhar(message); }
        };
    }
}
