package com.aula.fixa.ui.newrequest;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aula.fixa.R;
import com.aula.fixa.data.local.preferences.SessionStorage;
import com.aula.fixa.data.repository.RequestRepository;
import com.aula.fixa.data.remote.dto.CadastroOption;

import java.util.List;

public class NewRequestLocationFragment extends NewRequestStepFragment {
    public NewRequestLocationFragment() {
        super(R.layout.fragment_new_request_location, 2, R.id.action_location_to_photo, R.string.app_step_location_title);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AutoCompleteTextView field = view.findViewById(R.id.actvLocation);
        new Thread(() -> {
            try {
                List<CadastroOption> options = new RequestRepository(new SessionStorage(requireContext())).options(new RequestRepository(new SessionStorage(requireContext())).locais());
                requireActivity().runOnUiThread(() -> {
                    field.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.item_app_dropdown, options));
                    field.setOnItemClickListener((p, v, pos, id) -> form.localId = options.get(pos).getId());
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> android.widget.Toast.makeText(requireContext(), e.getMessage(), android.widget.Toast.LENGTH_LONG).show());
            }
        }).start();

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
    protected void restoreForm(View root) {
        ((AutoCompleteTextView) root.findViewById(R.id.actvLocation)).setText(form.location, false);
        ((EditText) root.findViewById(R.id.etLocationDescription)).setText(form.locationDescription);
    }

    @Override
    protected void saveForm(View root) {
        form.location = ((AutoCompleteTextView) root.findViewById(R.id.actvLocation)).getText().toString().trim();
        form.locationDescription = ((EditText) root.findViewById(R.id.etLocationDescription)).getText().toString().trim();
    }
}
