package com.aula.fixa.ui.requests;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.ui.common.AppText;
import com.aula.fixa.domain.model.RequestItem;
import com.aula.fixa.data.local.preferences.SessionStorage;
import com.aula.fixa.data.repository.RequestRepository;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class RequestsFragment extends Fragment implements RequestAdapter.Listener, RequestFiltersSheet.Listener {

    private RequestAdapter adapter;
    private View emptyView;
    private RecyclerView list;
    private final List<RequestItem> apiItems = new ArrayList<>();
    private String search = "", period = "", location = "", category = "", selectedStatus = "";

    public RequestsFragment() {
        super(R.layout.fragment_requests);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppText.setHtml(view.findViewById(R.id.tvTitle), R.string.app_requests_title);
        list = view.findViewById(R.id.rvRequests);
        emptyView = view.findViewById(R.id.layoutEmpty);
        adapter = new RequestAdapter(this);
        list.setAdapter(adapter);
        loadRequests();

        ((EditText) view.findViewById(R.id.etSearch)).addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { search = s.toString(); render(null); }
            public void afterTextChanged(Editable s) { }
        });

        RadioGroup filters = view.findViewById(R.id.groupFilters);
        filters.setOnCheckedChangeListener((group, checkedId) -> render(statusFor(checkedId)));

        view.findViewById(R.id.btnFilter).setOnClickListener(v -> {
            RequestFiltersSheet sheet = new RequestFiltersSheet();
            sheet.setTargetFragment(this, 0);
            sheet.show(getParentFragmentManager(), RequestFiltersSheet.TAG);
        });

        view.findViewById(R.id.fabNewRequest).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_requests_to_new_request));
    }

    private void loadRequests() {
        new Thread(() -> {
            try {
                JsonArray values = new RequestRepository(new SessionStorage(requireContext())).minhas();
                List<RequestItem> items = new ArrayList<>();
                for (var element : values) {
                    JsonObject item = element.getAsJsonObject();
                    items.add(new RequestItem(
                            item.get("id").getAsLong(),
                            item.get("titulo").getAsString(),
                            item.has("localUnidade") && !item.get("localUnidade").isJsonNull() ? item.get("localUnidade").getAsString() : "",
                            item.has("dataCriacao") ? item.get("dataCriacao").getAsString() : "",
                            status(item.get("status").getAsString()),
                            item.has("descricaoProblema") ? item.get("descricaoProblema").getAsString() : "",
                            item.has("categoriaEquipamento") && !item.get("categoriaEquipamento").isJsonNull() ? item.get("categoriaEquipamento").getAsString() : "",
                            0)
                    );
                }

                requireActivity().runOnUiThread(() -> { apiItems.clear(); apiItems.addAll(items); render(null); });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> android.widget.Toast.makeText(requireContext(), e.getMessage(), android.widget.Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void showItems(List<RequestItem> items) {
        adapter.submitList(items);
        boolean empty = items.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        list.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private RequestItem.Status status(String value) {
        if ("APROVADO".equalsIgnoreCase(value)) {
            return RequestItem.Status.APPROVED;
        }
        if ("REPROVADO".equalsIgnoreCase(value)) {
            return RequestItem.Status.REFUSED;
        }
        if ("CONCLUIDO".equalsIgnoreCase(value) || "CONCLUÍDO".equalsIgnoreCase(value)) {
            return RequestItem.Status.DONE;
        }
        return RequestItem.Status.ANALYSIS;
    }

    @Nullable
    private RequestItem.Status statusFor(int checkedId) {
        if (checkedId == R.id.chipAnalysis) {
            return RequestItem.Status.ANALYSIS;
        }
        if (checkedId == R.id.chipApproved) {
            return RequestItem.Status.APPROVED;
        }
        if (checkedId == R.id.chipDone) {
            return RequestItem.Status.DONE;
        }
        if (checkedId == R.id.chipRefused) {
            return RequestItem.Status.REFUSED;
        }

        return null;
    }

    private void render(@Nullable RequestItem.Status filter) {
        List<RequestItem> items = new ArrayList<>();

        for (RequestItem item : apiItems) {
            boolean matchesSearch = search.isBlank() || item.title.toLowerCase().contains(search.toLowerCase());
            boolean matchesLocation = location.isBlank() || location.startsWith("Todos") || item.location.equalsIgnoreCase(location);
            boolean matchesCategory = category.isBlank() || category.startsWith("Todos") || item.device.equalsIgnoreCase(category);
            boolean matchesStatus = selectedStatus.isBlank() || selectedStatus.startsWith("Todas") || item.status == status(selectedStatus);
            if ((filter == null || item.status == filter) && matchesSearch && matchesLocation && matchesCategory && matchesStatus && matchesPeriod(item.date)) {
                items.add(item);
            }
        }
        adapter.submitList(items);
        boolean empty = items.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        list.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private boolean matchesPeriod(String value) {
        if (period.isBlank() || period.equalsIgnoreCase("Todos")) return true;
        try {
            LocalDate date;
            try { date = LocalDateTime.parse(value).toLocalDate(); }
            catch (DateTimeParseException e) { date = LocalDate.parse(value); }
            long days = period.startsWith("Últimos 30") ? 30 : period.startsWith("Últimos 3") ? 90 : 365;
            return !date.isBefore(LocalDate.now().minusDays(days));
        } catch (Exception ignored) { return true; }
    }

    @Override
    public void onFilters(String period, String location, String category, String status) {
        this.period = period;
        this.location = location;
        this.category = category;
        this.selectedStatus = status;
        render(null);
    }

    @Override
    public void onRequestClick(RequestItem item) {
        Bundle args = new Bundle();

        args.putLong("requestId", item.id);
        args.putString("requestTitle", item.title);

        Navigation.findNavController(requireView()).navigate(R.id.action_requests_to_details, args);
    }

    @Override
    public void onRequestMenuClick(RequestItem item, View anchor) {
        RequestMenuPopup.show(anchor, item);
    }
}
