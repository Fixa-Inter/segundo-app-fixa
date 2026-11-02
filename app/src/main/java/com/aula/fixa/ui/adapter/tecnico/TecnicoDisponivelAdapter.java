package com.aula.fixa.ui.adapter.tecnico;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.aula.fixa.R;
import com.aula.fixa.domain.model.tecnico.TecnicoDisponivel;
import com.bumptech.glide.Glide;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class TecnicoDisponivelAdapter extends RecyclerView.Adapter<TecnicoDisponivelAdapter.Holder> {
    public interface OnSelect { void selected(TecnicoDisponivel tecnico); }
    private final List<TecnicoDisponivel> items = new ArrayList<>();
    private final OnSelect onSelect;
    private Long selectedId;

    public TecnicoDisponivelAdapter(OnSelect onSelect) { this.onSelect = onSelect; }

    public void submit(List<TecnicoDisponivel> values, Long selectedId) {
        items.clear();
        if (values != null) items.addAll(values);
        this.selectedId = selectedId;
        notifyDataSetChanged();
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ops_available_technician, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        TecnicoDisponivel technician = items.get(position);
        holder.name.setText(technician.nome);
        holder.role.setText("Técnico");
        holder.recommended.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        String competence = technician.competencia == null ? "" : technician.competencia;
        holder.competenceCategory.setText(competence);
        String score = "";
        if (technician.notaCompetencia != null) {
            score = technician.notaCompetencia.stripTrailingZeros().toPlainString() + "/10";
        }
        holder.competenceScore.setText(score);
        long tasks = technician.tarefasEmAndamento;
        holder.tasks.setText(tasks + (tasks == 1 ? " tarefa" : " tarefas"));
        holder.avatar.setImageResource(R.drawable.img_avatar_115);
        if (!technician.urlFoto.isEmpty()) {
            Glide.with(holder.avatar).load(technician.urlFoto)
                    .placeholder(R.drawable.img_avatar_115)
                    .fallback(R.drawable.img_avatar_115)
                    .error(R.drawable.img_avatar_115)
                    .circleCrop().into(holder.avatar);
        }
        boolean selected = selectedId != null && selectedId == technician.id;
        holder.itemView.setBackgroundResource(selected
                ? R.drawable.bg_ops_available_technician_selected : R.drawable.bg_ops_e04d888c51);
        int white = androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.app_on_primary);
        holder.name.setTextColor(androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(),
                selected ? R.color.app_on_primary : R.color.ops_color_ff0b013c));
        holder.role.setTextColor(selected ? white : androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.ops_color_ff8a93a2));
        holder.recommended.setTextColor(selected ? white : androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.ops_color_ff1d9954));
        holder.tasks.setTextColor(selected ? white : androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.ops_color_ff000000));
        holder.workLabel.setTextColor(selected ? white : androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.ops_color_ff8a93a2));
        holder.competenceCategory.setTextColor(selected ? white : androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.ops_color_ff000000));
        holder.competenceScore.setTextColor(selected ? white : androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.ops_color_ff8a93a2));
        if (selected) {
            holder.clock.setColorFilter(white);
            holder.competenceIcon.setColorFilter(white);
        } else {
            holder.clock.clearColorFilter();
            holder.competenceIcon.clearColorFilter();
        }
        float density = holder.itemView.getResources().getDisplayMetrics().density;
        holder.itemView.setElevation((selected ? 4f : 1.5f) * density);
        if (android.os.Build.VERSION.SDK_INT >= 28) {
            int shadow = androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(),
                    selected ? R.color.app_primary : R.color.app_shadow_soft);
            holder.itemView.setOutlineAmbientShadowColor(shadow);
            holder.itemView.setOutlineSpotShadowColor(shadow);
        }
        holder.itemView.setContentDescription(technician.nome + ", Técnico"
                + (position == 0 ? ", Recomendado" : "")
                + (competence.isEmpty() ? "" : ", " + competence)
                + (score.isEmpty() ? "" : ", nota " + score) + ", "
                + tasks + (tasks == 1 ? " tarefa em andamento" : " tarefas em andamento"));
        holder.itemView.setOnClickListener(view -> onSelect.selected(technician));
    }

    @Override public int getItemCount() { return items.size(); }

    static final class Holder extends RecyclerView.ViewHolder {
        final ImageView avatar, clock, competenceIcon;
        final TextView name, role, competenceCategory, competenceScore, tasks, workLabel, recommended;
        Holder(View item) {
            super(item);
            avatar = item.findViewById(R.id.technicianAvatar);
            clock = item.findViewById(R.id.technicianClock);
            competenceIcon = item.findViewById(R.id.technicianCompetenceIcon);
            name = item.findViewById(R.id.technicianName);
            role = item.findViewById(R.id.technicianRole);
            competenceCategory = item.findViewById(R.id.technicianCompetenceCategory);
            competenceScore = item.findViewById(R.id.technicianCompetenceScore);
            tasks = item.findViewById(R.id.technicianTasks);
            workLabel = item.findViewById(R.id.technicianWorkLabel);
            recommended = item.findViewById(R.id.technicianRecommended);
        }
    }
}
