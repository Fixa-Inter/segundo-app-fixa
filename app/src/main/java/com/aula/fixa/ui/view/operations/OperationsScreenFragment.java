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
import com.aula.fixa.R;
import com.aula.fixa.adapter.planejamento.PlanejamentoOSViewModel;
import com.aula.fixa.adapter.planejamento.PlanejamentoOSViewModelFactory;
import com.aula.fixa.adapter.tecnico.TecnicoViewModel;
import com.aula.fixa.adapter.tecnico.TecnicoViewModelFactory;
import com.aula.fixa.domain.enums.CategoriaProblema;
import com.aula.fixa.domain.model.tecnico.TecnicoDisponivel;
import com.aula.fixa.domain.model.tecnico.TecnicoDetalhes;
import com.aula.fixa.domain.model.tecnico.CompetenciaTecnico;
import com.aula.fixa.domain.model.planejamento.TarefaPlanejada;
import com.aula.fixa.ui.adapter.tecnico.TecnicoDisponivelAdapter;
import com.aula.fixa.ui.adapter.tecnico.TecnicoEquipeAdapter;
import com.aula.fixa.ui.adapter.tecnico.ResponsabilidadesTecnicoAdapter;
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
public final class OperationsScreenFragment extends Fragment implements ManagerRequestFiltersSheet.Listener, com.aula.fixa.ui.view.os.QuadroOSFiltersSheet.Listener, com.aula.fixa.ui.view.ocorrencia.OcorrenciaFiltrosSheet.Listener {
    private Dialog homeLoading;
    private boolean firstResume = true;
    private OperationsProfileFlow profileFlow;
    private Uri pendingProfileCameraUri;
    private SolicitacaoGestorFluxo solicitacaoGestorFluxo;
    private PlanejamentoTarefasFluxo planejamentoTarefasFluxo;
    private com.aula.fixa.ui.view.os.QuadroOSFluxo quadroOSFluxo;
    private com.aula.fixa.ui.view.os.ExecucaoOSFluxo execucaoOSFluxo;
    private com.aula.fixa.ui.view.os.ConclusaoOSFluxo conclusaoOSFluxo;
    private com.aula.fixa.ui.view.ocorrencia.OcorrenciaGestorFluxo ocorrenciaGestorFluxo;
    private com.aula.fixa.ui.view.equipamento.CategoriaEquipamentoGestorFluxo categoriaEquipamentoGestorFluxo;
    private com.aula.fixa.ui.view.equipamento.ModeloEquipamentoGestorFluxo modeloEquipamentoGestorFluxo;
    private com.aula.fixa.ui.view.equipamento.EquipamentoGestorFluxo equipamentoGestorFluxo;
    private com.aula.fixa.ui.view.checklist.ChecklistFlow checklistFlow;
    private final ActivityResultLauncher<String> occurrenceEditPhotoPicker = registerForActivityResult(
            new ActivityResultContracts.GetMultipleContents(), uris -> {
                if (ocorrenciaGestorFluxo != null && uris != null) {
                    ocorrenciaGestorFluxo.uploadNewPhotos(uris);
                }
            });
    private final ActivityResultLauncher<String> completionPhotoPicker = registerForActivityResult(
            new ActivityResultContracts.GetMultipleContents(), uris -> {
                if (conclusaoOSFluxo != null && uris != null) {
                    conclusaoOSFluxo.uploadPhotos(uris);
                }
            });
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
        if ("manager_board".equals(screen)) {
            ((OperationsActivity) requireActivity()).model.field("execution", "editOrderId", "");
        }
        host.removeAllViews();
        boolean managerOccurrenceCreate = isManagerOccurrenceCreateScreen(screen);
        boolean managerCategories = "manager_categories".equals(screen);
        boolean managerMemberDetails = "manager_member_details".equals(screen);
        int screenLayout = managerOccurrenceCreate
                ? managerOccurrenceLayout(screen)
                : "manager_request_details".equals(screen)
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
        if (!managerOccurrenceCreate && !managerCategories && !managerMemberDetails) {
            OperationsBinder binder = new OperationsBinder((OperationsActivity) requireActivity(), content, screen, null);
            binder.bind();
        }
        if (screen.contains("checklist") || screen.contains("inspection")) {
            checklistFlow = new com.aula.fixa.ui.view.checklist.ChecklistFlow(
                    this, (OperationsActivity) requireActivity());
            checklistFlow.bind(content, screen);
        } else {
            checklistFlow = null;
        }
        if ("manager_assign_member".equals(screen)) {
            bindFinishPlanning(content, (OperationsActivity) requireActivity());
        }
        if ("manager_assign_member".equals(screen)) {
            bindAvailableTechnicians(content, (OperationsActivity) requireActivity());
        }
        if ("manager_team".equals(screen)) {
            bindTeamTechnicians(content);
        }
        if ("manager_models".equals(screen)) {
            modeloEquipamentoGestorFluxo = new com.aula.fixa.ui.view.equipamento.ModeloEquipamentoGestorFluxo(
                    this, (OperationsActivity) requireActivity());
            modeloEquipamentoGestorFluxo.bindList(content);
        } else {
            modeloEquipamentoGestorFluxo = null;
        }
        if ("manager_devices".equals(screen)) {
            equipamentoGestorFluxo = new com.aula.fixa.ui.view.equipamento.EquipamentoGestorFluxo(
                    this, (OperationsActivity) requireActivity());
            equipamentoGestorFluxo.bindList(content);
        } else {
            equipamentoGestorFluxo = null;
        }
        if ("manager_member_details".equals(screen)) {
            bindMemberDetails(content);
        }
        if ("manager_competencies".equals(screen)) {
            bindCompetencies(content);
        }
        if ("manager_plan_information".equals(screen)) {
            bindPlanningOptions(content, (OperationsActivity) requireActivity());
        }
        if ("manager_plan_information".equals(screen)) {
            bindPlanningCalendar(content, (OperationsActivity) requireActivity());
        }
        String editingOrder = ((OperationsActivity) requireActivity()).model.field("execution", "editOrderId");
        if ("manager_plan_information".equals(screen) && editingOrder != null && !editingOrder.trim().isEmpty()) {
            bindOrderEditInformation(content, (OperationsActivity) requireActivity());
        }
        if ("manager_assign_member".equals(screen) && editingOrder != null && !editingOrder.trim().isEmpty()) {
            bindOrderEditTechnician(content);
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
        if ("manager_execution_pending".equals(screen)) {
            long orderId = getArguments() == null ? 0 : getArguments().getLong("orderId", 0);
            execucaoOSFluxo = new com.aula.fixa.ui.view.os.ExecucaoOSFluxo(this, (OperationsActivity) requireActivity(), content, orderId);
            execucaoOSFluxo.bind();
        } else {
            execucaoOSFluxo = null;
        }
        if ("manager_occurrence_result".equals(screen) && getArguments() != null
                && getArguments().getLong("orderId", 0) > 0) {
            conclusaoOSFluxo = new com.aula.fixa.ui.view.os.ConclusaoOSFluxo(this,
                    (OperationsActivity) requireActivity(), content, getArguments().getLong("orderId"));
            conclusaoOSFluxo.bind();
        } else {
            conclusaoOSFluxo = null;
        }
        if ("manager_board".equals(screen)) {
            quadroOSFluxo = new com.aula.fixa.ui.view.os.QuadroOSFluxo(this, (OperationsActivity) requireActivity());
            quadroOSFluxo.bind(content);
        } else {
            quadroOSFluxo = null;
        }
        if (managerCategories) {
            categoriaEquipamentoGestorFluxo = new com.aula.fixa.ui.view.equipamento.CategoriaEquipamentoGestorFluxo(
                    this, (OperationsActivity) requireActivity());
            categoriaEquipamentoGestorFluxo.bindList(content);
        } else {
            categoriaEquipamentoGestorFluxo = null;
        }
        if (managerOccurrenceCreate) {
            ocorrenciaGestorFluxo = new com.aula.fixa.ui.view.ocorrencia.OcorrenciaGestorFluxo(this, (OperationsActivity) requireActivity());
            ocorrenciaGestorFluxo.bindCreateStep(content, screen);
        } else if ("manager_occurrences".equals(screen)) {
            ocorrenciaGestorFluxo = new com.aula.fixa.ui.view.ocorrencia.OcorrenciaGestorFluxo(this, (OperationsActivity) requireActivity());
            ocorrenciaGestorFluxo.bindList(content);
        } else if ("manager_occurrence_details".equals(screen)) {
            ocorrenciaGestorFluxo = new com.aula.fixa.ui.view.ocorrencia.OcorrenciaGestorFluxo(this, (OperationsActivity) requireActivity());
            ocorrenciaGestorFluxo.bindDetails(content);
        } else {
            ocorrenciaGestorFluxo = null;
        }
        if ("manager_occurrence_edit_information".equals(screen)) {
            ocorrenciaGestorFluxo = new com.aula.fixa.ui.view.ocorrencia.OcorrenciaGestorFluxo(this, (OperationsActivity) requireActivity());
            ocorrenciaGestorFluxo.bindEditInformation(content);
        } else if ("manager_occurrence_edit_photos".equals(screen)) {
            ocorrenciaGestorFluxo = new com.aula.fixa.ui.view.ocorrencia.OcorrenciaGestorFluxo(this, (OperationsActivity) requireActivity());
            ocorrenciaGestorFluxo.bindEditPhotos(content);
        }
    }

