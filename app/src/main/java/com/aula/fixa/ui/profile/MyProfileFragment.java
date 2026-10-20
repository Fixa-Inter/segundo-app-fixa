package com.aula.fixa.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.aula.fixa.R;
import com.aula.fixa.data.local.preferences.SessionStorage;
import com.aula.fixa.data.repository.RequestRepository;
import com.google.gson.JsonObject;
import com.bumptech.glide.Glide;

public class MyProfileFragment extends Fragment {

    public MyProfileFragment() {
        super(R.layout.fragment_my_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) view.findViewById(R.id.tvPageTitle)).setText(R.string.app_my_profile_title);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        view.findViewById(R.id.btnEditInfo).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_my_profile_to_edit));

        loadProfile(view);

        bindRow(view, R.id.rowName, R.drawable.ic_user_24, "");
        bindRow(view, R.id.rowBirth, R.drawable.ic_calendar_24, "");
        bindRow(view, R.id.rowEmail, R.drawable.ic_mail_24, "");
        bindRow(view, R.id.rowPhone, R.drawable.ic_phone_24, "");
        bindRow(view, R.id.rowPassword, R.drawable.ic_lock_24, "*************");
        bindRow(view, R.id.rowAddress, R.drawable.ic_compass_24, "");
    }

    private void loadProfile(View view) {
        new Thread(() -> {
            try {
                JsonObject profile = new RequestRepository(new SessionStorage(requireContext())).perfil();
                String url = profile.has("urlFotoPerfil") && !profile.get("urlFotoPerfil").isJsonNull()
                        ? profile.get("urlFotoPerfil").getAsString() : "";
                requireActivity().runOnUiThread(() -> {
                    bindRow(view, R.id.rowName, R.drawable.ic_user_24, value(profile, "nomeCompleto"));
                    bindRow(view, R.id.rowBirth, R.drawable.ic_calendar_24, value(profile, "dataNascimento"));
                    bindRow(view, R.id.rowEmail, R.drawable.ic_mail_24, value(profile, "email"));
                    bindRow(view, R.id.rowPhone, R.drawable.ic_phone_24, value(profile, "cargo"));
                    bindRow(view, R.id.rowAddress, R.drawable.ic_compass_24, formatCnpj(value(profile, "cnpjUnidade")));
                    Glide.with(this)
                            .load(url.trim().isEmpty() ? null : url)
                            .placeholder(R.drawable.img_avatar_115)
                            .error(R.drawable.img_avatar_115)
                            .circleCrop()
                            .into((ImageView) view.findViewById(R.id.ivAvatar));
                });
            } catch (Exception ignored) {
                requireActivity().runOnUiThread(() -> ((ImageView) view.findViewById(R.id.ivAvatar))
                        .setImageResource(R.drawable.img_avatar_115));
            }
        }).start();
    }

    private String value(JsonObject profile, String key) {
        return profile.has(key) && !profile.get(key).isJsonNull() ? profile.get(key).getAsString() : "";
    }

    private String formatCnpj(String cnpj) {
        String digits = cnpj == null ? "" : cnpj.replaceAll("\\D", "");
        if (digits.length() != 14) return cnpj == null ? "" : cnpj;
        return digits.substring(0, 2) + "." + digits.substring(2, 5) + "."
                + digits.substring(5, 8) + "/" + digits.substring(8, 12) + "-"
                + digits.substring(12, 14);
    }

    private void bindRow(View root, @IdRes int rowId, @DrawableRes int icon, String value) {
        View row = root.findViewById(rowId);
        ((ImageView) row.findViewById(R.id.ivRowIcon)).setImageResource(icon);
        ((TextView) row.findViewById(R.id.tvRowValue)).setText(value);
    }
}
