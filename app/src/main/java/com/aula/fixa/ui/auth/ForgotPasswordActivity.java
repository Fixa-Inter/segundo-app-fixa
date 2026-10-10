package com.aula.fixa.ui.auth;

import android.content.Intent;
import android.os.Bundle;
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

        findViewById(R.id.btnSend).setOnClickListener(view ->
                startActivity(new Intent(this, VerifyEmailActivity.class)));
        findViewById(R.id.btnBackToLogin).setOnClickListener(view -> finish());
    }
}
