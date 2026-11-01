package com.aula.fixa.ui.view.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.aula.fixa.R;

public class SettingsFragment extends Fragment {
    private com.aula.fixa.ui.view.notifications.NotificationSettingsController notificationSettings;

    public SettingsFragment() {
        super(R.layout.fragment_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) view.findViewById(R.id.tvPageTitle)).setText(R.string.app_settings_title);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        notificationSettings = new com.aula.fixa.ui.view.notifications.NotificationSettingsController(
                this, view.findViewById(R.id.switchNotifications));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (notificationSettings != null) notificationSettings.onResume();
    }
}
