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

        bindRow(view, R.id.rowName, R.drawable.ic_user_24, "Ryan Cursino de Moraes");
        bindRow(view, R.id.rowBirth, R.drawable.ic_calendar_24, "26/11/2002");
        bindRow(view, R.id.rowEmail, R.drawable.ic_mail_24, "ryan.cursino@gmail.com");
        bindRow(view, R.id.rowPhone, R.drawable.ic_phone_24, "+(55) 11 123456789");
        bindRow(view, R.id.rowPassword, R.drawable.ic_lock_24, "*************");
        bindRow(view, R.id.rowAddress, R.drawable.ic_compass_24, "Instituto J&F, R. Irineu José Bordon, 365");
    }

    private void bindRow(View root, @IdRes int rowId, @DrawableRes int icon, String value) {
        View row = root.findViewById(rowId);
        ((ImageView) row.findViewById(R.id.ivRowIcon)).setImageResource(icon);
        ((TextView) row.findViewById(R.id.tvRowValue)).setText(value);
    }
}
