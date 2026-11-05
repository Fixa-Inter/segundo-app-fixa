package com.aula.fixa.ui.view.auth;

import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.aula.fixa.R;
import com.aula.fixa.adapter.auth.AutenticacaoViewModel;
import com.aula.fixa.ui.config.AppText;

import android.widget.Toast;

public class ForgotPasswordActivity extends RecoveryActivity {
    private AutenticacaoViewModel autenticacao;

    public ForgotPasswordActivity() {
        super(R.layout.activity_forgot_password);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        autenticacao = AutenticacaoViewModel.obter(this, this);
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

            autenticacao.requestPasswordRecovery(value,
                    new AutenticacaoViewModel.Listener<com.aula.fixa.domain.model.ResultadoRecuperacaoSenha>() {
                @Override public void onResultado(com.aula.fixa.domain.model.ResultadoRecuperacaoSenha resultado) {
                    startActivity(VerifyEmailActivity.intent(ForgotPasswordActivity.this, value, false));
                    finish();
                }
                @Override public void onErro(Throwable erro) {
                    findViewById(R.id.btnSend).setEnabled(true);
                    Toast.makeText(ForgotPasswordActivity.this,
                            AutenticacaoViewModel.messageFrom(erro), Toast.LENGTH_LONG).show();
                }
            });
        });

        findViewById(R.id.btnBackToLogin).setOnClickListener(view -> finish());
    }
}
