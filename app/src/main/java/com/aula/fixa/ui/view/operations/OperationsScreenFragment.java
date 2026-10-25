package com.aula.fixa.ui.view.operations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.net.Uri;
import android.widget.TextView;

import com.aula.fixa.adapter.home.ManagerHomeViewModel;
import com.aula.fixa.adapter.home.ManagerHomeViewModelFactory;
import com.aula.fixa.adapter.planejamento.PlanejamentoOSViewModel;
import com.aula.fixa.adapter.planejamento.PlanejamentoOSViewModelFactory;
import com.aula.fixa.adapter.tecnico.TecnicoViewModel;
import com.aula.fixa.adapter.tecnico.TecnicoViewModelFactory;
import com.aula.fixa.domain.enums.CategoriaProblema;
import com.aula.fixa.domain.model.tecnico.TecnicoDisponivel;
import com.aula.fixa.domain.model.planejamento.TarefaPlanejada;
import com.aula.fixa.ui.adapter.tecnico.TecnicoDisponivelAdapter;
import com.aula.fixa.ui.config.DataChangeNotifier;
import com.aula.fixa.ui.config.AppLoading;
import com.aula.fixa.ui.config.operations.ScreenCatalog;
import com.aula.fixa.ui.view.solicitacao.ManagerRequestFiltersSheet;
import com.aula.fixa.ui.view.solicitacao.SolicitacaoGestorFluxo;
import com.aula.fixa.ui.view.task.PlanejamentoTarefasFluxo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.List;

/**
 * Fragment nativo para uma tela do catálogo. O layout permanece editável em XML.
 */
