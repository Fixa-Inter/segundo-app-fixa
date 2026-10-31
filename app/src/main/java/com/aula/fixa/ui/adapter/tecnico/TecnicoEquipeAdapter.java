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
import com.aula.fixa.domain.enums.CategoriaProblema;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public final class TecnicoEquipeAdapter extends RecyclerView.Adapter<TecnicoEquipeAdapter.Holder> {
    public interface OnTechnicianClick { void click(TecnicoDisponivel technician); }
    private final List<TecnicoDisponivel> items = new ArrayList<>();
    private final OnTechnicianClick onClick;
    public TecnicoEquipeAdapter(OnTechnicianClick onClick) { this.onClick = onClick; }
    public void submit(List<TecnicoDisponivel> technicians) {
        items.clear();
        if (technicians != null) items.addAll(technicians);
        notifyDataSetChanged();
    }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ops_manager_team_member, parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        TecnicoDisponivel technician = items.get(position);
        holder.name.setText(technician.nome);
        holder.access.setText(technician.tipoAcesso.isEmpty() ? technician.cargo : technician.tipoAcesso);
        holder.shift.setText(technician.turno);
        holder.shiftHours.setText(technician.horarioTurno.replace(" - ", "–").replace(" – ", "–"));
        holder.competence.setText(competenceName(technician.competencia));
        holder.avatar.setImageResource(R.drawable.img_avatar_115);
        if (!technician.urlFoto.isEmpty()) {
            Glide.with(holder.avatar).load(technician.urlFoto)
                    .placeholder(R.drawable.img_avatar_115).fallback(R.drawable.img_avatar_115)
                    .error(R.drawable.img_avatar_115).centerCrop().into(holder.avatar);
        }
        holder.itemView.setOnClickListener(view -> {
            if (onClick != null) onClick.click(technician);
        });
    }
    @Override public int getItemCount() { return items.size(); }
    private static String competenceName(String value) {
        if (value == null || value.trim().isEmpty()) return "";
        for (CategoriaProblema category : CategoriaProblema.values()) {
            if (category.name().equalsIgnoreCase(value) || category.getNome().equalsIgnoreCase(value)) {
                return category.getNome();
            }
        }
        return value;
    }
    static final class Holder extends RecyclerView.ViewHolder {
        final ImageView avatar;
        final TextView name, access, shift, shiftHours, competence;
        Holder(View item) {
            super(item);
            avatar = item.findViewById(R.id.teamTechnicianAvatar);
            name = item.findViewById(R.id.teamTechnicianName);
            access = item.findViewById(R.id.teamTechnicianAccess);
            shift = item.findViewById(R.id.teamTechnicianShift);
            shiftHours = item.findViewById(R.id.teamTechnicianShiftHours);
            competence = item.findViewById(R.id.teamTechnicianCompetence);
        }
    }
}
