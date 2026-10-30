package com.aula.fixa.ui.view.events;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.aula.fixa.R;
import com.aula.fixa.ui.config.AppText;
import com.aula.fixa.ui.config.FragmentUi;
import com.aula.fixa.adapter.evento.EventoViewModel;
import com.aula.fixa.adapter.evento.EventoViewModelFactory;
import com.aula.fixa.domain.model.Evento;
import com.aula.fixa.adapter.auth.AutenticacaoViewModel;
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
        if (id > 0) {
            EventoViewModel viewModel = new androidx.lifecycle.ViewModelProvider(this,
                    new EventoViewModelFactory(requireContext())).get(EventoViewModel.class);
            viewModel.detalhar(id, new EventoViewModel.Listener() {
                @Override public void aoCarregar(boolean carregando) { }
                @Override public void aoListar(java.util.List<Evento> eventos) { }
                @Override public void aoDetalhar(Evento evento) {
                    LocalDateTime start = LocalDateTime.parse(evento.inicio);
                    LocalDateTime end = LocalDateTime.parse(evento.fim);
                    FragmentUi.runIfViewActive(EventInfoFragment.this, view, () -> {
                ((TextView) view.findViewById(R.id.tvEventName)).setText(evento.titulo);
                ((TextView) view.findViewById(R.id.tvLocation)).setText(evento.local);
                ((TextView) view.findViewById(R.id.tvDate)).setText(start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                ((TextView) view.findViewById(R.id.tvTime)).setText(start.format(DateTimeFormatter.ofPattern("HH:mm")) + " - " + end.format(DateTimeFormatter.ofPattern("HH:mm")));
                ((TextView) view.findViewById(R.id.tvDescription)).setText(evento.descricao);
                ((TextView) view.findViewById(R.id.tvNote)).setText(evento.observacao);
            });
                }
                @Override public void aoFalhar(Throwable error) {
                    FragmentUi.runIfViewActive(EventInfoFragment.this, view,
                            () -> android.widget.Toast.makeText(view.getContext(), AutenticacaoViewModel.messageFrom(error), android.widget.Toast.LENGTH_LONG).show());
                }
            });
        }
    }
}
