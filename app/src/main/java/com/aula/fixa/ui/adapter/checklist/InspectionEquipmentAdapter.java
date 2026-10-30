package com.aula.fixa.ui.adapter.checklist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.domain.model.checklist.ChecklistEquipment;

import java.util.ArrayList;
import java.util.List;

public final class InspectionEquipmentAdapter extends RecyclerView.Adapter<InspectionEquipmentAdapter.Holder> {
    public interface Listener { void onPendingEquipmentClick(ChecklistEquipment equipment); }
    public interface StatusResolver { boolean isCompleted(ChecklistEquipment equipment); }

    private final List<ChecklistEquipment> items = new ArrayList<>();
    private final Listener listener;
    private final StatusResolver statusResolver;

    public InspectionEquipmentAdapter(Listener listener, StatusResolver statusResolver) {
        this.listener = listener;
        this.statusResolver = statusResolver;
    }

    public void submit(List<ChecklistEquipment> equipment) {
        items.clear();
        if (equipment != null) items.addAll(equipment);
        notifyDataSetChanged();
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ops_manager_inspection_equipment, parent, false);
        return new Holder(item);
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        ChecklistEquipment equipment = items.get(position);
        boolean completed = statusResolver.isCompleted(equipment);
        holder.code.setText(equipment.codigo == null ? "" : equipment.codigo);
        holder.status.setText(completed ? "Inspeção Realizada" : "Inspeção Pendente");
        holder.status.setTextColor(ContextCompat.getColor(holder.itemView.getContext(),
                completed ? R.color.ops_color_ff143c7d : R.color.ops_color_ffde951b));
        holder.accent.setBackgroundResource(completed ? R.drawable.bg_ops_674617cd94 : R.drawable.bg_ops_4e33a872e8);
        holder.itemView.setAlpha(completed ? .55f : 1f);
        holder.itemView.setEnabled(!completed);
        holder.itemView.setOnClickListener(completed || listener == null ? null : v -> listener.onPendingEquipmentClick(equipment));
    }

    @Override public int getItemCount() { return items.size(); }

    static final class Holder extends RecyclerView.ViewHolder {
        final View accent;
        final TextView code;
        final TextView status;
        Holder(View item) {
            super(item);
            accent = item.findViewById(R.id.inspectionEquipmentAccent);
            code = item.findViewById(R.id.inspectionEquipmentCode);
            status = item.findViewById(R.id.inspectionEquipmentStatus);
        }
    }
}
