package com.aula.fixa.ui.adapter.equipamento;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.aula.fixa.R;
import com.aula.fixa.domain.model.OpcaoCadastro;
import java.util.ArrayList;
import java.util.List;

public final class CategoriaEquipamentoAdapter extends RecyclerView.Adapter<CategoriaEquipamentoAdapter.Holder> {
    public interface Listener { void onMenu(View anchor, OpcaoCadastro category); }
    private final Listener listener;
    private final List<OpcaoCadastro> items = new ArrayList<>();
    public CategoriaEquipamentoAdapter(Listener listener) { this.listener = listener; }
    public void submit(List<OpcaoCadastro> values) { items.clear(); if (values != null) items.addAll(values); notifyDataSetChanged(); }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ops_manager_category, parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        OpcaoCadastro item = items.get(position); holder.name.setText(item.getNome());
        holder.menu.setOnClickListener(view -> { if (listener != null) listener.onMenu(view, item); });
    }
    @Override public int getItemCount() { return items.size(); }
    static final class Holder extends RecyclerView.ViewHolder {
        final TextView name; final ImageView menu;
        Holder(View view) { super(view); name = view.findViewById(R.id.managerCategoryName); menu = view.findViewById(R.id.managerCategoryMenu); }
    }
}
