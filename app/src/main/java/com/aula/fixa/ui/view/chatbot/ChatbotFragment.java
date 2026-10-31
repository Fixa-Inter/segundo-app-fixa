package com.aula.fixa.ui.view.chatbot;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;

public class ChatbotFragment extends Fragment {

    public ChatbotFragment() {
        super(R.layout.fragment_chatbot);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DrawerLayout drawer = view.findViewById(R.id.drawerChat);
        ((TextView) view.findViewById(R.id.tvGreeting)).setText(getString(R.string.app_chat_greeting, "Rapha"));

        view.findViewById(R.id.btnMenu).setOnClickListener(v -> drawer.openDrawer(Gravity.START));

        RecyclerView recent = view.findViewById(R.id.rvRecentChats);
        RecentAdapter recentAdapter = new RecentAdapter(Arrays.asList(getResources().getStringArray(R.array.app_chat_recent_items)));
        recent.setAdapter(recentAdapter);
        EditText recentSearch = view.findViewById(R.id.etRecentSearch);
        view.findViewById(R.id.btnDrawerSearch).setOnClickListener(v -> {
            if (recentSearch.getVisibility() != View.VISIBLE) {
                recentSearch.setVisibility(View.VISIBLE);
                recentSearch.requestFocus();
                recentSearch.post(() -> ((InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .showSoftInput(recentSearch, InputMethodManager.SHOW_IMPLICIT));
            } else {
                ((InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(recentSearch.getWindowToken(), 0);
                recentSearch.setText("");
                recentSearch.setVisibility(View.GONE);
            }
        });
        recentSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { recentAdapter.filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) { }
        });
        view.findViewById(R.id.btnNewChat).setOnClickListener(v -> drawer.closeDrawers());
    }

    static class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {
        private final List<String> items;
        private final List<String> allItems;
        private int selected = 0;

        RecentAdapter(List<String> items) {
            this.allItems = new ArrayList<>(items);
            this.items = new ArrayList<>(items);
        }

        void filter(String query) {
            items.clear();
            String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
            for (String item : allItems) {
                if (normalized.isEmpty() || item.toLowerCase(Locale.ROOT).contains(normalized)) items.add(item);
            }
            selected = 0;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_app_chat_recent, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TextView text = (TextView) holder.itemView;
            text.setText(items.get(position));
            text.setSelected(position == selected);
            text.setBackgroundResource(position == selected ? R.drawable.bg_app_chat_recent_selected : 0);
            text.setOnClickListener(v -> {
                int old = selected;
                selected = holder.getBindingAdapterPosition();
                notifyItemChanged(old);
                notifyItemChanged(selected);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}
