package com.aula.fixa.infrastructure.cloudinary;

import android.content.Context;
import android.net.Uri;
import com.aula.fixa.domain.repository.FotoRepository;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.Looper;
import com.aula.fixa.infrastructure.local.NetworkAvailability;

public final class FotoRepositoryImpl implements FotoRepository {
    private final Context context;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    public FotoRepositoryImpl(Context context) { this.context = context.getApplicationContext(); }

    @Override public void enviarSolicitacao(String uri, Callback callback) {
        if (!NetworkAvailability.isOnline(context)) {
            callback.aoFalhar("Esta ação exige conexão com a internet.");
            return;
        }
        CloudinaryUploadService.salvar(context, Uri.parse(uri), CloudinaryUploadService.callback(adaptar(callback)));
    }
    public void guardarSolicitacaoLocalmente(String uri, Callback callback) {
        io.execute(() -> {
            try {
                File directory = new File(context.getFilesDir(), "offline_requests/images");
                if (!directory.exists() && !directory.mkdirs()) throw new IOException("Não foi possível preparar o armazenamento local.");
                String mime = context.getContentResolver().getType(Uri.parse(uri));
                String extension = mime != null && mime.toLowerCase(java.util.Locale.ROOT).contains("png") ? ".png" : ".jpg";
                File destination = new File(directory, UUID.randomUUID() + extension);
                try (InputStream input = context.getContentResolver().openInputStream(Uri.parse(uri));
                     OutputStream output = new java.io.FileOutputStream(destination)) {
                    if (input == null) throw new IOException("Não foi possível ler a foto selecionada.");
                    byte[] buffer = new byte[8192]; int count;
                    while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
                }
                main.post(() -> callback.aoConcluir(destination.getAbsolutePath(), null));
            } catch (Exception error) {
                main.post(() -> callback.aoFalhar(error.getMessage() == null ? "Não foi possível salvar a foto offline." : error.getMessage()));
            }
        });
    }
    @Override public void enviarPerfil(String uri, Callback callback) {
        if (!NetworkAvailability.isOnline(context)) { callback.aoFalhar("Esta ação exige conexão com a internet."); return; }
        CloudinaryUploadService.salvarPerfil(context, Uri.parse(uri), adaptar(callback));
    }
    @Override public void excluir(String token, Callback callback) {
        if (!NetworkAvailability.isOnline(context)) { callback.aoFalhar("Esta ação exige conexão com a internet."); return; }
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
