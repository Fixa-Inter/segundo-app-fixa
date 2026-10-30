package com.aula.fixa.ui.view.newrequest;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.aula.fixa.R;

public class NewRequestSuccessFragment extends Fragment {

    public NewRequestSuccessFragment() {
        super(R.layout.fragment_new_request_success);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnGoHome).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_success_to_home));
        view.findViewById(R.id.btnNewRequest).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_success_to_new_request));
    }
}
