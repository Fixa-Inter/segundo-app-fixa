package com.aula.fixa.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.aula.fixa.R;
import com.aula.fixa.ui.common.AppText;

public class ResetPasswordActivity extends RecoveryActivity {

    public ResetPasswordActivity() {
        super(R.layout.activity_reset_password);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppText.setTwoTone(findViewById(R.id.tvTitle), R.string.recovery_reset_title_lead,
                R.string.recovery_reset_title_rest, R.color.app_primary_dark);

        findViewById(R.id.btnConfirm).setOnClickListener(view ->
                startActivity(new Intent(this, PasswordSuccessActivity.class)));
    }
}
