package com.aula.fixa.ui.requests;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

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

public class RequestsFragment extends Fragment implements RequestAdapter.Listener {

    private RequestAdapter adapter;
    private View emptyView;
    private RecyclerView list;
    private final List<RequestItem> apiItems = new ArrayList<>();

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

        RadioGroup filters = view.findViewById(R.id.groupFilters);
        filters.setOnCheckedChangeListener((group, checkedId) -> render(statusFor(checkedId)));

        view.findViewById(R.id.btnFilter).setOnClickListener(v ->
                new RequestFiltersSheet().show(getParentFragmentManager(), RequestFiltersSheet.TAG));

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
                            "",
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
            if (filter == null || item.status == filter) {
                items.add(item);
            }
        }
        adapter.submitList(items);
        boolean empty = items.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        list.setVisibility(empty ? View.GONE : View.VISIBLE);
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