    private boolean isManagerOccurrenceCreateScreen(String screen) {
        return "manager_occurrence_intro".equals(screen)
                || "manager_occurrence_service".equals(screen)
                || "manager_occurrence_location".equals(screen)
                || ("manager_occurrence_result".equals(screen)
                && (getArguments() == null || getArguments().getLong("orderId", 0) == 0));
    }

    private int managerOccurrenceLayout(String screen) {
        if ("manager_occurrence_intro".equals(screen)) {
            return R.layout.fragment_ops_manager_occurrence_create_basic;
        }
        if ("manager_occurrence_service".equals(screen)) {
            return R.layout.fragment_ops_manager_occurrence_create_location;
        }
        if ("manager_occurrence_location".equals(screen)) {
            return R.layout.fragment_ops_manager_occurrence_create_technical;
        }
        return R.layout.fragment_ops_manager_occurrence_create_photos;
    }

    private void bindFinishPlanning(View content, OperationsActivity activity) {
        View finish = content.findViewWithTag("action:Finalizar");
        if (finish == null) {
            return;
        }
        finish.setOnClickListener(view -> {
            String editOrderId = activity.model.field("execution", "editOrderId");
            if (editOrderId != null && !editOrderId.trim().isEmpty()) {
                String technician = activity.model.field("plan_information", "tecnicoId");
                String category = activity.model.field("plan_information", "Competência");
                String date = activity.model.field("plan_information", "prazo");
                String priority = activity.model.field("plan_information", "Prioridade");
                if (technician == null || category == null || date == null || date.trim().isEmpty() || priority == null) {
                    android.widget.Toast.makeText(activity, "Selecione técnico, competência, prioridade e prazo.", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                long osId;
                try {
                    osId = Long.parseLong(editOrderId);
                } catch (NumberFormatException error) {
                    return;
                }
                long technicianId;
                try {
                    technicianId = Long.parseLong(technician);
                } catch (NumberFormatException error) {
                    return;
                }
                com.aula.fixa.domain.enums.CategoriaProblema categoryEnum = null;
                for (com.aula.fixa.domain.enums.CategoriaProblema option : com.aula.fixa.domain.enums.CategoriaProblema.values()) {
                    if (option.getNome().equalsIgnoreCase(category)) {
                        categoryEnum = option;
                    }
                }
                com.aula.fixa.domain.enums.Prioridade priorityEnum = null;
                for (com.aula.fixa.domain.enums.Prioridade option : com.aula.fixa.domain.enums.Prioridade.values()) {
                    if (option.getNome().equalsIgnoreCase(priority)) {
                        priorityEnum = option;
                    }
                }
                if (categoryEnum == null || priorityEnum == null) {
                    return;
                }
                android.app.Dialog loading = AppLoading.show(activity);
                com.aula.fixa.adapter.os.ExecucaoOSViewModel editModel = new ViewModelProvider(this,
                        new com.aula.fixa.adapter.os.ExecucaoOSViewModelFactory(activity)).get(com.aula.fixa.adapter.os.ExecucaoOSViewModel.class);
                String dateForApi = planningDateForApi(date);
                if (dateForApi == null) {
                    android.widget.Toast.makeText(activity, "Selecione um prazo válido.", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                editModel.atualizarOS(osId, technicianId, categoryEnum.name(), dateForApi, priorityEnum.name(),
                        new com.aula.fixa.adapter.os.ExecucaoOSViewModel.Callback<Void>() {
                            @Override
                            public void success(Void value) {
                                AppLoading.hide(loading);
                                activity.model.field("execution", "editOrderId", "");
                                activity.open("manager_board");
                            }

                            @Override
                            public void failure(Throwable error) {
                                AppLoading.hide(loading);
                                finish.setEnabled(true);
                                android.widget.Toast.makeText(activity, com.aula.fixa.adapter.auth.AutenticacaoViewModel.messageFrom(error), android.widget.Toast.LENGTH_LONG).show();
                            }
                        });
                return;
            }
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

    private String planningDateForApi(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ROOT)
                    .format(new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.ROOT).parse(value));
        } catch (java.text.ParseException ignored) {
            try {
                return java.time.LocalDate.parse(value).toString();
            } catch (java.time.format.DateTimeParseException invalid) {
                return null;
            }
        }
    }

    private void bindOrderEditInformation(View content, OperationsActivity activity) {
        hidePlanningTimeline(content);
        View next = content.findViewWithTag("action:Próximo");
        if (next != null) {
            next.setOnClickListener(v -> {
                String date = activity.model.field("plan_information", "prazo");
                if (date == null || date.trim().isEmpty()) {
                    android.widget.Toast.makeText(activity, "Selecione um prazo de entrega.", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                activity.openPlanningStep("assign_member");
            });
        }
        View back = content.findViewWithTag("text:Voltar");
        if (back != null) {
            back.setOnClickListener(v -> {
                activity.model.field("execution", "editOrderId", "");
                activity.back();
            });
        }
    }

    private void bindOrderEditTechnician(View content) {
        for (int id : new int[]{com.aula.fixa.R.id.ops_assign_step_1, com.aula.fixa.R.id.ops_assign_step_2,
                com.aula.fixa.R.id.ops_assign_step_number_1, com.aula.fixa.R.id.ops_assign_step_number_2,
                com.aula.fixa.R.id.ops_assign_step_line, com.aula.fixa.R.id.ops_assign_step_label_1,
                com.aula.fixa.R.id.ops_assign_step_line_1_progress,
                com.aula.fixa.R.id.ops_assign_step_label_2, com.aula.fixa.R.id.ops_assign_step_3,
                com.aula.fixa.R.id.ops_assign_step_number_3, com.aula.fixa.R.id.ops_assign_step_line_2,
                com.aula.fixa.R.id.ops_assign_step_line_1_progress,
                com.aula.fixa.R.id.ops_assign_step_line_2_progress,
                com.aula.fixa.R.id.ops_assign_step_label_3}) {
            View v = content.findViewById(id);
            if (v != null) {
                v.setVisibility(View.GONE);
            }
        }
        View finish = content.findViewWithTag("action:Finalizar");
        if (finish instanceof android.widget.TextView) {
            ((android.widget.TextView) finish).setText("Salvar");
        }
        View back = content.findViewWithTag("text:Voltar");
        if (back != null) {
            back.setOnClickListener(v -> ((OperationsActivity) requireActivity()).returnFromPlanningAssignment(true));
        }
        View backArrow = content.findViewWithTag("image:arrow-left");
        if (backArrow != null) {
            backArrow.setOnClickListener(v -> ((OperationsActivity) requireActivity()).returnFromPlanningAssignment(true));
        }
    }

    private void hidePlanningTimeline(View content) {
        for (int id : new int[]{com.aula.fixa.R.id.ops_5717_6249, com.aula.fixa.R.id.ops_5717_6246,
                com.aula.fixa.R.id.ops_5717_6250, com.aula.fixa.R.id.ops_5717_6247,
                com.aula.fixa.R.id.ops_shape_2, com.aula.fixa.R.id.ops_shape_9,
                com.aula.fixa.R.id.ops_5717_6243, com.aula.fixa.R.id.ops_5717_6244,
                com.aula.fixa.R.id.ops_plan_step_3_circle, com.aula.fixa.R.id.ops_plan_step_3_number,
                com.aula.fixa.R.id.ops_plan_step_connector_2, com.aula.fixa.R.id.ops_plan_step_3_label}) {
            View v = content.findViewById(id);
            if (v != null) {
                v.setVisibility(View.GONE);
            }
        }
    }

    private void bindAvailableTechnicians(View content, OperationsActivity activity) {
        View.OnClickListener returnToTasks = v -> activity.returnFromPlanningAssignment(false);
        View backButton = content.findViewWithTag("text:Voltar");
        if (backButton != null) {
            backButton.setOnClickListener(returnToTasks);
        }
        View backArrow = content.findViewWithTag("image:arrow-left");
        if (backArrow != null) {
            backArrow.setOnClickListener(returnToTasks);
        }
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

    private void bindTeamTechnicians(View content) {
        RecyclerView list = content.findViewById(R.id.ops_manager_team_list);
        TextView empty = content.findViewById(R.id.ops_manager_team_empty);
        if (list == null || empty == null) {
            return;
        }
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.setNestedScrollingEnabled(false);
        list.setClipToPadding(false);
        OperationsActivity activity = (OperationsActivity) requireActivity();
        TecnicoEquipeAdapter adapter = new TecnicoEquipeAdapter(technician -> {
            activity.model.field("manager_member_details", "tecnicoId", String.valueOf(technician.id));
            activity.model.field("manager_member_details", "nome", technician.nome);
            activity.model.field("manager_member_details", "tipoAcesso", technician.tipoAcesso);
            activity.model.field("manager_member_details", "foto", technician.urlFoto);
            activity.openName("member_details");
        });
        list.setAdapter(adapter);
        list.setVisibility(View.INVISIBLE);
        Dialog loading = AppLoading.show(requireContext());
        TecnicoViewModel viewModel = new ViewModelProvider(this,
                new TecnicoViewModelFactory(requireContext())).get(TecnicoViewModel.class);
        viewModel.listar(new TecnicoViewModel.Callback<List<TecnicoDisponivel>>() {
            @Override
            public void success(List<TecnicoDisponivel> technicians) {
                AppLoading.hide(loading);
                if (!isAdded() || getView() == null || !content.isAttachedToWindow()) {
                    return;
                }
                List<TecnicoDisponivel> values = technicians == null
                        ? java.util.Collections.emptyList() : technicians;
                adapter.submit(values);
                list.setVisibility(values.isEmpty() ? View.GONE : View.VISIBLE);
                empty.setText(values.isEmpty() ? "Sem técnicos cadastrados." : "");
                empty.setVisibility(values.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void failure(Throwable error) {
                AppLoading.hide(loading);
                if (!isAdded() || getView() == null || !content.isAttachedToWindow()) {
                    return;
                }
                list.setVisibility(View.GONE);
                empty.setText("Não foi possível carregar os técnicos. Toque para tentar novamente.");
                empty.setVisibility(View.VISIBLE);
                empty.setOnClickListener(v -> bindTeamTechnicians(content));
                android.widget.Toast.makeText(requireContext(),
                        com.aula.fixa.adapter.auth.AutenticacaoViewModel.messageFrom(error),
                        android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindMemberDetails(View content) {
        OperationsActivity activity = (OperationsActivity) requireActivity();
        TextView name = content.findViewById(R.id.ops_5717_7671);
        TextView role = content.findViewById(R.id.ops_5717_7672);
        TextView activeCount = content.findViewById(R.id.ops_5717_7679);
        TextView completedCount = content.findViewById(R.id.ops_5717_7680);
        android.widget.ImageView avatar = content.findViewById(R.id.ops_5717_7670);
        String technicianName = activity.model.field("manager_member_details", "nome");
        String accessType = activity.model.field("manager_member_details", "tipoAcesso");
        String photoUrl = activity.model.field("manager_member_details", "foto");
        String technicianId = activity.model.field("manager_member_details", "tecnicoId");
        if (name != null) {
            name.setText(technicianName == null ? "" : technicianName);
        }
        if (role != null) {
            role.setText(accessType == null || accessType.isEmpty() ? "Técnico" : accessType);
        }
        View backArrow = content.findViewWithTag("image:arrow-left");
        if (backArrow != null) {
            backArrow.setOnClickListener(v -> activity.back());
        }
        View seeMoreCompetencies = content.findViewById(R.id.ops_5717_7681);
        View seeMoreArrow = content.findViewById(R.id.ops_5717_7682);
        View.OnClickListener openCompetencies = v -> {
            try {
                long id = technicianId == null ? 0 : Long.parseLong(technicianId);
                if (id > 0) activity.openCompetencies(id);
                else android.widget.Toast.makeText(requireContext(), "Técnico inválido.", android.widget.Toast.LENGTH_LONG).show();
            } catch (NumberFormatException ignored) {
                android.widget.Toast.makeText(requireContext(), "Técnico inválido.", android.widget.Toast.LENGTH_LONG).show();
            }
        };
        if (seeMoreCompetencies != null) seeMoreCompetencies.setOnClickListener(openCompetencies);
        if (seeMoreArrow != null) seeMoreArrow.setOnClickListener(openCompetencies);
        if (avatar != null) {
            avatar.setImageResource(R.drawable.img_avatar_115);
            if (photoUrl != null && !photoUrl.trim().isEmpty()) {
                com.bumptech.glide.Glide.with(avatar).load(photoUrl)
                        .placeholder(R.drawable.img_avatar_115).fallback(R.drawable.img_avatar_115)
                        .error(R.drawable.img_avatar_115).circleCrop().into(avatar);
            }
        }
        RecyclerView responsibilities = content.findViewById(R.id.ops_manager_member_responsibilities);
        ResponsabilidadesTecnicoAdapter responsibilityAdapter = new ResponsabilidadesTecnicoAdapter();
        if (responsibilities != null) {
            responsibilities.setLayoutManager(new LinearLayoutManager(requireContext()));
            responsibilities.setNestedScrollingEnabled(false);
            responsibilities.setClipToPadding(false);
            responsibilities.setAdapter(responsibilityAdapter);
        }
        if (activeCount != null) {
            activeCount.setText("—");
        }
        if (completedCount != null) {
            completedCount.setText("—");
        }
        bindCompetence(content, java.util.Collections.emptyList(), 0, R.id.ops_5717_7694, R.id.ops_5717_7695);
        bindCompetence(content, java.util.Collections.emptyList(), 1, R.id.ops_5717_7686, R.id.ops_5717_7687);
        bindCompetence(content, java.util.Collections.emptyList(), 2, R.id.ops_5717_7690, R.id.ops_5717_7691);
        long id;
        try {
            id = technicianId == null ? 0 : Long.parseLong(technicianId);
        } catch (NumberFormatException ignored) {
            id = 0;
        }
        if (id <= 0) {
            return;
        }
        Dialog loading = AppLoading.show(requireContext());
        TecnicoViewModel viewModel = new ViewModelProvider(this,
                new TecnicoViewModelFactory(requireContext())).get(TecnicoViewModel.class);
        viewModel.detalhes(id, new TecnicoViewModel.Callback<TecnicoDetalhes>() {
            @Override
            public void success(TecnicoDetalhes data) {
                AppLoading.hide(loading);
                if (!isAdded() || getView() == null || !content.isAttachedToWindow()) {
                    return;
                }
                if (activeCount != null) {
                    activeCount.setText(String.valueOf(data.ordensAtivas));
                }
                if (completedCount != null) {
                    completedCount.setText(String.valueOf(data.ordensConcluidas));
                }
                bindCompetence(content, data.competencias, 0, R.id.ops_5717_7694, R.id.ops_5717_7695);
                bindCompetence(content, data.competencias, 1, R.id.ops_5717_7686, R.id.ops_5717_7687);
                bindCompetence(content, data.competencias, 2, R.id.ops_5717_7690, R.id.ops_5717_7691);
                RecyclerView list = content.findViewById(R.id.ops_manager_member_responsibilities);
                if (list != null && list.getAdapter() instanceof ResponsabilidadesTecnicoAdapter) {
                    ((ResponsabilidadesTecnicoAdapter) list.getAdapter()).submit(data.responsabilidades);
                }
            }

            @Override
            public void failure(Throwable error) {
                AppLoading.hide(loading);
                if (!isAdded() || getView() == null || !content.isAttachedToWindow()) {
                    return;
                }
                android.widget.Toast.makeText(requireContext(),
                        com.aula.fixa.adapter.auth.AutenticacaoViewModel.messageFrom(error),
                        android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindCompetencies(View content) {
        OperationsActivity activity = (OperationsActivity) requireActivity();
        View back = content.findViewWithTag("image:arrow-left");
        if (back != null) back.setOnClickListener(v -> activity.back());
        long technicianId = getArguments() == null ? 0 : getArguments().getLong("technicianId", 0);
        int[] cardIds = {R.id.ops_5717_6041, R.id.ops_5717_6032, R.id.ops_5717_5996,
                R.id.ops_5717_6005, R.id.ops_5717_6014, R.id.ops_5717_6023};
        int[] nameIds = {R.id.ops_5717_6045, R.id.ops_5717_6036, R.id.ops_5717_6000,
                R.id.ops_5717_6009, R.id.ops_5717_6018, R.id.ops_5717_6027};
        int[] scoreIds = {R.id.ops_5717_6046, R.id.ops_5717_6037, R.id.ops_5717_6001,
                R.id.ops_5717_6010, R.id.ops_5717_6019, R.id.ops_5717_6028};
        int[] editIds = {R.id.ops_5717_6042, R.id.ops_5717_6033, R.id.ops_5717_5997,
                R.id.ops_5717_6006, R.id.ops_5717_6015, R.id.ops_5717_6024};
        for (int i = 0; i < cardIds.length; i++) {
            View card = content.findViewById(cardIds[i]);
            if (card != null) card.setVisibility(View.GONE);
        }
        if (technicianId <= 0) {
            String saved = activity.model.field("manager_member_details", "tecnicoId");
            try { technicianId = saved == null ? 0 : Long.parseLong(saved); }
            catch (NumberFormatException ignored) { technicianId = 0; }
        }
        if (technicianId <= 0) {
            android.widget.Toast.makeText(requireContext(), "Técnico inválido.", android.widget.Toast.LENGTH_LONG).show();
            return;
        }
        Dialog loading = AppLoading.show(requireContext());
        TecnicoViewModel viewModel = new ViewModelProvider(this,
                new TecnicoViewModelFactory(requireContext())).get(TecnicoViewModel.class);
        final long targetId = technicianId;
        viewModel.competencias(targetId, new TecnicoViewModel.Callback<List<CompetenciaTecnico>>() {
            @Override public void success(List<CompetenciaTecnico> values) {
                AppLoading.hide(loading);
                if (!isAdded() || !content.isAttachedToWindow()) return;
                int count = Math.min(values == null ? 0 : values.size(), cardIds.length);
                for (int i = 0; i < count; i++) {
                    CompetenciaTecnico competence = values.get(i);
                    View card = content.findViewById(cardIds[i]);
                    TextView name = content.findViewById(nameIds[i]);
                    TextView score = content.findViewById(scoreIds[i]);
                    View edit = content.findViewById(editIds[i]);
                    if (card != null) card.setVisibility(View.VISIBLE);
                    if (name != null) name.setText(competenceName(competence.nome));
                    if (score != null) score.setText(competence.nota == null ? "_"
                            : competence.nota.stripTrailingZeros().toPlainString());
                    if (edit != null) {
                        edit.setVisibility(competence.id == null ? View.INVISIBLE : View.VISIBLE);
                        edit.setOnClickListener(v -> activity.openCompetencyEdit(competence));
                    }
                }
            }
            @Override public void failure(Throwable error) {
                AppLoading.hide(loading);
                if (isAdded()) android.widget.Toast.makeText(requireContext(),
                        com.aula.fixa.adapter.auth.AutenticacaoViewModel.messageFrom(error),
                        android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindCompetence(
            View content, List<CompetenciaTecnico> values, int index,
            int nameId, int scoreId) {
        TextView name = content.findViewById(nameId);
        TextView score = content.findViewById(scoreId);
        if (name == null || score == null) {
            return;
        }
        if (values == null || values.size() <= index) {
            name.setText("—");
            score.setText("—");
            return;
        }
        CompetenciaTecnico competence = values.get(index);
        name.setText(competenceName(competence.nome));
        score.setText(competence.nota == null ? "_"
                : competence.nota.stripTrailingZeros().toPlainString());
    }

    private String competenceName(String raw) {
        if (raw == null) {
            return "";
        }
        for (CategoriaProblema category : CategoriaProblema.values()) {
            if (category.name().equalsIgnoreCase(raw) || category.getNome().equalsIgnoreCase(raw)) {
                if (category == CategoriaProblema.REDE_CONECTIVIDADE) return "Rede";
                if (category == CategoriaProblema.LIMPEZA_CONSERVACAO) return "Limpeza";
                if (category == CategoriaProblema.CLIMATIZACAO) return "Clima";
                return category.getNome();
            }
        }
        return raw;
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
            java.util.Calendar today = java.util.Calendar.getInstance();
            java.util.Calendar current = (java.util.Calendar) today.clone();
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
                            renderPlanningCalendar(calendar, option, today, activity);
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
                boolean selected = isSelectedPlanningDate(selectedDate, dateValue);
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

    private boolean isSelectedPlanningDate(String selectedValue, String calendarValue) {
        if (selectedValue == null || selectedValue.trim().isEmpty()) {
            return false;
        }
        try {
            java.time.LocalDate selected = java.time.LocalDate.parse(selectedValue.trim());
            java.time.LocalDate cell = java.time.LocalDate.parse(calendarValue,
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu", new java.util.Locale("pt", "BR")));
            return selected.equals(cell);
        } catch (java.time.format.DateTimeParseException ignored) {
            return calendarValue.equals(selectedValue.trim());
        }
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

    public void selectCompletionPhotos() {
        completionPhotoPicker.launch("image/*");
    }

    public void selectOccurrenceEditPhotos() {
        occurrenceEditPhotoPicker.launch("image/*");
    }

    @Override
    public void onManagerOSFilters(String equipment, String problem, String priority, String period, String status) {
        if (quadroOSFluxo != null) {
            quadroOSFluxo.filters(equipment, problem, priority, period, status);
        }
    }

    @Override
    public void onOccurrenceFilters(String period, String author, String location, String equipment, String category, String priority) {
        if (ocorrenciaGestorFluxo != null) {
            ocorrenciaGestorFluxo.filters(period, author, location, equipment, category, priority);
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
        if ("manager_board".equals(screen) && !initialResume && quadroOSFluxo != null) {
            quadroOSFluxo.refresh();
        }
        if ("manager_occurrences".equals(screen) && !initialResume && ocorrenciaGestorFluxo != null) {
            ocorrenciaGestorFluxo.refresh();
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
        if (quadroOSFluxo != null) {
            quadroOSFluxo.clear();
            quadroOSFluxo = null;
        }
        if (categoriaEquipamentoGestorFluxo != null) {
            categoriaEquipamentoGestorFluxo.clear();
            categoriaEquipamentoGestorFluxo = null;
        }
        if (ocorrenciaGestorFluxo != null) {
            ocorrenciaGestorFluxo.clear();
            ocorrenciaGestorFluxo = null;
        }
        execucaoOSFluxo = null;
        super.onDestroyView();
    }
}
