package com.aula.fixa.ui.adapter.chatbot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.domain.model.chat.ChatHistory;
import com.aula.fixa.domain.model.chat.ChatPendingDraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Renderiza e filtra as conversas recentes, delegando seleção e menu à tela. */
public final class RecentChatsAdapter extends RecyclerView.Adapter<RecentChatsAdapter.Holder> {
    public static final String LOCAL_DRAFT_ID = "__local_unconfirmed_chat_draft__";
    public interface Callbacks {
        void onSelect(ChatHistory history);
        void onMenu(ChatHistory history, View anchor);
    }
    public interface TitleProvider { String title(ChatHistory history); }

    private final List<Row> all = new ArrayList<>();
    private final List<Row> visible = new ArrayList<>();
    private final TitleProvider titles;
    private final Callbacks callbacks;
    private final String localDraftTitle;
    private String query = "";
    private String selectedThreadId;

    public RecentChatsAdapter(TitleProvider titles, Callbacks callbacks, String localDraftTitle) {
        this.titles = titles;
        this.callbacks = callbacks;
        this.localDraftTitle = localDraftTitle;
    }

    public void submitList(List<ChatHistory> histories, ChatPendingDraft draft) {
        all.clear();
        if (histories != null) {
            for (ChatHistory history : histories) {
                boolean hasDraft = draft != null && draft.threadId != null
                        && draft.threadId.equals(history.sessionId);
                all.add(new Row(history, hasDraft, false, null));
            }
        }
        if (draft != null && (draft.threadId == null || draft.threadId.trim().isEmpty())) {
            ChatHistory local = new ChatHistory();
            local.sessionId = LOCAL_DRAFT_ID;
            local.title = "";
            all.add(0, new Row(local, true, true, localDraftTitle));
        }
        applyFilter();
    }

    public void filter(String value) {
        query = value == null ? "" : value.trim();
        applyFilter();
    }

    public void setSelectedThreadId(String threadId) {
        selectedThreadId = threadId;
        notifyDataSetChanged();
    }

    public boolean isEmpty() { return visible.isEmpty(); }
    public boolean hasSourceItems() { return !all.isEmpty(); }

    private void applyFilter() {
        visible.clear();
        String normalized = query.toLowerCase(Locale.ROOT);
        for (Row row : all) {
            if (normalized.isEmpty() || title(row).toLowerCase(Locale.ROOT).contains(normalized)) visible.add(row);
        }
        notifyDataSetChanged();
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_chat_recent, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        Row row = visible.get(position);
        ChatHistory history = row.history;
        holder.title.setText(title(row));
        boolean selected = history.sessionId != null && history.sessionId.equals(selectedThreadId);
        holder.title.setSelected(selected);
        holder.title.setBackgroundResource(selected ? R.drawable.bg_app_chat_recent_selected : android.R.color.transparent);
        holder.draftIndicator.setVisibility(row.hasDraft ? View.VISIBLE : View.GONE);
        holder.menu.setVisibility(row.localDraft ? View.GONE : View.VISIBLE);
        holder.title.setOnClickListener(v -> callbacks.onSelect(history));
        if (row.localDraft) holder.menu.setOnClickListener(null);
        else holder.menu.setOnClickListener(v -> callbacks.onMenu(history, holder.menu));
    }

    @Override public int getItemCount() { return visible.size(); }

    private String title(Row row) { return row.localDraft ? row.localDraftTitle : titles.title(row.history); }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView menu;
        final ImageView draftIndicator;
        Holder(View item) {
            super(item);
            title = item.findViewById(R.id.tvRecentChat);
            menu = item.findViewById(R.id.btnRecentMenu);
            draftIndicator = item.findViewById(R.id.ivRecentChatDraft);
        }
    }

    private static final class Row {
        final ChatHistory history;
        final boolean hasDraft;
        final boolean localDraft;
        final String localDraftTitle;
        Row(ChatHistory history, boolean hasDraft, boolean localDraft, String localDraftTitle) {
            this.history = history;
            this.hasDraft = hasDraft;
            this.localDraft = localDraft;
            this.localDraftTitle = localDraftTitle;
        }
    }
}
