package com.aula.fixa.ui.view.auth;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.core.view.WindowCompat;

import com.aula.fixa.R;
import com.aula.fixa.adapter.auth.AutenticacaoViewModel;
import com.aula.fixa.adapter.auth.SessaoViewModel;
import com.aula.fixa.adapter.auth.SessaoViewModelFactory;
import com.aula.fixa.domain.model.SessaoUsuario;
import com.aula.fixa.ui.config.NavegacaoAcesso;
import com.aula.fixa.ui.config.ImmersiveActivity;

import java.util.Calendar;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class RegisterDetailsActivity extends ImmersiveActivity {
    private AutenticacaoViewModel autenticacao;

    private EditText birthDateField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        autenticacao = AutenticacaoViewModel.obter(this, this);
        EdgeToEdge.enable(this);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(false);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightNavigationBars(false);
        setContentView(R.layout.activity_register_details);

        EditText fullName = findViewById(R.id.etFullName);
        EditText email = findViewById(R.id.etRegisterEmail);

        String googleName = getIntent().getStringExtra("google_name");
        String googleEmail = getIntent().getStringExtra("google_email");

        if (googleName != null) {
            fullName.setText(googleName);
        }

        if (googleEmail != null) {
            email.setText(googleEmail);
            email.setEnabled(false);
        }

        birthDateField = findViewById(R.id.etBirthDate);
        birthDateField.setOnClickListener(view -> showDatePicker());

        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnCreateAccount).setOnClickListener(view -> createAccount());
        findViewById(R.id.tvLoginAction).setOnClickListener(view -> openLogin());
    }

    private void createAccount() {
        EditText name = findViewById(R.id.etFullName);
        EditText email = findViewById(R.id.etRegisterEmail);
        EditText role = findViewById(R.id.etRole);
        EditText password = findViewById(R.id.etRegisterPassword);
        EditText confirm = findViewById(R.id.etConfirmPassword);
        long unitId = getIntent().getLongExtra("unit_id", -1);

        if (name.getText().toString().trim().isEmpty() || email.getText().toString().trim().isEmpty()
                || role.getText().toString().trim().isEmpty() || password.getText().toString().isEmpty() || !password.getText().toString().equals(confirm.getText().toString())
                || unitId < 0 || birthDateField.getText().toString().isEmpty()) {
            android.widget.Toast.makeText(this, "Preencha os campos corretamente.", android.widget.Toast.LENGTH_LONG).show();
            return;
        }
        String[] parts = birthDateField.getText().toString().split("/");
        String birthDate = parts[2] + "-" + parts[1] + "-" + parts[0];

        findViewById(R.id.btnCreateAccount).setEnabled(false);
        new Thread(() -> {
            try {
                SessaoUsuario sessao = autenticacao.register(
                        name.getText().toString().trim(),
                        email.getText().toString().trim(),
                        password.getText().toString(), unitId, role.getText().toString().trim(), birthDate);
                runOnUiThread(() -> {
                    if (NavegacaoAcesso.abrir(this, sessao.tipoAcesso)) return;
                    new androidx.lifecycle.ViewModelProvider(this, new SessaoViewModelFactory(this))
                            .get(SessaoViewModel.class).encerrar();
                    findViewById(R.id.btnCreateAccount).setEnabled(true);
                    android.widget.Toast.makeText(this, R.string.app_access_role_unsupported,
                            android.widget.Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    findViewById(R.id.btnCreateAccount).setEnabled(true);
                    android.widget.Toast.makeText(this, AutenticacaoViewModel.messageFrom(e), android.widget.Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void showDatePicker() {
        Calendar initialDate = Calendar.getInstance();
        initialDate.set(2000, Calendar.JANUARY, 1);

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> birthDateField.setText(
                        String.format(
                                new Locale("pt", "BR"),
                                "%02d/%02d/%04d",
                                dayOfMonth,
                                month + 1,
                                year
                        )
                ),
                initialDate.get(Calendar.YEAR),
                initialDate.get(Calendar.MONTH),
                initialDate.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void openLogin() {
        Intent intent = new Intent(RegisterDetailsActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finishAffinity();
    }
}
