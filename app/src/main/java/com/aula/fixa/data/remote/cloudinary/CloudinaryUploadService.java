package com.aula.fixa.data.remote.cloudinary;

import android.content.Context;
import android.net.Uri;

import com.aula.fixa.BuildConfig;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.Map;
import java.util.concurrent.Executors;

public final class CloudinaryUploadService {
    private static boolean initialized;

    private CloudinaryUploadService() {
    }

    public static synchronized void initialize(Context context) {
        if (initialized) {
            return;
        }
        if (BuildConfig.CLOUDINARY_CLOUD_NAME.isBlank()) {
            throw new IllegalStateException("Informe cloudinary.cloud_name no local.properties.");
        }

        MediaManager.init(context.getApplicationContext(), Map.of(
                "cloud_name", BuildConfig.CLOUDINARY_CLOUD_NAME,
                "api_key", BuildConfig.CLOUDINARY_API_KEY
        ));

        initialized = true;
    }

    public static String salvar(Context context, Uri arquivo, UploadCallback callback) {
        initialize(context);

        if (arquivo == null) {
            throw new IllegalArgumentException("A foto não pode estar vazia.");
        }
        if (BuildConfig.CLOUDINARY_UPLOAD_PRESET.isBlank()) {
            throw new IllegalStateException("Informe cloudinary.upload_preset no local.properties.");
        }

        return MediaManager.get().upload(arquivo)
                .unsigned(BuildConfig.CLOUDINARY_UPLOAD_PRESET)
                .option("folder", BuildConfig.CLOUDINARY_SOLICITACOES_FOLDER)
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
