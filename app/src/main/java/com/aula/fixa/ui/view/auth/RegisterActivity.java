package com.aula.fixa.ui.view.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.view.WindowCompat;

import com.aula.fixa.R;
import com.aula.fixa.adapter.auth.AutenticacaoViewModel;
import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.ui.adapter.auth.OpcoesCadastroAdapter;
import com.aula.fixa.ui.config.ImmersiveActivity;

public class RegisterActivity extends ImmersiveActivity {
    private AutenticacaoViewModel autenticacao;
    private OpcoesCadastroAdapter institutionAdapter;
    private OpcoesCadastroAdapter unitAdapter;
    private int unitRequestGeneration;
    private long selectedUnitId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(false);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightNavigationBars(false);
        setContentView(R.layout.activity_register);

        autenticacao = AutenticacaoViewModel.obter(this, this);

        setupRemoteDropdowns();
        findViewById(R.id.btnContinue).setOnClickListener(view -> openDetails());
        findViewById(R.id.tvLoginAction).setOnClickListener(view -> openLogin());
    }

    private void setupRemoteDropdowns() {
        AutoCompleteTextView institution = findViewById(R.id.actvInstitution);
        AutoCompleteTextView unit = findViewById(R.id.actvUnit);
        institutionAdapter = new OpcoesCadastroAdapter(this);
        unitAdapter = new OpcoesCadastroAdapter(this);
        institution.setAdapter(institutionAdapter);
        unit.setAdapter(unitAdapter);
        unit.setEnabled(false);
        configureDropdown(institution);
        configureDropdown(unit);
        unit.setOnItemClickListener((parent, view, position, id) -> {
            OpcaoCadastro selected = unitAdapter.getItem(position);
            selectedUnitId = selected == null ? -1L : selected.getId();
        });

        autenticacao.institutions(new AutenticacaoViewModel.Listener<java.util.List<OpcaoCadastro>>() {
            @Override public void onResultado(java.util.List<OpcaoCadastro> opcoes) {
                institutionAdapter.substituir(opcoes);
            }
            @Override public void onErro(Throwable erro) {
                Toast.makeText(RegisterActivity.this, AutenticacaoViewModel.messageFrom(erro), Toast.LENGTH_LONG).show();
            }
        });

        institution.setOnItemClickListener((parent, view, position, id) -> {
            OpcaoCadastro selecionada = institutionAdapter.getItem(position);
            int requestGeneration = ++unitRequestGeneration;
            selectedUnitId = -1L;
            unitAdapter.clear();
            unit.setText("", false);
            unit.setEnabled(false);
            if (selecionada == null) return;

            autenticacao.units(selecionada.getId(), new AutenticacaoViewModel.Listener<java.util.List<OpcaoCadastro>>() {
                @Override public void onResultado(java.util.List<OpcaoCadastro> opcoes) {
                    if (requestGeneration != unitRequestGeneration) return;
                    unitAdapter.substituir(opcoes);
                    unit.setEnabled(true);
                }
                @Override public void onErro(Throwable erro) {
                    if (requestGeneration != unitRequestGeneration) return;
                    unit.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, AutenticacaoViewModel.messageFrom(erro), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void configureDropdown(AutoCompleteTextView field) {
        field.setDropDownBackgroundResource(R.drawable.bg_app_popup_menu);
        field.setThreshold(0);
        field.setKeyListener(null);
        field.setFocusable(false);
        field.setClickable(true);
        field.setOnClickListener(view -> field.showDropDown());
        field.setOnTouchListener((view, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) view.performClick();
            return true;
        });
    }

    private void openDetails() {
        AutoCompleteTextView selectedUnitField = findViewById(R.id.actvUnit);
        if (selectedUnitId <= 0 || selectedUnitField.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.register_unit_required, Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, RegisterDetailsActivity.class)
                .putExtra("google_email", getIntent().getStringExtra("google_email"))
                .putExtra("google_name", getIntent().getStringExtra("google_name"))
                .putExtra("unit_id", selectedUnitId);
        startActivity(intent);
    }

    private void openLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
