package com.aula.fixa.ui.adapter.os;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.domain.model.os.OrdemServicoQuadro;
import com.aula.fixa.ui.config.EmptyStateStyler;

import java.util.ArrayList;
import java.util.List;

public final class QuadroOSAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface Listener {
        void onClick(OrdemServicoQuadro os);

        void onMenuClick(View anchor, OrdemServicoQuadro os);
    }

    private static final int TYPE_ORDER = 0, TYPE_EMPTY = 1;
    private final List<OrdemServicoQuadro> items = new ArrayList<>();
    private final Listener listener;
    private final boolean canManage;
    private final int itemLayout;
    private CharSequence emptyMessage;
    private Runnable emptyClick;

    public QuadroOSAdapter(Listener listener) {
        this(listener, true, R.layout.item_ops_manager_board_order);
    }

    public QuadroOSAdapter(Listener listener, boolean canManage) {
        this(listener, canManage, R.layout.item_ops_manager_board_order);
    }

    public QuadroOSAdapter(Listener listener, boolean canManage, int itemLayout) {
        this.listener = listener;
        this.canManage = canManage;
        this.itemLayout = itemLayout;
    }

    public void submit(List<OrdemServicoQuadro> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    public void setEmptyState(CharSequence message, Runnable onClick) {
        emptyMessage = message;
        emptyClick = onClick;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.isEmpty() ? TYPE_EMPTY : TYPE_ORDER;
    }

    @Override
    public int getItemCount() {
        return items.isEmpty() ? 1 : items.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
        if (t == TYPE_EMPTY) {
            return new EmptyHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_ops_manager_request_empty, p, false));
        }
        return new Holder(LayoutInflater.from(p.getContext()).inflate(itemLayout, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof EmptyHolder) {
            EmptyHolder empty = (EmptyHolder) holder;
            empty.text.setText(emptyMessage == null ? "Nenhum resultado encontrado." : emptyMessage);
            if (emptyClick == null) {
                empty.itemView.setOnClickListener(null);
            } else {
                empty.itemView.setOnClickListener(v -> emptyClick.run());
            }
            return;
        }
        Holder h = (Holder) holder;
        OrdemServicoQuadro os = items.get(position);
        String priority = priorityCode(os.prioridade);
        h.title.setText(os.titulo == null ? "" : os.titulo);
        h.category.setText(join(os.categoriaProblema, os.categoriaEquipamento));
        h.date.setText(formatDate(os.dataPrevista));
        h.priority.setText(priority);
        int color = priority.equals("P0") ? ContextCompat.getColor(h.itemView.getContext(), R.color.ops_color_ff96031a) : priority.equals("P1") ? Color.rgb(243, 190, 47) : Color.rgb(54, 119, 214);
        GradientDrawable bg = (GradientDrawable) ContextCompat.getDrawable(h.itemView.getContext(), R.drawable.bg_ops_39dc043443).mutate();
        bg.setColor(color);
        h.priority.setBackground(bg);
        h.priority.setTextColor(Color.WHITE);
        h.priority.setGravity(android.view.Gravity.CENTER);
        h.priority.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        h.priority.setIncludeFontPadding(false);
        GradientDrawable stripeBg = (GradientDrawable) ContextCompat.getDrawable(h.itemView.getContext(), R.drawable.bg_ops_9a48e661a7).mutate();
        stripeBg.setColor(color);
        h.priorityStripe.setBackground(stripeBg);
        String name = os.nomeUsuario == null || os.nomeUsuario.trim().isEmpty() ? "Técnico não atribuído" : "Atribuído para " + os.nomeUsuario + " (Técnico)";
        h.technician.setText(name);
        h.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(os);
            }
        });
        h.menu.setVisibility(canManage ? View.VISIBLE : View.GONE);
        if (canManage) {
            h.menu.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMenuClick(v, os);
                }
            });
        }
    }

    private String join(String a, String b) {
        if (a == null || a.isEmpty()) {
            return b == null ? "" : b;
        }
        if (b == null || b.isEmpty()) {
            return a;
        }
        return a + " • " + b;
    }

    private String priorityCode(String p) {
        if (p == null) {
            return "P2";
        }
        String s = p.toUpperCase(java.util.Locale.ROOT);
        if (s.contains("ALTA") || s.equals("P0")) {
            return "P0";
        }
        if (s.contains("MEDIA") || s.contains("MÉDIA") || s.equals("P1")) {
            return "P1";
        }
        return "P2";
    }

    private String formatDate(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        try {
            return java.time.LocalDate.parse(value).format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy", java.util.Locale.ENGLISH));
        } catch (Exception ignored) {
            return value;
        }
    }

    static final class EmptyHolder extends RecyclerView.ViewHolder {
        final TextView text;

        EmptyHolder(View v) {
            super(v);
            text = v.findViewById(R.id.opsManagerRequestEmptyText);
            EmptyStateStyler.apply(text);
        }
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView title, category, date, priority, technician;
        final View menu, priorityStripe;

        Holder(View v) {
            super(v);
            title = v.findViewById(R.id.orderTitle);
            category = v.findViewById(R.id.orderCategory);
            date = v.findViewById(R.id.orderDueDate);
            priority = v.findViewById(R.id.orderPriority);
            technician = v.findViewById(R.id.orderTechnician);
            menu = v.findViewById(R.id.orderMenu);
            priorityStripe = v.findViewById(R.id.orderPriorityStripe);
        }
    }
}
