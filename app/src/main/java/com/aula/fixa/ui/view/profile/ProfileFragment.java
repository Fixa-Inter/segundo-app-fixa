package com.aula.fixa.ui.view.profile;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.aula.fixa.R;
import com.aula.fixa.ui.view.auth.LoginActivity;
import com.aula.fixa.adapter.auth.SessaoViewModel;
import com.aula.fixa.adapter.auth.SessaoViewModelFactory;
import com.aula.fixa.ui.config.AppDialogs;
import com.aula.fixa.ui.config.FragmentUi;
import com.aula.fixa.ui.config.DataChangeNotifier;
import com.aula.fixa.adapter.perfil.PerfilViewModel;
import com.aula.fixa.adapter.perfil.PerfilViewModelFactory;
import com.aula.fixa.domain.model.Perfil;
import com.bumptech.glide.Glide;

public class ProfileFragment extends Fragment {
    private PerfilViewModel perfilViewModel;
    private SessaoViewModel sessaoViewModel;

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        perfilViewModel = new ViewModelProvider(this,
                new PerfilViewModelFactory(requireContext())).get(PerfilViewModel.class);
        sessaoViewModel = new ViewModelProvider(this,
                new SessaoViewModelFactory(requireContext())).get(SessaoViewModel.class);
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
                    sessaoViewModel.encerrar();
                    startActivity(new Intent(requireContext(), LoginActivity.class));
                    requireActivity().finish();
                }));
    }

    private void loadProfile(View view) {
        perfilViewModel.obter(new PerfilViewModel.Callback<Perfil>() {
            @Override public void aoConcluir(Perfil profile) {
                FragmentUi.runIfViewActive(ProfileFragment.this, view, () -> {
                    ((TextView) view.findViewById(R.id.tvName)).setText(profile.nomeCompleto);
                    ((TextView) view.findViewById(R.id.tvEmail)).setText(profile.email);
                    String url = profile.urlFotoPerfil.trim();
                    Glide.with(view).load(url.isEmpty() ? null : url).placeholder(R.drawable.img_avatar_115)
                            .error(R.drawable.img_avatar_115).circleCrop()
                            .into((ImageView) view.findViewById(R.id.ivAvatar));
                });
            }
            @Override public void aoFalhar(Throwable erro) {
                FragmentUi.runIfViewActive(ProfileFragment.this, view, () -> {
                    ((ImageView) view.findViewById(R.id.ivAvatar)).setImageResource(R.drawable.img_avatar_115);
                    android.widget.Toast.makeText(view.getContext(), erro.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void bindRow(View root, @IdRes int rowId, @DrawableRes int icon, @StringRes int label, @IdRes int action) {
        View row = root.findViewById(rowId);
        ((ImageView) row.findViewById(R.id.ivMenuIcon)).setImageResource(icon);
        ((TextView) row.findViewById(R.id.tvMenuLabel)).setText(label);
        if (action != 0) row.setOnClickListener(v -> Navigation.findNavController(v).navigate(action));
    }
}
