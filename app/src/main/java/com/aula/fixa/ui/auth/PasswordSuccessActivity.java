package com.aula.fixa.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.aula.fixa.R;

public class PasswordSuccessActivity extends RecoveryActivity {

    public PasswordSuccessActivity() {
        super(R.layout.activity_password_success);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.id.btnBackToLogin).setOnClickListener(view -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finishAffinity();
        });
    }
}
