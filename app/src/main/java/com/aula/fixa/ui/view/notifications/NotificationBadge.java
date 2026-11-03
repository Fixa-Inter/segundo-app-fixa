package com.aula.fixa.ui.view.notifications;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.dto.NotificacaoResponse;

import java.util.List;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class NotificationBadge {
    private static final List<WeakReference<View>> activeBadges = new ArrayList<>();
    private NotificationBadge() { }

    public static void load(Context context, View badge) {
        if (badge == null) return;
        synchronized (activeBadges) {
            activeBadges.removeIf(reference -> reference.get() == null || reference.get() == badge);
            activeBadges.add(new WeakReference<>(badge));
        }
        badge.setVisibility(View.GONE);
        SessionStorage session = new SessionStorage(context);
        if (!session.hasSession()) return;

        ApiClient.notificacoes(session).listar().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<NotificacaoResponse>> call,
                                   @NonNull Response<List<NotificacaoResponse>> response) {
                if (!response.isSuccessful() || response.body() == null) return;
                boolean hasUnread = response.body().stream().anyMatch(item -> !item.visualizada);
                badge.post(() -> badge.setVisibility(hasUnread ? View.VISIBLE : View.GONE));
            }

            @Override
            public void onFailure(@NonNull Call<List<NotificacaoResponse>> call, @NonNull Throwable error) {
                badge.post(() -> badge.setVisibility(View.GONE));
            }
        });
    }

    public static void refreshActive(Context context) {
        List<View> views = new ArrayList<>();
        synchronized (activeBadges) {
            activeBadges.removeIf(reference -> reference.get() == null);
            for (WeakReference<View> reference : activeBadges) {
                View view = reference.get();
                if (view != null && view.isAttachedToWindow()) views.add(view);
            }
        }
        for (View view : views) load(context, view);
    }
}
