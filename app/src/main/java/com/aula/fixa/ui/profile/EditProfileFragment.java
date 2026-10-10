package com.aula.fixa.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.aula.fixa.R;
import com.aula.fixa.ui.common.AppDialogs;

public class EditProfileFragment extends Fragment {

    private EditText name, birth, email, phone, password;
    private String initial;

    public EditProfileFragment() {
        super(R.layout.fragment_edit_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) view.findViewById(R.id.tvPageTitle)).setText(R.string.app_edit_title);

        name = view.findViewById(R.id.etName);
        birth = view.findViewById(R.id.etBirth);
        email = view.findViewById(R.id.etEmail);
        phone = view.findViewById(R.id.etPhone);
        password = view.findViewById(R.id.etPassword);
        name.setText("Ryan Cursino de Moraes");
        birth.setText("26/11/2002");
        email.setText("ryan.cursino@gmail.com");
        phone.setText("+(55) 11 123456789");
        password.setText("*************");
        initial = snapshot();

        view.findViewById(R.id.btnBack).setOnClickListener(v -> leave());
        view.findViewById(R.id.btnSave).setOnClickListener(v ->
                AppDialogs.showSaveConfirm(requireContext(), () -> Navigation.findNavController(view).navigateUp()));

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                leave();
            }
        });
    }

    /** Sem alterações sai direto; com alterações pergunta se salva (Figma "Salvar alterações?"). */
    private void leave() {
        Runnable exit = () -> Navigation.findNavController(requireView()).navigateUp();
        if (initial.equals(snapshot())) {
            exit.run();
        } else {
            AppDialogs.showDiscardConfirm(requireContext(),
                    () -> AppDialogs.showSaveConfirm(requireContext(), exit), exit);
        }
    }

    private String snapshot() {
        return name.getText() + "\u0000" + birth.getText() + "\u0000" + email.getText()
                + "\u0000" + phone.getText() + "\u0000" + password.getText();
    }
}
