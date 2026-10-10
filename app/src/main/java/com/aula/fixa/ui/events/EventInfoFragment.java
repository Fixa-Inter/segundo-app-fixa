package com.aula.fixa.ui.events;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.aula.fixa.R;
import com.aula.fixa.ui.common.AppText;

public class EventInfoFragment extends Fragment {

    public EventInfoFragment() {
        super(R.layout.fragment_event_info);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        AppText.setHtml(view.findViewById(R.id.tvTitle), R.string.app_event_info_title);
        ((TextView) view.findViewById(R.id.tvEventName)).setText("Entrega das Medalhas");
        ((TextView) view.findViewById(R.id.tvLocation)).setText("Átrio");
        ((TextView) view.findViewById(R.id.tvDate)).setText("20/02/2026");
        ((TextView) view.findViewById(R.id.tvTime)).setText("11:30 - 12:30");
        ((TextView) view.findViewById(R.id.tvDescription)).setText(
                "O evento irá ocorrer em toda a arena, incluindo palco, arquibancadas e área externa.");
        ((TextView) view.findViewById(R.id.tvNote)).setText("Não será possível fazer nada nesse dia.");
    }
}
