package com.aula.fixa.ui.adapter.checklist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.domain.model.checklist.ChecklistTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Renderiza e pesquisa checklists; as ações da tela são expostas por callbacks.
 */
public final class ChecklistCardsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface Callbacks {
        void onSelect(ChecklistTemplate template);

        void onMenu(ChecklistTemplate template, View anchor);

        void onRetry();
    }

    public interface Presenter {
        String categories(ChecklistTemplate template);

        String status(ChecklistTemplate template);
    }

    private static final int TYPE_CARD = 0;
    private static final int TYPE_EMPTY = 1;
    private final List<ChecklistTemplate> all = new ArrayList<>();
    private final List<ChecklistTemplate> visible = new ArrayList<>();
    private final Callbacks callbacks;
    private final Presenter presenter;
    private final boolean manager;
    private String query = "";
    private boolean loadFailed;

    public ChecklistCardsAdapter(boolean manager, Presenter presenter, Callbacks callbacks) {
        this.manager = manager;
        this.presenter = presenter;
        this.callbacks = callbacks;
    }

    public void submitList(List<ChecklistTemplate> values, boolean failed) {
        all.clear();
        if (values != null) {
            all.addAll(values);
        }
        loadFailed = failed;
        rebuild();
    }

    public void filter(String value) {
        query = value == null ? "" : value.trim();
        rebuild();
    }

    private void rebuild() {
        boolean wasEmpty = visible.isEmpty();
        List<ChecklistTemplate> next = new ArrayList<>();
        String normalized = query.toLowerCase(Locale.ROOT);
        for (ChecklistTemplate template : all) {
            String name = template.nome == null ? "" : template.nome;
            if (normalized.isEmpty() || name.toLowerCase(Locale.ROOT).contains(normalized)) {
                next.add(template);
            }
        }
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return visible.size();
            }

            @Override
            public int getNewListSize() {
                return next.size();
            }

            @Override
            public boolean areItemsTheSame(int oldPosition, int newPosition) {
                return sameId(visible.get(oldPosition), next.get(newPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldPosition, int newPosition) {
                ChecklistTemplate oldItem = visible.get(oldPosition), newItem = next.get(newPosition);
                return safe(oldItem.nome).equals(safe(newItem.nome))
                        && java.util.Objects.equals(oldItem.periodicidade, newItem.periodicidade)
                        && presenter.categories(oldItem).equals(presenter.categories(newItem))
                        && presenter.status(oldItem).equals(presenter.status(newItem))
                        && count(oldItem) == count(newItem);
            }
        });
        visible.clear();
        visible.addAll(next);
        if (wasEmpty || visible.isEmpty()) {
            notifyDataSetChanged();
        } else {
            diff.dispatchUpdatesTo(this);
        }
    }

    private static boolean sameId(ChecklistTemplate left, ChecklistTemplate right) {
        return left == right || left != null && right != null && safe(left.id).equals(safe(right.id));
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static int count(ChecklistTemplate value) {
        return value.modelos == null ? 0 : value.modelos.size();
    }

    @Override
    public int getItemViewType(int position) {
        return visible.isEmpty() ? TYPE_EMPTY : TYPE_CARD;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        if (type == TYPE_EMPTY) {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ops_checklist_empty, parent, false);
            return new EmptyHolder(item);
        }
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ops_manager_checklist, parent, false);
        return new CardHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder rawHolder, int position) {
        if (rawHolder instanceof EmptyHolder) {
            EmptyHolder holder = (EmptyHolder) rawHolder;
            int message = loadFailed ? R.string.ops_checklist_load_failed
                    : all.isEmpty() ? R.string.ops_checklist_empty : R.string.ops_checklist_no_matches;
            holder.message.setText(message);
            holder.itemView.setOnClickListener(loadFailed ? v -> callbacks.onRetry() : null);
            return;
        }

        ChecklistTemplate template = visible.get(position);
        CardHolder holder = (CardHolder) rawHolder;

        holder.title.setText(safe(template.nome));
        holder.category.setText(presenter.categories(template));
        holder.period.setText(template.periodicidade == null ? "_" : holder.itemView.getResources().getString(R.string.ops_checklist_period_days, template.periodicidade));
        holder.status.setText(presenter.status(template));
        holder.models.setText(String.valueOf(count(template)));
        holder.menu.setVisibility(manager ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> callbacks.onSelect(template));
        holder.menu.setOnClickListener(manager ? v -> callbacks.onMenu(template, holder.menu) : null);
    }

    @Override
    public int getItemCount() {
        return visible.isEmpty() ? 1 : visible.size();
    }

    private static final class CardHolder extends RecyclerView.ViewHolder {
        final TextView title, category, period, status, models;
        final View menu;

        CardHolder(View item) {
            super(item);
            title = item.findViewById(R.id.checklistCardTitle);
            category = item.findViewById(R.id.checklistCardCategory);
            period = item.findViewById(R.id.checklistCardPeriod);
            status = item.findViewById(R.id.checklistCardStatus);
            models = item.findViewById(R.id.checklistCardModels);
            menu = item.findViewById(R.id.checklistCardMenu);
        }
    }

    private static final class EmptyHolder extends RecyclerView.ViewHolder {
        final TextView message;

        EmptyHolder(View item) {
            super(item);
            message = item.findViewById(R.id.checklistEmptyMessage);
        }
    }
}
