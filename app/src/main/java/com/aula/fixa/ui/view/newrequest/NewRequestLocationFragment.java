package com.aula.fixa.ui.view.newrequest;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.aula.fixa.R;
import com.aula.fixa.ui.config.FragmentUi;
import com.aula.fixa.adapter.cadastro.CadastroViewModel;
import com.aula.fixa.adapter.cadastro.CadastroViewModelFactory;
import com.aula.fixa.domain.model.OpcaoCadastro;

import java.util.List;

public class NewRequestLocationFragment extends NewRequestStepFragment {
    public NewRequestLocationFragment() {
        super(R.layout.fragment_new_request_location, 2, R.id.action_location_to_photo, R.string.app_step_location_title);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CadastroViewModel viewModel = new ViewModelProvider(this,
                new CadastroViewModelFactory(requireContext())).get(CadastroViewModel.class);
        AutoCompleteTextView field = view.findViewById(R.id.actvLocation);
        viewModel.listarLocais(new CadastroViewModel.Callback<List<OpcaoCadastro>>() {
            @Override public void aoConcluir(List<OpcaoCadastro> options) {
                FragmentUi.runIfViewActive(NewRequestLocationFragment.this, view, () -> {
                    field.setAdapter(new ArrayAdapter<>(view.getContext(), R.layout.item_app_dropdown, options));
                    field.setOnItemClickListener((p, v, pos, id) -> form.localId = options.get(pos).getId());
                });
            }
            @Override public void aoFalhar(Throwable error) {
                FragmentUi.runIfViewActive(NewRequestLocationFragment.this, view,
                        () -> android.widget.Toast.makeText(view.getContext(), error.getMessage(), android.widget.Toast.LENGTH_LONG).show());
            }
        });

        field.setOnClickListener(v -> field.showDropDown());
        field.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                field.showDropDown();
            }
        });
    }

    @Override
    protected int currentDestinationId() {
        return R.id.newRequestLocationFragment;
    }

    @Override
    protected int operationsDestinationId() {
        return R.id.ops_new_request_location;
    }

    @Override
    protected void restoreForm(View root) {
        ((AutoCompleteTextView) root.findViewById(R.id.actvLocation)).setText(form.location, false);
        ((EditText) root.findViewById(R.id.etLocationDescription)).setText(form.locationDescription);
    }

    @Override
    protected void saveForm(View root) {
        form.location = ((AutoCompleteTextView) root.findViewById(R.id.actvLocation)).getText().toString().trim();
        form.locationDescription = ((EditText) root.findViewById(R.id.etLocationDescription)).getText().toString().trim();
    }

    @Override
    protected boolean validateStep(View root) {
        if (form.localId <= 0 || !hasText(form.location)) {
            showValidationMessage("Selecione o local da unidade.");
            return false;
        }
        return true;
    }
}
