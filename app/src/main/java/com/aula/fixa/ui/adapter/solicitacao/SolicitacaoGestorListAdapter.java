package com.aula.fixa.ui.adapter.solicitacao;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.domain.model.solicitacao.SolicitacaoGestor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** RecyclerView adapter for the manager's pending request cards and empty state. */
public final class SolicitacaoGestorListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface Listener { void onRequestSelected(SolicitacaoGestor request); }

    private static final int TYPE_REQUEST = 0;
    private static final int TYPE_EMPTY = 1;
    private final List<SolicitacaoGestor> items = new ArrayList<>();
    private final Listener listener;
    private final int emptyText;

    public SolicitacaoGestorListAdapter(Listener listener) {
        this(listener, R.string.ops_manager_no_pending_requests);
    }

    public SolicitacaoGestorListAdapter(Listener listener, int emptyText) {
        this.listener = listener;
        this.emptyText = emptyText;
        setHasStableIds(true);
    }

    public void submitList(List<SolicitacaoGestor> requests) {
        items.clear();
        if (requests != null) items.addAll(requests);
        notifyDataSetChanged();
    }

    @Override public int getItemViewType(int position) {
        return items.isEmpty() ? TYPE_EMPTY : TYPE_REQUEST;
    }

    @Override public long getItemId(int position) {
        return items.isEmpty() ? Long.MIN_VALUE : items.get(position).id;
    }

    @Override public int getItemCount() { return items.isEmpty() ? 1 : items.size(); }

    @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_EMPTY) {
            return new EmptyHolder(inflater.inflate(R.layout.item_ops_manager_request_empty, parent, false));
        }
        return new RequestHolder(inflater.inflate(R.layout.item_ops_manager_home_request, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RequestHolder) {
            SolicitacaoGestor request = items.get(position);
            RequestHolder requestHolder = (RequestHolder) holder;
            String title = request.titulo.trim().isEmpty()
                    ? holder.itemView.getContext().getString(R.string.ops_home_untitled_request) : request.titulo.trim();
            String location = request.local.trim().isEmpty() ? request.descricaoLocal.trim()
                    : request.descricaoLocal.trim().isEmpty() ? request.local.trim()
                    : request.local.trim() + " - " + request.descricaoLocal.trim();
            String requester = request.solicitante.trim().isEmpty()
                    ? holder.itemView.getContext().getString(R.string.ops_home_unknown_requester) : request.solicitante.trim();
            requestHolder.date.setText(formatDate(holder.itemView, request.dataCriacao));
            requestHolder.title.setText(title);
            requestHolder.location.setText(location.isEmpty()
                    ? holder.itemView.getContext().getString(R.string.ops_home_unknown_location) : location);
            requestHolder.owner.setText(holder.itemView.getContext().getString(R.string.ops_home_request_owner, requester));
            requestHolder.avatar.setImageResource(R.drawable.img_avatar_115);
            requestHolder.itemView.setContentDescription(title + ", " + location + ", " + requester);
            requestHolder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onRequestSelected(request);
            });
        } else if (holder instanceof EmptyHolder) {
            ((EmptyHolder) holder).text.setText(emptyText);
        }
    }

    private static String formatDate(View view, String value) {
        if (value == null || value.trim().isEmpty()) return view.getContext().getString(R.string.ops_home_unknown_date);
        try {
            String date = value.trim();
            Date parsed = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
                    .parse(date.substring(0, Math.min(10, date.length())));
            return new SimpleDateFormat("dd MMM yyyy", new Locale("pt", "BR")).format(parsed);
        } catch (Exception ignored) {
            return view.getContext().getString(R.string.ops_home_unknown_date);
        }
    }

    private static final class RequestHolder extends RecyclerView.ViewHolder {
        final TextView date, title, location, owner;
        final ImageView avatar;
        RequestHolder(@NonNull View view) {
            super(view);
            date = view.findViewById(R.id.opsHomeRequestDate);
            title = view.findViewById(R.id.opsHomeRequestTitle);
            location = view.findViewById(R.id.opsHomeRequestLocation);
            owner = view.findViewById(R.id.opsHomeRequestOwner);
            avatar = view.findViewById(R.id.opsHomeRequestAvatar);
        }
    }

    private static final class EmptyHolder extends RecyclerView.ViewHolder {
        final TextView text;
        EmptyHolder(@NonNull View view) { super(view); text = view.findViewById(R.id.opsManagerRequestEmptyText); }
    }
}
