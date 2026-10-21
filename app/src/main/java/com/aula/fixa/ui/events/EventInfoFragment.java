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
import com.aula.fixa.ui.common.FragmentUi;
import com.aula.fixa.data.local.preferences.SessionStorage;
import com.aula.fixa.data.repository.RequestRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EventInfoFragment extends Fragment {

    public EventInfoFragment() {
        super(R.layout.fragment_event_info);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        AppText.setHtml(view.findViewById(R.id.tvTitle), R.string.app_event_info_title);
        long id = getArguments() == null ? 0 : getArguments().getLong("eventId", 0);
        if (id > 0) new Thread(() -> { try {
            com.google.gson.JsonObject e = new RequestRepository(new SessionStorage(requireContext())).evento(id);
            LocalDateTime start = LocalDateTime.parse(e.get("dataHoraInicio").getAsString());
            LocalDateTime end = LocalDateTime.parse(e.get("dataHoraFim").getAsString());
            FragmentUi.runIfViewActive(this, view, () -> {
                ((TextView) view.findViewById(R.id.tvEventName)).setText(e.get("titulo").getAsString());
                ((TextView) view.findViewById(R.id.tvLocation)).setText(e.has("localUnidade") ? e.get("localUnidade").getAsString() : "");
                ((TextView) view.findViewById(R.id.tvDate)).setText(start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                ((TextView) view.findViewById(R.id.tvTime)).setText(start.format(DateTimeFormatter.ofPattern("HH:mm")) + " - " + end.format(DateTimeFormatter.ofPattern("HH:mm")));
                ((TextView) view.findViewById(R.id.tvDescription)).setText(e.has("descricao") ? e.get("descricao").getAsString() : "");
                ((TextView) view.findViewById(R.id.tvNote)).setText(e.has("observacao") ? e.get("observacao").getAsString() : "");
            });
        } catch (Exception e) { FragmentUi.runIfViewActive(this, view, () -> android.widget.Toast.makeText(view.getContext(), e.getMessage(), android.widget.Toast.LENGTH_LONG).show()); } }).start();
    }
}
