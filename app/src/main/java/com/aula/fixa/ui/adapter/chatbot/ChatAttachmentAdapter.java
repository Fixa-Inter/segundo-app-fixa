package com.aula.fixa.ui.adapter.chatbot;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/** Exibe anexos já enviados e imagens ainda em carregamento. */
public final class ChatAttachmentAdapter extends RecyclerView.Adapter<ChatAttachmentAdapter.Holder> {
    public interface OnRemove { void onRemove(String url); }

    public static final class Item {
        public final String url;
        public final Uri uri;

        private Item(String url, Uri uri) { this.url = url; this.uri = uri; }
        public static Item uploaded(String url) { return new Item(url, null); }
        public static Item pending(Uri uri) { return new Item(null, uri); }
    }

    private final List<Item> items = new ArrayList<>();
    private final OnRemove onRemove;

    public ChatAttachmentAdapter(OnRemove onRemove) { this.onRemove = onRemove; }

    public void submitList(List<Item> values) {
        items.clear();
        if (values != null) items.addAll(values);
        notifyDataSetChanged();
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_chat_attachment, parent, false);
        return new Holder(view);
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        Item item = items.get(position);
        if (item.uri != null) Glide.with(holder.image).load(item.uri).centerCrop().into(holder.image);
        else Glide.with(holder.image).load(item.url).centerCrop().into(holder.image);
        boolean removable = item.url != null;
        holder.remove.setVisibility(removable ? View.VISIBLE : View.GONE);
        holder.remove.setOnClickListener(removable ? v -> onRemove.onRemove(item.url) : null);
    }

    @Override public int getItemCount() { return items.size(); }

    static final class Holder extends RecyclerView.ViewHolder {
        final ImageView image;
        final ImageButton remove;
        Holder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ivChatAttachment);
            remove = itemView.findViewById(R.id.btnRemoveChatAttachment);
        }
    }
}
