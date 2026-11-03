package com.aula.fixa.infrastructure.remote.notifications;

import android.content.Context;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.content.pm.PackageManager;
import android.util.Log;
import androidx.core.app.NotificationManagerCompat;

import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.dto.TokenDispositivoRequest;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class FcmTokenRegistrar {
    private static final String TAG = "FcmTokenRegistrar";
    // v2 deliberately starts disabled; this also migrates installations that inherited
    // the old implicit-enabled default without an explicit user choice.
    private static final String PREFERENCES = "fcm_notifications_v2";
    private static final String ENABLED_KEY = "enabled";
    private FcmTokenRegistrar() { }

    public static void registrarTokenAtual(Context context) {
        if (!isEnabled(context)) {
            Log.i(TAG, "Registro FCM ignorado: preferência desligada");
            return;
        }
        if (!notificationsAllowed(context)) {
            Log.i(TAG, "Registro FCM ignorado: permissão ou notificações do sistema desativadas");
            return;
        }
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> registrar(context, token))
                .addOnFailureListener(error -> Log.e(TAG, "Falha ao obter token FCM", error));
    }

    public static void inicializar(Activity activity) {
        ensureNotificationChannel(activity.getApplicationContext());
        SessionStorage session = new SessionStorage(activity);
        if (!session.hasSession()) return;
        registrarTokenAtual(activity.getApplicationContext());
    }

    private static void ensureNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        android.app.NotificationManager manager = context.getSystemService(android.app.NotificationManager.class);
        if (manager == null || manager.getNotificationChannel("fixa_notifications") != null) return;
        manager.createNotificationChannel(new android.app.NotificationChannel(
                "fixa_notifications",
                context.getString(com.aula.fixa.R.string.app_notifications_channel),
                android.app.NotificationManager.IMPORTANCE_DEFAULT));
    }

    public static boolean isEnabled(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .getBoolean(ENABLED_KEY, false);
    }

    public static void setEnabled(Context context, boolean enabled) {
        context.getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
                .edit().putBoolean(ENABLED_KEY, enabled).apply();
        if (enabled) {
            registrarTokenAtual(context.getApplicationContext());
        } else {
            removerTokenAtual(new SessionStorage(context), () -> { });
        }
    }

    public static boolean notificationsAllowed(Context context) {
        if (Build.VERSION.SDK_INT >= 33
                && context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) return false;
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    public static void removerTokenAtual(SessionStorage session, Runnable onFinished) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null || !session.hasSession()) {
                onFinished.run();
                return;
            }

            ApiClient.notificacoes(session).removerDispositivo(task.getResult()).enqueue(new Callback<>() {
                @Override public void onResponse(Call<Void> call, Response<Void> response) {
                    if (!response.isSuccessful()) Log.w(TAG, "API recusou a remoção do token FCM: HTTP " + response.code());
                    onFinished.run();
                }
                @Override public void onFailure(Call<Void> call, Throwable error) {
                    Log.e(TAG, "Falha ao remover token FCM da API", error);
                    onFinished.run();
                }
            });

        });
    }

    public static void registrar(Context context, String token) {
        if (token == null || token.isBlank() || !isEnabled(context) || !notificationsAllowed(context)) return;

        SessionStorage session = new SessionStorage(context);
        if (!session.hasSession()) {
            Log.w(TAG, "Token FCM não registrado: sessão ausente");
            return;
        }

        ApiClient.notificacoes(session).registrarDispositivo(new TokenDispositivoRequest(token))
                .enqueue(new Callback<>() {
                    @Override public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) Log.d(TAG, "Token FCM registrado na API");
                        else Log.w(TAG, "API recusou o registro do token FCM: HTTP " + response.code());
                    }
                    @Override public void onFailure(Call<Void> call, Throwable error) {
                        Log.e(TAG, "Falha ao registrar token FCM na API", error);
                    }
                });
    }
}
