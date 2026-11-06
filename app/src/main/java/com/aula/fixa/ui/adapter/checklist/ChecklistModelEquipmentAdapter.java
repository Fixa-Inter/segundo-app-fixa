package com.aula.fixa.ui.adapter.checklist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.domain.model.checklist.ChecklistEquipment;
import com.aula.fixa.domain.model.checklist.EquipmentModelOption;

import java.util.ArrayList;
import java.util.List;

/** Renders selectable model groups and their equipment rows. Selection remains owned by the screen. */
public final class ChecklistModelEquipmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface Selection {
        boolean modelExpanded(EquipmentModelOption model);
        boolean equipmentSelected(EquipmentModelOption model, ChecklistEquipment equipment);
        void onModelExpansionChanged(EquipmentModelOption model, boolean expanded);
        void onEquipmentSelected(EquipmentModelOption model, ChecklistEquipment equipment, boolean selected);
    }

    private static final int GROUP = 0, EQUIPMENT = 1;
    private final List<Row> rows = new ArrayList<>();
    private final Selection selection;

    public ChecklistModelEquipmentAdapter(Selection selection) { this.selection = selection; }

    public void submit(List<EquipmentModelOption> models, String query) {
        rows.clear();
        String normalized = normalize(query);
        if (models != null) for (EquipmentModelOption model : models) {
            boolean modelMatches = normalize(model.nome).contains(normalized);
            List<ChecklistEquipment> matching = new ArrayList<>();
            if (model.equipamentos != null) for (ChecklistEquipment item : model.equipamentos) {
                if (normalized.isEmpty() || modelMatches || normalize(item.codigo).contains(normalized)) matching.add(item);
            }
            if (normalized.isEmpty() || modelMatches || !matching.isEmpty()) {
                rows.add(Row.group(model));
                if (model.disponivel && selection.modelExpanded(model)) {
                    for (ChecklistEquipment equipment : matching) rows.add(Row.equipment(model, equipment));
                }
            }
        }
        if (rows.isEmpty()) rows.add(Row.empty());
        notifyDataSetChanged();
    }

    @Override public int getItemViewType(int position) { return rows.get(position).type; }
    @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        int layout = type == GROUP ? R.layout.item_ops_checklist_model_group : type == EQUIPMENT
                ? R.layout.item_ops_checklist_model_equipment : R.layout.item_ops_checklist_model_empty;
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false), type);
    }
    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder raw, int position) {
        Holder holder = (Holder) raw; Row row = rows.get(position);
        if (row.type == GROUP) {
            boolean expanded = row.model.disponivel && selection.modelExpanded(row.model);
            holder.modelTitle.setText(row.model.nome == null ? "" : row.model.nome);
            holder.modelRow.setEnabled(row.model.disponivel);
            holder.modelTitle.setEnabled(row.model.disponivel);
            holder.expand.setEnabled(row.model.disponivel);
            holder.expand.setRotation(expanded ? 180f : 0f);
            holder.modelRow.setOnClickListener(row.model.disponivel
                    ? view -> selection.onModelExpansionChanged(row.model, !expanded) : null);
        } else if (row.type == EQUIPMENT) {
            holder.check.setOnCheckedChangeListener(null);
            holder.check.setText(row.equipment.codigo == null ? "" : row.equipment.codigo);
            holder.check.setEnabled(row.equipment.disponivel);
            holder.check.setChecked(row.equipment.disponivel
                    && selection.equipmentSelected(row.model, row.equipment));
            if (row.equipment.disponivel) {
                holder.check.setOnCheckedChangeListener((button, checked) ->
                        selection.onEquipmentSelected(row.model, row.equipment, checked));
            }
        }
    }
    @Override public int getItemCount() { return rows.size(); }

    private static String normalize(String value) {
        if (value == null) return "";
        String decomposed = java.text.Normalizer.normalize(value.trim().toLowerCase(java.util.Locale.ROOT),
                java.text.Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}+", "");
    }

    private static final class Holder extends RecyclerView.ViewHolder {
        final CheckBox check;
        final View modelRow;
        final TextView modelTitle;
        final android.widget.ImageView expand;
        Holder(View view, int type) {
            super(view);
            check = type == EQUIPMENT ? view.findViewById(R.id.checklistModelOption) : null;
            modelRow = type == GROUP ? view.findViewById(R.id.checklistModelRow) : null;
            modelTitle = type == GROUP ? view.findViewById(R.id.checklistModelTitle) : null;
            expand = type == GROUP ? view.findViewById(R.id.checklistModelExpand) : null;
        }
    }
    private static final class Row {
        final int type; final EquipmentModelOption model; final ChecklistEquipment equipment;
        private Row(int type, EquipmentModelOption model, ChecklistEquipment equipment) { this.type = type; this.model = model; this.equipment = equipment; }
        static Row group(EquipmentModelOption model) { return new Row(GROUP, model, null); }
        static Row equipment(EquipmentModelOption model, ChecklistEquipment item) { return new Row(EQUIPMENT, model, item); }
        static Row empty() { return new Row(2, null, null); }
    }
}
