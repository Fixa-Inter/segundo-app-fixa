package com.aula.fixa.infrastructure.cloudinary;

import android.content.Context;
import android.net.Uri;

import com.aula.fixa.BuildConfig;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.io.File;
import java.io.IOException;

public final class CloudinaryUploadService {
    private static boolean initialized;

    private CloudinaryUploadService() {
    }

    public static synchronized void initialize(Context context) {
        if (initialized) {
            return;
        }
        if (BuildConfig.CLOUDINARY_CLOUD_NAME.trim().isEmpty()) {
            throw new IllegalStateException("Informe cloudinary.cloud_name no local.properties.");
        }

        Map<String, Object> configuration = new HashMap<>();
        configuration.put("cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME);
        configuration.put("api_key", BuildConfig.CLOUDINARY_API_KEY);
        MediaManager.init(context.getApplicationContext(), configuration);

        initialized = true;
    }

    public static String salvar(Context context, Uri arquivo, UploadCallback callback) {
        return salvar(context, arquivo, BuildConfig.CLOUDINARY_SOLICITACOES_FOLDER, callback);
    }

    /** Upload síncrono apenas para o worker de sincronização offline, sempre fora da UI thread. */
    public static String salvarBloqueante(Context context, String caminhoLocal) throws Exception {
        if (caminhoLocal == null || caminhoLocal.trim().isEmpty()) throw new IOException("Arquivo local ausente.");
        File file = new File(caminhoLocal);
        if (!file.isFile()) throw new IOException("A foto local da solicitação não foi encontrada.");
        CountDownLatch finished = new CountDownLatch(1);
        AtomicReference<String> url = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        salvar(context, Uri.fromFile(file), new UploadCallback() {
            @Override public void onStart(String requestId) { }
            @Override public void onProgress(String requestId, long bytes, long totalBytes) { }
            @Override public void onSuccess(String requestId, Map resultData) {
                Object value = resultData == null ? null : resultData.get("secure_url");
                if (value != null) url.set(String.valueOf(value));
                else failure.set(new IOException("O Cloudinary não retornou a URL da foto."));
                finished.countDown();
            }
            @Override public void onError(String requestId, ErrorInfo error) {
                failure.set(new IOException(error == null ? "Falha ao enviar foto." : error.getDescription()));
                finished.countDown();
            }
            @Override public void onReschedule(String requestId, ErrorInfo error) {
                failure.set(new IOException(error == null ? "O envio da foto será repetido." : error.getDescription()));
                finished.countDown();
            }
        });
        if (!finished.await(2, TimeUnit.MINUTES)) throw new IOException("Tempo esgotado ao enviar foto pendente.");
        if (failure.get() != null) throw new IOException("Não foi possível sincronizar uma foto pendente.", failure.get());
        return url.get();
    }

    public static String salvarPerfil(Context context, Uri arquivo, ResultadoCallback callback) {
        return salvar(context, arquivo, "usuarios", callback(callback));
    }

    private static String salvar(Context context, Uri arquivo, String folder, UploadCallback callback) {
        initialize(context);

        if (arquivo == null) {
            throw new IllegalArgumentException("A foto não pode estar vazia.");
        }
        if (BuildConfig.CLOUDINARY_UPLOAD_PRESET.trim().isEmpty()) {
            throw new IllegalStateException("Informe cloudinary.upload_preset no local.properties.");
        }

        return MediaManager.get().upload(arquivo)
                .unsigned(BuildConfig.CLOUDINARY_UPLOAD_PRESET)
                .option("folder", folder)
                .callback(callback)
                .dispatch(context);
    }

    public static UploadCallback callback(ResultadoCallback callback) {
        return new UploadCallback() {
            @Override
            public void onStart(String requestId) {
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {
            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                callback.onSuccess(String.valueOf(resultData.get("secure_url")), String.valueOf(resultData.get("public_id")));
                if (resultData.get("delete_token") != null) {
                    callback.onDeleteToken(String.valueOf(resultData.get("delete_token")));
                }
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                callback.onError(error.getDescription());
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
            }
        };
    }

    public interface ResultadoCallback {
        void onSuccess(String url, String publicId);

        void onError(String message);

        default void onDeleteToken(String token) {
        }
    }

    public static void excluir(String deleteToken, ResultadoCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                okhttp3.RequestBody body = new okhttp3.FormBody.Builder().add("token", deleteToken).build();
                okhttp3.Response response = new okhttp3.OkHttpClient().newCall(new okhttp3.Request.Builder()
                        .url("https://api.cloudinary.com/v1_1/" + BuildConfig.CLOUDINARY_CLOUD_NAME + "/delete_by_token")
                        .post(body).build()).execute();
                if (!response.isSuccessful()) {
                    callback.onError("Não foi possível excluir a imagem do Cloudinary.");
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() == null ? "Erro ao excluir imagem." : e.getMessage());
            }
        });
    }
}
