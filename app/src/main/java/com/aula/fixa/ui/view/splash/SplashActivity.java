package com.aula.fixa.ui.view.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.aula.fixa.R;
import com.bumptech.glide.Glide;
import com.aula.fixa.adapter.auth.SessaoViewModel;
import com.aula.fixa.adapter.auth.SessaoViewModelFactory;
import com.aula.fixa.ui.config.NavegacaoAcesso;
import com.aula.fixa.ui.config.ImmersiveActivity;
import com.aula.fixa.ui.view.onboarding.OnboardingActivity;

public class SplashActivity extends ImmersiveActivity {

    private static final long SPLASH_DURATION_MILLIS = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ImageView logo = findViewById(R.id.ivLogoFixa);
        Glide.with(this)
                .asGif()
                .load(R.drawable.logo_fixa_splash_animado)
                .into(logo);
        SessaoViewModel sessaoViewModel = new ViewModelProvider(this,
                new SessaoViewModelFactory(this)).get(SessaoViewModel.class);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (sessaoViewModel.temSessao() && sessaoViewModel.tokenValido()
                    && NavegacaoAcesso.abrir(this, sessaoViewModel.tipoAcesso())) return;
            sessaoViewModel.encerrar();
            Intent intent = new Intent(SplashActivity.this, OnboardingActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DURATION_MILLIS);
    }
}
