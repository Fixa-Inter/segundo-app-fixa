package com.aula.fixa.ui.view.auth;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.view.WindowCompat;

import com.aula.fixa.R;
import com.aula.fixa.adapter.auth.AutenticacaoViewModel;
import com.aula.fixa.adapter.auth.SessaoViewModel;
import com.aula.fixa.adapter.auth.SessaoViewModelFactory;
import com.aula.fixa.domain.model.SessaoUsuario;
import com.aula.fixa.ui.config.ImmersiveActivity;
import com.aula.fixa.ui.config.NavegacaoAcesso;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Calendar;
import java.util.Locale;

public class RegisterDetailsActivity extends ImmersiveActivity {
    private static final DateTimeFormatter INPUT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT);

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
        if (googleName != null) fullName.setText(googleName);
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

        if (name.getText().toString().trim().isEmpty()
                || role.getText().toString().trim().isEmpty()
                || password.getText().toString().isEmpty()
                || confirm.getText().toString().isEmpty()) {
            showValidationError(R.string.register_fields_required);
            return;
        }
        String emailValue = email.getText().toString().trim();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            showValidationError(R.string.register_email_invalid);
            return;
        }
        if (password.getText().length() < 4) {
            showValidationError(R.string.register_password_minimum_length);
            return;
        }
        if (!password.getText().toString().equals(confirm.getText().toString())) {
            showValidationError(R.string.register_password_mismatch);
            return;
        }
        if (unitId <= 0) {
            showValidationError(R.string.register_unit_required);
            return;
        }

        final LocalDate birthDate;
        try {
            birthDate = LocalDate.parse(birthDateField.getText().toString().trim(), INPUT_DATE_FORMAT);
        } catch (DateTimeParseException error) {
            showValidationError(R.string.register_birth_date_invalid);
            return;
        }
        if (birthDate.isAfter(LocalDate.now().minusYears(12))) {
            showValidationError(R.string.register_minimum_age);
            return;
        }

        findViewById(R.id.btnCreateAccount).setEnabled(false);
        autenticacao.register(name.getText().toString().trim(), emailValue, password.getText().toString(),
                unitId, role.getText().toString().trim(), birthDate.toString(),
                new AutenticacaoViewModel.Listener<SessaoUsuario>() {
                    @Override public void onResultado(SessaoUsuario sessao) {
                        if (NavegacaoAcesso.abrir(RegisterDetailsActivity.this, sessao.tipoAcesso)) return;
                        new androidx.lifecycle.ViewModelProvider(RegisterDetailsActivity.this,
                                new SessaoViewModelFactory(RegisterDetailsActivity.this))
                                .get(SessaoViewModel.class).encerrar();
                        findViewById(R.id.btnCreateAccount).setEnabled(true);
                        Toast.makeText(RegisterDetailsActivity.this, R.string.app_access_role_unsupported,
                                Toast.LENGTH_LONG).show();
                    }

                    @Override public void onErro(Throwable erro) {
                        findViewById(R.id.btnCreateAccount).setEnabled(true);
                        Toast.makeText(RegisterDetailsActivity.this,
                                AutenticacaoViewModel.messageFrom(erro), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showValidationError(int messageId) {
        Toast.makeText(this, messageId, Toast.LENGTH_LONG).show();
    }

    private void showDatePicker() {
        Calendar latestAllowedDate = Calendar.getInstance();
        latestAllowedDate.add(Calendar.YEAR, -12);
        int year = latestAllowedDate.get(Calendar.YEAR);
        int month = latestAllowedDate.get(Calendar.MONTH);
        int day = latestAllowedDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> birthDateField.setText(
                        String.format(Locale.forLanguageTag("pt-BR"), "%02d/%02d/%04d",
                                selectedDay, selectedMonth + 1, selectedYear)),
                year, month, day);
        dialog.getDatePicker().setMaxDate(latestAllowedDate.getTimeInMillis());
        dialog.show();
    }

    private void openLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finishAffinity();
    }
}
