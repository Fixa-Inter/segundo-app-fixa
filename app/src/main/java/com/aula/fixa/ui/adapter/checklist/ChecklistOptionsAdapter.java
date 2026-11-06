package com.aula.fixa.ui.adapter.checklist;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.domain.model.checklist.ChecklistOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Mantém, reordena e remove as opções editáveis de um campo. */
public final class ChecklistOptionsAdapter extends RecyclerView.Adapter<ChecklistOptionsAdapter.Holder> {
    private final List<ChecklistOption> options = new ArrayList<>();
    private ItemTouchHelper touchHelper;

    public ChecklistOptionsAdapter(List<ChecklistOption> initial) {
        if (initial != null) options.addAll(initial);
    }

    public void setTouchHelper(ItemTouchHelper helper) { touchHelper = helper; }
    public void addOption(String value) {
        options.add(new ChecklistOption(UUID.randomUUID().toString(), value, false));
        notifyItemInserted(options.size() - 1);
    }
    public List<ChecklistOption> optionValues() { return new ArrayList<>(options); }
    public void move(int from, int to) {
        if (from < 0 || to < 0 || from >= options.size() || to >= options.size() || from == to) return;
        Collections.swap(options, from, to);
        notifyItemMoved(from, to);
    }
    public ItemTouchHelper.Callback dragCallback() {
        return new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override public boolean onMove(@NonNull RecyclerView list, @NonNull RecyclerView.ViewHolder source,
                                            @NonNull RecyclerView.ViewHolder target) {
                int from = source.getBindingAdapterPosition(), to = target.getBindingAdapterPosition();
                if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false;
                move(from, to);
                return true;
            }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder holder, int direction) { }
        };
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ops_checklist_option, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        ChecklistOption option = options.get(position);
        if (holder.watcher != null) holder.input.removeTextChangedListener(holder.watcher);
        holder.input.setText(option.nome == null ? "" : option.nome);
        holder.watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                int current = holder.getBindingAdapterPosition();
                if (current != RecyclerView.NO_POSITION && current < options.size()) options.get(current).nome = s.toString();
            }
            @Override public void afterTextChanged(Editable s) { }
        };
        holder.input.addTextChangedListener(holder.watcher);
        holder.handle.setOnTouchListener((view, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN && touchHelper != null) {
                touchHelper.startDrag(holder);
                return true;
            }
            return false;
        });
        holder.remove.setOnClickListener(v -> {
            int current = holder.getBindingAdapterPosition();
            if (current != RecyclerView.NO_POSITION) {
                options.remove(current);
                notifyItemRemoved(current);
            }
        });
    }

    @Override public void onViewRecycled(@NonNull Holder holder) {
        if (holder.watcher != null) holder.input.removeTextChangedListener(holder.watcher);
        holder.handle.setOnTouchListener(null);
        holder.remove.setOnClickListener(null);
        super.onViewRecycled(holder);
    }

    @Override public int getItemCount() { return options.size(); }

    static final class Holder extends RecyclerView.ViewHolder {
        final View handle, remove;
        final EditText input;
        TextWatcher watcher;
        Holder(View item) {
            super(item);
            handle = item.findViewById(R.id.checklistOptionDrag);
            input = item.findViewById(R.id.checklistOptionInput);
            remove = item.findViewById(R.id.checklistOptionRemove);
        }
    }
}
