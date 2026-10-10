package com.aula.fixa.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.aula.fixa.R;
import com.aula.fixa.ui.common.AppText;

public class ForgotPasswordActivity extends RecoveryActivity {

    public ForgotPasswordActivity() {
        super(R.layout.activity_forgot_password);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppText.setTwoTone(findViewById(R.id.tvTitle), R.string.recovery_forgot_title_lead,
                R.string.recovery_forgot_title_rest, R.color.app_primary_dark);

        EditText email = findViewById(R.id.etEmail);
        findViewById(R.id.btnSend).setOnClickListener(view ->
                startActivity(VerifyEmailActivity.intent(this, email.getText().toString().trim())));
        findViewById(R.id.btnBackToLogin).setOnClickListener(view -> finish());
    }
}
