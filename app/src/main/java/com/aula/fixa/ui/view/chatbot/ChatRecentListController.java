package com.aula.fixa.ui.view.chatbot;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.domain.model.chat.ChatHistory;
import com.aula.fixa.domain.model.chat.ChatPendingDraft;
import com.aula.fixa.ui.adapter.chatbot.RecentChatsAdapter;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Owns recent chat list rendering, filtering, selected state, and display titles. */
final class ChatRecentListController {
    interface Actions {
        void onSelect(ChatHistory history);
        void onMenu(ChatHistory history, View anchor);
    }

    private final Fragment fragment;
    private final List<ChatHistory> histories;
    private final TextView empty;
    private final RecentChatsAdapter adapter;

    ChatRecentListController(Fragment fragment, RecyclerView list, TextView empty,
                             List<ChatHistory> histories, Actions actions) {
        this.fragment = fragment;
        this.histories = histories;
        this.empty = empty;
        adapter = new RecentChatsAdapter(this::title, new RecentChatsAdapter.Callbacks() {
            @Override public void onSelect(ChatHistory history) { actions.onSelect(history); }
            @Override public void onMenu(ChatHistory history, View anchor) { actions.onMenu(history, anchor); }
        }, fragment.getString(R.string.app_chat_recent_draft_only));
        list.setLayoutManager(new LinearLayoutManager(fragment.requireContext()));
        list.setAdapter(adapter);
    }

    void submit(ChatPendingDraft draft, String selectedId) {
        adapter.submitList(histories, draft);
        adapter.setSelectedThreadId(selectedId);
        updateEmptyState();
    }

    void filter(String query) { adapter.filter(query); updateEmptyState(); }
    void select(String id) { adapter.setSelectedThreadId(id); }
    boolean isEmpty() { return adapter.isEmpty(); }

    String title(ChatHistory history) {
        if (history.title != null && !history.title.trim().isEmpty()) return history.title.trim();
        String date = sessionDate(history.updatedAt);
        return date.isEmpty() ? fragment.getString(R.string.app_chat_new_conversation)
                : fragment.getString(R.string.app_chat_new_conversation_date, date);
    }

    String titleForThread(String threadId) {
        if (threadId == null) return fragment.getString(R.string.app_chat_title);
        for (ChatHistory history : histories) if (threadId.equals(history.sessionId)) return title(history);
        return fragment.getString(R.string.app_chat_title);
    }

    private void updateEmptyState() {
        if (empty == null) return;
        empty.setVisibility(adapter.isEmpty() ? View.VISIBLE : View.GONE);
        empty.setText(adapter.hasSourceItems() ? R.string.app_chat_recent_no_matches : R.string.app_chat_recent_empty);
    }

    private String sessionDate(String iso) {
        if (iso == null || iso.trim().isEmpty()) return "";
        try {
            String normalized = iso.trim();
            if (!normalized.endsWith("Z") && !normalized.matches(".*[+-]\\d\\d:\\d\\d$")) normalized += "Z";
            Instant instant = OffsetDateTime.parse(normalized).toInstant();
            return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date.from(instant));
        } catch (Exception ignored) { return ""; }
    }
}
