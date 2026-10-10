package com.aula.fixa.ui.events;

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
import com.aula.fixa.ui.common.AppText;
import com.aula.fixa.model.EventItem;

import java.util.Arrays;
import java.util.List;

public class EventsFragment extends Fragment {

    public EventsFragment() {
        super(R.layout.fragment_events);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppText.setHtml(view.findViewById(R.id.tvTitle), R.string.app_events_title);
        ((TextView) view.findViewById(R.id.tvMonth)).setText("Out, 2026");

        RecyclerView days = view.findViewById(R.id.rvDays);
        days.setAdapter(new DayAdapter(Arrays.asList(10, 11, 12, 13, 14),
                getResources().getStringArray(R.array.app_sample_weekdays), 0));

        RecyclerView events = view.findViewById(R.id.rvEvents);
        events.setAdapter(new EventAdapter(Arrays.asList(
                new EventItem("10", "Seg", "Entrega das medalhas", "11:30-12:30h"),
                new EventItem("10", "Seg", "Reunião de Pais", "18:30-19:30h")
        ), item -> Navigation.findNavController(view).navigate(R.id.action_events_to_event_info)));
    }

    /** Seletor horizontal de dias (60x100, r=25). */
    static class DayAdapter extends RecyclerView.Adapter<DayAdapter.ViewHolder> {
        private final List<Integer> days;
        private final String[] labels;
        private int selected;

        DayAdapter(List<Integer> days, String[] labels, int selected) {
            this.days = days;
            this.labels = labels;
            this.selected = selected;
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
        void onClick(EventItem item);
    }

    static class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {
        private final List<EventItem> items;
        private final OnEventClick listener;

        EventAdapter(List<EventItem> items, OnEventClick listener) {
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
            EventItem item = items.get(position);
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
