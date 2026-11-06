package com.aula.fixa.ui.view.checklist;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;

import com.aula.fixa.R;
import com.aula.fixa.adapter.auth.AutenticacaoViewModel;
import com.aula.fixa.adapter.checklist.ChecklistViewModel;
import com.aula.fixa.adapter.checklist.ChecklistViewModelFactory;
import com.aula.fixa.ui.adapter.checklist.InspectionEquipmentAdapter;
import com.aula.fixa.ui.adapter.checklist.ChecklistCardsAdapter;
import com.aula.fixa.ui.adapter.checklist.ChecklistFieldsAdapter;
import com.aula.fixa.ui.adapter.checklist.ChecklistModelEquipmentAdapter;
import com.aula.fixa.domain.enums.TipoCampoChecklist;
import com.aula.fixa.domain.model.checklist.*;
import com.aula.fixa.ui.config.AppLoading;
import com.aula.fixa.ui.config.AppDialogs;
import com.aula.fixa.ui.config.EmptyStateStyler;
import com.aula.fixa.ui.view.operations.OperationsActivity;
import com.google.gson.Gson;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Binds the existing checklist and inspection layouts to the API.
 */
public final class ChecklistFlow {
    private static final String DRAFT = "templateDraft";
    private final Fragment fragment;
    private final OperationsActivity activity;
    private final ChecklistViewModel vm;
    private final Gson gson = new Gson();
    private List<ChecklistTemplate> templates = new ArrayList<>();
    private ChecklistCardsAdapter checklistCardsAdapter;
    private final android.os.Handler inspectionFilterHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable pendingInspectionFilter;
    private List<EquipmentModelOption> availableModels = new ArrayList<>();
    private final Map<String, Object> answers = new LinkedHashMap<>();
    private LinearLayout answerContainer;
    private ChecklistTemplate activeTemplate;
    private ChecklistEquipment activeEquipment;
    private boolean templatesLoadFailed;
    private String checklistSearchQuery = "";

    public ChecklistFlow(Fragment fragment, OperationsActivity activity) {
        this.fragment = fragment;
        this.activity = activity;
        vm = new ViewModelProvider(fragment, new ChecklistViewModelFactory(activity)).get(ChecklistViewModel.class);
    }

    public void bind(View content, String screen) {
        if (screen.startsWith("manager_checklist_")) bindTimeline(content, screen);
        switch (screen) {
            case "manager_checklists":
            case "technician_checklists":
                bindChecklistList(content, screen);
                break;
            case "manager_checklist_intro":
                bindIntro(content);
                break;
            case "manager_checklist_information":
                bindInformation(content);
                break;
            case "manager_checklist_fields":
                bindFields(content);
                break;
            case "manager_checklist_models":
                bindModels(content);
                break;
            case "manager_checklist_success":
                bindSuccess(content);
                break;
            case "manager_inspections":
            case "technician_inspections":
                bindInspections(content);
                break;
            case "manager_inspection_form":
            case "technician_inspection_form":
                bindInspectionForm(content);
                break;
        }
    }

    private void bindTimeline(View content, String screen) {
        if (screen.endsWith("intro")) {
            int[][] circlesAndNumbers = {{R.id.ops_5717_8025, R.id.ops_5717_8030},
                    {R.id.ops_5717_8026, R.id.ops_5717_8031}, {R.id.ops_5717_8027, R.id.ops_5717_8032}};
            for (int[] pair : circlesAndNumbers) centerNumberInCircle(content, pair[0], pair[1]);
            hideViews(content, R.id.ops_5717_8034, R.id.ops_5717_8036);
            return;
        }

        int[][] legacyCircles;
        int[] legacyLabels;
        int[] legacyLines;
        int backId;
        int nextId;
        int step;
        if (screen.endsWith("information")) {
            legacyCircles = new int[][]{{R.id.ops_5717_8089, R.id.ops_5717_8090},
                    {R.id.ops_5717_8095, R.id.ops_5717_8096}, {R.id.ops_5717_8092, R.id.ops_5717_8093}};
            legacyLabels = new int[]{R.id.ops_5717_8085, R.id.ops_5717_8086, R.id.ops_5717_8087};
            legacyLines = new int[]{R.id.ops_shape_0, R.id.ops_shape_1, R.id.ops_shape_2};
            backId = R.id.ops_5717_8104;
            nextId = R.id.ops_5717_8106;
            step = 1;
        } else if (screen.endsWith("fields")) {
            legacyCircles = new int[][]{{R.id.ops_5717_8050, R.id.ops_5717_8051},
                    {R.id.ops_5717_8056, R.id.ops_5717_8057}, {R.id.ops_5717_8053, R.id.ops_5717_8054}};
            legacyLabels = new int[]{R.id.ops_5717_8046, R.id.ops_5717_8047, R.id.ops_5717_8048};
            legacyLines = new int[]{R.id.ops_shape_1, R.id.ops_shape_2, R.id.ops_shape_3, R.id.ops_shape_13};
            backId = R.id.ops_5717_8062;
            nextId = R.id.ops_5717_8064;
            step = 2;
        } else if (screen.endsWith("models")) {
            legacyCircles = new int[][]{{R.id.ops_5717_8131, R.id.ops_5717_8132},
                    {R.id.ops_5717_8137, R.id.ops_5717_8138}, {R.id.ops_5717_8134, R.id.ops_5717_8135}};
            legacyLabels = new int[]{R.id.ops_5717_8127, R.id.ops_5717_8128, R.id.ops_5717_8129};
            legacyLines = new int[]{R.id.ops_shape_1, R.id.ops_shape_2, R.id.ops_shape_3};
            backId = R.id.ops_5717_8142;
            nextId = R.id.ops_5717_8144;
            step = 3;
        } else return;

        for (int[] pair : legacyCircles) hideViews(content, pair);
        hideViews(content, legacyLabels);
        hideViews(content, legacyLines);
        hideViews(content, backId, nextId);
        if (screen.endsWith("models")) {
            View indicator = content.findViewById(R.id.stepIndicator);
            if (indicator != null) indicator.setVisibility(View.GONE);
            return;
        }
        String editId = activity.model.field("checklist", "editId");
        if (editId != null && !editId.isEmpty()) {
            View indicator = content.findViewById(R.id.stepIndicator);
            if (indicator != null) indicator.setVisibility(View.GONE);
            return;
        }
        bindChecklistIndicator(content, step);
        // The step timeline shares a fixed footer with the actions on both phones and tablets.
        // This keeps it out of the scrollable calendar/form content.
        pinChecklistIndicator(content);
    }

    private void centerNumberInCircle(View content, int circleId, int numberId) {
        View circle = content.findViewById(circleId);
        View number = content.findViewById(numberId);
        if (circle == null || number == null
                || !(number.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) return;
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) number.getLayoutParams();
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.startToStart = circle.getId();
        params.endToEnd = circle.getId();
        params.topToTop = circle.getId();
        params.bottomToBottom = circle.getId();
        params.leftMargin = params.rightMargin = params.topMargin = params.bottomMargin = 0;
        number.setLayoutParams(params);
        if (number instanceof TextView) ((TextView) number).setGravity(Gravity.CENTER);
    }

