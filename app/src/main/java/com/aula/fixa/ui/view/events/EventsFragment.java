package com.aula.fixa.ui.view.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.ui.config.AppText;
import com.aula.fixa.domain.model.EventoItem;
import com.aula.fixa.adapter.evento.EventoViewModel;
import com.aula.fixa.adapter.evento.EventoViewModelFactory;
import com.aula.fixa.domain.model.Evento;
import com.aula.fixa.adapter.auth.AutenticacaoViewModel;
import com.aula.fixa.ui.config.AppLoading;
import com.aula.fixa.ui.config.FragmentUi;
import com.aula.fixa.ui.config.DataChangeNotifier;

import android.app.Dialog;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class EventsFragment extends Fragment {
    private LocalDate selectedDate;
    private EventoViewModel viewModel;

    public EventsFragment() {
        super(R.layout.fragment_events);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.getString("selectedDate") != null) {
            selectedDate = LocalDate.parse(savedInstanceState.getString("selectedDate"));
        }
        AppText.setHtml(view.findViewById(R.id.tvTitle), R.string.app_events_title);
        viewModel = new androidx.lifecycle.ViewModelProvider(this,
                new EventoViewModelFactory(requireContext())).get(EventoViewModel.class);

        loadEvents(view);
        DataChangeNotifier.observe(getViewLifecycleOwner(), ignored -> {
            if (getView() == view) loadEvents(view);
        });

    }

    private void loadEvents(View view) {
        Dialog loading = AppLoading.show(requireContext());
        viewModel.listar(new EventoViewModel.Listener() {
            @Override public void aoCarregar(boolean carregando) { }
            @Override public void aoDetalhar(Evento evento) { }
            @Override public void aoListar(List<Evento> response) {
                List<EventoItem> items = new ArrayList<>();

                for (Evento event : response) {
                    LocalDateTime start = LocalDateTime.parse(event.inicio);
                    LocalDateTime end = LocalDateTime.parse(event.fim);
                    items.add(new EventoItem(event.id, String.valueOf(start.getDayOfMonth()),
                            start.format(DateTimeFormatter.ofPattern("EEE")), event.titulo,
                            start.format(DateTimeFormatter.ofPattern("HH:mm")) + " - " + end.format(DateTimeFormatter.ofPattern("HH:mm")), start.toLocalDate().toString()));
                }

                FragmentUi.runIfViewActive(EventsFragment.this, view, () -> {
                    AppLoading.hide(loading);
                    bindCalendar(view, items, selectedDate == null ? LocalDate.now() : selectedDate);
                    view.findViewById(R.id.btnCalendar).setOnClickListener(v -> {
                        java.util.Calendar calendar = java.util.Calendar.getInstance();
                        new android.app.DatePickerDialog(view.getContext(), (picker, year, month, day) -> bindCalendar(view, items, LocalDate.of(year, month + 1, day)), calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show();
                    });
                });

            }
            @Override public void aoFalhar(Throwable error) {
                FragmentUi.runIfViewActive(EventsFragment.this, view, () -> {
                    AppLoading.hide(loading);
                    android.widget.Toast.makeText(view.getContext(), AutenticacaoViewModel.messageFrom(error), android.widget.Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void bindCalendar(View view, List<EventoItem> items, LocalDate selectedDate) {
        this.selectedDate = selectedDate;
        LocalDate sunday = selectedDate.minusDays(selectedDate.getDayOfWeek().getValue() % 7);
        List<Integer> days = new ArrayList<>();
        String[] labels = new String[7];
        for (int i = 0; i < 7; i++) {
            LocalDate day = sunday.plusDays(i);
            days.add(day.getDayOfMonth());
            labels[i] = day.format(DateTimeFormatter.ofPattern("EEE"));
        }
        int selectedIndex = selectedDate.getDayOfWeek().getValue() % 7;
        view.<RecyclerView>findViewById(R.id.rvDays).setAdapter(new DayAdapter(days, labels, selectedIndex, day -> {
            List<EventoItem> filtered = new ArrayList<>();
            LocalDate chosen = sunday.plusDays(days.indexOf(day));
            for (EventoItem item : items) {
                if (item.dateKey.equals(chosen.toString())) {
                    filtered.add(item);
                }
            }
            showEvents(view, filtered);
        }));
        List<EventoItem> todayEvents = new ArrayList<>();
        for (EventoItem item : items) {
            if (item.dateKey.equals(selectedDate.toString())) {
                todayEvents.add(item);
            }
        }
        showEvents(view, todayEvents);
        String month = sunday.format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("pt", "BR")));
        month = month.substring(0, 1).toUpperCase(new Locale("pt", "BR")) + month.substring(1);
        ((TextView) view.findViewById(R.id.tvMonth)).setText(month);
    }

    private void showEvents(View view, List<EventoItem> events) {
        boolean empty = events.isEmpty();
        view.findViewById(R.id.tvEmptyEvents).setVisibility(empty ? View.VISIBLE : View.GONE);
        RecyclerView list = view.findViewById(R.id.rvEvents);
        list.setVisibility(empty ? View.GONE : View.VISIBLE);
        list.setAdapter(new EventAdapter(events, selected -> {
            Bundle args = new Bundle();
            args.putLong("eventId", selected.id);
            androidx.navigation.NavController navigation = Navigation.findNavController(view);
            if (navigation.getCurrentDestination() != null
                    && navigation.getCurrentDestination().getId() == R.id.ops_manager_events) {
                navigation.navigate(R.id.action_ops_manager_events_to_event_details, args);
            } else {
                navigation.navigate(R.id.action_events_to_event_info, args);
            }
        }));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedDate != null) outState.putString("selectedDate", selectedDate.toString());
    }

    /**
     * Seletor horizontal de dias (60x100, r=25).
     */
    interface OnDaySelected {
        void onSelected(int day);
    }

    static class DayAdapter extends RecyclerView.Adapter<DayAdapter.ViewHolder> {
        private final List<Integer> days;
        private final String[] labels;
        private int selected;
        private final OnDaySelected listener;

        DayAdapter(List<Integer> days, String[] labels, int selected, OnDaySelected listener) {
            this.days = days;
            this.labels = labels;
            this.selected = selected;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_app_event_day, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            boolean isSelected = position == selected;
            holder.number.setText(String.valueOf(days.get(position)));
            holder.label.setText(labels[position % labels.length]);
            holder.itemView.setBackgroundResource(isSelected
                    ? R.drawable.bg_app_event_day_selected : R.drawable.bg_app_event_day);
            int numberColor = isSelected ? R.color.app_on_primary : R.color.app_text_secondary;
            int labelColor = isSelected ? R.color.app_on_primary : R.color.app_text_muted;
            holder.number.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), numberColor));
            holder.label.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), labelColor));
            holder.itemView.setOnClickListener(v -> {
                int old = selected;
                selected = holder.getBindingAdapterPosition();
                notifyItemChanged(old);
                notifyItemChanged(selected);
                if (listener != null) {
                    listener.onSelected(days.get(selected));
                }
            });
        }

        @Override
        public int getItemCount() {
            return days.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView number, label;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                number = itemView.findViewById(R.id.tvDayNumber);
                label = itemView.findViewById(R.id.tvDayLabel);
            }
        }
    }

    interface OnEventClick {
        void onClick(EventoItem item);
    }

    static class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
        private final List<EventoItem> items;
        private final OnEventClick listener;

        EventAdapter(List<EventoItem> items, OnEventClick listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_app_event, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            EventoItem item = items.get(position);
            holder.day.setText(item.day);
            holder.dayLabel.setText(item.dayLabel);
            holder.title.setText(item.title);
            holder.time.setText(item.time);
            holder.itemView.setOnClickListener(v -> listener.onClick(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView day, dayLabel, title, time;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                day = itemView.findViewById(R.id.tvEventDay);
                dayLabel = itemView.findViewById(R.id.tvEventDayLabel);
                title = itemView.findViewById(R.id.tvEventTitle);
                time = itemView.findViewById(R.id.tvEventTime);
            }
        }
    }
}
