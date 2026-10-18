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
import com.aula.fixa.data.local.preferences.SessionStorage;
import com.aula.fixa.data.repository.RequestRepository;
import com.google.gson.JsonObject;
import com.bumptech.glide.Glide;
import android.widget.ImageView;

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
        loadProfile(view);
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

    private void loadProfile(View view) {
        new Thread(() -> { try {
            JsonObject profile = new RequestRepository(new SessionStorage(requireContext())).perfil();
            requireActivity().runOnUiThread(() -> {
                name.setText(value(profile, "nomeCompleto")); birth.setText(value(profile, "dataNascimento")); email.setText(value(profile, "email")); phone.setText(value(profile, "cargo"));
                String url = value(profile, "urlFotoPerfil"); Glide.with(this).load(url.isEmpty() ? null : url).placeholder(R.drawable.img_avatar_115).error(R.drawable.img_avatar_115).circleCrop().into((ImageView) view.findViewById(R.id.ivAvatar)); initial = snapshot();
            });
        } catch (Exception e) { requireActivity().runOnUiThread(() -> { name.setText(""); birth.setText(""); email.setText(""); phone.setText(""); initial = snapshot(); }); } }).start();
    }

    private String value(JsonObject profile, String key) { return profile.has(key) && !profile.get(key).isJsonNull() ? profile.get(key).getAsString() : ""; }

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
