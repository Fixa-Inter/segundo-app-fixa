package com.aula.fixa.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.aula.fixa.R;
import com.aula.fixa.data.local.preferences.SessionStorage;
import com.aula.fixa.data.repository.AuthRepository;
import com.aula.fixa.ui.common.AppText;
import android.widget.Toast;

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
        email.setText(getIntent().getStringExtra(EXTRA_EMAIL));
        findViewById(R.id.btnSend).setOnClickListener(view -> {
            String value = email.getText().toString().trim();
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
                Toast.makeText(this, R.string.recovery_email_invalid, Toast.LENGTH_SHORT).show();
                return;
            }
            findViewById(R.id.btnSend).setEnabled(false);
            new Thread(() -> {
                try {
                    new AuthRepository(new SessionStorage(this)).requestPasswordRecovery(value);
                    runOnUiThread(() -> {
                        startActivity(VerifyEmailActivity.intent(this, value, false));
                        finish();
                    });
                } catch (Exception exception) {
                    runOnUiThread(() -> {
                        findViewById(R.id.btnSend).setEnabled(true);
                        Toast.makeText(this, AuthRepository.messageFrom(exception), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        });
        findViewById(R.id.btnBackToLogin).setOnClickListener(view -> finish());
    }
}
