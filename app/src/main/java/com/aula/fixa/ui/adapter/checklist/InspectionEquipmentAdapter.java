package com.aula.fixa.ui.adapter.checklist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.aula.fixa.R;
import com.aula.fixa.domain.model.checklist.ChecklistEquipment;

import java.util.ArrayList;
import java.util.List;

/** Apresenta os equipamentos filtrados, com atualizações diferenciais entre listas. */
public final class InspectionEquipmentAdapter extends ListAdapter<InspectionEquipmentAdapter.Row, InspectionEquipmentAdapter.Holder> {
    public interface Listener { void onPendingEquipmentClick(ChecklistEquipment equipment); }
    public interface StatusResolver { boolean isCompleted(ChecklistEquipment equipment); }

    private static final DiffUtil.ItemCallback<Row> DIFF = new DiffUtil.ItemCallback<Row>() {
        @Override public boolean areItemsTheSame(@NonNull Row oldItem, @NonNull Row newItem) {
            return oldItem.equipment.id == newItem.equipment.id;
        }
        @Override public boolean areContentsTheSame(@NonNull Row oldItem, @NonNull Row newItem) {
            ChecklistEquipment oldValue = oldItem.equipment;
            ChecklistEquipment newValue = newItem.equipment;
            return oldItem.completed == newItem.completed
                    && equal(oldValue.codigo, newValue.codigo)
                    && equal(oldValue.modeloNome, newValue.modeloNome)
                    && equal(oldValue.prazoInspecao, newValue.prazoInspecao)
                    && java.util.Objects.equals(oldValue.status, newValue.status);
        }
        private boolean equal(String left, String right) { return left == null ? right == null : left.equals(right); }
    };

    private final Listener listener;
    private final StatusResolver statusResolver;

    public InspectionEquipmentAdapter(Listener listener, StatusResolver statusResolver) {
        super(DIFF);
        this.listener = listener;
        this.statusResolver = statusResolver;
    }

    public void submit(List<ChecklistEquipment> equipment) {
        List<Row> rows = new ArrayList<>();
        if (equipment != null) for (ChecklistEquipment item : equipment) {
            if (item != null) rows.add(new Row(item, statusResolver.isCompleted(item)));
        }
        submitList(rows);
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ops_manager_inspection_equipment, parent, false);
        return new Holder(item);
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        Row row = getItem(position);
        ChecklistEquipment equipment = row.equipment;
        holder.code.setText(equipment.codigo == null ? "" : equipment.codigo);
        holder.status.setText(row.completed ? R.string.app_checklist_inspection_done : R.string.app_checklist_inspection_pending);
        holder.status.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(),
                row.completed ? R.color.ops_color_ff143c7d : R.color.ops_color_ffde951b));
        holder.accent.setBackgroundResource(row.completed ? R.drawable.bg_ops_674617cd94 : R.drawable.bg_ops_4e33a872e8);
        holder.itemView.setAlpha(row.completed ? .55f : 1f);
        holder.itemView.setEnabled(!row.completed);
        holder.itemView.setOnClickListener(row.completed || listener == null
                ? null : v -> listener.onPendingEquipmentClick(equipment));
    }

    public static final class Holder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
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

    public static final class Row {
        final ChecklistEquipment equipment;
        final boolean completed;
        Row(ChecklistEquipment equipment, boolean completed) { this.equipment = equipment; this.completed = completed; }
    }
}
