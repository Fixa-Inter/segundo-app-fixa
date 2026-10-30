package com.aula.fixa.ui.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.aula.fixa.R;
import com.aula.fixa.adapter.auth.AutenticacaoViewModel;
import com.aula.fixa.ui.config.AppText;

public class ResetPasswordActivity extends RecoveryActivity {
    private AutenticacaoViewModel autenticacao;

    public ResetPasswordActivity() {
        super(R.layout.activity_reset_password);
    }

    public static Intent intent(android.content.Context context, String email, int code, boolean profileFlow) {
        return new Intent(context, ResetPasswordActivity.class)
                .putExtra(EXTRA_EMAIL, email)
                .putExtra("recovery_code", code)
                .putExtra(EXTRA_PROFILE_FLOW, profileFlow);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        autenticacao = AutenticacaoViewModel.obter(this, this);
        AppText.setTwoTone(findViewById(R.id.tvTitle), R.string.recovery_reset_title_lead,
                R.string.recovery_reset_title_rest, R.color.app_primary_dark);

        findViewById(R.id.btnConfirm).setOnClickListener(view -> resetPassword());
    }

    private void resetPassword() {
        String password = ((EditText) findViewById(R.id.etPassword)).getText().toString();
        String confirmation = ((EditText) findViewById(R.id.etConfirmPassword)).getText().toString();
        if (password.trim().isEmpty() || confirmation.trim().isEmpty()) {
            Toast.makeText(this, R.string.recovery_password_required, Toast.LENGTH_LONG).show();
            return;
        }
        if (!password.equals(confirmation)) {
            Toast.makeText(this, R.string.recovery_password_mismatch, Toast.LENGTH_LONG).show();
            return;
        }
        String email = getIntent().getStringExtra(EXTRA_EMAIL);
        int code = getIntent().getIntExtra("recovery_code", -1);
        if (email == null || code < 0) {
            Toast.makeText(this, R.string.recovery_code_invalid, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        findViewById(R.id.btnConfirm).setEnabled(false);
        new Thread(() -> {
            try {
                autenticacao.resetPassword(email, code, password);
                runOnUiThread(() -> {
                    startActivity(PasswordSuccessActivity.intent(this,
                            getIntent().getBooleanExtra(EXTRA_PROFILE_FLOW, false)));
                    finish();
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    findViewById(R.id.btnConfirm).setEnabled(true);
                    Toast.makeText(this, AutenticacaoViewModel.messageFrom(error), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
