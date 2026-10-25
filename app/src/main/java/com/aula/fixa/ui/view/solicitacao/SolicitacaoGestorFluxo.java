package com.aula.fixa.ui.view.solicitacao;

import android.app.Dialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.adapter.auth.AutenticacaoViewModel;
import com.aula.fixa.adapter.solicitacao.SolicitacaoGestorViewModel;
import com.aula.fixa.adapter.solicitacao.SolicitacaoGestorViewModelFactory;
import com.aula.fixa.domain.enums.StatusProblema;
import com.aula.fixa.domain.model.solicitacao.SolicitacaoGestor;
import com.aula.fixa.ui.adapter.solicitacao.SolicitacaoGestorListAdapter;
import com.aula.fixa.ui.adapter.solicitacao.SolicitacaoGestorPhotoAdapter;
import com.aula.fixa.ui.config.AppLoading;
import com.aula.fixa.ui.config.AppText;
import com.aula.fixa.ui.view.operations.OperationsActivity;
import com.aula.fixa.ui.view.operations.OperationsScreenFragment;

import java.util.ArrayList;
import java.util.List;

/** Coordinates the manager-specific presentation of the shared request feature. */
public final class SolicitacaoGestorFluxo {
    private final OperationsScreenFragment host;
    private final OperationsActivity activity;
    private final List<SolicitacaoGestor> pendingRequests = new ArrayList<>();
    private SolicitacaoGestorViewModel viewModel;
    private SolicitacaoGestorListAdapter listAdapter;
    private Dialog loading;
    private String periodFilter = "";
    private String locationFilter = "";
    private String categoryFilter = "";
    private String authorFilter = "";
    private boolean planningSelection;

    public SolicitacaoGestorFluxo(OperationsScreenFragment host, OperationsActivity activity) {
        this.host = host;
        this.activity = activity;
    }

    public void bindList(View content) {
        bindList(content, false);
    }

    public void bindApprovedForPlanning(View content) {
        planningSelection = true;
        bindList(content, true);
    }

