package com.aula.fixa.ui.adapter.equipamento;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.aula.fixa.R;
import com.aula.fixa.domain.model.equipamento.EquipamentoItem;
import java.util.ArrayList;
import java.util.List;

public final class EquipamentoAdapter extends RecyclerView.Adapter<EquipamentoAdapter.Holder> {
    public interface Listener { void onMenu(View anchor, EquipamentoItem item); }
    private final List<EquipamentoItem> items = new ArrayList<>();
    private final Listener listener;
    public EquipamentoAdapter(Listener listener) { this.listener = listener; }
    public void submit(List<EquipamentoItem> values) {
        items.clear();
        if (values != null) items.addAll(values);
        notifyDataSetChanged();
    }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ops_manager_device, parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        EquipamentoItem item = items.get(position);
        holder.code.setText(item.codigo);
        holder.local.setText(item.localUnidade);
        holder.menu.setOnClickListener(view -> { if (listener != null) listener.onMenu(view, item); });
    }
    @Override public int getItemCount() { return items.size(); }
    static final class Holder extends RecyclerView.ViewHolder {
        final TextView code, local;
        final ImageView menu;
        Holder(View root) {
            super(root);
            code = root.findViewById(R.id.deviceCode);
            local = root.findViewById(R.id.deviceLocal);
            menu = root.findViewById(R.id.deviceMenu);
        }
    }
}
