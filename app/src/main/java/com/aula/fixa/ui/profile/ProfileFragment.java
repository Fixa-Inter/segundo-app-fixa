package com.aula.fixa.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.aula.fixa.R;
import com.aula.fixa.ui.auth.LoginActivity;
import com.aula.fixa.data.local.preferences.SessionStorage;
import com.aula.fixa.ui.common.AppDialogs;
import com.aula.fixa.ui.common.FragmentUi;
import com.aula.fixa.ui.common.DataChangeNotifier;
import com.aula.fixa.data.repository.RequestRepository;
import com.bumptech.glide.Glide;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        loadProfile(view);
        DataChangeNotifier.observe(getViewLifecycleOwner(), ignored -> {
            if (getView() == view) loadProfile(view);
        });

        bindRow(view, R.id.rowMyProfile, R.drawable.ic_user_24, R.string.app_profile_my_profile, R.id.action_profile_to_my_profile);
        bindRow(view, R.id.rowSettings, R.drawable.ic_settings_28, R.string.app_profile_settings, R.id.action_profile_to_settings);
        bindRow(view, R.id.rowFaq, R.drawable.ic_help_circle_28, R.string.app_profile_faq, R.id.action_profile_to_faq);
        bindRow(view, R.id.rowAbout, R.drawable.ic_alert_circle_24, R.string.app_profile_about, R.id.action_profile_to_about);
        bindRow(view, R.id.rowLogout, R.drawable.ic_log_out_21, R.string.app_profile_logout, 0);
        View logout = view.findViewById(R.id.rowLogout);
        ((TextView) logout.findViewById(R.id.tvMenuLabel))
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.app_danger));
        ((ImageView) logout.findViewById(R.id.ivMenuIcon))
                .setImageTintList(ContextCompat.getColorStateList(requireContext(), R.color.app_danger));

        view.findViewById(R.id.btnEditProfile).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_profile_to_edit));
        view.findViewById(R.id.rowLogout).setOnClickListener(v ->
                AppDialogs.showLogoutConfirm(requireContext(), () -> {
                    new SessionStorage(requireContext()).clear();
                    startActivity(new Intent(requireContext(), LoginActivity.class));
                    requireActivity().finish();
                }));
    }

    private void loadProfile(View view) { android.content.Context appContext = requireContext().getApplicationContext(); new Thread(() -> { try { com.google.gson.JsonObject profile=new RequestRepository(new SessionStorage(appContext)).perfil(); FragmentUi.runIfViewActive(this, view, () -> { ((TextView)view.findViewById(R.id.tvName)).setText(profile.has("nomeCompleto") ? profile.get("nomeCompleto").getAsString() : ""); ((TextView)view.findViewById(R.id.tvEmail)).setText(profile.has("email") ? profile.get("email").getAsString() : ""); String url = profile.has("urlFotoPerfil") && !profile.get("urlFotoPerfil").isJsonNull() ? profile.get("urlFotoPerfil").getAsString().trim() : ""; Glide.with(view).load(url.isEmpty() ? null : url).placeholder(R.drawable.img_avatar_115).error(R.drawable.img_avatar_115).circleCrop().into((ImageView) view.findViewById(R.id.ivAvatar)); }); } catch(Exception e) { FragmentUi.runIfViewActive(this, view, () -> { ((ImageView)view.findViewById(R.id.ivAvatar)).setImageResource(R.drawable.img_avatar_115); android.widget.Toast.makeText(view.getContext(),e.getMessage(),android.widget.Toast.LENGTH_LONG).show(); }); } }).start(); }

    private void bindRow(View root, @IdRes int rowId, @DrawableRes int icon, @StringRes int label, @IdRes int action) {
        View row = root.findViewById(rowId);
        ((ImageView) row.findViewById(R.id.ivMenuIcon)).setImageResource(icon);
        ((TextView) row.findViewById(R.id.tvMenuLabel)).setText(label);
        if (action != 0) row.setOnClickListener(v -> Navigation.findNavController(v).navigate(action));
    }
}
