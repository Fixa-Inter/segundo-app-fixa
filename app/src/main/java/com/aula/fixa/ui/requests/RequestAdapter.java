package com.aula.fixa.ui.requests;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.domain.model.RequestItem;

public class RequestAdapter extends ListAdapter<RequestItem, RequestAdapter.ViewHolder> {

    public interface Listener {
        void onRequestClick(RequestItem item);

        void onRequestMenuClick(RequestItem item, View anchor);
    }

    private final Listener listener;

    public RequestAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final View accent;
        private final TextView date;
        private final TextView title;
        private final TextView location;
        private final TextView status;
        private final View menu;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            accent = itemView.findViewById(R.id.viewStatusAccent);
            date = itemView.findViewById(R.id.tvRequestDate);
            title = itemView.findViewById(R.id.tvRequestTitle);
            location = itemView.findViewById(R.id.tvRequestLocation);
            status = itemView.findViewById(R.id.tvRequestStatus);
            menu = itemView.findViewById(R.id.btnRequestMenu);
        }

        void bind(RequestItem item, Listener listener) {
            date.setText(item.date);
            title.setText(item.title);
            location.setText(item.location);
            status.setText(statusLabel(item.status));
            int color = ContextCompat.getColor(itemView.getContext(), statusColor(item.status));
            status.setTextColor(color);
            accent.setBackgroundColor(color);
            itemView.setOnClickListener(v -> listener.onRequestClick(item));
            menu.setOnClickListener(v -> listener.onRequestMenuClick(item, v));
        }

        @StringRes
        private static int statusLabel(RequestItem.Status status) {
            switch (status) {
                case APPROVED: return R.string.app_request_status_approved;
                case DONE: return R.string.app_request_status_done;
                case REFUSED: return R.string.app_request_status_refused;
                default: return R.string.app_request_status_analysis;
            }
        }

        @ColorRes
        static int statusColor(RequestItem.Status status) {
            switch (status) {
                case APPROVED: return R.color.app_status_approved;
                case DONE: return R.color.app_status_done_chart;
                case REFUSED: return R.color.app_status_refused;
                default: return R.color.app_status_analysis;
            }
        }
    }

    private static final DiffUtil.ItemCallback<RequestItem> DIFF = new DiffUtil.ItemCallback<RequestItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull RequestItem a, @NonNull RequestItem b) {
            return a == b;
        }

        @Override
        public boolean areContentsTheSame(@NonNull RequestItem a, @NonNull RequestItem b) {
            return a.title.equals(b.title) && a.location.equals(b.location)
                    && a.date.equals(b.date) && a.status == b.status;
        }
    };
}
