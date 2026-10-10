package com.aula.fixa.ui.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aula.fixa.R;

/** Tela "Sem conexão": fecha sozinha quando a internet volta, devolvendo o usuário à tela anterior. */
public class OfflineActivity extends ImmersiveActivity {

    private ConnectivityManager connectivity;
    private final ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            runOnUiThread(() -> {
                if (ConnectivityWatcher.isOnline(OfflineActivity.this)) finish();
            });
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(true);
        setContentView(R.layout.activity_offline);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recoveryRoot), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        connectivity.registerDefaultNetworkCallback(callback);
        if (ConnectivityWatcher.isOnline(this)) finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        connectivity.unregisterNetworkCallback(callback);
    }
}
