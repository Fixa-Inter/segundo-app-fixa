package com.aula.fixa.ui.view.notifications;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.aula.fixa.infrastructure.remote.notifications.FcmTokenRegistrar;

/** Keeps the in-app notification switch synchronized with Android permission and FCM registration. */
public final class NotificationSettingsController {
    private static final String TAG = "NotificationSettings";
    private final Fragment fragment;
    private final CompoundButton toggle;
    private final ActivityResultLauncher<String> permissionRequest;
    private boolean updating;
    private boolean waitingForSystemSettings;

    public NotificationSettingsController(@NonNull Fragment fragment, @NonNull View toggleView) {
        this.fragment = fragment;
        this.toggle = (CompoundButton) toggleView;
        permissionRequest = fragment.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), granted -> {
                    Log.i(TAG, "Resultado de POST_NOTIFICATIONS: " + granted);
                    if (granted && FcmTokenRegistrar.notificationsAllowed(fragment.requireContext())) {
                        FcmTokenRegistrar.setEnabled(fragment.requireContext(), true);
                    } else {
                        FcmTokenRegistrar.setEnabled(fragment.requireContext(), false);
                        Toast.makeText(fragment.requireContext(),
                                "Permita as notificações nas configurações do aplicativo.", Toast.LENGTH_LONG).show();
                    }
                    refresh();
                });
        toggle.setOnCheckedChangeListener((button, checked) -> {
            if (updating) return;
            Log.i(TAG, "Switch alterado para " + checked + ", SDK=" + Build.VERSION.SDK_INT
                    + ", permitido=" + FcmTokenRegistrar.notificationsAllowed(fragment.requireContext()));
            if (!checked) {
                FcmTokenRegistrar.setEnabled(fragment.requireContext(), false);
                return;
            }
            enableNotifications();
        });
        refresh();
    }

    public void refresh() {
        if (!fragment.isAdded() || fragment.getContext() == null) return;
        boolean enabled = FcmTokenRegistrar.isEnabled(fragment.requireContext())
                && FcmTokenRegistrar.notificationsAllowed(fragment.requireContext());
        updating = true;
        toggle.setChecked(enabled);
        updating = false;
    }

    public void onResume() {
        if (!fragment.isAdded()) return;
        if (waitingForSystemSettings) {
            waitingForSystemSettings = false;
            if (FcmTokenRegistrar.notificationsAllowed(fragment.requireContext())) {
                FcmTokenRegistrar.setEnabled(fragment.requireContext(), true);
            }
        }
        refresh();
    }

    private void enableNotifications() {
        if (Build.VERSION.SDK_INT >= 33
                && fragment.requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Solicitando permissão POST_NOTIFICATIONS");
            permissionRequest.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            return;
        }
        if (!FcmTokenRegistrar.notificationsAllowed(fragment.requireContext())) {
            waitingForSystemSettings = true;
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, fragment.requireContext().getPackageName());
            fragment.startActivity(intent);
            return;
        }
        FcmTokenRegistrar.setEnabled(fragment.requireContext(), true);
    }
}
