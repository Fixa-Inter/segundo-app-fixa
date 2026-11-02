package com.aula.fixa.ui.view.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.api.NotificacaoApi;
import com.aula.fixa.infrastructure.remote.dto.NotificacaoResponse;
import com.aula.fixa.ui.view.operations.OperationsActivity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {
    private static final String TAG = "NotificationsFragment";
    private final List<NotificacaoResponse> notifications = new ArrayList<>();
    private NotificationAdapter adapter;
    private RecyclerView list;
    private View empty;
    private NotificacaoApi api;

    public NotificationsFragment() { super(R.layout.fragment_notifications); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        list = view.findViewById(R.id.rvNotifications);
        empty = view.findViewById(R.id.tvNotificationsEmpty);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationAdapter();
        list.setAdapter(adapter);
        api = ApiClient.notificacoes(new SessionStorage(requireContext()));

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                            @NonNull RecyclerView.ViewHolder target) { return false; }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder holder, int direction) {
                int position = holder.getBindingAdapterPosition();
                if (position < 0 || position >= adapter.rows.size()) return;
                DisplayRow row = adapter.rows.get(position);
                if (row.notification == null) {
                    adapter.notifyItemChanged(position);
                    return;
                }
                deletar(row.notification);
            }
        }).attachToRecyclerView(list);

        carregar();
    }

    private void carregar() {
        api.listar().enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<List<NotificacaoResponse>> call,
                                             @NonNull Response<List<NotificacaoResponse>> response) {
                if (!isAdded()) return;
                if (!response.isSuccessful()) {
                    String body = "";
                    try {
                        if (response.errorBody() != null) body = response.errorBody().string();
                    } catch (Exception error) {
                        Log.e(TAG, "Falha lendo o corpo de erro do GET /api/v1/notificacao", error);
                    }
                    Log.e(TAG, "GET /api/v1/notificacao falhou: HTTP " + response.code() + " - " + body);
                    Toast.makeText(requireContext(), "Não foi possível carregar as notificações.", Toast.LENGTH_LONG).show();
                    return;
                }
                notifications.clear();
                if (response.body() != null) notifications.addAll(response.body());
                notifications.sort(Comparator.comparing(NotificationsFragment::createdAt).reversed());
                render();
            }
            @Override public void onFailure(@NonNull Call<List<NotificacaoResponse>> call, @NonNull Throwable error) {
                Log.e(TAG, "Erro de rede ou conversão no GET /api/v1/notificacao", error);
                if (isAdded()) Toast.makeText(requireContext(), "Não foi possível conectar ao serviço de notificações.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void render() {
        if (adapter == null || list == null) return;
        adapter.rows.clear();
        LocalDate today = LocalDate.now();
        boolean todayHeader = false, weekHeader = false, olderHeader = false;
        for (NotificacaoResponse notification : notifications) {
            LocalDate date = createdAt(notification).toLocalDate();
            if (date.equals(today)) {
                if (!todayHeader) adapter.rows.add(DisplayRow.header(getString(R.string.app_notifications_today)));
                todayHeader = true;
            } else if (!date.isBefore(today.minusDays(6))) {
                if (!weekHeader) adapter.rows.add(DisplayRow.header(getString(R.string.app_notifications_last_week)));
                weekHeader = true;
            } else {
                if (!olderHeader) adapter.rows.add(DisplayRow.header(getString(R.string.app_notifications_older)));
                olderHeader = true;
            }
            adapter.rows.add(DisplayRow.item(notification));
        }
        adapter.notifyDataSetChanged();
        empty.setVisibility(notifications.isEmpty() ? View.VISIBLE : View.GONE);
        list.setVisibility(notifications.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void marcarVisualizada(NotificacaoResponse item) {
        if (item.visualizada) return;
        api.marcarVisualizada(item.id).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<NotificacaoResponse> call,
                                             @NonNull Response<NotificacaoResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    for (int i = 0; i < notifications.size(); i++) {
                        if (item.id.equals(notifications.get(i).id)) {
                            notifications.set(i, response.body());
                            break;
                        }
                    }
                    render();
                } else {
                    Toast.makeText(requireContext(), "Não foi possível atualizar a notificação.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(@NonNull Call<NotificacaoResponse> call, @NonNull Throwable error) {
                if (isAdded()) Toast.makeText(requireContext(), "Não foi possível atualizar a notificação.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void abrirNotificacao(NotificacaoResponse item) {
        if (!item.visualizada) {
            marcarVisualizada(item);
            item.visualizada = true;
            render();
        }
        if (item.referenciaId == null || item.referenciaId.isBlank()) return;
        long referenceId;
        try {
            referenceId = Long.parseLong(item.referenciaId);
        } catch (NumberFormatException ignored) {
            // Checklist references are Mongo IDs; their detail navigation is handled by its own flow.
            return;
        }
        if (referenceId <= 0) return;
        if (requireActivity() instanceof OperationsActivity) {
            OperationsActivity activity = (OperationsActivity) requireActivity();
            if (item.tipoNotificacao == 1) activity.openEventDetails(referenceId);
            else if (item.tipoNotificacao == 2) activity.openRequestDetails(referenceId, "");
            else if (item.tipoNotificacao == 3) activity.openOrderDetails(referenceId, "");
            return;
        }

        NavController navigation = Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        if (item.tipoNotificacao == 1) {
            args.putLong("eventId", referenceId);
            navigation.navigate(R.id.eventInfoFragment, args);
        } else if (item.tipoNotificacao == 2) {
            args.putLong("requestId", referenceId);
            args.putString("requestTitle", "");
            navigation.navigate(R.id.requestDetailsFragment, args);
        }
    }

    private void deletar(NotificacaoResponse item) {
        api.deletar(item.id).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    notifications.removeIf(value -> item.id.equals(value.id));
                    render();
                } else {
                    Toast.makeText(requireContext(), "Não foi possível excluir a notificação.", Toast.LENGTH_SHORT).show();
                    render();
                }
            }
            @Override public void onFailure(@NonNull Call<Void> call, @NonNull Throwable error) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Não foi possível excluir a notificação.", Toast.LENGTH_SHORT).show();
                    render();
                }
            }
        });
    }

    private static LocalDateTime createdAt(NotificacaoResponse notification) {
        try { return LocalDateTime.parse(notification.dataCriacao); }
        catch (Exception ignored) { return LocalDateTime.MIN; }
    }

    private static final class DisplayRow {
        final String header;
        final NotificacaoResponse notification;
        private DisplayRow(String header, NotificacaoResponse notification) { this.header = header; this.notification = notification; }
        static DisplayRow header(String value) { return new DisplayRow(value, null); }
        static DisplayRow item(NotificacaoResponse value) { return new DisplayRow(null, value); }
    }

    private final class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;
        private final List<DisplayRow> rows = new ArrayList<>();

        @Override public int getItemViewType(int position) { return rows.get(position).notification == null ? TYPE_HEADER : TYPE_ITEM; }

        @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
            int layout = type == TYPE_HEADER ? R.layout.item_app_notification_header : R.layout.item_app_notification;
            View item = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
            return new RecyclerView.ViewHolder(item) { };
        }

        @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            DisplayRow row = rows.get(position);
            if (row.notification == null) {
                ((TextView) holder.itemView).setText(row.header);
                return;
            }
            NotificacaoResponse item = row.notification;
            ((TextView) holder.itemView.findViewById(R.id.tvNotificationText)).setText(item.mensagem);
            ((TextView) holder.itemView.findViewById(R.id.tvNotificationTime)).setText(createdAt(item).format(DateTimeFormatter.ofPattern("HH:mm")));
            holder.itemView.findViewById(R.id.viewUnreadDot).setVisibility(item.visualizada ? View.GONE : View.VISIBLE);
            ImageView typeIcon = holder.itemView.findViewById(R.id.ivNotificationType);
            int iconResource;
            if (item.tipoNotificacao == 1) iconResource = R.drawable.ic_calendar_24;
            else if (item.tipoNotificacao == 2) iconResource = R.drawable.ic_mail_24;
            else if (item.tipoNotificacao == 3) iconResource = R.drawable.figma_bea42;
            else if (item.tipoNotificacao == 4) iconResource = R.drawable.figma_a40a3;
            else iconResource = 0;
            if (iconResource == 0) {
                typeIcon.setVisibility(View.GONE);
            } else {
                typeIcon.setVisibility(View.VISIBLE);
                typeIcon.setImageResource(iconResource);
                if (item.tipoNotificacao == 3 || item.tipoNotificacao == 4) {
                    // Keep the original navigation Kanban and equipment checklist asset colors.
                    typeIcon.setImageTintList(null);
                    typeIcon.clearColorFilter();
                } else {
                    typeIcon.setColorFilter(androidx.core.content.ContextCompat.getColor(
                            holder.itemView.getContext(), R.color.app_primary_deep));
                }
            }
            holder.itemView.setBackgroundResource(item.visualizada
                    ? R.drawable.bg_app_notification_read : R.drawable.bg_app_notification_unread);
            holder.itemView.setOnClickListener(v -> abrirNotificacao(item));
        }

        @Override public int getItemCount() { return rows.size(); }
    }
}
