package com.aula.fixa.ui.newrequest;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aula.fixa.R;

public class NewRequestDescriptionFragment extends NewRequestStepFragment {
    public NewRequestDescriptionFragment() {
        super(R.layout.fragment_new_request_description, 1, R.id.action_description_to_location, R.string.app_step_description_title);
    }

    @Override
    protected int currentDestinationId() {
        return R.id.newRequestDescriptionFragment;
    }

    @Override
    protected void restoreForm(View root) {
        ((EditText) root.findViewById(R.id.etTitle)).setText(form.title);
        ((EditText) root.findViewById(R.id.etDescription)).setText(form.description);
    }

    @Override
    protected void saveForm(View root) {
        form.title = ((EditText) root.findViewById(R.id.etTitle)).getText().toString().trim();
        form.description = ((EditText) root.findViewById(R.id.etDescription)).getText().toString().trim();
    }

    @Override
    protected boolean validateStep(View root) {
        if (!hasText(form.title)) {
            showValidationMessage("Informe o título da solicitação.");
            return false;
        }
        if (!hasText(form.description)) {
            showValidationMessage("Informe a descrição da solicitação.");
            return false;
        }
        return true;
    }
}
