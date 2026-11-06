package com.aula.fixa.ui.view.chatbot;

import android.net.Uri;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.ui.adapter.chatbot.ChatAttachmentAdapter;

import java.util.ArrayList;
import java.util.List;

/** Keeps attachment preview rendering isolated from the conversation flow coordinator. */
final class ChatAttachmentPreview {
    private final RecyclerView list;
    private final ChatAttachmentAdapter adapter;

    ChatAttachmentPreview(RecyclerView list, ChatAttachmentAdapter adapter) {
        this.list = list;
        this.adapter = adapter;
    }

    void render(List<String> uploaded, List<Uri> pending) {
        if (list == null || adapter == null) return;
        List<ChatAttachmentAdapter.Item> items = new ArrayList<>();
        if (uploaded != null) for (String url : uploaded) items.add(ChatAttachmentAdapter.Item.uploaded(url));
        if (pending != null) for (Uri uri : pending) items.add(ChatAttachmentAdapter.Item.pending(uri));
        adapter.submitList(items);
        list.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
