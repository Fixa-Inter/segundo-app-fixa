package com.aula.fixa.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;

import com.aula.fixa.R;
import com.aula.fixa.MainActivity;

public class PasswordSuccessActivity extends RecoveryActivity {

    public PasswordSuccessActivity() {
        super(R.layout.activity_password_success);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean profileFlow = getIntent().getBooleanExtra(EXTRA_PROFILE_FLOW, false);
        if (profileFlow) {
            ((MaterialButton) findViewById(R.id.btnBackToLogin))
                    .setText(R.string.recovery_success_back_to_profile);
            findViewById(R.id.btnNewRequest).setVisibility(android.view.View.GONE);
        }
        findViewById(R.id.btnBackToLogin).setOnClickListener(view -> goToLogin());
        // Registrar um problema exige estar logado: leva ao login, de onde o fluxo segue para a Home.
        findViewById(R.id.btnNewRequest).setOnClickListener(view -> goToLogin());
    }

    private void goToLogin() {
        if (getIntent().getBooleanExtra(EXTRA_PROFILE_FLOW, false)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            return;
        }
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    public static Intent intent(android.content.Context context, boolean profileFlow) {
        return new Intent(context, PasswordSuccessActivity.class)
                .putExtra(EXTRA_PROFILE_FLOW, profileFlow);
    }
}
