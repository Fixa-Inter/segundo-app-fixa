package com.aula.fixa.ui.adapter.tecnico;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.domain.model.tecnico.ResponsabilidadeTecnico;

import java.util.ArrayList;
import java.util.List;

public final class ResponsabilidadesTecnicoAdapter extends RecyclerView.Adapter<ResponsabilidadesTecnicoAdapter.Holder> {
    private final List<ResponsabilidadeTecnico> items = new ArrayList<>();

    public void submit(List<ResponsabilidadeTecnico> values) {
        items.clear();
        if (values != null) {
            items.addAll(values);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ops_manager_member_responsibility, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ResponsabilidadeTecnico item = items.get(position);
        holder.title.setText(item.titulo);
        holder.location.setText(item.local);
        holder.due.setText(item.prazo.isEmpty() ? "" : "Prazo: " + formatDate(item.prazo));
        String priority = priorityCode(item.prioridade);
        holder.priority.setText(priority == null ? "" : priority);
        holder.priority.setVisibility(priority == null ? View.GONE : View.VISIBLE);
        holder.priorityBadge.setVisibility(priority == null ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static String priorityCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = java.text.Normalizer.normalize(value.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toUpperCase(java.util.Locale.ROOT);
        switch (normalized) {
            case "P0":
            case "ALTA":
            case "0":
                return "P0";
            case "P1":
            case "MEDIA":
            case "1":
                return "P1";
            case "P2":
            case "BAIXA":
            case "2":
                return "P2";
            default:
                return null;
        }
    }

    private static String formatDate(String value) {
        try {
            return java.time.LocalDate.parse(value.substring(0, 10)).format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception ignored) {
            return value;
        }
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView title, location, due, priority;
        final View priorityBadge;

        Holder(View item) {
            super(item);
            title = item.findViewById(R.id.memberResponsibilityTitle);
            location = item.findViewById(R.id.memberResponsibilityLocation);
            due = item.findViewById(R.id.memberResponsibilityDue);
            priority = item.findViewById(R.id.memberResponsibilityPriority);
            priorityBadge = item.findViewById(R.id.memberResponsibilityPriorityBadge);
        }
    }
}
