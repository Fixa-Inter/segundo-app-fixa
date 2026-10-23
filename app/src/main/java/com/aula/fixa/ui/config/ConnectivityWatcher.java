package com.aula.fixa.ui.config;

import com.aula.fixa.ui.view.offline.OfflineActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

/**
 * Abre a tela "Sem conexão" (Figma 5178:9529) quando a internet cai enquanto a Activity está visível.
 * Uso: {@code getLifecycle().addObserver(new ConnectivityWatcher(this));}
 */
public class ConnectivityWatcher implements DefaultLifecycleObserver {

    private final Activity activity;
    private final ConnectivityManager connectivity;
    private final ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onLost(@NonNull Network network) {
            activity.runOnUiThread(ConnectivityWatcher.this::showIfOffline);
        }
    };

    public ConnectivityWatcher(@NonNull Activity activity) {
        this.activity = activity;
        this.connectivity = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static boolean isOnline(@NonNull Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        connectivity.registerDefaultNetworkCallback(callback);
        showIfOffline();
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        connectivity.unregisterNetworkCallback(callback);
    }

    private void showIfOffline() {
        if (!activity.isFinishing() && !isOnline(activity)) {
            activity.startActivity(new Intent(activity, OfflineActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
        }
    }
}
