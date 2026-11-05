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
import com.aula.fixa.adapter.planejamento.RecomendacaoCategoriaViewModel;
import com.aula.fixa.adapter.planejamento.RecomendacaoCategoriaViewModelFactory;
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
import com.aula.fixa.ui.config.EmptyStateStyler;
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
    private com.aula.fixa.ui.view.chatbot.ChatbotFlow chatbotFlow;
    private final ActivityResultLauncher<String> chatbotPhotoPicker = registerForActivityResult(
            new ActivityResultContracts.GetMultipleContents(), uris -> {
                if (chatbotFlow != null && uris != null) chatbotFlow.onPhotoUris(uris);
            });
    private final ActivityResultLauncher<String> occurrenceEditPhotoPicker = registerForActivityResult(
            new ActivityResultContracts.GetMultipleContents(), uris -> {
                if (ocorrenciaGestorFluxo != null && uris != null) {
                    ocorrenciaGestorFluxo.uploadNewPhotos(uris);
                }
            });
    private final com.aula.fixa.ui.config.SolicitacaoPhotoPicker completionPhotoPicker =
            new com.aula.fixa.ui.config.SolicitacaoPhotoPicker(this);
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
            if (ignored != null && getView() == view && ("manager_home".equals(screen)
                    || "technician_home".equals(screen) || profile)) {
                render();
            }
        });
    }

    public void render() {
        if (getView() == null) {
            return;
        }
        android.widget.FrameLayout host = (android.widget.FrameLayout) getView();
        String requestedScreen = requireArguments().getString("screen", "manager_home");
        String screen = sharedManagerScreen(requestedScreen);
        if ("manager_board".equals(screen)) {
            ((OperationsActivity) requireActivity()).model.field("execution", "editOrderId", "");
        }
        if (chatbotFlow != null) {
            chatbotFlow.close();
            chatbotFlow = null;
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
                : screen.endsWith("_chatbot")
                ? com.aula.fixa.R.layout.fragment_chatbot
                : ScreenCatalog.get(requireContext()).layout(requireContext(), screen);
        View inflatedContent = getLayoutInflater().inflate(screenLayout, host, false);
        String editingOrderId = ((OperationsActivity) requireActivity()).model.field("execution", "editOrderId");
        boolean creatingPlanningStep = "manager_planning_tasks".equals(screen)
                || (("manager_plan_information".equals(screen) || "manager_assign_member".equals(screen))
                && (editingOrderId == null || editingOrderId.trim().isEmpty()));
        View content = creatingPlanningStep ? wrapPlanningStepWithFixedFooter(inflatedContent, screen) : inflatedContent;
        host.addView(content);
         styleSharedFormFields(content);
        if (screen.endsWith("_chatbot")) {
            boolean managerChatbot = screen.startsWith("manager_");
            Runnable chatBack = null;
            if (managerChatbot) {
                OperationsActivity managerActivity = (OperationsActivity) requireActivity();
                chatBack = managerActivity::backFromManagerChatbot;
            }
            chatbotFlow = new com.aula.fixa.ui.view.chatbot.ChatbotFlow(
                    this, content, () -> chatbotPhotoPicker.launch("image/*"), managerChatbot, chatBack);
        }
        if ("manager_home".equals(screen) || "technician_home".equals(requestedScreen)) {
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
        if ("technician_management".equals(screen)) {
            View boardCard = content.findViewById(R.id.ops_5717_9720);
            View occurrencesCard = content.findViewById(R.id.ops_5717_9727);
            if (boardCard != null) boardCard.setOnClickListener(v -> ((OperationsActivity) requireActivity()).openName("board"));
            if (occurrencesCard != null) occurrencesCard.setOnClickListener(v -> ((OperationsActivity) requireActivity()).openName("occurrences"));
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
        if ("manager_equipment".equals(screen)) {
            View categories = content.findViewById(R.id.ops_5717_6683);
            View models = content.findViewById(R.id.ops_5717_6705);
            View checklists = content.findViewById(R.id.ops_5717_6694);
            if (categories != null) categories.setOnClickListener(v -> ((OperationsActivity) requireActivity()).openName("categories"));
            if (models != null) models.setOnClickListener(v -> ((OperationsActivity) requireActivity()).openName("models"));
            if (checklists != null) checklists.setOnClickListener(v -> ((OperationsActivity) requireActivity()).openName("checklists"));
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
        } else if ("manager_plan_information".equals(screen)) {
            stylePlanningCreateInformation(content);
            bindPlanningCreateStepIndicator(content, 1);
        }
        if ("manager_assign_member".equals(screen) && editingOrder != null && !editingOrder.trim().isEmpty()) {
            bindOrderEditTechnician(content);
        } else if ("manager_assign_member".equals(screen)) {
            stylePlanningCreateTechnician(content);
            bindPlanningCreateStepIndicator(content, 3);
        }
        if ("manager_planning_tasks".equals(screen)) {
            styleOrderEditTitle(content, R.id.ops_5717_8499);
            styleOrderEditActions(content, R.id.ops_5717_8530,
                    R.id.ops_5717_8514, R.id.ops_5717_8516, false, 0);
            bindPlanningCreateStepIndicator(content, 2);
            planejamentoTarefasFluxo = new PlanejamentoTarefasFluxo(this, (OperationsActivity) requireActivity(), content);
            planejamentoTarefasFluxo.bind();
        } else {
            planejamentoTarefasFluxo = null;
        }
        if ("manager_planning_success".equals(screen)) {
            bindPlanningSuccess(content, (OperationsActivity) requireActivity());
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
        } else if ("technician_home".equals(requestedScreen)) {
            loadTechnicianHome(host, content);
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
        if ("manager_board".equals(screen) || "technician_board".equals(screen)) {
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
            ocorrenciaGestorFluxo.bindCreateStep(content, requestedScreen);
        } else if ("manager_occurrences".equals(screen)) {
            ocorrenciaGestorFluxo = new com.aula.fixa.ui.view.ocorrencia.OcorrenciaGestorFluxo(this, (OperationsActivity) requireActivity());
            ocorrenciaGestorFluxo.bindList(content);
        } else if ("manager_occurrence_details".equals(screen)) {
            ocorrenciaGestorFluxo = new com.aula.fixa.ui.view.ocorrencia.OcorrenciaGestorFluxo(this, (OperationsActivity) requireActivity());
            ocorrenciaGestorFluxo.bindDetails(content);
        } else if ("technician_occurrence_details".equals(screen)) {
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
        content.post(() -> com.aula.fixa.ui.config.TwoToneTitleStyler.apply(content));
    }


    private void styleSharedFormFields(View view) {
        if (view instanceof android.widget.EditText) {
            android.widget.EditText field = (android.widget.EditText) view;
            field.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.app_text_black));
            field.setHintTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.ops_color_ff637087));
            field.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(requireContext(), R.font.poppins_regular));
            if ("field:Pesquisar".equals(field.getTag())) field.setHint("Pesquisar...");
        }
        if (!(view instanceof ViewGroup)) return;
        ViewGroup group = (ViewGroup) view;
        boolean hasSearchField = false;
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof android.widget.EditText && "field:Pesquisar".equals(child.getTag())) hasSearchField = true;
        }
        if (hasSearchField) {
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                Object tag = child.getTag();
                CharSequence description = child.getContentDescription();
                boolean searchIcon = "image:search".equals(tag)
                        || description != null && (description.toString().toLowerCase(java.util.Locale.ROOT).contains("pesquis")
                        || description.toString().toLowerCase(java.util.Locale.ROOT).contains("search"));
                if (searchIcon && child instanceof android.widget.ImageView) {
                    android.widget.ImageView icon = (android.widget.ImageView) child;
                    icon.setImageResource(R.drawable.ic_search_26);
                    icon.setColorFilter(androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.white));
                    if (!(icon instanceof android.widget.ImageButton)) icon.setTranslationX(-getResources().getDisplayMetrics().density * 10f);
                }
            }
        }
        for (int i = 0; i < group.getChildCount(); i++) styleSharedFormFields(group.getChildAt(i));
    }

    private boolean isManagerOccurrenceCreateScreen(String screen) {
        return "manager_occurrence_intro".equals(screen)
                || "manager_occurrence_basic".equals(screen)
                || "manager_occurrence_service".equals(screen)
                || "manager_occurrence_location".equals(screen)
                || ("manager_occurrence_result".equals(screen)
                && (getArguments() == null || getArguments().getLong("orderId", 0) == 0));
    }

    private String sharedManagerScreen(String screen) {
        if (!"technician".equals(((OperationsActivity) requireActivity()).role())) return screen;
        if ("technician_occurrences".equals(screen)) return "manager_occurrences";
        if (screen.startsWith("technician_occurrence_")) {
            String suffix = screen.substring("technician_occurrence_".length());
            return "success".equals(suffix) ? screen : "manager_occurrence_" + suffix;
        }
        if ("technician_execution_pending".equals(screen)) return "manager_execution_pending";
        return screen;
    }

    private int managerOccurrenceLayout(String screen) {
        if ("manager_occurrence_intro".equals(screen)) {
            return R.layout.fragment_ops_manager_occurrence_intro;
        }
        if ("manager_occurrence_basic".equals(screen)) {
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
        hidePlanningCreateIndicator(content);
        styleOrderEditTitle(content, R.id.ops_5717_6240);
        styleOrderEditLabel(content, R.id.ops_5717_6259);
        styleOrderEditLabel(content, R.id.ops_5717_6239);
        styleOrderEditLabel(content, R.id.ops_plan_due_date_label);
        setOrderEditTopMarginDp(content, R.id.ops_5717_6259, 12);
        styleOrderEditField(content, R.id.ops_5717_6260);
        styleOrderEditField(content, R.id.ops_5717_6263);
        positionOrderEditPriority(content);
        setOrderEditTopMarginDp(content, R.id.ops_plan_due_date_label, 18);
        styleOrderEditMonthSelector(content);
        styleOrderEditCalendar(content);
        styleOrderEditActions(content, R.id.ops_5717_8889,
                R.id.ops_5717_6255, R.id.ops_5717_6257, false, dp(42));
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

    /** Mantém o mesmo desenho do formulário de edição durante a criação da OS. */
    private void stylePlanningCreateInformation(View content) {
        styleOrderEditTitle(content, R.id.ops_5717_6240);
        styleOrderEditLabel(content, R.id.ops_5717_6259);
        styleOrderEditLabel(content, R.id.ops_5717_6239);
        styleOrderEditLabel(content, R.id.ops_plan_due_date_label);
        setOrderEditTopMarginDp(content, R.id.ops_5717_6259, 12);
        styleOrderEditField(content, R.id.ops_5717_6260);
        styleOrderEditField(content, R.id.ops_5717_6263);
        positionOrderEditPriority(content);
        setOrderEditTopMarginDp(content, R.id.ops_plan_due_date_label, 18);
        styleOrderEditMonthSelector(content);
        styleOrderEditCalendar(content);
        styleOrderEditActions(content, R.id.ops_5717_8889,
                R.id.ops_5717_6255, R.id.ops_5717_6257, false, dp(42));
    }

    /** Aplica no planejamento de criação as dimensões usadas ao editar e atribuir uma OS. */
    private void stylePlanningCreateTechnician(View content) {
        styleOrderEditTitle(content, R.id.ops_5717_8739);
        styleOrderEditLabel(content, R.id.ops_5717_8743);
        styleOrderEditTechnicianList(content);
        styleOrderEditActions(content, R.id.ops_available_technicians_container,
                R.id.ops_5717_8745, R.id.ops_5717_8744, true, dp(150));
    }

    private void bindOrderEditTechnician(View content) {
        hidePlanningCreateIndicator(content);
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
        styleOrderEditTitle(content, R.id.ops_5717_8739);
        styleOrderEditLabel(content, R.id.ops_5717_8743);
        styleOrderEditTechnicianList(content);
        styleOrderEditActions(content, R.id.ops_available_technicians_container,
                R.id.ops_5717_8745, R.id.ops_5717_8744, true, dp(150));
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

    private void styleOrderEditTitle(View content, int titleId) {
        View view = content.findViewById(titleId);
        if (!(view instanceof TextView)) return;
        TextView title = (TextView) view;
        androidx.core.widget.TextViewCompat.setTextAppearance(title, R.style.TextAppearance_FIXA_App_StepTitle);
        title.setIncludeFontPadding(false);
        title.setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);
        title.setMinHeight(0);
        title.setPadding(0, 0, 0, 0);
        if (title.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) title.getLayoutParams();
            int side = getResources().getDimensionPixelSize(R.dimen.app_step_title_margin_start);
            params.width = params.width == 0
                    ? androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
                    : ViewGroup.LayoutParams.WRAP_CONTENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            params.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            params.setMarginStart(side);
            params.setMarginEnd(side);
            params.topMargin = getResources().getDimensionPixelSize(R.dimen.app_step_title_margin_top);
            title.setLayoutParams(params);
        }
    }

    private void styleOrderEditLabel(View content, int labelId) {
        View view = content.findViewById(labelId);
        if (!(view instanceof TextView)) return;
        TextView label = (TextView) view;
        androidx.core.widget.TextViewCompat.setTextAppearance(label, R.style.TextAppearance_FIXA_App_Step_Label);
        label.setIncludeFontPadding(false);
        label.setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);
        label.setMinHeight(0);
        label.setPadding(0, 0, 0, 0);
        if (label.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) label.getLayoutParams();
            params.width = params.width == 0
                    ? androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
                    : ViewGroup.LayoutParams.WRAP_CONTENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            params.setMarginStart(getResources().getDimensionPixelSize(R.dimen.app_step_label_margin_start));
            params.topMargin = getResources().getDimensionPixelSize(R.dimen.app_step_label_margin_top);
            label.setLayoutParams(params);
        }
    }

    private void setOrderEditTopMarginDp(View content, int viewId, int marginDp) {
        View view = content.findViewById(viewId);
        if (view == null || !(view.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) return;
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) view.getLayoutParams();
        params.topMargin = dp(marginDp);
        view.setLayoutParams(params);
    }

    private void styleOrderEditField(View content, int fieldId) {
        View field = content.findViewById(fieldId);
        if (!(field instanceof androidx.constraintlayout.widget.ConstraintLayout)) return;
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) field.getLayoutParams();
        int side = getResources().getDimensionPixelSize(R.dimen.app_step_label_margin_start);
        params.width = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
        params.height = getResources().getDimensionPixelSize(R.dimen.app_step_field_height);
        params.setMarginStart(side);
        params.setMarginEnd(side);
        params.topMargin = dp(4);
        field.setLayoutParams(params);
        field.setBackgroundResource(R.drawable.bg_app_step_field);

        if (field instanceof ViewGroup) {
            ViewGroup container = (ViewGroup) field;
            for (int i = 0; i < container.getChildCount(); i++) {
                View child = container.getChildAt(i);
                if (child instanceof android.widget.EditText) {
                    android.widget.EditText input = (android.widget.EditText) child;
                    androidx.core.widget.TextViewCompat.setTextAppearance(input, R.style.TextAppearance_FIXA_App_Step_Field);
                    input.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.app_text_black));
                    input.setHintTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.app_text_placeholder_dark));
                    input.setIncludeFontPadding(false);
                    int padding = getResources().getDimensionPixelSize(R.dimen.app_step_field_padding_start);
                    input.setPaddingRelative(padding, 0, padding, 0);
                    input.setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);
                } else if (child instanceof android.widget.ImageView
                        && child.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams iconParams =
                            (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) child.getLayoutParams();
                    iconParams.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                    iconParams.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                    iconParams.topMargin = 0;
                    iconParams.bottomMargin = 0;
                    iconParams.setMarginEnd(dp(8));
                    child.setLayoutParams(iconParams);
                }
            }
        }
    }

    private void positionOrderEditPriority(View content) {
        View label = content.findViewById(R.id.ops_5717_6239);
        if (label == null || !(label.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) return;
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) label.getLayoutParams();
        params.topToBottom = R.id.ops_5717_6260;
        params.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
        params.topMargin = dp(4);
        label.setLayoutParams(params);
    }

    private void styleOrderEditMonthSelector(View content) {
        View selector = content.findViewById(R.id.ops_5717_8884);
        if (selector == null || !(selector.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) return;
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) selector.getLayoutParams();
        params.height = dp(39);
        params.topToTop = R.id.ops_plan_due_date_label;
        params.bottomToBottom = R.id.ops_plan_due_date_label;
        params.setMarginStart(dp(8));
        params.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.app_step_label_margin_start));
        params.topMargin = 0;
        params.bottomMargin = 0;
        selector.setLayoutParams(params);
        selector.setBackgroundResource(R.drawable.bg_ops_b3c2842933);
        TextView month = content.findViewById(R.id.ops_5717_8885);
        if (month != null) {
            month.setTextSize(11);
            month.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(requireContext(), R.font.poppins_medium));
            month.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.app_on_primary));
            month.setGravity(android.view.Gravity.CENTER_VERTICAL);
            month.setIncludeFontPadding(false);
        }
        View arrow = content.findViewById(R.id.ops_5717_8886);
        if (arrow instanceof android.widget.ImageView) {
            ((android.widget.ImageView) arrow).setColorFilter(
                    androidx.core.content.ContextCompat.getColor(requireContext(), R.color.app_on_primary));
        }
    }

    private void styleOrderEditCalendar(View content) {
        View calendar = content.findViewById(R.id.ops_5717_8889);
        if (calendar == null || !(calendar.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) return;
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) calendar.getLayoutParams();
        int side = getResources().getDimensionPixelSize(R.dimen.app_step_label_margin_start);
        params.setMarginStart(side);
        params.setMarginEnd(side);
        params.topMargin = dp(16);
        calendar.setLayoutParams(params);
    }

    private void styleOrderEditTechnicianList(View content) {
        if (content instanceof ViewGroup && ((ViewGroup) content).getChildCount() > 0) {
            ((ViewGroup) content).getChildAt(0).setMinimumHeight(0);
        }
        View list = content.findViewById(R.id.ops_available_technicians_container);
        if (list == null || !(list.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) return;
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) list.getLayoutParams();
        int side = getResources().getDimensionPixelSize(R.dimen.app_step_label_margin_start);
        params.setMarginStart(side);
        params.setMarginEnd(side);
        params.topMargin = getResources().getDimensionPixelSize(R.dimen.app_step_field_margin_top);
        list.setLayoutParams(params);
    }

    private void styleOrderEditActions(View content, int anchorId, int cancelId, int primaryId,
                                       boolean cancelIsContainer, int extraTopMargin) {
        int height = getResources().getDimensionPixelSize(R.dimen.app_button_height_lg);
        int top = extraTopMargin > 0 ? extraTopMargin
                : getResources().getDimensionPixelSize(R.dimen.app_step_section_margin_top);
        int backStart = getResources().getDimensionPixelSize(R.dimen.app_step_back_margin_start);
        int sideEnd = getResources().getDimensionPixelSize(R.dimen.app_step_actions_side_margin);
        int buttonWidth = getResources().getDimensionPixelSize(R.dimen.app_button_step_width);
        View cancel = content.findViewById(cancelId);
        View primary = content.findViewById(primaryId);
        if (cancel != null && cancel.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) cancel.getLayoutParams();
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.height = height;
            params.setMarginStart(backStart);
            params.setMarginEnd(0);
            params.topMargin = top;
            params.bottomMargin = 0;
            params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            params.endToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
            params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
            params.topToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
            params.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
            params.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
            params.topToBottom = anchorId;
            cancel.setLayoutParams(params);
            cancel.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            if (cancelIsContainer && cancel instanceof ViewGroup && ((ViewGroup) cancel).getChildCount() > 0) {
                View child = ((ViewGroup) cancel).getChildAt(0);
                if (child instanceof TextView) {
                    TextView text = (TextView) child;
                    androidx.core.widget.TextViewCompat.setTextAppearance(text, R.style.TextAppearance_FIXA_App_Step_Action);
                    text.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.app_text_black));
                    text.setGravity(android.view.Gravity.CENTER);
                    text.setIncludeFontPadding(false);
                    text.setMinWidth(0);
                    text.setMinHeight(0);
                    text.setPadding(0, 0, 0, 0);
                    if (text.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
                        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams childParams =
                                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) text.getLayoutParams();
                        childParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                        childParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                        childParams.setMarginStart(0);
                        childParams.topMargin = 0;
                        text.setLayoutParams(childParams);
                    }
                }
            } else if (cancel instanceof TextView) {
                TextView text = (TextView) cancel;
                androidx.core.widget.TextViewCompat.setTextAppearance(text, R.style.TextAppearance_FIXA_App_Step_Action);
                text.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.app_text_black));
                text.setGravity(android.view.Gravity.CENTER);
                text.setIncludeFontPadding(false);
                text.setMinWidth(0);
                text.setMinHeight(0);
                text.setPadding(0, 0, 0, 0);
            }
        }
        if (primary != null && primary.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) primary.getLayoutParams();
            params.width = buttonWidth;
            params.height = height;
            params.setMarginStart(0);
            params.setMarginEnd(sideEnd);
            params.topMargin = 0;
            params.bottomMargin = 0;
            params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
            params.startToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
            params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            params.topToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
            params.topToTop = cancelId;
            params.bottomToBottom = cancelId;
            primary.setLayoutParams(params);
            primary.setBackgroundResource(R.drawable.bg_ops_e9ff3d87c6);
            primary.setPadding(0, 0, 0, 0);
            primary.setMinimumWidth(0);
            primary.setMinimumHeight(0);
            if (primary instanceof TextView) {
                TextView text = (TextView) primary;
                androidx.core.widget.TextViewCompat.setTextAppearance(text, R.style.TextAppearance_FIXA_App_Step_Action);
                text.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.app_on_primary));
                text.setIncludeFontPadding(false);
                text.setAllCaps(false);
                text.setGravity(android.view.Gravity.CENTER);
                android.graphics.Typeface regular = androidx.core.content.res.ResourcesCompat.getFont(requireContext(), R.font.poppins_regular);
                if (regular != null) text.setTypeface(regular);
            }
            if (primary instanceof androidx.appcompat.widget.AppCompatButton) {
                ((androidx.appcompat.widget.AppCompatButton) primary).setSupportBackgroundTintList(null);
            }
        }
    }

    private void bindPlanningCreateStepIndicator(View content, int currentStep) {
        View indicator = content.findViewById(R.id.ops_create_planning_step_indicator);
        if (indicator == null) return;
        indicator.setVisibility(View.VISIBLE);

        String screen = requireArguments().getString("screen", "");
        if ("manager_plan_information".equals(screen)) {
            hidePlanningTimeline(content);
        } else if ("manager_planning_tasks".equals(screen)) {
            hidePlanningTasksTimeline(content);
        } else if ("manager_assign_member".equals(screen)) {
            hidePlanningAssignmentTimeline(content);
        }

        int[] circles = {R.id.tvStep1, R.id.tvStep2, R.id.tvStep3};
        int[] labels = {R.id.tvStep1Label, R.id.tvStep2Label, R.id.tvStep3Label};
        String[] names = {getString(R.string.ops_text_informacoes_07156d),
                getString(R.string.ops_text_tarefas_4b2b8a),
                getString(R.string.ops_planning_assign_technician)};
        for (int i = 0; i < circles.length; i++) {
            int state = i + 1 < currentStep ? 0 : i + 1 == currentStep ? 1 : 2;
            View circle = indicator.findViewById(circles[i]);
            TextView label = indicator.findViewById(labels[i]);
            if (circle != null) {
                circle.setBackgroundResource(state == 0 ? R.drawable.bg_app_circle_done
                        : state == 1 ? R.drawable.bg_app_circle_primary : R.drawable.bg_app_circle_muted);
            }
            if (label != null) {
                label.setText(names[i]);
                label.setSingleLine(true);
                label.setMaxLines(1);
                label.setEllipsize(android.text.TextUtils.TruncateAt.END);
                label.setGravity(android.view.Gravity.CENTER);
                if (label.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams labelParams =
                            (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) label.getLayoutParams();
                    labelParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    label.setLayoutParams(labelParams);
                }
                int color = state == 0 ? R.color.app_status_done
                        : state == 1 ? R.color.app_primary : R.color.app_text_muted;
                label.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), color));
            }
        }

        int[] lines = {R.id.viewLine1, R.id.viewLine2};
        for (int i = 0; i < lines.length; i++) {
            View line = indicator.findViewById(lines[i]);
            if (line == null) continue;
            if (currentStep > i + 1) {
                line.setBackgroundResource(R.drawable.bg_app_indicator_line_done);
            } else if (currentStep == i + 1) {
                line.setBackgroundResource(R.drawable.bg_app_indicator_line_progress);
                if (line.getBackground() != null) line.getBackground().setLevel(5000);
            } else {
                line.setBackgroundResource(R.drawable.bg_app_indicator_line_pending);
            }
        }

        int[] actions;
        if ("manager_plan_information".equals(screen)) {
            actions = new int[]{R.id.ops_5717_6255, R.id.ops_5717_6257};
        } else if ("manager_planning_tasks".equals(screen)) {
            actions = new int[]{R.id.ops_5717_8514, R.id.ops_5717_8516};
        } else {
            actions = new int[]{R.id.ops_5717_8745, R.id.ops_5717_8744};
        }
        if ("manager_planning_tasks".equals(screen)) {
            positionPlanningTaskFlow(content);
        }
        positionPlanningFixedFooter(content, indicator, actions[0], actions[1], screen);
    }

    /** Reparents the progress and action controls so all planning steps share a fixed footer. */
    private View wrapPlanningStepWithFixedFooter(View inflated, String screen) {
        if (!(inflated instanceof androidx.core.widget.NestedScrollView)) return inflated;
        androidx.core.widget.NestedScrollView scroll = (androidx.core.widget.NestedScrollView) inflated;
        if (scroll.getChildCount() == 0
                || !(scroll.getChildAt(0) instanceof androidx.constraintlayout.widget.ConstraintLayout)) return inflated;

        int cancelId;
        int primaryId;
        if ("manager_plan_information".equals(screen)) {
            cancelId = R.id.ops_5717_6255;
            primaryId = R.id.ops_5717_6257;
        } else if ("manager_planning_tasks".equals(screen)) {
            cancelId = R.id.ops_5717_8514;
            primaryId = R.id.ops_5717_8516;
        } else {
            cancelId = R.id.ops_5717_8745;
            primaryId = R.id.ops_5717_8744;
        }

        View indicator = scroll.findViewById(R.id.ops_create_planning_step_indicator);
        View cancel = scroll.findViewById(cancelId);
        View primary = scroll.findViewById(primaryId);
        android.view.ViewParent footerParent = indicator == null ? null : indicator.getParent();
        if (!(footerParent instanceof androidx.constraintlayout.widget.ConstraintLayout)
                || cancel == null || primary == null
                || cancel.getParent() != footerParent || primary.getParent() != footerParent) return inflated;

        androidx.constraintlayout.widget.ConstraintLayout page = new androidx.constraintlayout.widget.ConstraintLayout(requireContext());
        ((androidx.constraintlayout.widget.ConstraintLayout) scroll.getChildAt(0)).setMinimumHeight(0);
        page.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        android.graphics.drawable.Drawable background = scroll.getBackground();
        if (background != null) page.setBackground(background.mutate());
        else page.setBackgroundColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.ops_color_fff5f7fa));
        page.setClipChildren(false);
        page.setClipToPadding(false);
        scroll.setBackgroundColor(android.graphics.Color.TRANSPARENT);

        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams indicatorParams =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) indicator.getLayoutParams();
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams cancelParams =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) cancel.getLayoutParams();
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams primaryParams =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) primary.getLayoutParams();
        ((ViewGroup) footerParent).removeView(indicator);
        ((ViewGroup) footerParent).removeView(cancel);
        ((ViewGroup) footerParent).removeView(primary);

        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams scrollParams =
                new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(0, 0);
        scrollParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        scrollParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        scrollParams.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        scrollParams.bottomToTop = indicator.getId();
        scroll.setLayoutParams(scrollParams);
        page.addView(scroll, scrollParams);
        page.addView(indicator, indicatorParams);
        page.addView(cancel, cancelParams);
        page.addView(primary, primaryParams);
        return page;
    }

    private void positionPlanningTaskFlow(View content) {
        int contentSide = getResources().getDimensionPixelSize(R.dimen.app_step_title_margin_start);
        View list = content.findViewById(R.id.ops_planning_tasks_list);
        if (list != null && list.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) list.getLayoutParams();
            params.topToBottom = R.id.ops_5717_8499;
            params.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
            params.topMargin = getResources().getDimensionPixelSize(R.dimen.app_step_field_margin_top);
            params.setMarginStart(contentSide);
            params.setMarginEnd(contentSide);
            list.setLayoutParams(params);
        }

        View addTask = content.findViewById(R.id.ops_5717_8530);
        if (addTask != null && addTask.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) addTask.getLayoutParams();
            params.topToBottom = R.id.ops_planning_tasks_list;
            params.topMargin = dp(12);
            params.setMarginStart(contentSide);
            params.setMarginEnd(contentSide);
            addTask.setLayoutParams(params);
        }
    }

    private void positionPlanningFixedFooter(View content, View indicator, int cancelId, int primaryId, String screen) {
        if (!(content instanceof androidx.constraintlayout.widget.ConstraintLayout)
                || !(indicator.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) return;
        int parentId = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        int footerGap = getResources().getDimensionPixelSize(R.dimen.app_step_section_margin_top);
        int bottomGap = "manager_plan_information".equals(screen)
                ? dp(24) : getResources().getDimensionPixelSize(R.dimen.app_step_actions_margin_bottom);

        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams indicatorParams =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) indicator.getLayoutParams();
        indicatorParams.width = 0;
        indicatorParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        indicatorParams.startToStart = parentId;
        indicatorParams.endToEnd = parentId;
        indicatorParams.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
        indicatorParams.topToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
        indicatorParams.bottomToTop = cancelId;
        indicatorParams.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
        indicatorParams.topMargin = 0;
        indicatorParams.bottomMargin = footerGap;
        indicator.setLayoutParams(indicatorParams);

        View cancel = content.findViewById(cancelId);
        View primary = content.findViewById(primaryId);
        if (cancel == null || primary == null
                || !(cancel.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)
                || !(primary.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) return;

        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams cancelParams =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) cancel.getLayoutParams();
        cancelParams.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
        cancelParams.topToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
        cancelParams.bottomToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
        cancelParams.bottomToBottom = parentId;
        cancelParams.topMargin = 0;
        cancelParams.bottomMargin = bottomGap;
        cancel.setLayoutParams(cancelParams);

        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams primaryParams =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) primary.getLayoutParams();
        primaryParams.topToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
        primaryParams.bottomToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
        primaryParams.topToTop = cancelId;
        primaryParams.bottomToBottom = cancelId;
        primaryParams.topMargin = 0;
        primaryParams.bottomMargin = 0;
        primary.setLayoutParams(primaryParams);

        View scroll = ((ViewGroup) content).getChildAt(0);
        if (scroll instanceof androidx.core.widget.NestedScrollView
                && scroll.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams scrollParams =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) scroll.getLayoutParams();
            scrollParams.bottomToTop = indicator.getId();
            scroll.setLayoutParams(scrollParams);
        }
    }

    private void hidePlanningTasksTimeline(View content) {
        for (int id : new int[]{R.id.ops_5717_8506, R.id.ops_5717_8509, R.id.ops_5717_8507,
                R.id.ops_5717_8510, R.id.ops_shape_1, R.id.ops_shape_2, R.id.ops_5717_8503,
                R.id.ops_5717_8504, R.id.ops_tasks_step_3_circle, R.id.ops_tasks_step_3_number,
                R.id.ops_tasks_step_connector_2, R.id.ops_tasks_step_3_label}) {
            View view = content.findViewById(id);
            if (view != null) view.setVisibility(View.GONE);
        }
    }

    private void hidePlanningAssignmentTimeline(View content) {
        for (int id : new int[]{R.id.ops_assign_step_1, R.id.ops_assign_step_2,
                R.id.ops_assign_step_number_1, R.id.ops_assign_step_number_2,
                R.id.ops_assign_step_line, R.id.ops_assign_step_label_1,
                R.id.ops_assign_step_line_1_progress, R.id.ops_assign_step_label_2,
                R.id.ops_assign_step_3, R.id.ops_assign_step_number_3,
                R.id.ops_assign_step_line_2, R.id.ops_assign_step_line_2_progress,
                R.id.ops_assign_step_label_3}) {
            View view = content.findViewById(id);
            if (view != null) view.setVisibility(View.GONE);
        }
    }

    private void movePlanningActionsBelow(View content, int anchorId, int... actionIds) {
        for (int actionId : actionIds) {
            View action = content.findViewById(actionId);
            if (action == null || !(action.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) {
                continue;
            }
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) action.getLayoutParams();
            params.topToBottom = anchorId;
            params.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
            action.setLayoutParams(params);
        }
    }

    private void movePlanningActionsBelowIndicator(View content, int... actionIds) {
        movePlanningActionsBelow(content, R.id.ops_create_planning_step_indicator, actionIds);
    }

    private void hidePlanningCreateIndicator(View content) {
        View indicator = content.findViewById(R.id.ops_create_planning_step_indicator);
        if (indicator != null) indicator.setVisibility(View.GONE);
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
        EmptyStateStyler.apply(empty);
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
                boolean editingOrder = !isBlank(activity.model.field("execution", "editOrderId"));
                if (!editingOrder && selectedTechnicianId(activity) == null && !currentTechnicians.isEmpty()) {
                    // A recomendação da API vem ordenada; o primeiro técnico é o recomendado.
                    activity.model.field("plan_information", "tecnicoId",
                            String.valueOf(currentTechnicians.get(0).id));
                }
                adapter.submit(currentTechnicians, selectedTechnicianId(activity));
                list.setVisibility(View.VISIBLE);
                if (currentTechnicians.isEmpty()) {
                    empty.setText("Nenhum técnico disponível para esta competência.");
                    empty.setVisibility(View.VISIBLE);
                    list.setVisibility(View.GONE);
                } else {
                    empty.setOnClickListener(null);
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
            }
        });
    }

    private void bindTeamTechnicians(View content) {
        RecyclerView list = content.findViewById(R.id.ops_manager_team_list);
        TextView empty = content.findViewById(R.id.ops_manager_team_empty);
        if (list == null || empty == null) {
            return;
        }
        EmptyStateStyler.apply(empty);
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
                empty.setOnClickListener(null);
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
        ResponsabilidadesTecnicoAdapter responsibilityAdapter = new ResponsabilidadesTecnicoAdapter(item ->
                activity.openOrderDetails(item.ordemServicoId, item.titulo));
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
            EmptyStateStyler.showBelowBack(content, "Técnico não encontrado.", null);
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
                EmptyStateStyler.removeFromRoot(content);
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
                EmptyStateStyler.showBelowBack(content,
                        "Não foi possível carregar os dados do técnico. Toque para tentar novamente.",
                        () -> bindMemberDetails(content));
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
        String normalized = java.text.Normalizer.normalize(raw.trim(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replaceAll("[^a-zA-Z0-9]", "")
                .toLowerCase(java.util.Locale.ROOT);
        for (CategoriaProblema category : CategoriaProblema.values()) {
            String enumName = java.text.Normalizer.normalize(category.name(), java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{M}+", "").replaceAll("[^a-zA-Z0-9]", "")
                    .toLowerCase(java.util.Locale.ROOT);
            String displayName = java.text.Normalizer.normalize(category.getNome(), java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{M}+", "").replaceAll("[^a-zA-Z0-9]", "")
                    .toLowerCase(java.util.Locale.ROOT);
            if (enumName.equals(normalized) || displayName.equals(normalized)) {
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
        android.widget.AutoCompleteTextView competenceState = root.findViewById(com.aula.fixa.R.id.ops_5717_6260_input);
        android.widget.AutoCompleteTextView priorityState = root.findViewById(com.aula.fixa.R.id.ops_5717_6263_input);
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
            competenceState.setText(savedCompetence, false);
        }
        if (savedPriority != null) {
            priorityState.setText(savedPriority, false);
        }
        configurePlanningDropdown(competenceState, competenceField, competenceArrow, activity,
                "Competência", com.aula.fixa.domain.enums.CategoriaProblema.values());
        configurePlanningDropdown(priorityState, priorityField, priorityArrow, activity,
                "Prioridade", com.aula.fixa.domain.enums.Prioridade.values());
        recommendProblemCategory(root, activity, competenceState);
    }

    private void configurePlanningDropdown(
            android.widget.AutoCompleteTextView field, View container, View arrow,
            OperationsActivity activity, String label, Enum<?>[] options) {
        java.util.List<String> names = new java.util.ArrayList<>();
        for (Enum<?> option : options) {
            if (option instanceof CategoriaProblema) names.add(((CategoriaProblema) option).getNome());
            else names.add(((com.aula.fixa.domain.enums.Prioridade) option).getNome());
        }
        field.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), R.layout.item_app_dropdown, names));
        field.setDropDownBackgroundDrawable(androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.bg_app_popup_menu));
        field.setDropDownVerticalOffset(dp(3));
        field.setThreshold(0);
        field.setKeyListener(null);
        field.setFocusable(false);
        field.setShowSoftInputOnFocus(false);
        field.setOnItemClickListener((parent, view, position, id) -> {
            Enum<?> option = options[position];
            String name;
            int valueId;
            if (option instanceof CategoriaProblema) {
                CategoriaProblema category = (CategoriaProblema) option;
                name = category.getNome();
                valueId = category.getId();
            } else {
                com.aula.fixa.domain.enums.Prioridade priority = (com.aula.fixa.domain.enums.Prioridade) option;
                name = priority.getNome();
                valueId = priority.getId();
            }
            field.setText(name, false);
            activity.model.field("plan_information", label, name);
            activity.model.field("plan_information", label + "Id", String.valueOf(valueId));
        });
        View.OnClickListener open = v -> field.showDropDown();
        field.setOnClickListener(open);
        container.setOnClickListener(open);
        if (arrow != null) arrow.setOnClickListener(open);
    }

    private void recommendProblemCategory(View root, OperationsActivity activity,
                                          android.widget.AutoCompleteTextView competenceField) {
        if ("true".equals(activity.model.field("planning_tasks", "jev_requested"))) return;
        String title = activity.model.field("planning_tasks", "source_title");
        String description = activity.model.field("planning_tasks", "source_description");
        String equipmentCategory = activity.model.field("planning_tasks", "source_equipment_category");
        if (isBlank(title) || isBlank(description) || isBlank(equipmentCategory)) return;

        activity.model.field("planning_tasks", "jev_requested", "true");
        RecomendacaoCategoriaViewModel viewModel = new ViewModelProvider(this,
                new RecomendacaoCategoriaViewModelFactory(requireContext()))
                .get(RecomendacaoCategoriaViewModel.class);
        viewModel.recomendar(title, description, equipmentCategory,
                new RecomendacaoCategoriaViewModel.Callback() {
                    @Override public void success(String classification) {
                        if (!isAdded() || !root.isAttachedToWindow()
                                || !isBlank(activity.model.field("plan_information", "Competência"))) return;
                        CategoriaProblema category = matchProblemCategory(classification);
                        if (category == null) return;
                        competenceField.setText(category.getNome(), false);
                        activity.model.field("plan_information", "Competência", category.getNome());
                        activity.model.field("plan_information", "CompetênciaId", String.valueOf(category.getId()));
                    }
                    @Override public void failure(Throwable error) {
                        // Recomendação é assistiva; o gestor ainda pode escolher manualmente.
                    }
                });
    }

    private CategoriaProblema matchProblemCategory(String value) {
        if (isBlank(value)) return null;
        String normalized = normalizeCategory(value);
        for (CategoriaProblema category : CategoriaProblema.values()) {
            String enumName = normalizeCategory(category.name());
            String displayName = normalizeCategory(category.getNome());
            if (enumName.equals(normalized) || displayName.equals(normalized)
                    || normalized.endsWith(enumName) || normalized.endsWith(displayName)) return category;
        }
        return null;
    }

    private String normalizeCategory(String value) {
        return java.text.Normalizer.normalize(value == null ? "" : value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void bindPlanningSuccess(View content, OperationsActivity activity) {
        View board = content.findViewById(R.id.ops_5717_8475);
        View orderDetails = content.findViewById(R.id.ops_planning_success_order);
        View backToPlanning = content.findViewById(R.id.ops_5717_8477);
        if (board != null) board.setOnClickListener(v -> activity.openName("board"));
        if (orderDetails != null) orderDetails.setOnClickListener(v -> {
            String rawId = activity.model.field("planning_tasks", "ordemServicoId");
            try {
                long orderId = rawId == null ? 0 : Long.parseLong(rawId);
                if (orderId > 0) activity.openOrderDetails(orderId,
                        activity.model.field("planning_tasks", "source_title"));
            } catch (NumberFormatException ignored) { }
        });
        if (backToPlanning != null) backToPlanning.setOnClickListener(v -> activity.openName("planning"));
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
            java.util.List<String> monthOptions = new java.util.ArrayList<>();
            java.text.DateFormatSymbols symbols = new java.text.DateFormatSymbols(new java.util.Locale("pt", "BR"));
            for (int offset = 0; offset < 24; offset++) {
                java.util.Calendar option = (java.util.Calendar) current.clone();
                option.add(java.util.Calendar.MONTH, offset);
                String monthName = symbols.getMonths()[option.get(java.util.Calendar.MONTH)];
                monthName = monthName.substring(0, 1).toUpperCase(new java.util.Locale("pt", "BR")) + monthName.substring(1);
                monthOptions.add(monthName + " - " + option.get(java.util.Calendar.YEAR));
            }
            android.widget.ListPopupWindow popup = new android.widget.ListPopupWindow(requireContext());
            popup.setAnchorView(monthSelector);
            popup.setWidth(monthSelector.getWidth());
            popup.setHeight(android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            popup.setBackgroundDrawable(androidx.core.content.ContextCompat.getDrawable(requireContext(), R.drawable.bg_app_popup_menu));
            popup.setVerticalOffset(dp(3));
            popup.setAdapter(new android.widget.ArrayAdapter<>(requireContext(), R.layout.item_app_dropdown, monthOptions));
            popup.setOnItemClickListener((parent, itemView, position, id) -> {
                java.util.Calendar option = (java.util.Calendar) current.clone();
                option.add(java.util.Calendar.MONTH, position);
                String value = String.format(java.util.Locale.ROOT, "%04d-%02d",
                        option.get(java.util.Calendar.YEAR), option.get(java.util.Calendar.MONTH) + 1);
                activity.model.field("plan_information", "mes", value);
                activity.model.field("plan_information", "prazo", "");
                setMonthText(monthLabel, option);
                renderPlanningCalendar(calendar, option, today, activity);
                popup.dismiss();
            });
            popup.show();
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
        params.width = height;
        params.height = height;
        params.setGravity(android.view.Gravity.CENTER);
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
                showHomeLoadError(host, content);
            }
        });
    }

    private void loadTechnicianHome(android.widget.FrameLayout host, View content) {
        final com.aula.fixa.domain.model.Perfil[] profile = new com.aula.fixa.domain.model.Perfil[1];
        final java.util.List<com.aula.fixa.domain.model.os.OrdemServicoQuadro>[] orders = new java.util.List[]{null};
        final boolean[] failed = {false};
        final int[] pending = {2};
        Runnable complete = () -> {
            if (!isAdded() || getView() != host || host.indexOfChild(content) < 0) return;
            if (pending[0] != 0) return;
            try {
                if (failed[0]) {
                    showHomeLoadError(host, content);
                } else {
                    OperationsData.bindTechnicianHome((OperationsActivity) requireActivity(), content, profile[0], orders[0]);
                    content.setVisibility(View.VISIBLE);
                }
            } finally {
                AppLoading.hide(homeLoading);
                homeLoading = null;
            }
        };
        new ViewModelProvider(this, new com.aula.fixa.adapter.perfil.PerfilViewModelFactory(requireContext()))
                .get(com.aula.fixa.adapter.perfil.PerfilViewModel.class).obter(new com.aula.fixa.adapter.perfil.PerfilViewModel.Callback<com.aula.fixa.domain.model.Perfil>() {
                    @Override public void aoConcluir(com.aula.fixa.domain.model.Perfil result) { profile[0] = result; pending[0]--; complete.run(); }
                    @Override public void aoFalhar(Throwable error) { failed[0] = true; pending[0]--; complete.run(); }
                });
        new ViewModelProvider(this, new com.aula.fixa.adapter.os.QuadroOSViewModelFactory(requireContext(), true))
                .get(com.aula.fixa.adapter.os.QuadroOSViewModel.class).carregar(new com.aula.fixa.adapter.os.QuadroOSViewModel.Callback() {
                    @Override public void success(java.util.List<com.aula.fixa.domain.model.os.OrdemServicoQuadro> result) { orders[0] = result; pending[0]--; complete.run(); }
                    @Override public void failure(Throwable error) { failed[0] = true; pending[0]--; complete.run(); }
                });
    }

    private void showHomeLoadError(android.widget.FrameLayout host, View content) {
        if (!isAdded() || host == null || content == null || content.getParent() != host) return;
        content.setVisibility(View.GONE);
    }

    @Override
    public void onManagerRequestFilters(String period, String location, String category, String author) {
        if (solicitacaoGestorFluxo != null) {
            solicitacaoGestorFluxo.onFiltersChanged(period, location, category, author);
        }
    }

    public void selectCompletionPhotos() {
        completionPhotoPicker.open(uris -> {
            if (conclusaoOSFluxo != null && uris != null) conclusaoOSFluxo.uploadPhotos(uris);
        });
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
        if (chatbotFlow != null) chatbotFlow.onResume();
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
        if (("manager_board".equals(screen) || "technician_board".equals(screen)) && !initialResume && quadroOSFluxo != null) {
            quadroOSFluxo.refresh();
        }
        if (("manager_occurrences".equals(screen) || "technician_occurrences".equals(screen)) && !initialResume && ocorrenciaGestorFluxo != null) {
            ocorrenciaGestorFluxo.refresh();
        }
        String currentScreen = requireArguments().getString("screen", "manager_home");
        if ((!"manager_home".equals(currentScreen) && !"technician_home".equals(currentScreen)) || getView() == null) {
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
            if ("technician_home".equals(currentScreen)) loadTechnicianHome(host, content);
            else loadManagerHome(host, content);
        }
    }

    @Override
    public void onDestroyView() {
        if (chatbotFlow != null) {
            chatbotFlow.close();
            chatbotFlow = null;
        }
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
