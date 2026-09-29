package com.aula.fixa.ui.newrequest;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aula.fixa.R;

public class NewRequestLocationFragment extends NewRequestStepFragment {
    public NewRequestLocationFragment() {
        super(R.layout.fragment_new_request_location, 2, R.id.action_location_to_photo, R.string.app_step_location_title);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AutoCompleteTextView field = view.findViewById(R.id.actvLocation);
        field.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1,
                new String[]{"Sala T02 - 2º andar", "Sala T07 - 2º andar", "Átrio", "Banheiro - 2º Andar"}));
        field.setOnClickListener(v -> field.showDropDown());
    }
}
