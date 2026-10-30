package com.aula.fixa.ui.adapter.equipamento;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.aula.fixa.R;
import com.aula.fixa.domain.model.equipamento.ModeloEquipamentoItem;
import java.util.ArrayList;
import java.util.List;

public final class ModeloEquipamentoAdapter extends RecyclerView.Adapter<ModeloEquipamentoAdapter.Holder> {
    public interface Listener { void onMenu(View anchor, ModeloEquipamentoItem modelo); void onSelect(ModeloEquipamentoItem modelo); }
    private final List<ModeloEquipamentoItem> items = new ArrayList<>();
    private final Listener listener;
    public ModeloEquipamentoAdapter(Listener listener) { this.listener = listener; }
    public void submit(List<ModeloEquipamentoItem> values) { items.clear(); if (values != null) items.addAll(values); notifyDataSetChanged(); }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ops_manager_model, parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder h, int position) {
        ModeloEquipamentoItem value = items.get(position);
        h.name.setText(value.nome); h.category.setText(value.categoria); h.brand.setText(value.marca);
        h.devices.setText(String.valueOf(value.quantidadeAparelhos));
        h.menu.setOnClickListener(v -> { if (listener != null) listener.onMenu(v, value); });
        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onSelect(value); });
    }
    @Override public int getItemCount() { return items.size(); }
    static final class Holder extends RecyclerView.ViewHolder {
        final TextView name, category, brand, devices; final ImageView menu;
        Holder(View root) { super(root); name=root.findViewById(R.id.modelName); category=root.findViewById(R.id.modelCategory); brand=root.findViewById(R.id.modelBrand); devices=root.findViewById(R.id.modelDevices); menu=root.findViewById(R.id.modelMenu); }
    }
}