    private void bindChecklistIndicator(View content, int currentStep) {
        View indicator = content.findViewById(R.id.stepIndicator);
        if (indicator == null) return;
        int[] circles = {R.id.tvStep1, R.id.tvStep2, R.id.tvStep3};
        int[] labels = {R.id.tvStep1Label, R.id.tvStep2Label, R.id.tvStep3Label};
        String[] names = {"Informações", "Campos", "Modelos"};
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
                label.setGravity(Gravity.CENTER);
                if (label.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams labelParams =
                            (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) label.getLayoutParams();
                    labelParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    label.setLayoutParams(labelParams);
                }
                int color = state == 0 ? R.color.app_status_done
                        : state == 1 ? R.color.app_primary : R.color.app_text_muted;
                label.setTextColor(androidx.core.content.ContextCompat.getColor(activity, color));
            }
        }
        int[] lineIds = {R.id.viewLine1, R.id.viewLine2};
        for (int i = 0; i < lineIds.length; i++) {
            View line = indicator.findViewById(lineIds[i]);
            if (line == null) continue;
            if (currentStep > i + 1) line.setBackgroundResource(R.drawable.bg_app_indicator_line_done);
            else if (currentStep == i + 1) {
                line.setBackgroundResource(R.drawable.bg_app_indicator_line_progress);
                if (line.getBackground() != null) line.getBackground().setLevel(5000);
            } else line.setBackgroundResource(R.drawable.bg_app_indicator_line_pending);
        }
    }

    private void pinChecklistIndicator(View content) {
        View indicator = content.findViewById(R.id.stepIndicator);
        View scroll = content.findViewById(R.id.checklistScroll);
        View actions = content.findViewById(R.id.stepActions);
        if (indicator == null || scroll == null || actions == null
                || !(content instanceof androidx.constraintlayout.widget.ConstraintLayout)
                || !(indicator.getParent() instanceof ViewGroup)) return;
        ViewGroup oldParent = (ViewGroup) indicator.getParent();
        for (int index = 0; index < oldParent.getChildCount(); index++) {
            View child = oldParent.getChildAt(index);
            if (child == indicator || !(child.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) continue;
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams childParams =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) child.getLayoutParams();
            if (childParams.bottomToTop == indicator.getId()) {
                childParams.bottomToTop = -1;
                childParams.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                child.setLayoutParams(childParams);
            }
        }
        oldParent.removeView(indicator);
        androidx.constraintlayout.widget.ConstraintLayout page =
                (androidx.constraintlayout.widget.ConstraintLayout) content;
        page.addView(indicator, new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) indicator.getLayoutParams();
        params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        params.topToTop = -1;
        params.topToBottom = -1;
        params.topMargin = 0;
        params.bottomToTop = actions.getId();
        params.bottomToBottom = -1;
        params.bottomMargin = activity.getResources().getDimensionPixelSize(R.dimen.app_step_section_margin_top);
        indicator.setLayoutParams(params);
        if (scroll.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams scrollParams =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) scroll.getLayoutParams();
            scrollParams.bottomToTop = indicator.getId();
            scroll.setLayoutParams(scrollParams);
        }
    }

    private void hideViews(View root, int... ids) {
        for (int id : ids) {
            View view = root.findViewById(id);
            if (view != null) view.setVisibility(View.GONE);
        }
    }

    private void bindChecklistList(View content, String screen) {
        boolean canManage = activity.role().equals("manager");
        boolean technician = screen.startsWith("technician");
        int circleFabId = technician ? R.id.ops_5717_10222 : R.id.ops_5717_7935;
        int plusFabId = technician ? R.id.ops_5717_10224 : R.id.ops_5717_7937;
        View circleFab = content.findViewById(circleFabId);
        View plusFab = content.findViewById(plusFabId);
        View back = content.findViewWithTag("image:arrow-left");
        if (back != null) back.setOnClickListener(v -> activity.back());
        if (circleFab != null) circleFab.setVisibility(View.GONE);
        if (plusFab != null) plusFab.setVisibility(View.GONE);
        installChecklistFab(content, canManage);
        for (View card : new View[]{content.findViewById(R.id.ops_5717_7973), content.findViewById(R.id.ops_5717_7988), content.findViewById(R.id.ops_5717_10260), content.findViewById(R.id.ops_5717_10275)}) {
            if (card != null) {
                card.setVisibility(View.GONE);
            }
        }
        // Retain the existing card geometry/background while filling server data.
        RecyclerView list = new RecyclerView(activity);
        list.setLayoutManager(new LinearLayoutManager(activity));
        list.setNestedScrollingEnabled(false);
        list.setClipChildren(false);
        list.setClipToPadding(false);
        list.setTag("checklist:cards");
        int searchContainerId = screen.startsWith("technician")
                ? R.id.technician_checklist_search_container : R.id.checklist_search_container;
        int titleMargin = activity.getResources().getDimensionPixelSize(R.dimen.ops_manager_home_card_side_margin);
        attachBelow(content, list, searchContainerId, 18);
        if (list.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) list.getLayoutParams();
            params.setMarginStart(titleMargin);
            params.setMarginEnd(titleMargin);
            list.setLayoutParams(params);
        }
        list.setPadding(0, 0, 0, activity.getResources().getDimensionPixelSize(R.dimen.ops_checklist_list_bottom_padding));
        content.setTag(R.id.ops_5717_7930, list);
        checklistCardsAdapter = new ChecklistCardsAdapter(activity.role().equals("manager"),
                new ChecklistCardsAdapter.Presenter() {
                    @Override public String categories(ChecklistTemplate template) { return categoryNames(template); }
                    @Override public String status(ChecklistTemplate template) { return ChecklistFlow.this.status(template); }
                }, new ChecklistCardsAdapter.Callbacks() {
                    @Override public void onSelect(ChecklistTemplate template) { selectChecklist(template); }
                    @Override public void onMenu(ChecklistTemplate template, View anchor) { showChecklistMenu(anchor, template); }
                    @Override public void onRetry() { loadTemplates(content, screen); }
                });
        list.setAdapter(checklistCardsAdapter);
        EditText search = content.findViewById(technician
                ? R.id.technician_checklist_search_input : R.id.checklist_search_input);
        View searchButton = content.findViewById(technician
                ? R.id.technician_checklist_search_button : R.id.checklist_search_button);
        if (search != null) search.addTextChangedListener(watcher(value -> {
            checklistSearchQuery = value == null ? "" : value;
            if (checklistCardsAdapter != null) checklistCardsAdapter.filter(checklistSearchQuery);
        }));
        if (searchButton != null && search != null) searchButton.setOnClickListener(v -> {
            search.requestFocus();
            android.view.inputmethod.InputMethodManager keyboard =
                    (android.view.inputmethod.InputMethodManager) activity.getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (keyboard != null) keyboard.showSoftInput(search, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        });
        loadTemplates(content, screen);
    }

    private void installChecklistFab(View content, boolean visible) {
        if (!(content.getParent() instanceof android.widget.FrameLayout)) return;
        android.widget.FrameLayout host = (android.widget.FrameLayout) content.getParent();
        com.google.android.material.floatingactionbutton.FloatingActionButton fab =
                new com.google.android.material.floatingactionbutton.FloatingActionButton(activity);
        fab.setTag("checklist:create-fab");
        int fabSize = activity.getResources().getDimensionPixelSize(R.dimen.app_fab_size);
        fab.setCustomSize(fabSize);
        fab.setMaxImageSize(dp(activity, 24));
        fab.setShapeAppearanceModel(com.google.android.material.shape.ShapeAppearanceModel.builder()
                .setAllCornerSizes(fabSize / 2f)
                .build());
        fab.setImageResource(R.drawable.figma_c5cb2);
        fab.setImageTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE));
        fab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                androidx.core.content.ContextCompat.getColor(activity, R.color.ops_color_ff184384)));
        fab.setCompatElevation(dp(activity, 6));
        fab.setContentDescription("Criar checklist");
        fab.setOnClickListener(v -> startNew());
        android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.Gravity.BOTTOM | android.view.Gravity.END);
        params.setMargins(0, 0, dp(activity, 24), dp(activity, 24));
        host.addView(fab, params);
        fab.setVisibility(visible ? View.VISIBLE : View.GONE);
        fab.bringToFront();
    }

    private void loadTemplates(View content, String screen) {
        Dialog loading = AppLoading.show(activity);
        vm.listar(new ChecklistViewModel.Callback<List<ChecklistTemplate>>() {
            public void success(List<ChecklistTemplate> data) {
                AppLoading.hide(loading);
                if (!fragment.isAdded()) {
                    return;
                }
                templates = data == null ? new ArrayList<>() : data;
                templatesLoadFailed = false;
                renderTemplates(content, screen);
            }

            public void failure(Throwable e) {
                AppLoading.hide(loading);
                if (fragment.isAdded()) {
                    templates.clear();
                    templatesLoadFailed = true;
                    renderTemplates(content, screen);
                }
            }
        });
    }

    private void renderTemplates(View content, String screen) {
        if (checklistCardsAdapter != null) checklistCardsAdapter.submitList(templates, templatesLoadFailed);
    }

    private void showChecklistMenu(View anchor, ChecklistTemplate template) {
        View menu = activity.getLayoutInflater().inflate(R.layout.popup_app_request_menu, null, false);
        android.widget.PopupWindow popup = new android.widget.PopupWindow(
                menu,
                activity.getResources().getDimensionPixelSize(R.dimen.app_popup_menu_width),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        popup.setBackgroundDrawable(androidx.core.content.ContextCompat.getDrawable(activity, R.drawable.bg_app_popup_menu));
        popup.setElevation(activity.getResources().getDimension(R.dimen.app_popup_menu_elevation));
        popup.setOutsideTouchable(true);
        menu.findViewById(R.id.tvPopupEdit).setOnClickListener(v -> {
            popup.dismiss();
            edit(template);
        });
        menu.findViewById(R.id.tvPopupDelete).setOnClickListener(v -> {
            popup.dismiss();
            confirmDelete(template);
        });
        popup.showAsDropDown(anchor, -popup.getWidth() + anchor.getWidth(), 0);
    }

    private String categoryNames(ChecklistTemplate t) {
        if (t.modelos == null || t.modelos.isEmpty()) {
            return "Categoria não informada";
        }
        Set<String> names = new LinkedHashSet<>();
        for (ChecklistEquipmentModel m : t.modelos) {
            if (m.categoria != null && !m.categoria.trim().isEmpty()) names.add(m.categoria.trim());
        }
        return names.isEmpty() ? "Categoria não informada" : android.text.TextUtils.join(", ", names);
    }

    private String status(ChecklistTemplate t) {
        if (t.modelos == null || t.modelos.isEmpty()) {
            return "Em andamento";
        }
        boolean hasEquipment = false;
        for (ChecklistEquipmentModel m : t.modelos) {
            if (m.equipamentos != null) {
                for (ChecklistEquipment e : m.equipamentos) {
                    hasEquipment = true;
                    if (!isCompleted(e)) {
                        return "Em andamento";
                    }
                }
            }
        }
        return hasEquipment ? "Concluído" : "Em andamento";
    }

    private boolean isCompleted(ChecklistEquipment e) {
        if (!Boolean.TRUE.equals(e.status)) {
            return false;
        }
        if (e.prazoInspecao == null || e.prazoInspecao.isEmpty()) {
            return true;
        }
        try {
            return !LocalDate.parse(e.prazoInspecao.substring(0, 10)).isBefore(LocalDate.now());
        } catch (Exception ignored) {
            return true;
        }
    }

    private void selectChecklist(ChecklistTemplate t) {
        activity.model.field("checklist", "selectedTemplate", gson.toJson(t));
        activity.model.field("checklist", "checklistId", t.id);
        activity.openName("inspections");
    }

    private void startNew() {
        activity.model.field("checklist", "editId", "");
        activity.model.field("checklist", "clearModelSelection", "true");
        ChecklistTemplate template = new ChecklistTemplate();
        template.primeiraAbertura = LocalDate.now().toString();
        activity.model.field("checklist", "templateDraft", gson.toJson(template));
        activity.open("manager_checklist_intro");
    }

    private void edit(ChecklistTemplate t) {
        activity.model.field("checklist", "editId", t.id);
        activity.model.field("checklist", "clearModelSelection", "true");
        activity.model.field("checklist", "templateDraft", gson.toJson(t));
        activity.open("manager_checklist_information");
    }

    private void confirmDelete(ChecklistTemplate t) {
        activity.model.field("checklist", "deleteId", t.id);
        com.aula.fixa.ui.view.operations.OperationsDialogFragment.create("manager_delete_checklist").show(activity.getSupportFragmentManager(), "ops_dialog");
    }

    private void bindIntro(View content) {
        TextView back = content.findViewById(R.id.btnStepBack);
        TextView next = content.findViewById(R.id.btnStepNext);
        if (back != null) {
            back.setText("Cancelar");
            back.setOnClickListener(v -> activity.back());
        }
        if (next != null) {
            next.setText("Continuar");
            next.setOnClickListener(v -> activity.open("manager_checklist_information"));
        }
        clickTag(content, "image:arrow-left", v -> activity.back());
    }

    private void bindSuccess(View content) {
        TextView message = content.findViewWithTag("text:Ocorrência registrada com sucesso!");
        if (message != null) {
            message.setText("Checklist cadastrado com sucesso!");
        }
        TextView another = content.findViewWithTag("text:Registrar novo problema");
        if (another != null) {
            another.setText("Criar outro checklist");
            another.setOnClickListener(v -> startNew());
        }
        TextView backToChecklists = content.findViewWithTag("text:Ir para a Home");
        if (backToChecklists != null) {
            backToChecklists.setText("Voltar para checklists");
            backToChecklists.setOnClickListener(v -> activity.open("manager_checklists"));
        }
    }

    private ChecklistTemplate draft() {
        String raw = activity.model.field("checklist", DRAFT);
        if (raw == null || raw.isEmpty()) {
            return new ChecklistTemplate();
        }
        try {
            return gson.fromJson(raw, ChecklistTemplate.class);
        } catch (Exception e) {
            return new ChecklistTemplate();
        }
    }

    private void saveDraft(ChecklistTemplate value) {
        activity.model.field("checklist", DRAFT, gson.toJson(value));
    }

    private void bindInformation(View content) {
        ChecklistTemplate t = draft();
        EditText name = content.findViewWithTag("field:Nome");
        EditText periodic = content.findViewWithTag("field:Periodicidade em dias");
        TextView date = content.findViewWithTag("field:Data de primeira abertura");
        styleChecklistInput(name);
        styleChecklistInput(periodic);
        increaseChecklistFieldHeight(name, 8);
        increaseChecklistFieldHeight(periodic, 8);
        increaseChecklistFieldHeight(content.findViewById(R.id.ops_5717_8112), 8);
        if (date != null) {
            date.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,
                    activity.getResources().getDimension(R.dimen.app_step_field_text_size));
            date.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(activity, R.font.poppins_regular));
            date.setTextColor(androidx.core.content.ContextCompat.getColor(activity, R.color.app_text_black));
            date.setHintTextColor(androidx.core.content.ContextCompat.getColor(activity, R.color.app_text_placeholder_dark));
        }
        if (name != null) {
            name.setText(t.nome);
        }
        if (periodic != null && t.periodicidade != null) {
            periodic.setText(String.valueOf(t.periodicidade));
        }
        if (date != null && t.primeiraAbertura != null) {
            date.setText(t.primeiraAbertura);
        } else if (date != null) {
            t.primeiraAbertura = LocalDate.now().toString();
            date.setText(t.primeiraAbertura);
            saveDraft(t);
        }
        if (date != null) {
            date.setFocusable(false);
            date.setCursorVisible(false);
            date.setOnClickListener(v -> chooseDate(date));
        }
        View dateContainer = content.findViewById(R.id.ops_5717_8112);
        View calendar = content.findViewById(R.id.ops_5717_8115);
        View.OnClickListener openCalendar = v -> { if (date != null) chooseDate(date); };
        if (dateContainer != null) dateContainer.setOnClickListener(openCalendar);
        if (calendar != null) calendar.setOnClickListener(openCalendar);
        // The generic operations binder treats calendar tags as request confirmation actions.
        // Override that binding specifically on this checklist date field.
        clickTag(content, "image:calendar", openCalendar);
        clickTag(content, "image:arrow-left", v -> activity.back());
        TextView back = content.findViewById(R.id.btnStepBack);
        TextView next = content.findViewById(R.id.btnStepNext);
        if (back != null) {
            back.setText(R.string.app_back);
            back.setOnClickListener(v -> activity.back());
        }
        if (next != null) {
            next.setText(R.string.app_next);
            next.setOnClickListener(v -> {
                String n = name == null ? "" : name.getText().toString().trim();
                String p = periodic == null ? "" : periodic.getText().toString().trim();
                String d = date == null ? "" : date.getText().toString().trim();
                if (n.isEmpty()) {
                    if (name != null) name.setError("Informe o nome.");
                    return;
                }
                try {
                    int days = Integer.parseInt(p);
                    if (days <= 0) throw new NumberFormatException();
                    t.periodicidade = days;
                } catch (NumberFormatException e) {
                    if (periodic != null) periodic.setError("Informe a periodicidade em dias.");
                    return;
                }
                if (d.isEmpty()) {
                    toast("Informe a data de primeira abertura.");
                    return;
                }
                t.nome = n;
                t.primeiraAbertura = d;
                saveDraft(t);
                activity.open("manager_checklist_fields");
            });
        }
    }

    private void styleChecklistInput(EditText input) {
        if (input == null) return;
        input.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,
                activity.getResources().getDimension(R.dimen.app_step_field_text_size));
        input.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(activity, R.font.poppins_regular));
        input.setTextColor(androidx.core.content.ContextCompat.getColor(activity, R.color.app_text_black));
        input.setHintTextColor(androidx.core.content.ContextCompat.getColor(activity, R.color.app_text_placeholder_dark));
    }

    private void increaseChecklistFieldHeight(View field, int extraDp) {
        if (field == null) return;
        ViewGroup.LayoutParams params = field.getLayoutParams();
        params.height += dp(activity, extraDp);
        field.setLayoutParams(params);
    }

    private void chooseDate(TextView target) {
        Calendar c = Calendar.getInstance();
        try {
            String current = target.getText().toString().trim();
            LocalDate d;
            try {
                d = LocalDate.parse(current);
            } catch (Exception notIso) {
                d = LocalDate.parse(current, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            c.set(d.getYear(), d.getMonthValue() - 1, d.getDayOfMonth());
        } catch (Exception ignored) {
        }
        DatePickerDialog picker = new DatePickerDialog(activity, (v, y, m, day) -> target.setText(LocalDate.of(y, m + 1, day).toString()), c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        picker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        picker.show();
    }

    private void bindFields(View content) {
        ChecklistTemplate template = draft();
        if (template.campos == null) template.campos = new ArrayList<>();
        RecyclerView list = content.findViewById(R.id.checklistFieldsRecycler);
        list.setLayoutManager(new LinearLayoutManager(activity));
        list.setNestedScrollingEnabled(false);
        list.setClipChildren(false);
        list.setClipToPadding(false);
        final ChecklistFieldsAdapter[] adapterRef = new ChecklistFieldsAdapter[1];
        ChecklistFieldsAdapter adapter = new ChecklistFieldsAdapter(template.campos,
                new ChecklistFieldsAdapter.Callbacks() {
                    @Override public void onAddField() { showFieldDialog(null, -1); }
                    @Override public void onFieldMenu(ChecklistField field, int position, View anchor) {
                        showFieldMenu(anchor, template, field, position, adapterRef[0]);
                    }
                    @Override public void onFieldsReordered() { saveDraft(template); }
                });
        list.setAdapter(adapter);
        adapterRef[0] = adapter;
        ItemTouchHelper touchHelper = new ItemTouchHelper(adapter.dragCallback());
        adapter.setTouchHelper(touchHelper);
        touchHelper.attachToRecyclerView(list);
        TextView back = content.findViewById(R.id.btnStepBack);
        TextView next = content.findViewById(R.id.btnStepNext);
        clickTag(content, "image:arrow-left", v -> activity.back());
        if (back != null) {
            back.setText(R.string.app_back);
            back.setOnClickListener(v -> activity.back());
        }
        if (next != null) {
            next.setText(R.string.app_next);
            next.setOnClickListener(v -> {
                if (template.campos == null || template.campos.isEmpty()) {
                    toast("Adicione ao menos um campo de inspeção.");
                    return;
                }
                saveDraft(template);
                activity.open("manager_checklist_models");
            });
        }
    }

    private void showFieldMenu(View anchor, ChecklistTemplate template, ChecklistField field,
                               int position, ChecklistFieldsAdapter adapter) {
        if (field == null || position < 0 || position >= template.campos.size()) return;
        View menu = activity.getLayoutInflater().inflate(R.layout.popup_app_request_menu, null, false);
        android.widget.PopupWindow popup = new android.widget.PopupWindow(menu,
                activity.getResources().getDimensionPixelSize(R.dimen.app_popup_menu_width),
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setBackgroundDrawable(androidx.core.content.ContextCompat.getDrawable(activity, R.drawable.bg_app_popup_menu));
        popup.setElevation(activity.getResources().getDimension(R.dimen.app_popup_menu_elevation));
        popup.setOutsideTouchable(true);
        menu.findViewById(R.id.tvPopupEdit).setOnClickListener(v -> {
            popup.dismiss();
            showFieldDialog(field, position);
        });
        menu.findViewById(R.id.tvPopupDelete).setOnClickListener(v -> {
            popup.dismiss();
            AppDialogs.showDeleteConfirm(activity, () -> {
                int current = template.campos.indexOf(field);
                if (current >= 0) {
                    adapter.removeAt(current);
                    saveDraft(template);
                }
            });
        });
        popup.showAsDropDown(anchor, -popup.getWidth() + anchor.getWidth(), 0);
    }

    public static void bindDialog(Fragment owner, View content, String key) {
        new ChecklistFieldDialogController(owner).bindDialog(owner, content, key);
    }

    private void bindModels(View content) {
        ChecklistTemplate template = draft();
        EditText search = content.findViewWithTag("field:Pesquisar");
        TextView back = content.findViewById(R.id.btnStepBack), next = content.findViewById(R.id.btnStepNext);
        clickTag(content, "image:arrow-left", v -> activity.back());
        if (back != null) {
            back.setText(R.string.app_back);
            back.setOnClickListener(v -> activity.back());
        }
        RecyclerView list = content.findViewById(R.id.checklistModelsRecycler);
        list.setLayoutManager(new LinearLayoutManager(activity));
        list.setNestedScrollingEnabled(false);
        final ChecklistModelEquipmentAdapter[] adapterRef = new ChecklistModelEquipmentAdapter[1];
        ChecklistModelEquipmentAdapter modelAdapter = new ChecklistModelEquipmentAdapter(
                new ChecklistModelEquipmentAdapter.Selection() {
                    @Override public boolean modelExpanded(EquipmentModelOption model) { return ChecklistFlow.this.modelExpanded(model); }
                    @Override public boolean equipmentSelected(EquipmentModelOption model, ChecklistEquipment equipment) { return ChecklistFlow.this.equipmentSelected(model, equipment); }
                    @Override public void onModelExpansionChanged(EquipmentModelOption model, boolean expanded) {
                        activity.model.selected("checklist:modelExpanded:" + model.id, expanded);
                        if (adapterRef[0] != null) adapterRef[0].submit(availableModels, search == null ? "" : search.getText().toString());
                    }
                    @Override public void onEquipmentSelected(EquipmentModelOption model, ChecklistEquipment equipment, boolean selected) {
                        if (selected) activity.model.selected("checklist:modelExpanded:" + model.id, true);
                        activity.model.selected("checklist:equipment:" + equipment.id, selected);
                        if (adapterRef[0] != null) adapterRef[0].submit(availableModels, search == null ? "" : search.getText().toString());
                    }
                });
        adapterRef[0] = modelAdapter;
        list.setAdapter(modelAdapter);
        Dialog loading = AppLoading.show(activity);
        vm.listar(new ChecklistViewModel.Callback<List<ChecklistTemplate>>() {
            public void success(List<ChecklistTemplate> values) {
                templates = values == null ? new ArrayList<>() : values;
                vm.modelosDisponiveis(templates, template.id, new ChecklistViewModel.Callback<List<EquipmentModelOption>>() {
                    public void success(List<EquipmentModelOption> models) {
                        AppLoading.hide(loading);
                        availableModels = models == null ? new ArrayList<>() : models;
                        if (Boolean.parseBoolean(activity.model.field("checklist", "clearModelSelection"))) {
                            for (EquipmentModelOption model : availableModels) {
                                activity.model.selected("checklist:modelExpanded:" + model.id, false);
                                for (ChecklistEquipment equipment : model.equipamentos) {
                                    activity.model.selected("checklist:equipment:" + equipment.id, false);
                                }
                            }
                            activity.model.field("checklist", "modelSelectionInitialized", "true");
                            activity.model.field("checklist", "clearModelSelection", "false");
                        }
                        modelAdapter.submit(availableModels, search == null ? "" : search.getText().toString());
                        if (search != null) {
                            search.addTextChangedListener(new android.text.TextWatcher() {
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                }

                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    modelAdapter.submit(availableModels, s.toString());
                                }

                                public void afterTextChanged(android.text.Editable e) {
                                }
                            });
                        }
                    }

                    public void failure(Throwable error) {
                        AppLoading.hide(loading);
                        modelAdapter.submit(null, "");
                    }
                });
            }

            public void failure(Throwable error) {
                AppLoading.hide(loading);
                modelAdapter.submit(null, "");
            }
        });
        if (next != null) {
            next.setText(R.string.app_save);
            next.setOnClickListener(v -> {
                List<ChecklistEquipmentModel> selected = new ArrayList<>();
                for (EquipmentModelOption model : availableModels) {
                    if (!model.disponivel) continue;
                    List<ChecklistEquipment> equipment = new ArrayList<>();
                    for (ChecklistEquipment item : model.equipamentos) {
                        if (item.disponivel && equipmentSelected(model, item)) {
                            equipment.add(item);
                        }
                    }
                    if (!equipment.isEmpty()) {
                        selected.add(new ChecklistEquipmentModel(model.id, model.nome, equipment));
                    }
                }
                if (selected.isEmpty()) {
                    toast("Selecione um modelo e ao menos um equipamento.");
                    return;
                }
                template.modelos = selected;
                saveDraft(template);
                finishTemplate(template);
            });
        }
    }

    private boolean modelExpanded(EquipmentModelOption value) {
        return activity.model.selected("checklist:modelExpanded:" + value.id);
    }

    private boolean equipmentSelected(EquipmentModelOption model, ChecklistEquipment value) {
        if (Boolean.parseBoolean(activity.model.field("checklist", "modelSelectionInitialized"))) {
            return activity.model.selected("checklist:equipment:" + value.id);
        }
        if (activity.model.selected("checklist:equipment:" + value.id)) return true;
        ChecklistTemplate current = draft();
        if (current.modelos != null) {
            for (ChecklistEquipmentModel linked : current.modelos) {
                if (linked.modeloEquipamentoId == model.id && linked.equipamentos != null) {
                    for (ChecklistEquipment equipment : linked.equipamentos) {
                        if (equipment.id == value.id) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private void finishTemplate(ChecklistTemplate template) {
        Dialog loading = AppLoading.show(activity);
        String id = activity.model.field("checklist", "editId");
        ChecklistViewModel.Callback<ChecklistTemplate> callback = new ChecklistViewModel.Callback<ChecklistTemplate>() {
            public void success(ChecklistTemplate result) {
                AppLoading.hide(loading);
                activity.model.field("checklist", DRAFT, gson.toJson(result));
                activity.open("manager_checklist_success");
            }

            public void failure(Throwable error) {
                AppLoading.hide(loading);
                System.out.println(error);
                toast(AutenticacaoViewModel.messageFrom(error));
            }
        };
        if (id == null || id.isEmpty()) {
            vm.cadastrar(template, callback);
        } else {
            template.id = id;
            vm.atualizar(template, callback);
        }
    }

    private void bindInspections(View content) {
        String checklistId = activity.model.field("checklist", "checklistId");
        if (checklistId == null || checklistId.isEmpty()) {
            toast("Selecione um checklist.");
            activity.back();
            return;
        }
        AutoCompleteTextView statusDropdown = content.findViewById(R.id.inspectionStatusDropdown);
        EditText search = content.findViewById(R.id.inspectionSearch);
        RecyclerView list = content.findViewById(R.id.inspectionEquipmentList);
        View back = content.findViewById(R.id.inspectionBack);
        if (back != null) back.setOnClickListener(v -> activity.back());
        list.setLayoutManager(new LinearLayoutManager(activity));
        list.setNestedScrollingEnabled(false);
        InspectionEquipmentAdapter adapter = new InspectionEquipmentAdapter(
                this::openInspection,
                this::isCompleted);
        list.setAdapter(adapter);
        statusDropdown.setAdapter(new ArrayAdapter<>(activity, R.layout.item_app_dropdown, new String[]{"Pendente", "Realizada"}));
        statusDropdown.setKeyListener(null);
        statusDropdown.setOnClickListener(v -> statusDropdown.showDropDown());
        final TextView empty = content.findViewById(R.id.inspectionEquipmentEmpty);
        EmptyStateStyler.apply(empty);
        final String[] statusFilter = {"Todos"};
        loadChecklistEquipment(checklistId, adapter, empty, statusFilter, search.getText().toString());
        statusDropdown.setOnItemClickListener((parent, view, position, id) -> {
            statusFilter[0] = String.valueOf(parent.getItemAtPosition(position));
            loadChecklistEquipment(checklistId, adapter, empty, statusFilter, search.getText().toString());
        });
        search.addTextChangedListener(watcher(value -> {
            if (pendingInspectionFilter != null) inspectionFilterHandler.removeCallbacks(pendingInspectionFilter);
            pendingInspectionFilter = () -> loadChecklistEquipment(checklistId, adapter, empty, statusFilter, value);
            inspectionFilterHandler.postDelayed(pendingInspectionFilter, 350);
        }));
    }

    private void loadChecklistEquipment(String checklistId, InspectionEquipmentAdapter adapter, TextView empty, String[] statusFilter, String query) {
        Dialog loading = AppLoading.show(activity);
        vm.modelos(checklistId, new ChecklistViewModel.Callback<List<ChecklistEquipment>>() {
            public void success(List<ChecklistEquipment> equipment) {
                AppLoading.hide(loading);
                renderEquipment(equipment, adapter, empty, statusFilter, query);
            }

            public void failure(Throwable error) {
                AppLoading.hide(loading);
                if (empty != null) {
                    empty.setText("Não foi possível carregar os equipamentos. Toque para tentar novamente.");
                    empty.setVisibility(View.VISIBLE);
                    empty.setOnClickListener(v -> loadChecklistEquipment(checklistId, adapter, empty, statusFilter, query));
                }
            }
        });
    }

    private void renderEquipment(List<ChecklistEquipment> values, InspectionEquipmentAdapter adapter, TextView empty, String[] statusFilter, String query) {
        List<ChecklistEquipment> filtered = new ArrayList<>();
        if (values != null) {
            for (ChecklistEquipment equipment : values) {
                boolean done = isCompleted(equipment);
                if (!statusFilter[0].equals("Todos") && ((done && !statusFilter[0].equals("Realizada")) || (!done && !statusFilter[0].equals("Pendente")))) {
                    continue;
                }
                String code = equipment.codigo == null ? "" : equipment.codigo;
                if (query != null && !code.toLowerCase(Locale.ROOT).contains(query.trim().toLowerCase(Locale.ROOT))) continue;
                filtered.add(equipment);
            }
        }
        adapter.submit(filtered);
        if (empty != null) {
            ((TextView) empty).setText("Não existem equipamentos neste status.");
            empty.setOnClickListener(null);
            empty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void openInspection(ChecklistEquipment equipment) {
        String json = activity.model.field("checklist", "selectedTemplate");
        activeTemplate = json == null ? null : gson.fromJson(json, ChecklistTemplate.class);
        activity.model.field("checklist", "equipmentId", String.valueOf(equipment.id));
        activity.model.field("checklist", "equipmentCode", equipment.codigo);
        activity.model.field("checklist", "equipmentModelName", equipment.modeloNome);
        activity.openName("inspection_form");
    }

    private void bindInspectionForm(View content) {
        String json = activity.model.field("checklist", "selectedTemplate");
        if (json == null) {
            toast("Checklist não encontrado.");
            return;
        }
        activeTemplate = gson.fromJson(json, ChecklistTemplate.class);
        long equipmentId;
        try {
            equipmentId = Long.parseLong(activity.model.field("checklist", "equipmentId"));
        } catch (Exception e) {
            equipmentId = 0;
        }
        activeEquipment = new ChecklistEquipment(equipmentId, activity.model.field("checklist", "equipmentCode"), null, false);
        boolean tech = activity.role().equals("technician");
        TextView title = content.findViewWithTag("text:Inspecionar AP-001");
        if (title != null) {
            String equipmentCode = activeEquipment.codigo == null ? "" : activeEquipment.codigo;
            String fullTitle = "Inspecionar " + equipmentCode;
            android.text.SpannableString styledTitle = new android.text.SpannableString(fullTitle);
            int split = "Inspecionar".length();
            styledTitle.setSpan(new android.text.style.ForegroundColorSpan(
                            androidx.core.content.ContextCompat.getColor(activity, R.color.ops_color_ff153d7d)),
                    0, split, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            styledTitle.setSpan(new android.text.style.ForegroundColorSpan(
                            androidx.core.content.ContextCompat.getColor(activity, R.color.ops_color_ff0b013c)),
                    split, fullTitle.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            title.setText(styledTitle);
            title.setLineSpacing(dp(activity, 12), 1f);
            title.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(activity, R.font.poppins_semibold));
        }
        TextView modelName = content.findViewById(tech ? R.id.ops_5717_10384 : R.id.ops_5717_8405);
        if (modelName != null) {
            modelName.setText(activity.model.field("checklist", "equipmentModelName"));
        }
        TextView checklistName = content.findViewById(tech ? R.id.ops_5717_10385 : R.id.ops_5717_8406);
        if (checklistName != null) {
            checklistName.setText(activeTemplate.nome);
        }
        int[] sampleIds = {R.id.ops_5717_8407, R.id.ops_5717_8412, R.id.ops_5717_8408, R.id.ops_5717_8422, R.id.ops_5717_8425, R.id.ops_5717_8428, R.id.ops_5717_8416, R.id.ops_5717_8419, R.id.ops_5717_8409, R.id.ops_5717_8430, R.id.ops_5717_8431, R.id.ops_5717_8432, R.id.ops_5717_8433, R.id.ops_5717_8434, R.id.ops_5717_8410, R.id.ops_5717_8436, R.id.ops_5717_8438, R.id.ops_5717_8437, R.id.ops_5717_8441, R.id.ops_5717_8443, R.id.ops_5717_8442, R.id.ops_5717_8446, R.id.ops_5717_8448, R.id.ops_5717_8447, R.id.ops_5717_8451, R.id.ops_5717_8452, R.id.ops_5717_10386, R.id.ops_5717_10391, R.id.ops_5717_10387, R.id.ops_5717_10401, R.id.ops_5717_10404, R.id.ops_5717_10407, R.id.ops_5717_10395, R.id.ops_5717_10398, R.id.ops_5717_10388, R.id.ops_5717_10409, R.id.ops_5717_10410, R.id.ops_5717_10411, R.id.ops_5717_10412, R.id.ops_5717_10413, R.id.ops_5717_10389, R.id.ops_5717_10415, R.id.ops_5717_10417, R.id.ops_5717_10416, R.id.ops_5717_10420, R.id.ops_5717_10422, R.id.ops_5717_10421, R.id.ops_5717_10425, R.id.ops_5717_10427, R.id.ops_5717_10426, R.id.ops_5717_10430, R.id.ops_5717_10431};
        for (int id : sampleIds) {
            View mock = content.findViewById(id);
            if (mock != null) {
                mock.setVisibility(View.GONE);
            }
        }
        answerContainer = vertical();
        answerContainer.setId(View.generateViewId());
        int contentInset = activity.getResources().getDimensionPixelSize(R.dimen.app_events_title_margin_start);
        answerContainer.setPadding(contentInset, 0, contentInset, dp(activity, 20));
        attachBelow(content, answerContainer, tech ? R.id.ops_5717_10382 : R.id.ops_5717_8403, 14);
        answers.clear();
        if (activeTemplate.campos != null) {
            for (ChecklistField field : activeTemplate.campos) {
                renderAnswer(field);
            }
        }
        int footerId = tech ? R.id.ops_5717_10433 : R.id.ops_5717_8454;
        View footer = content.findViewById(footerId);
        View submit = content.findViewWithTag("action:Enviar");
        positionInspectionActions(footer, submit, answerContainer.getId());
        clickTag(content, "image:arrow-left", v -> activity.back());
        clickTag(content, "text:Cancelar", v -> activity.back());
        clickTag(content, "action:Enviar", v -> submitInspection());
    }

    private void positionInspectionActions(View cancel, View submit, int fieldsId) {
        if (cancel == null || submit == null
                || !(cancel.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)
                || !(submit.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) return;
        int side = activity.getResources().getDimensionPixelSize(
                activity.role().equals("manager")
                        ? R.dimen.ops_manager_home_card_side_margin
                        : R.dimen.app_events_title_margin_start);
        int gap = activity.getResources().getDimensionPixelSize(R.dimen.ops_dp_18_0);
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams cancelParams =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) cancel.getLayoutParams();
        int buttonWidth = activity.getResources().getDimensionPixelSize(R.dimen.ops_dp_160_0);
        cancelParams.width = buttonWidth;
        cancelParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        cancelParams.startToEnd = -1;
        cancelParams.endToStart = submit.getId();
        cancelParams.endToEnd = -1;
        cancelParams.leftMargin = 0;
        cancelParams.rightMargin = 0;
        cancelParams.setMarginStart(side);
        cancelParams.setMarginEnd(gap / 2);
        cancelParams.horizontalChainStyle = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.CHAIN_PACKED;
        cancelParams.horizontalBias = 0.5f;
        cancelParams.horizontalWeight = 0f;
        cancelParams.topToTop = -1;
        cancelParams.topToBottom = fieldsId;
        cancelParams.bottomToTop = -1;
        cancelParams.bottomToBottom = -1;
        cancelParams.topMargin = activity.getResources().getDimensionPixelSize(R.dimen.ops_dp_40_0);
        cancel.setLayoutParams(cancelParams);
        android.graphics.drawable.GradientDrawable cancelBackground = new android.graphics.drawable.GradientDrawable();
        cancelBackground.setColor(androidx.core.content.ContextCompat.getColor(activity, R.color.ops_color_4cc4cfe0));
        cancelBackground.setCornerRadius(dp(activity, 24));
        cancel.setBackground(cancelBackground);

        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams submitParams =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) submit.getLayoutParams();
        submitParams.width = buttonWidth;
        submitParams.startToStart = -1;
        submitParams.startToEnd = cancel.getId();
        submitParams.endToStart = -1;
        submitParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        submitParams.leftMargin = 0;
        submitParams.rightMargin = 0;
        submitParams.setMarginStart(gap / 2);
        submitParams.setMarginEnd(side);
        submitParams.horizontalWeight = 0f;
        submitParams.topToTop = -1;
        submitParams.topToBottom = fieldsId;
        submitParams.bottomToTop = -1;
        submitParams.bottomToBottom = -1;
        submitParams.topMargin = activity.getResources().getDimensionPixelSize(R.dimen.ops_dp_40_0);
        submit.setLayoutParams(submitParams);
        android.graphics.drawable.GradientDrawable submitBackground = new android.graphics.drawable.GradientDrawable();
        submitBackground.setColor(androidx.core.content.ContextCompat.getColor(activity, R.color.ops_color_ff153d7d));
        submitBackground.setCornerRadius(dp(activity, 24));
        submit.setBackground(submitBackground);
        if (submit instanceof TextView) {
            ((TextView) submit).setGravity(Gravity.CENTER);
        }

        View cancelText = cancel.findViewWithTag("text:Cancelar");
        if (cancelText instanceof TextView
                && cancelText.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams textParams =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) cancelText.getLayoutParams();
            textParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            textParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            textParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            textParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            textParams.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            textParams.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            textParams.leftMargin = 0;
            textParams.rightMargin = 0;
            textParams.topMargin = 0;
            textParams.bottomMargin = 0;
            textParams.setMarginStart(0);
            textParams.setMarginEnd(0);
            cancelText.setLayoutParams(textParams);
            ((TextView) cancelText).setGravity(Gravity.CENTER);
        }
    }

    private void renderAnswer(ChecklistField field) {
        TextView label = text(field.nome + (Boolean.TRUE.equals(field.obrigatorio) ? " *" : ""), 18, true);
        label.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(activity, R.font.poppins_semibold));
        label.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,
                activity.getResources().getDimension(R.dimen.ops_sp_20_0));
        label.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        if (Boolean.TRUE.equals(field.obrigatorio)) {
            android.text.SpannableString styled = new android.text.SpannableString(field.nome + " *");
            styled.setSpan(new android.text.style.ForegroundColorSpan(Color.RED), styled.length() - 1, styled.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            label.setText(styled);
        }
        answerContainer.addView(label);
        switch (field.tipoCampo) {
            case TEXTO:
            case NUMERO: {
                EditText input = new EditText(activity);
                input.setSingleLine(false);
                input.setTextColor(androidx.core.content.ContextCompat.getColor(activity, R.color.app_text_black));
                input.setHintTextColor(androidx.core.content.ContextCompat.getColor(activity, R.color.app_text_placeholder_dark));
                input.setBackgroundResource(R.drawable.bg_app_checklist_input_outline);
                input.setBackgroundTintList(null);
                input.setAlpha(1f);
                input.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(activity, R.font.poppins_medium));
                input.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 22);
                input.setPadding(dp(activity, 16), dp(activity, 12), dp(activity, 16), dp(activity, 12));
                input.setInputType(field.tipoCampo == TipoCampoChecklist.NUMERO ? InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, activity.getResources().getDimensionPixelSize(R.dimen.ops_dp_60_0));
                inputParams.topMargin = activity.getResources().getDimensionPixelSize(R.dimen.ops_dp_10_0);
                inputParams.bottomMargin = activity.getResources().getDimensionPixelSize(R.dimen.ops_dp_18_0);
                answerContainer.addView(input, inputParams);
                input.addTextChangedListener(watcher(value -> {
                    if (value.trim().isEmpty()) {
                        answers.remove(field.id);
                    } else {
                        answers.put(field.id, value);
                    }
                }));
                break;
            }
            case BOOLEANO: {
                Switch toggle = new Switch(activity);
                toggle.setText("Sim");
                toggle.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 18);
                toggle.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(activity, R.font.poppins_regular));
                toggle.setTextColor(androidx.core.content.ContextCompat.getColor(activity, R.color.ops_color_ff153d7d));
                int blue = androidx.core.content.ContextCompat.getColor(activity, R.color.ops_color_ff153d7d);
                int muted = androidx.core.content.ContextCompat.getColor(activity, R.color.ops_color_ff637087);
                android.content.res.ColorStateList switchTint = new android.content.res.ColorStateList(
                        new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                        new int[]{blue, muted});
                toggle.setThumbTintList(switchTint);
                toggle.setTrackTintList(switchTint);
                answerContainer.addView(toggle);
                answers.put(field.id, false);
                toggle.setOnCheckedChangeListener((button, checked) -> answers.put(field.id, checked));
                break;
            }
            case RADIO: {
                RadioGroup group = new RadioGroup(activity);
                for (ChecklistOption option : field.opcoes) {
                    RadioButton radio = new RadioButton(activity);
                    radio.setText(option.nome);
                    radio.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 18);
                    radio.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(activity, R.font.poppins_regular));
                    radio.setTextColor(androidx.core.content.ContextCompat.getColor(activity, R.color.ops_color_ff153d7d));
                    radio.setButtonTintList(android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(activity, R.color.ops_color_ff153d7d)));
                    radio.setTag(option.id);
                    group.addView(radio);
                }
                answerContainer.addView(group);
                answers.put(field.id, new LinkedHashMap<String, Boolean>());
                group.setOnCheckedChangeListener((g, checked) -> {
                    Map<String, Boolean> selected = new LinkedHashMap<>();
                    for (ChecklistOption option : field.opcoes) {
                        selected.put(option.id, false);
                    }
                    View chosen = g.findViewById(checked);
                    if (chosen != null) {
                        selected.put(String.valueOf(chosen.getTag()), true);
                    }
                    answers.put(field.id, selected);
                });
                break;
            }
            case CHECK: {
                LinearLayout options = vertical();
                Map<String, Boolean> selected = new LinkedHashMap<>();
                for (ChecklistOption option : field.opcoes) {
                    CheckBox check = new CheckBox(activity);
                    check.setText(option.nome);
                    check.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 18);
                    check.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(activity, R.font.poppins_regular));
                    check.setTextColor(androidx.core.content.ContextCompat.getColor(activity, R.color.ops_color_ff153d7d));
                    check.setButtonTintList(android.content.res.ColorStateList.valueOf(
                            androidx.core.content.ContextCompat.getColor(activity, R.color.ops_color_ff153d7d)));
                    selected.put(option.id, false);
                    check.setOnCheckedChangeListener((button, checked) -> {
                        selected.put(option.id, checked);
                        answers.put(field.id, new LinkedHashMap<>(selected));
                    });
                    options.addView(check);
                }
                answerContainer.addView(options);
                answers.put(field.id, selected);
                break;
            }
            case AVALIACAO: {
                RatingBar rating = new RatingBar(activity);
                rating.setNumStars(5);
                rating.setMax(10);
                rating.setStepSize(0.5f);
                rating.setRating(0f);
                rating.setIsIndicator(false);
                rating.setProgressTintList(android.content.res.ColorStateList.valueOf(
                        androidx.core.content.ContextCompat.getColor(activity, R.color.ops_color_ff153d7d)));
                rating.setSecondaryProgressTintList(android.content.res.ColorStateList.valueOf(
                        androidx.core.content.ContextCompat.getColor(activity, R.color.ops_color_ff153d7d)));
                rating.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        androidx.core.content.ContextCompat.getColor(activity, R.color.app_text_muted)));
                answerContainer.addView(rating);
                answers.remove(field.id);
                rating.setOnRatingBarChangeListener((bar, value, user) -> {
                    float selected = Math.min(5f, Math.max(0f, Math.round(value * 2f) / 2f));
                    if (selected == 0f) answers.remove(field.id);
                    else answers.put(field.id, selected);
                });
                break;
            }
        }
    }

    private void submitInspection() {
        if (activeTemplate == null || activeEquipment == null) {
            return;
        }
        if (activeTemplate.campos != null) for (ChecklistField field : activeTemplate.campos) {
            if (Boolean.TRUE.equals(field.obrigatorio) && !hasInspectionAnswer(field)) {
                toast("Preencha o campo obrigatório: " + field.nome + ".");
                return;
            }
            if (field.tipoCampo == TipoCampoChecklist.NUMERO && answers.get(field.id) instanceof String) {
                String raw = ((String) answers.get(field.id)).trim();
                if (raw.isEmpty()) {
                    answers.remove(field.id);
                    continue;
                }
                try {
                    answers.put(field.id, Double.valueOf(raw));
                } catch (Exception e) {
                    toast("Informe um número válido em " + field.nome + ".");
                    return;
                }
            }
        }
        Dialog loading = AppLoading.show(activity);
        String checklistId = activity.model.field("checklist", "checklistId");
        vm.inspecionar(checklistId, activeEquipment.id, activeTemplate, new LinkedHashMap<>(answers), new ChecklistViewModel.Callback<Void>() {
            public void success(Void value) {
                AppLoading.hide(loading);
                vm.invalidarCacheEquipamentos();
                activity.back();
                activity.refresh();
                toast("Inspeção registrada.");
            }

            public void failure(Throwable error) {
                AppLoading.hide(loading);
                toast(AutenticacaoViewModel.messageFrom(error));
            }
        });
    }

    private boolean hasInspectionAnswer(ChecklistField field) {
        Object value = answers.get(field.id);
        if (value == null) return false;
        if (field.tipoCampo == TipoCampoChecklist.TEXTO
                || field.tipoCampo == TipoCampoChecklist.NUMERO) {
            return !String.valueOf(value).trim().isEmpty();
        }
        if (field.tipoCampo == TipoCampoChecklist.RADIO
                || field.tipoCampo == TipoCampoChecklist.CHECK) {
            if (!(value instanceof Map)) return false;
            for (Object selected : ((Map<?, ?>) value).values()) {
                if (Boolean.TRUE.equals(selected)) return true;
            }
            return false;
        }
        if (field.tipoCampo == TipoCampoChecklist.AVALIACAO) {
            return value instanceof Number && ((Number) value).doubleValue() >= 0.5d
                    && ((Number) value).doubleValue() <= 5d;
        }
        // BOOLEANO armazena false como uma resposta válida, pois o switch representa ambos os valores.
        return field.tipoCampo == TipoCampoChecklist.BOOLEANO;
    }

    private interface TextValue {
        void accept(String value);
    }

    private android.text.TextWatcher watcher(TextValue target) {
        return new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                target.accept(s.toString());
            }

            public void afterTextChanged(android.text.Editable e) {
            }
        };
    }

    private void clickTag(View root, String tag, View.OnClickListener listener) {
        View target = root.findViewWithTag(tag);
        if (target != null) {
            target.setOnClickListener(listener);
        }
    }

    private void toast(String message) {
        if (fragment.isAdded()) {
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
        }
    }

    private void showFieldDialog(ChecklistField field, int index) {
        Bundle args = new Bundle();
        if (field != null) {
            args.putString("field", gson.toJson(field));
        }
        args.putInt("index", index);
        String key = field == null ? "manager_create_field" : "manager_edit_field";
        com.aula.fixa.ui.view.operations.OperationsDialogFragment.create(key, args)
                .show(activity.getSupportFragmentManager(), "ops_dialog");
    }

    private ViewGroup layoutRoot(View content) {
        ViewGroup group = (ViewGroup) content;
        View scroll = group.findViewById(R.id.checklistScroll);
        if (scroll instanceof androidx.core.widget.NestedScrollView
                && ((androidx.core.widget.NestedScrollView) scroll).getChildAt(0) instanceof ViewGroup) {
            return (ViewGroup) ((androidx.core.widget.NestedScrollView) scroll).getChildAt(0);
        }
        return group.getChildCount() == 1 && group.getChildAt(0) instanceof ViewGroup ? (ViewGroup) group.getChildAt(0) : group;
    }

    private void setBelow(View content, View child, int anchor, int marginDp) {
        if (child == null || !(child.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) {
            return;
        }
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams p = (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) child.getLayoutParams();
        p.topToBottom = anchor;
        p.topMargin = dp(activity, marginDp);
        ((androidx.constraintlayout.widget.ConstraintLayout) child.getParent()).updateViewLayout(child, p);
    }

    private void attachBelow(View content, View child, int anchor, int marginDp) {
        ViewGroup root = layoutRoot(content);
        if (root instanceof androidx.constraintlayout.widget.ConstraintLayout) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams p = new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(0, -2);
            p.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            p.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            p.topToBottom = anchor;
            p.topMargin = dp(activity, marginDp);
            root.addView(child, p);
        } else {
            root.addView(child, new ViewGroup.LayoutParams(-1, -2));
        }
    }

    private LinearLayout vertical() {
        LinearLayout l = new LinearLayout(activity);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout card(int height, int background) {
        LinearLayout l = vertical();
        l.setBackgroundResource(background);
        l.setElevation(dp(activity, 2));
        l.setMinimumHeight(dp(activity, height));
        return l;
    }

    private LinearLayout row(View left, View right) {
        LinearLayout l = new LinearLayout(activity);
        l.setGravity(Gravity.CENTER_VERTICAL);
        l.addView(left, new LinearLayout.LayoutParams(0, -2, 1));
        l.addView(right, new LinearLayout.LayoutParams(-2, -2));
        return l;
    }

    private TextView text(String value, int sp, boolean bold) {
        TextView t = new TextView(activity);
        t.setText(value == null ? "" : value);
        t.setTextSize(sp);
        t.setTextColor(Color.rgb(23, 14, 74));
        if (bold) {
            t.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        return t;
    }

    private static int dp(Context c, int value) {
        return Math.round(value * c.getResources().getDisplayMetrics().density);
    }
}