public final class OperationsScreenFragment extends Fragment implements ManagerRequestFiltersSheet.Listener {
    private Dialog homeLoading;
    private boolean firstResume = true;
    private OperationsProfileFlow profileFlow;
    private Uri pendingProfileCameraUri;
    private SolicitacaoGestorFluxo solicitacaoGestorFluxo;
    private PlanejamentoTarefasFluxo planejamentoTarefasFluxo;
    private final ActivityResultLauncher<String> profilePhotoPicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && profileFlow != null) {
                    profileFlow.uploadPhoto(uri);
                }
            });
    private final ActivityResultLauncher<Uri> profileCamera = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), captured -> {
                if (Boolean.TRUE.equals(captured) && pendingProfileCameraUri != null && profileFlow != null) {
                    profileFlow.uploadPhoto(pendingProfileCameraUri);
                }
                pendingProfileCameraUri = null;
            });

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(com.aula.fixa.R.layout.fragment_ops_screen_host, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        render();
        DataChangeNotifier.observe(getViewLifecycleOwner(), ignored -> {
            String screen = requireArguments().getString("screen", "manager_home");
            boolean profile = screen.equals(((OperationsActivity) requireActivity()).role() + "_profile")
                    || screen.equals(((OperationsActivity) requireActivity()).role() + "_my_profile");
            if (ignored != null && getView() == view && ("manager_home".equals(screen) || profile)) {
                render();
            }
        });
    }

    public void render() {
        if (getView() == null) {
            return;
        }
        android.widget.FrameLayout host = (android.widget.FrameLayout) getView();
        String screen = requireArguments().getString("screen", "manager_home");
        host.removeAllViews();
        int screenLayout = "manager_request_details".equals(screen)
                ? com.aula.fixa.R.layout.fragment_new_request_confirm
                : "manager_planning".equals(screen)
                ? com.aula.fixa.R.layout.fragment_ops_manager_requests
                : ScreenCatalog.get(requireContext()).layout(requireContext(), screen);
        View content = getLayoutInflater().inflate(screenLayout, host, false);
        host.addView(content);
        if ("manager_home".equals(screen)) {
            content.setVisibility(View.INVISIBLE);
            if (homeLoading != null) {
                AppLoading.hide(homeLoading);
            }
            homeLoading = AppLoading.show(requireContext());
        } else if (homeLoading != null) {
            AppLoading.hide(homeLoading);
            homeLoading = null;
        }
        OperationsBinder binder = new OperationsBinder((OperationsActivity) requireActivity(), content, screen, null);
        binder.bind();
        if ("manager_assign_member".equals(screen)) {
            bindFinishPlanning(content, (OperationsActivity) requireActivity());
        }
        if ("manager_assign_member".equals(screen)) {
            bindAvailableTechnicians(content, (OperationsActivity) requireActivity());
        }
        if ("manager_plan_information".equals(screen)) {
            bindPlanningOptions(content, (OperationsActivity) requireActivity());
        }
        if ("manager_plan_information".equals(screen)) {
            bindPlanningCalendar(content, (OperationsActivity) requireActivity());
        }
        if ("manager_planning_tasks".equals(screen)) {
            planejamentoTarefasFluxo = new PlanejamentoTarefasFluxo(this, (OperationsActivity) requireActivity(), content);
            planejamentoTarefasFluxo.bind();
        } else {
            planejamentoTarefasFluxo = null;
        }
        if ("manager_requests".equals(screen)) {
            solicitacaoGestorFluxo = new SolicitacaoGestorFluxo(this, (OperationsActivity) requireActivity());
            solicitacaoGestorFluxo.bindList(content);
        } else if ("manager_planning".equals(screen)) {
            solicitacaoGestorFluxo = new SolicitacaoGestorFluxo(this, (OperationsActivity) requireActivity());
            solicitacaoGestorFluxo.bindApprovedForPlanning(content);
        } else if ("manager_request_details".equals(screen)) {
            solicitacaoGestorFluxo = new SolicitacaoGestorFluxo(this, (OperationsActivity) requireActivity());
            solicitacaoGestorFluxo.bindDetails(content);
        } else {
            solicitacaoGestorFluxo = null;
        }
        if (screen.endsWith("profile") || screen.endsWith("my_profile") || screen.endsWith("edit_profile")) {
            OperationsProfileFlow.PhotoActions photoActions = new OperationsProfileFlow.PhotoActions() {
                @Override
                public void escolher() {
                    profilePhotoPicker.launch("image/*");
                }

                @Override
                public void capturar() {
                    capturarFotoPerfil();
                }
            };
            profileFlow = new OperationsProfileFlow(this, (OperationsActivity) requireActivity(),
                    content, screen, photoActions);
            profileFlow.bind();
        } else {
            profileFlow = null;
        }
        if ("manager_home".equals(screen)) {
            loadManagerHome(host, content);
        }
    }

    private void bindFinishPlanning(View content, OperationsActivity activity) {
        View finish = content.findViewWithTag("action:Finalizar");
        if (finish == null) {
            return;
        }
        finish.setOnClickListener(view -> {
            String technicianValue = activity.model.field("plan_information", "tecnicoId");
            String categoryIdValue = activity.model.field("plan_information", "CompetênciaId");
            String priorityIdValue = activity.model.field("plan_information", "PrioridadeId");
            String dateValue = activity.model.field("plan_information", "prazo");
            if (technicianValue == null || categoryIdValue == null || priorityIdValue == null
                    || dateValue == null || dateValue.trim().isEmpty()) {
                android.widget.Toast.makeText(activity,
                        "Selecione o técnico, competência, prioridade e prazo.", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            long requestId = getArguments() == null ? 0 : getArguments().getLong("requestId", 0);
            String savedOrderId = activity.model.field("planning_tasks", "ordemServicoId");
            long existingOrderId = 0;
            try {
                if (savedOrderId != null) {
                    existingOrderId = Long.parseLong(savedOrderId);
                }
            } catch (NumberFormatException ignored) {
            }
            java.util.ArrayList<android.os.Bundle> entries = activity.model.entries("planning_tasks");
            java.util.List<TarefaPlanejada> tasks = new java.util.ArrayList<>();
            for (android.os.Bundle row : entries) {
                tasks.add(new TarefaPlanejada(row.getString("title"), row.getString("description")));
            }
            if (tasks.isEmpty()) {
                android.widget.Toast.makeText(activity,
                        "Adicione ao menos uma tarefa antes de finalizar.", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            final long technicianId, categoryId, priorityId;
            try {
                technicianId = Long.parseLong(technicianValue);
                categoryId = Long.parseLong(categoryIdValue);
                priorityId = Long.parseLong(priorityIdValue);
            } catch (NumberFormatException invalidSelection) {
                android.widget.Toast.makeText(activity,
                        "As informações do planejamento são inválidas. Selecione novamente.",
                        android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            String prazoIso;
            try {
                java.text.SimpleDateFormat parser = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.ROOT);
                parser.setLenient(false);
                java.util.Date parsed = parser.parse(dateValue);
                if (parsed == null) {
                    throw new java.text.ParseException("Prazo inválido", 0);
                }
                prazoIso = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT).format(parsed);
            } catch (java.text.ParseException invalidDate) {
                android.widget.Toast.makeText(activity, "Selecione um prazo válido.", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            finish.setEnabled(false);
            android.app.Dialog loading = AppLoading.show(activity);
            PlanejamentoOSViewModel viewModel = new ViewModelProvider(this,
                    new PlanejamentoOSViewModelFactory(requireContext())).get(PlanejamentoOSViewModel.class);
            viewModel.finalizar(requestId, technicianId, (int) categoryId, (int) priorityId,
                    prazoIso, existingOrderId, tasks, new PlanejamentoOSViewModel.Callback() {
                        @Override
                        public void success(long ordemServicoId) {
                            AppLoading.hide(loading);
                            if (!isAdded()) {
                                return;
                            }
                            activity.model.field("planning_tasks", "ordemServicoId", String.valueOf(ordemServicoId));
                            activity.openPlanningStep("planning_success");
                        }

                        @Override
                        public void failure(Throwable error) {
                            AppLoading.hide(loading);
                            if (!isAdded()) {
                                return;
                            }
                            if (error instanceof com.aula.fixa.application.planejamento.FinalizarPlanejamentoOS.TarefasPlanejamentoException) {
                                long createdId = ((com.aula.fixa.application.planejamento.FinalizarPlanejamentoOS.TarefasPlanejamentoException) error).ordemServicoId;
                                activity.model.field("planning_tasks", "ordemServicoId", String.valueOf(createdId));
                            }
                            finish.setEnabled(true);
                            Throwable displayError = error.getCause() == null ? error : error.getCause();
                            android.widget.Toast.makeText(activity,
                                    com.aula.fixa.adapter.auth.AutenticacaoViewModel.messageFrom(displayError),
                                    android.widget.Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private void bindAvailableTechnicians(View content, OperationsActivity activity) {
        RecyclerView list = content.findViewById(com.aula.fixa.R.id.ops_available_technicians_list);
        TextView empty = content.findViewById(com.aula.fixa.R.id.ops_available_technicians_empty);
        if (list == null || empty == null) {
            return;
        }
        list.setNestedScrollingEnabled(false);
        list.setClipToPadding(false);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        TecnicoDisponivelAdapter adapter = new TecnicoDisponivelAdapter(technician -> {
            activity.model.field("plan_information", "tecnicoId", String.valueOf(technician.id));
            adapterSelectionRefresh[0].run();
        });
        list.setAdapter(adapter);

        TecnicoViewModel viewModel = new ViewModelProvider(this,
                new TecnicoViewModelFactory(requireContext())).get(TecnicoViewModel.class);
        String selectedCategory = activity.model.field("plan_information", "Competência");
        String apiCategory = apiCategory(selectedCategory);
        if (apiCategory == null) {
            empty.setText("Defina a competência para carregar os técnicos recomendados.");
            empty.setVisibility(View.VISIBLE);
            list.setVisibility(View.GONE);
            return;
        }

        list.setVisibility(View.INVISIBLE);
        Dialog loading = AppLoading.show(requireContext());
        Runnable selectionRefresh = () -> adapter.submit(currentTechnicians, selectedTechnicianId(activity));
        adapterSelectionRefresh[0] = selectionRefresh;
        viewModel.recomendar(apiCategory, new TecnicoViewModel.Callback<List<TecnicoDisponivel>>() {
            @Override
            public void success(List<TecnicoDisponivel> technicians) {
                AppLoading.hide(loading);
                if (!isAdded() || getView() == null || !content.isAttachedToWindow()) {
                    return;
                }
                currentTechnicians = technicians == null ? java.util.Collections.emptyList() : technicians;
                adapter.submit(currentTechnicians, selectedTechnicianId(activity));
                list.setVisibility(View.VISIBLE);
                if (currentTechnicians.isEmpty()) {
                    empty.setText("Nenhum técnico disponível para esta competência.");
                    empty.setVisibility(View.VISIBLE);
                    list.setVisibility(View.GONE);
                } else {
                    empty.setVisibility(View.GONE);
                }
            }

            @Override
            public void failure(Throwable error) {
                AppLoading.hide(loading);
                if (!isAdded() || getView() == null || !content.isAttachedToWindow()) {
                    return;
                }
                empty.setText("Não foi possível carregar os técnicos. Toque para tentar novamente.");
                empty.setVisibility(View.VISIBLE);
                list.setVisibility(View.GONE);
                empty.setOnClickListener(v -> {
                    activity.refresh();
                });
                android.widget.Toast.makeText(requireContext(),
                        com.aula.fixa.adapter.auth.AutenticacaoViewModel.messageFrom(error),
                        android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }

    private java.util.List<TecnicoDisponivel> currentTechnicians = java.util.Collections.emptyList();
    private final Runnable[] adapterSelectionRefresh = {() -> {
    }};

    private Long selectedTechnicianId(OperationsActivity activity) {
        String id = activity.model.field("plan_information", "tecnicoId");
        try {
            return id == null ? null : Long.valueOf(id);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String apiCategory(String selectedName) {
        if (selectedName == null) {
            return null;
        }
        String normalized = selectedName.trim();
        for (CategoriaProblema category : CategoriaProblema.values()) {
            if (category.getNome().equalsIgnoreCase(normalized) || category.name().equalsIgnoreCase(normalized)) {
                return category.name();
            }
        }
        return null;
    }

    private void bindPlanningOptions(View root, OperationsActivity activity) {
        android.widget.EditText competenceState = root.findViewById(com.aula.fixa.R.id.ops_5717_6260_input);
        android.widget.EditText priorityState = root.findViewById(com.aula.fixa.R.id.ops_5717_6263_input);
        android.view.View competenceField = root.findViewById(com.aula.fixa.R.id.ops_5717_6260);
        android.view.View priorityField = root.findViewById(com.aula.fixa.R.id.ops_5717_6263);
        android.view.View competenceArrow = root.findViewById(com.aula.fixa.R.id.ops_5717_6262);
        android.view.View priorityArrow = root.findViewById(com.aula.fixa.R.id.ops_5717_6265);
        if (competenceState == null || priorityState == null) {
            return;
        }
        applyCompactPlaceholder(competenceState);
        applyCompactPlaceholder(priorityState);

        String savedCompetence = activity.model.field("plan_information", "Competência");
        String savedPriority = activity.model.field("plan_information", "Prioridade");
        if (savedCompetence != null) {
            competenceState.setText(savedCompetence);
        }
        if (savedPriority != null) {
            priorityState.setText(savedPriority);
        }
        competenceState.setFocusable(false);
        priorityState.setFocusable(false);
        competenceState.setShowSoftInputOnFocus(false);
        priorityState.setShowSoftInputOnFocus(false);
        View.OnClickListener competenceClick = anchor -> showPlanningDropdown(anchor, competenceState, activity,
                "Competência", com.aula.fixa.domain.enums.CategoriaProblema.values());
        View.OnClickListener priorityClick = anchor -> showPlanningDropdown(anchor, priorityState, activity,
                "Prioridade", com.aula.fixa.domain.enums.Prioridade.values());
        competenceField.setOnClickListener(competenceClick);
        competenceState.setOnClickListener(competenceClick);
        competenceArrow.setOnClickListener(competenceClick);
        priorityField.setOnClickListener(priorityClick);
        priorityState.setOnClickListener(priorityClick);
        priorityArrow.setOnClickListener(priorityClick);
    }

    private void applyCompactPlaceholder(android.widget.EditText field) {
        CharSequence hint = field.getHint();
        if (hint == null) {
            return;
        }
        android.text.SpannableString compactHint = new android.text.SpannableString(hint);
        compactHint.setSpan(new android.text.style.RelativeSizeSpan(0.9f), 0, compactHint.length(),
                android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        field.setHint(compactHint);
    }

    private void showPlanningDropdown(
            View anchor, android.widget.EditText field, OperationsActivity activity,
            String label, Enum<?>[] options) {
        android.widget.PopupMenu menu = new android.widget.PopupMenu(requireContext(), anchor);
        for (int i = 0; i < options.length; i++) {
            Enum<?> option = options[i];
            String display;
            int id;
            if (option instanceof com.aula.fixa.domain.enums.CategoriaProblema) {
                com.aula.fixa.domain.enums.CategoriaProblema category = (com.aula.fixa.domain.enums.CategoriaProblema) option;
                display = category.getNome();
                id = category.getId();
            } else {
                com.aula.fixa.domain.enums.Prioridade priority = (com.aula.fixa.domain.enums.Prioridade) option;
                display = priority.getNome();
                id = priority.getId();
            }
            final String selectedName = display;
            final int selectedId = id;
            menu.getMenu().add(0, i, i, display).setOnMenuItemClickListener(item -> {
                field.setText(selectedName);
                activity.model.field("plan_information", label, selectedName);
                activity.model.field("plan_information", label + "Id", String.valueOf(selectedId));
                return true;
            });
        }
        menu.show();
    }

    private void bindPlanningCalendar(View root, OperationsActivity activity) {
        android.widget.TextView monthLabel = root.findViewById(com.aula.fixa.R.id.ops_5717_8885);
        android.view.View monthSelector = root.findViewById(com.aula.fixa.R.id.ops_5717_8884);
        android.view.View monthArrow = root.findViewById(com.aula.fixa.R.id.ops_5717_8886);
        android.view.ViewGroup calendar = root.findViewById(com.aula.fixa.R.id.ops_5717_8889);
        if (monthLabel == null || monthSelector == null || calendar == null) {
            return;
        }

        java.util.Calendar now = java.util.Calendar.getInstance();
        String savedMonth = activity.model.field("plan_information", "mes");
        java.util.Calendar visibleMonth = (java.util.Calendar) now.clone();
        visibleMonth.set(java.util.Calendar.DAY_OF_MONTH, 1);
        if (savedMonth != null) {
            try {
                java.text.SimpleDateFormat parser = new java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.ROOT);
                java.util.Date parsed = parser.parse(savedMonth);
                if (parsed != null) {
                    visibleMonth.setTime(parsed);
                }
            } catch (java.text.ParseException ignored) {
            }
        }
        if (savedMonth == null || savedMonth.trim().isEmpty()) {
            monthLabel.setText(com.aula.fixa.R.string.ops_text_selecione_mes);
            calendar.removeAllViews();
        } else {
            setMonthText(monthLabel, visibleMonth);
            renderPlanningCalendar(calendar, visibleMonth, now, activity);
        }

        View.OnClickListener chooseMonth = anchor -> {
            java.util.Calendar current = java.util.Calendar.getInstance();
            current.set(java.util.Calendar.DAY_OF_MONTH, 1);
            android.widget.PopupMenu menu = new android.widget.PopupMenu(requireContext(), anchor);
            java.text.DateFormatSymbols symbols = new java.text.DateFormatSymbols(new java.util.Locale("pt", "BR"));
            for (int offset = 0; offset < 24; offset++) {
                java.util.Calendar option = (java.util.Calendar) current.clone();
                option.add(java.util.Calendar.MONTH, offset);
                String monthName = symbols.getMonths()[option.get(java.util.Calendar.MONTH)];
                monthName = monthName.substring(0, 1).toUpperCase(new java.util.Locale("pt", "BR")) + monthName.substring(1);
                String value = String.format(java.util.Locale.ROOT, "%04d-%02d", option.get(java.util.Calendar.YEAR), option.get(java.util.Calendar.MONTH) + 1);
                menu.getMenu().add(0, offset, offset, monthName + " - " + option.get(java.util.Calendar.YEAR))
                        .setOnMenuItemClickListener(item -> {
                            activity.model.field("plan_information", "mes", value);
                            activity.model.field("plan_information", "prazo", "");
                            setMonthText(monthLabel, option);
                            renderPlanningCalendar(calendar, option, current, activity);
                            return true;
                        });
            }
            menu.show();
        };
        monthSelector.setOnClickListener(chooseMonth);
        monthLabel.setOnClickListener(chooseMonth);
        if (monthArrow != null) {
            monthArrow.setOnClickListener(chooseMonth);
        }
    }

    private void setMonthText(android.widget.TextView label, java.util.Calendar month) {
        String name = new java.text.DateFormatSymbols(new java.util.Locale("pt", "BR")).getMonths()[month.get(java.util.Calendar.MONTH)];
        name = name.substring(0, 1).toUpperCase(new java.util.Locale("pt", "BR")) + name.substring(1);
        label.setText(name + " - " + month.get(java.util.Calendar.YEAR));
    }

    private void renderPlanningCalendar(
            android.view.ViewGroup calendar, java.util.Calendar month,
            java.util.Calendar today, OperationsActivity activity) {
        calendar.removeAllViews();
        android.widget.GridLayout grid = new android.widget.GridLayout(requireContext());
        grid.setColumnCount(7);
        grid.setRowCount(7);
        grid.setPadding(dp(12), dp(8), dp(12), dp(4));
        String[] weekdays = {"D", "S", "T", "Q", "Q", "S", "S"};
        for (int column = 0; column < 7; column++) {
            android.widget.TextView dayName = new android.widget.TextView(requireContext());
            dayName.setText(weekdays[column]);
            dayName.setGravity(android.view.Gravity.CENTER);
            dayName.setTextSize(13);
            dayName.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), com.aula.fixa.R.color.ops_color_ff0e223f));
            grid.addView(dayName, calendarCell(0, column, dp(28)));
        }
        java.util.Calendar first = (java.util.Calendar) month.clone();
        first.set(java.util.Calendar.DAY_OF_MONTH, 1);
        int leading = first.get(java.util.Calendar.DAY_OF_WEEK) - java.util.Calendar.SUNDAY;
        int daysInMonth = first.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
        String selectedDate = activity.model.field("plan_information", "prazo");
        for (int index = 0; index < 42; index++) {
            int day = index - leading + 1;
            android.widget.TextView cell = new android.widget.TextView(requireContext());
            cell.setGravity(android.view.Gravity.CENTER);
            cell.setTextSize(14);
            if (day >= 1 && day <= daysInMonth) {
                cell.setText(String.valueOf(day));
                java.util.Calendar candidate = (java.util.Calendar) first.clone();
                candidate.set(java.util.Calendar.DAY_OF_MONTH, day);
                boolean past = dateOnly(candidate).before(dateOnly(today));
                String dateValue = String.format(java.util.Locale.ROOT, "%02d/%02d/%04d", day,
                        first.get(java.util.Calendar.MONTH) + 1, first.get(java.util.Calendar.YEAR));
                boolean selected = dateValue.equals(selectedDate);
                if (selected) {
                    android.graphics.drawable.GradientDrawable selectedBg = new android.graphics.drawable.GradientDrawable();
                    selectedBg.setShape(android.graphics.drawable.GradientDrawable.OVAL);
                    selectedBg.setColor(androidx.core.content.ContextCompat.getColor(requireContext(), com.aula.fixa.R.color.ops_color_ff142b57));
                    cell.setBackground(selectedBg);
                    cell.setTextColor(android.graphics.Color.WHITE);
                } else {
                    cell.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), past ? com.aula.fixa.R.color.ops_color_668994a8 : com.aula.fixa.R.color.ops_color_ff0e223f));
                }
                cell.setEnabled(!past);
                if (!past) {
                    cell.setOnClickListener(v -> {
                        activity.model.field("plan_information", "mes", String.format(java.util.Locale.ROOT, "%04d-%02d",
                                first.get(java.util.Calendar.YEAR), first.get(java.util.Calendar.MONTH) + 1));
                        activity.model.field("plan_information", "prazo", dateValue);
                        renderPlanningCalendar(calendar, first, today, activity);
                    });
                }
            }
            grid.addView(cell, calendarCell(1 + index / 7, index % 7, dp(31)));
        }
        calendar.addView(grid, new android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private android.widget.GridLayout.LayoutParams calendarCell(int row, int column, int height) {
        android.widget.GridLayout.LayoutParams params = new android.widget.GridLayout.LayoutParams(
                android.widget.GridLayout.spec(row), android.widget.GridLayout.spec(column, 1f));
        params.width = 0;
        params.height = height;
        params.setGravity(android.view.Gravity.FILL_HORIZONTAL);
        return params;
    }

    private java.util.Date dateOnly(java.util.Calendar date) {
        java.util.Calendar copy = (java.util.Calendar) date.clone();
        copy.set(java.util.Calendar.HOUR_OF_DAY, 0);
        copy.set(java.util.Calendar.MINUTE, 0);
        copy.set(java.util.Calendar.SECOND, 0);
        copy.set(java.util.Calendar.MILLISECOND, 0);
        return copy.getTime();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void capturarFotoPerfil() {
        try {
            java.io.File directory = requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
            if (directory == null) {
                throw new java.io.IOException("Armazenamento indisponível.");
            }
            java.io.File image = java.io.File.createTempFile("fixa_perfil_", ".jpg", directory);
            pendingProfileCameraUri = androidx.core.content.FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileProvider", image);
            profileCamera.launch(pendingProfileCameraUri);
        } catch (java.io.IOException | android.content.ActivityNotFoundException
                 | SecurityException error) {
            android.widget.Toast.makeText(requireContext(), "Não foi possível abrir a câmera.",
                    android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private void loadManagerHome(android.widget.FrameLayout host, View content) {
        ManagerHomeViewModel model = new ViewModelProvider(this,
                new ManagerHomeViewModelFactory(requireContext())).get(ManagerHomeViewModel.class);
        model.carregar(new ManagerHomeViewModel.Callback() {
            @Override
            public void aoCarregar(com.aula.fixa.domain.model.home.ManagerHomeData data) {
                if (!isAdded() || getView() != host || host.indexOfChild(content) < 0) {
                    return;
                }
                try {
                    OperationsData.bindManagerHome((OperationsActivity) requireActivity(), content, data);
                    View home = content.findViewById(com.aula.fixa.R.id.opsManagerHomeContent);
                    if (home != null) {
                        home.setVisibility(View.VISIBLE);
                    }
                    content.setVisibility(View.VISIBLE);
                } finally {
                    AppLoading.hide(homeLoading);
                    homeLoading = null;
                }
            }

            @Override
            public void aoFalhar(Throwable erro) {
                if (!isAdded() || getView() != host || host.indexOfChild(content) < 0) {
                    return;
                }
                AppLoading.hide(homeLoading);
                homeLoading = null;
                content.setVisibility(View.VISIBLE);
                android.widget.Toast.makeText(requireContext(), com.aula.fixa.R.string.ops_home_load_error,
                        android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onManagerRequestFilters(String period, String location, String category, String author) {
        if (solicitacaoGestorFluxo != null) {
            solicitacaoGestorFluxo.onFiltersChanged(period, location, category, author);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean initialResume = firstResume;
        firstResume = false;
        String screen = requireArguments().getString("screen", "");
        if ("manager_planning_tasks".equals(screen) && planejamentoTarefasFluxo != null) {
            planejamentoTarefasFluxo.maybeShowFirstTaskPrompt();
        }
        if (initialResume) {
            return;
        }
        if (("manager_requests".equals(screen) || "manager_planning".equals(screen)) && getView() != null) {
            android.widget.FrameLayout host = (android.widget.FrameLayout) getView();
            if (host.getChildCount() > 0 && solicitacaoGestorFluxo != null) {
                solicitacaoGestorFluxo.refreshList(host.getChildAt(0));
            }
        }
        if (!"manager_home".equals(requireArguments().getString("screen", "manager_home")) || getView() == null) {
            return;
        }
        android.widget.FrameLayout host = (android.widget.FrameLayout) getView();
        if (host.getChildCount() > 0) {
            View content = host.getChildAt(0);
            content.setVisibility(View.INVISIBLE);
            if (homeLoading != null) {
                AppLoading.hide(homeLoading);
            }
            homeLoading = AppLoading.show(requireContext());
            loadManagerHome(host, content);
        }
    }

    @Override
    public void onDestroyView() {
        AppLoading.hide(homeLoading);
        homeLoading = null;
        if (solicitacaoGestorFluxo != null) {
            solicitacaoGestorFluxo.onDestroyView();
            solicitacaoGestorFluxo = null;
        }
        planejamentoTarefasFluxo = null;
        super.onDestroyView();
    }
}
