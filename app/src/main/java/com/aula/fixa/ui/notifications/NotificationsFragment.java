package com.aula.fixa.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.model.NotificationItem;

import java.util.Arrays;
import java.util.List;

public class NotificationsFragment extends Fragment {

    public NotificationsFragment() {
        super(R.layout.fragment_notifications);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        RecyclerView list = view.findViewById(R.id.rvNotifications);
        String sample = getString(R.string.app_notification_sample);
        list.setAdapter(new NotificationAdapter(Arrays.asList(
                NotificationItem.header(getString(R.string.app_notifications_today)),
                NotificationItem.item(sample, "11:40", true),
                NotificationItem.header(getString(R.string.app_notifications_last_week)),
                NotificationItem.item(sample, "11:40", false),
                NotificationItem.item(sample, "11:40", false),
                NotificationItem.item(sample, "11:40", false)
        )));
    }

    static class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;
        private final List<NotificationItem> items;

        NotificationAdapter(List<NotificationItem> items) {
            this.items = items;
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).isHeader() ? TYPE_HEADER : TYPE_ITEM;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layout = viewType == TYPE_HEADER ? R.layout.item_app_notification_header : R.layout.item_app_notification;
            View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
            return new RecyclerView.ViewHolder(view) {};
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            NotificationItem item = items.get(position);
            if (item.isHeader()) {
                ((TextView) holder.itemView).setText(item.header);
                return;
            }
            ((TextView) holder.itemView.findViewById(R.id.tvNotificationText)).setText(item.text);
            ((TextView) holder.itemView.findViewById(R.id.tvNotificationTime)).setText(item.time);
            holder.itemView.findViewById(R.id.viewUnreadDot).setVisibility(item.unread ? View.VISIBLE : View.GONE);
            holder.itemView.setBackgroundResource(item.unread
                    ? R.drawable.bg_app_notification_unread : R.drawable.bg_app_notification_read);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
}
