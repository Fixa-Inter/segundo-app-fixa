package com.aula.fixa.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.activity.EdgeToEdge;
import androidx.core.view.WindowCompat;

import com.aula.fixa.R;
import com.aula.fixa.data.local.preferences.SessionStorage;
import com.aula.fixa.data.repository.AuthRepository;
import com.aula.fixa.data.remote.dto.CadastroOption;
import com.aula.fixa.ui.common.ImmersiveActivity;

public class RegisterActivity extends ImmersiveActivity {
    private AuthRepository repository;
    private ArrayAdapter<CadastroOption> institutionAdapter;
    private ArrayAdapter<CadastroOption> unitAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(false);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightNavigationBars(false);
        setContentView(R.layout.activity_register);
        repository = new AuthRepository(new SessionStorage(this));

        setupRemoteDropdowns();

        findViewById(R.id.btnContinue).setOnClickListener(view -> openDetails());
        findViewById(R.id.tvLoginAction).setOnClickListener(view -> openLogin());
    }

    private void setupRemoteDropdowns() {
        AutoCompleteTextView institution = findViewById(R.id.actvInstitution);
        AutoCompleteTextView unit = findViewById(R.id.actvUnit);
        institutionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        institution.setAdapter(institutionAdapter);
        unit.setAdapter(unitAdapter);
        configureDropdown(institution);
        configureDropdown(unit);
        new Thread(() -> {
            try {
                var values = repository.institutions();
                runOnUiThread(() -> {
                    institutionAdapter.addAll(values);
                    institutionAdapter.notifyDataSetChanged();
                });
            } catch (Exception exception) {
                runOnUiThread(() -> android.widget.Toast.makeText(this, AuthRepository.messageFrom(exception), android.widget.Toast.LENGTH_LONG).show());
            }
        }).start();
        institution.setOnItemClickListener((parent, view, position, id) -> {
            CadastroOption selected = institutionAdapter.getItem(position);
            unitAdapter.clear();
            if (selected == null) {
                return;
            }
            new Thread(() -> {
                try {
                    java.util.List<CadastroOption> values = repository.units(selected.getId());
                    runOnUiThread(() -> {
                        unitAdapter.addAll(values);
                        unitAdapter.notifyDataSetChanged();
                    });
                } catch (Exception exception) {
                    runOnUiThread(() -> android.widget.Toast.makeText(this, AuthRepository.messageFrom(exception), android.widget.Toast.LENGTH_LONG).show());
                }
            }).start();
        });
    }

    private void configureDropdown(AutoCompleteTextView field) {
        field.setThreshold(0);
        field.setKeyListener(null);
        field.setFocusable(false);
        field.setClickable(true);
        field.setOnClickListener(view -> field.showDropDown());
        field.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                view.performClick();
            }
            return true;
        });
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
        if (unitAdapter == null || unitAdapter.getCount() == 0) {
            android.widget.Toast.makeText(this, "Aguarde o carregamento das unidades.", android.widget.Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(RegisterActivity.this, RegisterDetailsActivity.class);
        intent.putExtra("google_email", getIntent().getStringExtra("google_email"));
        intent.putExtra("google_name", getIntent().getStringExtra("google_name"));
        Object selectedUnit = ((AutoCompleteTextView) findViewById(R.id.actvUnit)).getText();
        boolean unitFound = false;
        if (selectedUnit != null) {
            for (int i = 0; i < unitAdapter.getCount(); i++) {
                if (unitAdapter.getItem(i).toString().equals(selectedUnit.toString())) {
                    intent.putExtra("unit_id", unitAdapter.getItem(i).getId());
                    unitFound = true;
                    break;
                }
            }
        }
        if (!unitFound) {
            android.widget.Toast.makeText(this, "Selecione uma unidade.", android.widget.Toast.LENGTH_LONG).show();
            return;
        }
        startActivity(intent);
    }

    private void openLogin() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
