package com.aula.fixa.infrastructure.remote.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.aula.fixa.R;
import com.aula.fixa.ui.view.splash.SplashActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public final class FixaMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "fixa_notifications";

    @Override public void onNewToken(String token) {
        FcmTokenRegistrar.registrar(getApplicationContext(), token);
    }

    @Override public void onMessageReceived(RemoteMessage message) {
        if (!FcmTokenRegistrar.isEnabled(getApplicationContext())
                || !FcmTokenRegistrar.notificationsAllowed(getApplicationContext())) return;
        com.aula.fixa.ui.view.notifications.NotificationBadge.refreshActive(getApplicationContext());

        String title = message.getNotification() == null || message.getNotification().getTitle() == null
                ? getString(R.string.app_name) : message.getNotification().getTitle();

        String body = message.getNotification() == null ? "" : message.getNotification().getBody();
        if (body == null || body.isBlank()) body = message.getData().getOrDefault("mensagem", "Você tem uma nova notificação.");

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new android.app.NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.app_notifications_channel),
                    NotificationManager.IMPORTANCE_DEFAULT
            ));
        }

        Intent launch = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (launch == null) launch = new Intent(this, SplashActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        String type = message.getData().get("tipoNotificacao");
        String referenceId = message.getData().get("referenciaId");
        String notificationId = message.getData().get("notificacaoId");
        if (type != null) launch.putExtra("notification_type", type);
        if (referenceId != null) launch.putExtra("notification_reference_id", referenceId);
        if (notificationId != null) launch.putExtra("notification_id", notificationId);

        PendingIntent pending = PendingIntent.getActivity(
                this,
                0,
                launch,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        int id = message.getData().getOrDefault("notificacaoId", String.valueOf(System.currentTimeMillis())).hashCode();
        androidx.core.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_bell_24)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pending)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat.from(this).notify(id, builder.build());
    }
}
