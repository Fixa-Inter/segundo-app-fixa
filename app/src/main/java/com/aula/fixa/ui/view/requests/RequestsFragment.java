package com.aula.fixa.ui.view.requests;

import com.aula.fixa.domain.enums.StatusProblema;
import com.aula.fixa.ui.adapter.solicitacao.RequestAdapter;

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
import com.aula.fixa.ui.config.FragmentUi;
import com.aula.fixa.ui.config.DataChangeNotifier;
import com.aula.fixa.ui.config.AppText;
import com.aula.fixa.domain.model.solicitacao.ProblemaItem;
import com.aula.fixa.domain.model.solicitacao.Problema;
import com.aula.fixa.adapter.solicitacao.SolicitacaoViewModel;
import com.aula.fixa.adapter.solicitacao.SolicitacaoViewModelFactory;
import com.aula.fixa.adapter.cadastro.CadastroViewModel;
import com.aula.fixa.adapter.cadastro.CadastroViewModelFactory;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class RequestsFragment extends Fragment implements RequestAdapter.Listener, RequestFiltersSheet.Listener {

    private RequestAdapter adapter;
    private View emptyView;
    private RecyclerView list;
    private SolicitacaoViewModel viewModel;
    private CadastroViewModel cadastroViewModel;
    private final List<ProblemaItem> apiItems = new ArrayList<>();
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
        viewModel = new androidx.lifecycle.ViewModelProvider(this,
                new SolicitacaoViewModelFactory(requireContext())).get(SolicitacaoViewModel.class);
        cadastroViewModel = new androidx.lifecycle.ViewModelProvider(this,
                new CadastroViewModelFactory(requireContext())).get(CadastroViewModel.class);
        loadRequests();
        DataChangeNotifier.observe(getViewLifecycleOwner(), ignored -> {
            if (getView() == view) loadRequests();
        });

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
        View requestView = getView();
        if (requestView == null) return;
        viewModel.listar(new SolicitacaoViewModel.Listener() {
            @Override public void aoCarregar(boolean carregando) { }
            @Override public void aoCriar(Problema problema) { }
            @Override public void aoListar(List<Problema> values) {
                List<ProblemaItem> items = new ArrayList<>();
                for (Problema item : values) {
                    items.add(new ProblemaItem(
                            item.id,
                            item.titulo,
                            item.local,
                            item.dataCriacao,
                            status(item.status),
                            item.descricao,
                            item.categoria,
                            0)
                    );
                }
                FragmentUi.runIfViewActive(RequestsFragment.this, requestView,
                        () -> { apiItems.clear(); apiItems.addAll(items); render(null); });
            }
            @Override public void aoFalhar(Throwable error) {
                FragmentUi.runIfViewActive(RequestsFragment.this, requestView,
                        () -> android.widget.Toast.makeText(requestView.getContext(),
                                com.aula.fixa.adapter.auth.AutenticacaoViewModel.messageFrom(error),
                                android.widget.Toast.LENGTH_LONG).show());
            }
        });
    }

    private void showItems(List<ProblemaItem> items) {
        adapter.submitList(items);
        boolean empty = items.isEmpty();
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        list.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private StatusProblema status(String value) {
        if ("APROVADO".equalsIgnoreCase(value)) {
            return StatusProblema.APROVADO;
        }
        if ("REPROVADO".equalsIgnoreCase(value)) {
            return StatusProblema.REPROVADO;
        }
        if ("CONCLUIDO".equalsIgnoreCase(value) || "CONCLUÍDO".equalsIgnoreCase(value)) {
            return StatusProblema.CONCLUIDO;
        }
        if ("PENDENTE".equalsIgnoreCase(value)) {
            return StatusProblema.PENDENTE;
        }
        return StatusProblema.PENDENTE;
    }

    @Nullable
    private StatusProblema statusFor(int checkedId) {
        if (checkedId == R.id.chipAnalysis) {
            return StatusProblema.PENDENTE;
        }
        if (checkedId == R.id.chipApproved) {
            return StatusProblema.APROVADO;
        }
        if (checkedId == R.id.chipDone) {
            return StatusProblema.CONCLUIDO;
        }
        if (checkedId == R.id.chipRefused) {
            return StatusProblema.REPROVADO;
        }

        return null;
    }

    private void render(@Nullable StatusProblema filter) {
        List<ProblemaItem> items = new ArrayList<>();

        for (ProblemaItem item : apiItems) {
            boolean matchesSearch = search.trim().isEmpty() || item.title.toLowerCase().contains(search.toLowerCase());
            boolean matchesLocation = location.trim().isEmpty() || location.startsWith("Todos") || item.location.equalsIgnoreCase(location);
            boolean matchesCategory = category.trim().isEmpty() || category.startsWith("Todos") || item.device.equalsIgnoreCase(category);
            boolean matchesStatus = selectedStatus.trim().isEmpty() || selectedStatus.startsWith("Todas") || item.status == status(selectedStatus);
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
        if (period.trim().isEmpty() || period.equalsIgnoreCase("Todos")) return true;
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
    public void onRequestClick(ProblemaItem item) {
        Bundle args = new Bundle();

        args.putLong("requestId", item.id);
        args.putString("requestTitle", item.title);

        Navigation.findNavController(requireView()).navigate(R.id.action_requests_to_details, args);
    }

    @Override
    public void onRequestMenuClick(ProblemaItem item, View anchor) {
        RequestMenuPopup.show(anchor, item, viewModel, cadastroViewModel);
    }
}
