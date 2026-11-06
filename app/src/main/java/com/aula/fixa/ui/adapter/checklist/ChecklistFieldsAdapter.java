package com.aula.fixa.ui.adapter.checklist;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.domain.model.checklist.ChecklistField;

import java.util.Collections;
import java.util.List;

/** Renderiza a edição de campos e encaminha comandos e alterações de ordem à tela. */
public final class ChecklistFieldsAdapter extends RecyclerView.Adapter<ChecklistFieldsAdapter.Holder> {
    public interface Callbacks {
        void onAddField();
        void onFieldMenu(ChecklistField field, int position, View anchor);
        void onFieldsReordered();
    }

    private static final int TYPE_FIELD = 0;
    private static final int TYPE_ADD = 1;
    private final List<ChecklistField> fields;
    private final Callbacks callbacks;
    private ItemTouchHelper touchHelper;

    public ChecklistFieldsAdapter(List<ChecklistField> fields, Callbacks callbacks) {
        this.fields = fields;
        this.callbacks = callbacks;
    }

    public void setTouchHelper(ItemTouchHelper helper) { touchHelper = helper; }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == TYPE_ADD ? R.layout.item_ops_checklist_add_field : R.layout.item_ops_checklist_field;
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false), viewType);
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        if (holder.viewType == TYPE_ADD) {
            holder.itemView.setOnClickListener(v -> callbacks.onAddField());
            return;
        }
        ChecklistField field = fields.get(position);
        holder.title.setText(field.nome == null ? "" : field.nome);
        String type = field.tipoCampo == null ? "" : field.tipoCampo.nome;
        holder.meta.setText(holder.itemView.getResources().getString(
                Boolean.TRUE.equals(field.obrigatorio) ? R.string.ops_checklist_field_required : R.string.ops_checklist_field_optional, type));
        holder.drag.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN && touchHelper != null) {
                touchHelper.startDrag(holder);
                return true;
            }
            return false;
        });
        holder.menu.setOnClickListener(view -> {
            int current = holder.getBindingAdapterPosition();
            if (current != RecyclerView.NO_POSITION && current < fields.size())
                callbacks.onFieldMenu(fields.get(current), current, holder.menu);
        });
    }

    @Override public int getItemCount() { return fields.size() + 1; }
    @Override public int getItemViewType(int position) { return position >= fields.size() ? TYPE_ADD : TYPE_FIELD; }

    public void removeAt(int position) {
        if (position < 0 || position >= fields.size()) return;
        fields.remove(position);
        notifyItemRemoved(position);
    }

    @Override public void onViewRecycled(@NonNull Holder holder) {
        if (holder.drag != null) holder.drag.setOnTouchListener(null);
        if (holder.menu != null) holder.menu.setOnClickListener(null);
        holder.itemView.setOnClickListener(null);
        super.onViewRecycled(holder);
    }

    final class Holder extends RecyclerView.ViewHolder {
        final int viewType;
        final TextView title;
        final TextView meta;
        final View drag;
        final View menu;
        Holder(View item, int viewType) {
            super(item);
            this.viewType = viewType;
            title = item.findViewById(R.id.checklistFieldTitle);
            meta = item.findViewById(R.id.checklistFieldMeta);
            drag = item.findViewById(R.id.checklistFieldDrag);
            menu = item.findViewById(R.id.checklistFieldMenu);
        }
    }

    public ItemTouchHelper.Callback dragCallback() {
        return new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder source,
                                            @NonNull RecyclerView.ViewHolder target) {
                int from = source.getBindingAdapterPosition();
                int to = target.getBindingAdapterPosition();
                if (from < 0 || to < 0 || from >= fields.size() || to >= fields.size() || from == to) return false;
                Collections.swap(fields, from, to);
                notifyItemMoved(from, to);
                callbacks.onFieldsReordered();
                return true;
            }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder holder, int direction) { }
        };
    }
}
