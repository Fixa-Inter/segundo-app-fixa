package com.aula.fixa.ui.auth;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aula.fixa.R;
import com.aula.fixa.ui.common.ImmersiveActivity;

/**
 * Base das telas de recuperação de senha (fundo claro): ícones escuros nas barras do sistema,
 * conteúdo respeitando status bar/recorte e botão voltar padrão.
 */
public abstract class RecoveryActivity extends ImmersiveActivity {

    @LayoutRes
    private final int layoutRes;

    protected RecoveryActivity(@LayoutRes int layoutRes) {
        this.layoutRes = layoutRes;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightNavigationBars(true);
        setContentView(layoutRes);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recoveryRoot), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        View back = findViewById(R.id.btnBack);
        if (back != null) back.setOnClickListener(v -> finish());
    }
}
