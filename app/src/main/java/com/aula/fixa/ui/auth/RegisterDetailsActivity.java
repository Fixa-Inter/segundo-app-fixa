package com.aula.fixa.ui.auth;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.core.view.WindowCompat;

import com.aula.fixa.R;
import com.aula.fixa.ui.common.ImmersiveActivity;

import java.util.Calendar;
import java.util.Locale;

public class RegisterDetailsActivity extends ImmersiveActivity {

    private EditText birthDateField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(false);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightNavigationBars(false);
        setContentView(R.layout.activity_register_details);

        birthDateField = findViewById(R.id.etBirthDate);
        birthDateField.setOnClickListener(view -> showDatePicker());

        findViewById(R.id.btnBack).setOnClickListener(view -> finish());
        findViewById(R.id.btnCreateAccount).setOnClickListener(view -> openLogin());
        findViewById(R.id.tvLoginAction).setOnClickListener(view -> openLogin());
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
