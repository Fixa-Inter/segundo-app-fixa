package com.aula.fixa.ui.adapter.task;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;

import java.util.ArrayList;
import java.util.List;

/** Lista local de tarefas ainda em rascunho da OS. */
public final class PlanejamentoTarefaAdapter extends RecyclerView.Adapter<PlanejamentoTarefaAdapter.TaskHolder> {
    public interface Listener { void onOptions(View anchor, Bundle task); }

    private final List<Bundle> tasks = new ArrayList<>();
    private final Listener listener;

    public PlanejamentoTarefaAdapter(Listener listener) { this.listener = listener; }

    public void submitList(List<Bundle> values) {
        tasks.clear();
        if (values != null) tasks.addAll(values);
        notifyDataSetChanged();
    }

    @NonNull @Override public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ops_manager_planning_task, parent, false);
        return new TaskHolder(item);
    }

    @Override public void onBindViewHolder(@NonNull TaskHolder holder, int position) {
        Bundle task = tasks.get(position);
        String title = task.getString("title", "");
        String description = task.getString("description", "");
        holder.title.setText(title);
        holder.description.setText(description);
        holder.more.setOnClickListener(view -> {
            if (listener != null) listener.onOptions(view, new Bundle(task));
        });
        holder.more.setContentDescription(holder.itemView.getContext().getString(R.string.ops_task_options_for, title));
    }

    @Override public int getItemCount() { return tasks.size(); }

    static final class TaskHolder extends RecyclerView.ViewHolder {
        final TextView title, description;
        final ImageView more;
        TaskHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.planningTaskTitle);
            description = itemView.findViewById(R.id.planningTaskDescription);
            more = itemView.findViewById(R.id.planningTaskMore);
        }
    }
}
