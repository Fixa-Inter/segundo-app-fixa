package com.aula.fixa.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.activity.EdgeToEdge;
import androidx.core.view.WindowCompat;

import com.aula.fixa.R;
import com.aula.fixa.ui.common.ImmersiveActivity;

public class RegisterActivity extends ImmersiveActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(false);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightNavigationBars(false);
        setContentView(R.layout.activity_register);

        setupDropdown(R.id.actvInstitution, R.array.register_test_institutions);
        setupDropdown(R.id.actvUnit, R.array.register_test_units);

        findViewById(R.id.btnContinue).setOnClickListener(view -> openDetails());
        findViewById(R.id.tvLoginAction).setOnClickListener(view -> openLogin());
    }

    private void setupDropdown(int fieldId, int itemsId) {
        AutoCompleteTextView field = findViewById(fieldId);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                itemsId,
                android.R.layout.simple_dropdown_item_1line
        );
        field.setAdapter(adapter);
        field.setThreshold(0);
        field.setKeyListener(null);
        field.setFocusable(false);
        field.setFocusableInTouchMode(false);
        field.setClickable(true);
        field.setOnClickListener(view -> field.showDropDown());
        field.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                view.performClick();
            }
            return true;
        });
    }

    private void openDetails() {
        Intent intent = new Intent(RegisterActivity.this, RegisterDetailsActivity.class);
        startActivity(intent);
    }

    private void openLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
