package com.aula.fixa.ui.newrequest;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aula.fixa.R;

public class NewRequestPhotoFragment extends NewRequestStepFragment {
    public NewRequestPhotoFragment() {
        super(R.layout.fragment_new_request_photo, 3, R.id.action_photo_to_confirm, R.string.app_step_photo_title);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AutoCompleteTextView field = view.findViewById(R.id.actvDevice);
        field.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1,
                new String[]{"Projetor", "Ar-condicionado", "Lâmpada", "Torneira"}));
        field.setOnClickListener(v -> field.showDropDown());
    }
}