    private void bindList(View content, boolean approvedForPlanning) {
        ensureViewModel();
        android.widget.TextView heading = content.findViewById(R.id.ops_5717_6864);
        if (heading != null) heading.setText(approvedForPlanning
                ? R.string.ops_text_planeje_as_tarefas_da_os_0527e3 : R.string.ops_text_solicitacoes_07d0b7);
        View back = content.findViewWithTag("image:arrow-left");
        if (back != null) back.setOnClickListener(v -> activity.back());
        if (approvedForPlanning) {
            for (int suffix : new int[]{6893, 6910, 6927}) {
                View sample = content.findViewById(host.getResources().getIdentifier(
                        "ops_5717_" + suffix, "id", host.requireContext().getPackageName()));
                if (sample != null) sample.setVisibility(View.GONE);
            }
        }
        RecyclerView requestsList = content.findViewById(R.id.ops_manager_requests_list);
        if (requestsList != null) {
            requestsList.setLayoutManager(new LinearLayoutManager(host.requireContext()));
            listAdapter = new SolicitacaoGestorListAdapter(
                    request -> {
                        if (planningSelection) {
                            activity.model.field("planning_tasks", "source_title", request.titulo);
                            activity.model.field("planning_tasks", "source_description", request.descricao);
                            activity.openPlanningIntro(request.id);
                        } else activity.openRequestDetails(request.id, request.titulo);
                    }, approvedForPlanning ? R.string.ops_manager_no_approved_requests
                            : R.string.ops_manager_no_pending_requests);
            requestsList.setAdapter(listAdapter);
        }
        View filter = content.findViewById(R.id.ops_manager_requests_filter);
        if (filter != null) {
            filter.setOnClickListener(ignored -> {
                ManagerRequestFiltersSheet sheet = new ManagerRequestFiltersSheet();
                sheet.setTargetFragment(host, 0);
                sheet.show(host.getParentFragmentManager(), ManagerRequestFiltersSheet.TAG);
            });
        }
        View fab = content.findViewById(R.id.ops_manager_requests_fab);
        if (fab != null) {
            if (approvedForPlanning) fab.setVisibility(View.GONE);
            else fab.setOnClickListener(ignored -> activity.openRequestCreation());
        }
        EditText search = content.findViewById(R.id.ops_5717_6869);
        if (search != null) {
            search.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { renderList(content); }
                @Override public void afterTextChanged(Editable s) { }
            });
        }
        loadList(content, approvedForPlanning);
    }

    public void bindDetails(View content) {
        ensureViewModel();
        loadDetails(content);
    }

    public void refreshList(View content) {
        if (viewModel != null) loadList(content, planningSelection);
    }

    public void onFiltersChanged(String period, String location, String category, String author) {
        String screen = planningSelection ? "manager_planning" : "manager_requests";
        activity.model.field(screen, "Período", period);
        activity.model.field(screen, "Local", location);
        activity.model.field(screen, "Categoria", category);
        activity.model.field(screen, "Autor", author);
        periodFilter = period == null ? "" : period;
        locationFilter = location == null ? "" : location;
        categoryFilter = category == null ? "" : category;
        authorFilter = author == null ? "" : author;
        if (host.getView() instanceof android.widget.FrameLayout) {
            View content = ((android.widget.FrameLayout) host.getView()).getChildAt(0);
            if (content != null) renderList(content);
        }
    }

    public void onDestroyView() {
        AppLoading.hide(loading);
        loading = null;
        listAdapter = null;
    }

    private void ensureViewModel() {
        if (viewModel == null) {
            viewModel = new ViewModelProvider(host, new SolicitacaoGestorViewModelFactory(host.requireContext()))
                    .get(SolicitacaoGestorViewModel.class);
        }
    }

    private void loadList(View content, boolean approvedForPlanning) {
        content.setVisibility(View.INVISIBLE);
        showLoading();
        SolicitacaoGestorViewModel.Callback<List<SolicitacaoGestor>> callback = new SolicitacaoGestorViewModel.Callback<List<SolicitacaoGestor>>() {
            @Override public void success(List<SolicitacaoGestor> result) {
                if (!isCurrent(content)) return;
                pendingRequests.clear();
                pendingRequests.addAll(result);
                renderList(content);
                finishLoading(content);
            }
            @Override public void failure(Throwable error) {
                if (!isCurrent(content)) return;
                pendingRequests.clear();
                renderList(content);
                finishLoading(content);
                Toast.makeText(host.requireContext(), AutenticacaoViewModel.messageFrom(error), Toast.LENGTH_LONG).show();
            }
        };
        if (approvedForPlanning) viewModel.loadApproved(callback);
        else viewModel.load(callback);
    }

    private void renderList(View content) {
        if (listAdapter == null || viewModel == null) return;
        EditText search = content.findViewById(R.id.ops_5717_6869);
        String query = search == null ? "" : search.getText().toString().trim();
        listAdapter.submitList(viewModel.filter(pendingRequests, query,
                periodFilter, locationFilter, categoryFilter, authorFilter));
    }

    private void loadDetails(View content) {
        content.setVisibility(View.INVISIBLE);
        showLoading();
        long id = host.requireArguments().getLong("requestId", 0);
        if (id <= 0) {
            finishLoading(content);
            return;
        }
        android.widget.TextView title = content.findViewById(R.id.tvTitle);
        AppText.setHtml(title, "<b><font color=\"#153D7D\">Detalhes</font></b> da solicitação");
        content.findViewById(R.id.btnBack).setOnClickListener(v -> host.requireActivity().getOnBackPressedDispatcher().onBackPressed());
        com.google.android.material.button.MaterialButton reject = content.findViewById(R.id.btnStepBack);
        com.google.android.material.button.MaterialButton approve = content.findViewById(R.id.btnStepNext);
        int width = Math.round(132 * host.getResources().getDisplayMetrics().density);
        int height = Math.round(50 * host.getResources().getDisplayMetrics().density);
        reject.getLayoutParams().width = width;
        reject.getLayoutParams().height = height;
        approve.getLayoutParams().width = width;
        approve.getLayoutParams().height = height;
        reject.requestLayout();
        approve.requestLayout();
        reject.setText("Reprovar");
        approve.setText("Aprovar");
        reject.setBackgroundResource(R.drawable.bg_ops_c1bf431229);
        reject.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                androidx.core.content.ContextCompat.getColor(host.requireContext(), R.color.ops_color_80f7a9a0)));
        reject.setTextColor(androidx.core.content.ContextCompat.getColor(host.requireContext(), R.color.ops_color_ff96031a));
        approve.setBackgroundResource(R.drawable.bg_ops_e9ff3d87c6);
        approve.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                androidx.core.content.ContextCompat.getColor(host.requireContext(), R.color.ops_color_ff153d7d)));
        approve.setTextColor(androidx.core.content.ContextCompat.getColor(host.requireContext(), R.color.app_on_primary));
        reject.setEnabled(false);
        approve.setEnabled(false);

        viewModel.details(id, new SolicitacaoGestorViewModel.Callback<SolicitacaoGestor>() {
            @Override public void success(SolicitacaoGestor item) {
                if (!isCurrent(content)) return;
                ((android.widget.TextView) content.findViewById(R.id.tvSummaryTitle)).setText(item.titulo);
                ((android.widget.TextView) content.findViewById(R.id.tvSummaryDescription)).setText(item.descricao);
                AppText.setHtml(content.findViewById(R.id.tvSummaryDevice), R.string.app_confirm_device, item.categoria);
                AppText.setHtml(content.findViewById(R.id.tvSummaryLocation), R.string.app_confirm_location, item.local);
                AppText.setHtml(content.findViewById(R.id.tvSummaryDescriptionLabel), R.string.app_confirm_description);
                ((android.widget.TextView) content.findViewById(R.id.tvSummaryLocationDescription)).setText(item.descricaoLocal);
                RecyclerView photosList = content.findViewById(R.id.rvSummaryPhotos);
                android.widget.TextView photosLabel = content.findViewById(R.id.tvSummaryImagesLabel);
                photosList.setLayoutManager(new LinearLayoutManager(host.requireContext(), LinearLayoutManager.HORIZONTAL, false));
                photosList.setAdapter(new SolicitacaoGestorPhotoAdapter(host, item.fotos));
                photosList.setVisibility(item.fotos.isEmpty() ? View.GONE : View.VISIBLE);
                photosLabel.setVisibility(item.fotos.isEmpty() ? View.GONE : View.VISIBLE);
                boolean pending = "PENDENTE".equalsIgnoreCase(item.status);
                reject.setOnClickListener(v -> showRejectDialog(id));
                approve.setOnClickListener(v -> updateStatus(id, StatusProblema.APROVADO, null));
                content.findViewById(R.id.stepActions).setVisibility(pending ? View.VISIBLE : View.GONE);
                reject.setEnabled(pending);
                approve.setEnabled(pending);
                finishLoading(content);
            }
            @Override public void failure(Throwable error) {
                if (!isCurrent(content)) return;
                finishLoading(content);
                Toast.makeText(host.requireContext(), AutenticacaoViewModel.messageFrom(error), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showRejectDialog(long id) {
        EditText reason = new EditText(host.requireContext());
        reason.setHint("Motivo da reprovação");
        int padding = Math.round(20 * host.getResources().getDisplayMetrics().density);
        reason.setPadding(padding, padding, padding, padding);
        AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(host.requireContext())
                .setTitle("Reprovar solicitação").setView(reason).setNegativeButton("Cancelar", null)
                .setPositiveButton("Reprovar", null).create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String value = reason.getText().toString().trim();
            if (value.isEmpty()) {
                reason.setError("Informe o motivo da reprovação.");
                return;
            }
            dialog.dismiss();
            updateStatus(id, StatusProblema.REPROVADO, value);
        }));
        dialog.show();
    }

    private void updateStatus(long id, StatusProblema status, String reason) {
        viewModel.updateStatus(id, status, reason, new SolicitacaoGestorViewModel.Callback<SolicitacaoGestor>() {
            @Override public void success(SolicitacaoGestor value) {
                if (!host.isAdded()) return;
                Toast.makeText(host.requireContext(), status == StatusProblema.APROVADO
                        ? "Solicitação aprovada." : "Solicitação reprovada.", Toast.LENGTH_SHORT).show();
                host.requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
            @Override public void failure(Throwable error) {
                if (host.isAdded()) Toast.makeText(host.requireContext(),
                        AutenticacaoViewModel.messageFrom(error), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showLoading() {
        AppLoading.hide(loading);
        loading = AppLoading.show(host.requireContext());
    }

    private void finishLoading(View content) {
        content.setVisibility(View.VISIBLE);
        AppLoading.hide(loading);
        loading = null;
    }

    private boolean isCurrent(View content) {
        return host.isAdded() && host.getView() instanceof android.widget.FrameLayout
                && ((android.widget.FrameLayout) host.getView()).indexOfChild(content) >= 0;
    }
}
