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
import com.aula.fixa.domain.enums.TipoCampoChecklist;
import com.aula.fixa.domain.model.checklist.*;
import com.aula.fixa.ui.config.AppLoading;
import com.aula.fixa.ui.config.AppDialogs;
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
    private final Map<String, List<ChecklistEquipment>> inspectionEquipmentCache = new HashMap<>();
    private List<EquipmentModelOption> availableModels = new ArrayList<>();
    private final Map<String, Object> answers = new LinkedHashMap<>();
    private LinearLayout answerContainer;
    private ChecklistTemplate activeTemplate;
    private ChecklistEquipment activeEquipment;

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
        int[][] circlesAndNumbers;
        int[] labels = null;
        if (screen.endsWith("intro")) {
            circlesAndNumbers = new int[][]{{R.id.ops_5717_8025, R.id.ops_5717_8030},
                    {R.id.ops_5717_8026, R.id.ops_5717_8031}, {R.id.ops_5717_8027, R.id.ops_5717_8032}};
        } else if (screen.endsWith("information")) {
            circlesAndNumbers = new int[][]{{R.id.ops_5717_8089, R.id.ops_5717_8090},
                    {R.id.ops_5717_8095, R.id.ops_5717_8096}, {R.id.ops_5717_8092, R.id.ops_5717_8093}};
            labels = new int[]{R.id.ops_5717_8085, R.id.ops_5717_8086, R.id.ops_5717_8087};
        } else if (screen.endsWith("fields")) {
            circlesAndNumbers = new int[][]{{R.id.ops_5717_8050, R.id.ops_5717_8051},
                    {R.id.ops_5717_8056, R.id.ops_5717_8057}, {R.id.ops_5717_8053, R.id.ops_5717_8054}};
            labels = new int[]{R.id.ops_5717_8046, R.id.ops_5717_8047, R.id.ops_5717_8048};
        } else if (screen.endsWith("models")) {
            circlesAndNumbers = new int[][]{{R.id.ops_5717_8131, R.id.ops_5717_8132},
                    {R.id.ops_5717_8137, R.id.ops_5717_8138}, {R.id.ops_5717_8134, R.id.ops_5717_8135}};
            labels = new int[]{R.id.ops_5717_8127, R.id.ops_5717_8128, R.id.ops_5717_8129};
        } else return;

        for (int[] pair : circlesAndNumbers) {
            View circle = content.findViewById(pair[0]);
            View number = content.findViewById(pair[1]);
            if (circle == null || number == null
                    || !(number.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) continue;
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
        if (labels != null) {
            for (int i = 0; i < labels.length; i++) {
                View label = content.findViewById(labels[i]);
                if (label == null || !(label.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) continue;
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                        (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) label.getLayoutParams();
                params.startToStart = circlesAndNumbers[i][0];
                params.endToEnd = circlesAndNumbers[i][0];
                params.setMarginStart(0);
                params.setMarginEnd(0);
                params.leftMargin = 0;
                params.rightMargin = 0;
                label.setLayoutParams(params);
                if (label instanceof TextView) ((TextView) label).setGravity(Gravity.CENTER);
            }
        }
    }

    private void bindChecklistList(View content, String screen) {
        boolean canManage = activity.role().equals("manager");
        boolean technician = screen.startsWith("technician");
        int circleFabId = technician ? R.id.ops_5717_10222 : R.id.ops_5717_7935;
        int plusFabId = technician ? R.id.ops_5717_10224 : R.id.ops_5717_7937;
        View circleFab = content.findViewById(circleFabId);
        View plusFab = content.findViewById(plusFabId);
        if (circleFab != null) circleFab.setVisibility(View.GONE);
        if (plusFab != null) plusFab.setVisibility(View.GONE);
        installChecklistFab(content, canManage);
        loadTemplates(content, screen);
        for (View card : new View[]{content.findViewById(R.id.ops_5717_7973), content.findViewById(R.id.ops_5717_7988), content.findViewById(R.id.ops_5717_10260), content.findViewById(R.id.ops_5717_10275)}) {
            if (card != null) {
                card.setVisibility(View.GONE);
            }
        }
        // Retain the existing card geometry/background while filling server data.
        LinearLayout list = new LinearLayout(activity);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setTag("checklist:cards");
        attachBelow(content, list, screen.startsWith("technician") ? R.id.ops_5717_10217 : R.id.ops_5717_7930, 18);
        list.setPadding(dp(activity, 24), 0, dp(activity, 24), dp(activity, 100));
        content.setTag(R.id.ops_5717_7930, list);
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
                renderTemplates(content, screen);
            }

            public void failure(Throwable e) {
                AppLoading.hide(loading);
                if (fragment.isAdded()) {
                    System.out.println(e);
                    toast(AutenticacaoViewModel.messageFrom(e));
                }
            }
        });
    }

    private void renderTemplates(View content, String screen) {
        LinearLayout list = (LinearLayout) content.getTag(R.id.ops_5717_7930);
        if (list == null) {
            return;
        }
        list.removeAllViews();
        for (ChecklistTemplate template : templates) {
            View card = activity.getLayoutInflater().inflate(R.layout.item_ops_manager_checklist, list, false);
            ((TextView) card.findViewById(R.id.checklistCardTitle)).setText(template.nome == null ? "" : template.nome);
            ((TextView) card.findViewById(R.id.checklistCardCategory)).setText(categoryNames(template));
            ((TextView) card.findViewById(R.id.checklistCardPeriod)).setText(
                    template.periodicidade == null ? "_" : template.periodicidade + " dias");
            ((TextView) card.findViewById(R.id.checklistCardStatus)).setText(status(template));
            ((TextView) card.findViewById(R.id.checklistCardModels)).setText(
                    String.valueOf(template.modelos == null ? 0 : template.modelos.size()));
            View menu = card.findViewById(R.id.checklistCardMenu);
            card.setOnClickListener(v -> selectChecklist(template));
            if (activity.role().equals("manager")) {
                menu.setOnClickListener(v -> showChecklistMenu(menu, template));
            } else {
                menu.setVisibility(View.GONE);
            }
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, dp(activity, 210));
            cp.bottomMargin = dp(activity, 14);
            list.addView(card, cp);
        }
        if (templates.isEmpty()) {
            TextView empty = text("Não existem checklists cadastrados.", 16, false);
            empty.setGravity(Gravity.CENTER);
            list.addView(empty, new LinearLayout.LayoutParams(-1, dp(activity, 120)));
        }
    }

    private void showChecklistMenu(View anchor, ChecklistTemplate template) {
        View menu = activity.getLayoutInflater().inflate(R.layout.popup_app_request_menu, null, false);
        android.widget.PopupWindow popup = new android.widget.PopupWindow(
                menu,
                activity.getResources().getDimensionPixelSize(R.dimen.app_popup_menu_width),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true);
        popup.setBackgroundDrawable(androidx.core.content.ContextCompat.getDrawable(activity, R.drawable.bg_app_popup_menu));
        popup.setElevation(dp(activity, 6));
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
        activity.model.field("checklist", "templateDraft", gson.toJson(new ChecklistTemplate()));
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
        clickTag(content, "action:Continuar", v -> activity.open("manager_checklist_information"));
        clickTag(content, "text:Cancelar", v -> activity.back());
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
        clickTag(content, "text:Ir para a Home", v -> activity.openName("home"));
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
        if (name != null) {
            name.setText(t.nome);
        }
        if (periodic != null && t.periodicidade != null) {
            periodic.setText(String.valueOf(t.periodicidade));
        }
        if (date != null && t.primeiraAbertura != null) {
            date.setText(t.primeiraAbertura);
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
        clickTag(content, "text:Voltar", v -> activity.back());
        alignWizardFooter(content, content.findViewWithTag("text:Voltar"), content.findViewWithTag("action:Próximo"));
        clickTag(content, "action:Próximo", v -> {
            String n = name == null ? "" : name.getText().toString().trim(), p = periodic == null ? "" : periodic.getText().toString().trim(), d = date == null ? "" : date.getText().toString().trim();
            if (n.isEmpty()) {
                name.setError("Informe o nome.");
                return;
            }
            try {
                int days = Integer.parseInt(p);
                if (days <= 0) {
                    throw new Exception();
                }
                t.periodicidade = days;
            } catch (Exception e) {
                periodic.setError("Informe a periodicidade em dias.");
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
        View add = content.findViewById(R.id.ops_5717_8078);
        View addLabel = content.findViewById(R.id.ops_5717_8079);
        View sample = content.findViewById(R.id.ops_5717_8068);
        if (sample != null) {
            sample.setVisibility(View.GONE);
        }
        RecyclerView list = new RecyclerView(activity);
        list.setTag("field:list");
        list.setId(View.generateViewId());
        list.setLayoutManager(new LinearLayoutManager(activity));
        list.setNestedScrollingEnabled(false);
        attachBelow(content, list, R.id.ops_5717_8041, 12);
        list.setPadding(dp(activity, 34), 0, dp(activity, 34), 0);
        FieldListAdapter adapter = new FieldListAdapter(template, list);
        list.setAdapter(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source,
                                           RecyclerView.ViewHolder target) {
                int from = source.getBindingAdapterPosition();
                int to = target.getBindingAdapterPosition();
                if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION || from == to) return false;
                Collections.swap(template.campos, from, to);
                adapter.notifyItemMoved(from, to);
                saveDraft(template);
                return true;
            }
            @Override public void onSwiped(RecyclerView.ViewHolder holder, int direction) { }
        });
        adapter.setTouchHelper(touchHelper);
        touchHelper.attachToRecyclerView(list);
        placeBelow(add, list.getId(), 16);
        View back = content.findViewWithTag("text:Voltar");
        View next = content.findViewWithTag("action:Próximo");
        Runnable moveTimelineAfterContent = () -> positionChecklistFieldsFlow(content, add, back, next);
        list.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                list.post(moveTimelineAfterContent));
        if (add != null) {
            add.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) ->
                    list.post(moveTimelineAfterContent));
        }
        list.post(moveTimelineAfterContent);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override public void onChanged() { list.post(moveTimelineAfterContent); }
            @Override public void onItemRangeInserted(int positionStart, int itemCount) { list.post(moveTimelineAfterContent); }
            @Override public void onItemRangeRemoved(int positionStart, int itemCount) { list.post(moveTimelineAfterContent); }
        });
        if (add != null) {
            add.setOnClickListener(v -> showFieldDialog(null, -1));
        }
        if (addLabel != null) {
            addLabel.setOnClickListener(v -> showFieldDialog(null, -1));
        }
        clickTag(content, "image:arrow-left", v -> activity.back());
        clickTag(content, "text:Voltar", v -> activity.back());
        clickTag(content, "action:Próximo", v -> {
            if (template.campos == null || template.campos.isEmpty()) {
                toast("Adicione ao menos um campo de inspeção.");
                return;
            }
            saveDraft(template);
            activity.open("manager_checklist_models");
        });
    }

    private void positionChecklistFieldsFlow(View content, View add, View back, View next) {
        if (add == null || add.getHeight() <= 0) return;
        int[] circles = {R.id.ops_5717_8050, R.id.ops_5717_8056, R.id.ops_5717_8053};
        int[] labels = {R.id.ops_5717_8046, R.id.ops_5717_8047, R.id.ops_5717_8048};
        int baseTimelineTop = dp(activity, 430);
        int gapAfterAdd = dp(activity, 40);
        int timelineTopMargin = Math.max(gapAfterAdd, baseTimelineTop - add.getBottom());

        for (int circleId : circles) setTopBelow(content, circleId, add.getId(), timelineTopMargin);
        setCenteredVertically(content, R.id.ops_shape_1, circles[0]);
        setCenteredVertically(content, R.id.ops_shape_2, circles[0]);
        setCenteredVertically(content, R.id.ops_shape_3, circles[1]);
        setCenteredVertically(content, R.id.ops_shape_13, circles[1]);
        for (int i = 0; i < labels.length; i++) setTopBelow(content, labels[i], circles[i], 12);

        if (next != null && next.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) next.getLayoutParams();
            params.topToTop = -1;
            params.topToBottom = labels[labels.length - 1];
            params.bottomToBottom = -1;
            params.topMargin = dp(activity, 60);
            params.bottomMargin = 0;
            params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            params.setMarginEnd(dp(activity, 36));
            next.setLayoutParams(params);
        }
        if (back != null && next != null
                && back.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) back.getLayoutParams();
            params.topToTop = next.getId();
            params.topToBottom = -1;
            params.bottomToBottom = next.getId();
            params.topMargin = params.bottomMargin = 0;
            params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            params.setMarginStart(dp(activity, 36));
            back.setLayoutParams(params);
        }
    }

    private void setTopBelow(View content, int childId, int anchorId, int marginDp) {
        View child = content.findViewById(childId);
        if (child == null || !(child.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) return;
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) child.getLayoutParams();
        params.topToTop = -1;
        params.topToBottom = anchorId;
        params.bottomToTop = -1;
        params.bottomToBottom = -1;
        params.topMargin = dp(activity, marginDp);
        child.setLayoutParams(params);
    }

    private void setCenteredVertically(View content, int childId, int anchorId) {
        View child = content.findViewById(childId);
        if (child == null || !(child.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) return;
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) child.getLayoutParams();
        params.topToTop = anchorId;
        params.bottomToBottom = anchorId;
        params.topToBottom = -1;
        params.bottomToTop = -1;
        params.topMargin = params.bottomMargin = 0;
        child.setLayoutParams(params);
    }

    private void placeBelow(View child, int anchorId, int marginDp) {
        if (child == null || !(child.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) return;
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) child.getLayoutParams();
        params.topToTop = -1;
        params.topToBottom = anchorId;
        params.bottomToTop = -1;
        params.bottomToBottom = -1;
        params.topMargin = dp(activity, marginDp);
        ((androidx.constraintlayout.widget.ConstraintLayout) child.getParent()).updateViewLayout(child, params);
    }

    private void alignWizardFooter(View content, View back, View next) {
        if (!(content instanceof ViewGroup) || back == null || next == null) return;
        ViewGroup parent = (ViewGroup) back.getParent();
        if (!(parent instanceof androidx.constraintlayout.widget.ConstraintLayout)) return;
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams backParams =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) back.getLayoutParams();
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams nextParams =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) next.getLayoutParams();
        int footerTop = activity.getResources().getDimensionPixelSize(R.dimen.ops_dp_762_0);
        backParams.topToBottom = -1;
        backParams.topToTop = next.getId();
        backParams.bottomToBottom = next.getId();
        backParams.topMargin = backParams.bottomMargin = 0;
        nextParams.topToBottom = -1;
        nextParams.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        nextParams.bottomToBottom = -1;
        nextParams.topMargin = footerTop;
        nextParams.bottomMargin = 0;
        backParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        nextParams.startToStart = -1;
        nextParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        backParams.setMarginStart(dp(activity, 36));
        nextParams.setMarginEnd(dp(activity, 36));
        back.setLayoutParams(backParams);
        next.setLayoutParams(nextParams);
    }

    private final class FieldListAdapter extends RecyclerView.Adapter<FieldListAdapter.FieldHolder> {
        private final ChecklistTemplate template;
        private final RecyclerView recycler;
        private ItemTouchHelper touchHelper;

        FieldListAdapter(ChecklistTemplate template, RecyclerView recycler) {
            this.template = template;
            this.recycler = recycler;
        }

        void setTouchHelper(ItemTouchHelper helper) { touchHelper = helper; }

        @Override public FieldHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View card = activity.getLayoutInflater().inflate(R.layout.item_ops_checklist_field, parent, false);
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, 100));
            params.bottomMargin = dp(activity, 10);
            card.setLayoutParams(params);
            return new FieldHolder(card);
        }

        @Override public void onBindViewHolder(FieldHolder holder, int position) {
            ChecklistField field = template.campos.get(position);
            holder.title.setText(field.nome == null ? "" : field.nome);
            String type = field.tipoCampo == null ? "" : field.tipoCampo.nome;
            holder.meta.setText(type + (Boolean.TRUE.equals(field.obrigatorio) ? " · Obrigatório" : " · Opcional"));
            holder.drag.setOnTouchListener((view, event) -> {
                if (event.getActionMasked() == android.view.MotionEvent.ACTION_DOWN && touchHelper != null) {
                    touchHelper.startDrag(holder);
                    return true;
                }
                return false;
            });
            holder.menu.setOnClickListener(view -> showFieldMenu(view, holder.getBindingAdapterPosition()));
        }

        @Override public int getItemCount() { return template.campos == null ? 0 : template.campos.size(); }

        private void showFieldMenu(View anchor, int position) {
            if (position < 0 || position >= template.campos.size()) return;
            ChecklistField field = template.campos.get(position);
            View menu = activity.getLayoutInflater().inflate(R.layout.popup_app_request_menu, null, false);
            android.widget.PopupWindow popup = new android.widget.PopupWindow(menu,
                    activity.getResources().getDimensionPixelSize(R.dimen.app_popup_menu_width),
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popup.setBackgroundDrawable(androidx.core.content.ContextCompat.getDrawable(activity, R.drawable.bg_app_popup_menu));
            popup.setElevation(dp(activity, 6));
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
                        template.campos.remove(current);
                        saveDraft(template);
                        notifyItemRemoved(current);
                    }
                });
            });
            popup.showAsDropDown(anchor, -popup.getWidth() + anchor.getWidth(), 0);
        }

        final class FieldHolder extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView meta;
            final View drag;
            final View menu;
            FieldHolder(View item) {
                super(item);
                title = item.findViewById(R.id.checklistFieldTitle);
                meta = item.findViewById(R.id.checklistFieldMeta);
                drag = item.findViewById(R.id.checklistFieldDrag);
                menu = item.findViewById(R.id.checklistFieldMenu);
            }
        }
    }

    public static void bindDialog(Fragment owner, View content, String key) {
        OperationsActivity activity = (OperationsActivity) owner.requireActivity();
        ChecklistFlow flow = new ChecklistFlow(owner, activity);
        Bundle args = owner.getArguments();
        if (key.equals("manager_delete_checklist")) {
            View cancel = content.findViewWithTag("text:Cancelar"), remove = content.findViewWithTag("action:Excluir");
            if (cancel != null) {
                cancel.setOnClickListener(v -> ((androidx.fragment.app.DialogFragment) owner).dismiss());
            }
            if (remove != null) {
                remove.setOnClickListener(v -> {
                    String id = activity.model.field("checklist", "deleteId");
                    if (id == null || id.isEmpty()) {
                        Toast.makeText(activity, "Checklist inválido.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Dialog loading = AppLoading.show(activity);
                    flow.vm.excluir(id, new ChecklistViewModel.Callback<Void>() {
                        public void success(Void x) {
                            AppLoading.hide(loading);
                            ((androidx.fragment.app.DialogFragment) owner).dismiss();
                            activity.refresh();
                        }

                        public void failure(Throwable e) {
                            AppLoading.hide(loading);
                            Toast.makeText(activity, AutenticacaoViewModel.messageFrom(e), Toast.LENGTH_LONG).show();
                        }
                    });
                });
            }
            return;
        }
        ChecklistField old = null;
        if (args != null && args.getString("field") != null) {
            try { old = flow.gson.fromJson(args.getString("field"), ChecklistField.class); }
            catch (Exception ignored) { }
        }
        flow.bindFieldDialog(owner, content, key, args, old);
    }

    private void bindFieldDialog(Fragment owner, View content, String key, Bundle args, ChecklistField editing) {
        boolean editingDialog = key.equals("manager_edit_field") || key.equals("manager_edit_choice_field");
        int nameId = editingDialog ? R.id.ops_5717_8180 : R.id.ops_5717_8198;
        int typeId = editingDialog ? R.id.ops_5717_8183_input : R.id.ops_5717_8201_input;
        int typeContainerId = editingDialog ? R.id.ops_5717_8183 : R.id.ops_5717_8201;
        int toggleId = editingDialog ? R.id.ops_5717_8189 : R.id.ops_5717_8207;
        int arrowId = editingDialog ? R.id.ops_5717_8184 : R.id.ops_5717_8202;
        int saveId = editingDialog ? R.id.ops_5717_8176 : R.id.ops_5717_8194;

        EditText name = content.findViewById(nameId);
        if (name != null) {
            resizeView(name, dp(activity, 42));
            name.setTextSize(14);
            name.setIncludeFontPadding(true);
            if (editing != null) name.setText(editing.nome);
            else if (args != null && args.getString("fieldName") != null) name.setText(args.getString("fieldName"));
        }

        AutoCompleteTextView type = replaceTypeDropdown(content, typeId, typeContainerId, arrowId);
        if (type != null) {
            String initialType = args == null ? null : args.getString("fieldType");
            String initialTypeValue = initialType != null ? initialType
                    : editing != null && editing.tipoCampo != null ? editing.tipoCampo.nome : "";
            if (!initialTypeValue.isEmpty()) type.setText(initialTypeValue, false);
        }

        TextView requiredLabel = content.findViewWithTag("text:Campo obrigatório");
        TextView requiredValue = content.findViewWithTag("text:Sim");
        boolean initialRequired = editing != null && Boolean.TRUE.equals(editing.obrigatorio)
                || args != null && args.getBoolean("required", false);
        if (requiredValue != null) requiredValue.setText(initialRequired ? "Sim" : "Não");
        SwitchCompat requiredSwitch = replaceRequiredSwitch(content, toggleId, initialRequired);
        alignRequiredControls(requiredLabel, requiredValue, requiredSwitch);
        final boolean[] required = {initialRequired};
        if (requiredSwitch != null) requiredSwitch.setOnCheckedChangeListener((button, checked) -> {
            required[0] = checked;
            if (requiredValue != null) requiredValue.setText(checked ? "Sim" : "Não");
        });
        if (requiredValue != null) requiredValue.setOnClickListener(v -> {
            required[0] = !required[0];
            requiredValue.setText(required[0] ? "Sim" : "Não");
            if (requiredSwitch != null) requiredSwitch.setChecked(required[0]);
        });

        List<ChecklistOption> initialOptions = new ArrayList<>();
        if (editing != null && editing.opcoes != null) {
            for (ChecklistOption option : editing.opcoes) {
                if (option != null) initialOptions.add(new ChecklistOption(option.id, option.nome, false));
            }
        }
        OptionDraftAdapter optionAdapter = new OptionDraftAdapter(initialOptions);
        View optionSection = createOptionSection(content, optionAdapter);
        int requiredLabelId = requiredLabel == null ? View.NO_ID : requiredLabel.getId();
        View cancelLabel = content.findViewWithTag("text:Cancelar");
        View cancelButton = cancelLabel != null && cancelLabel.getParent() instanceof View
                ? (View) cancelLabel.getParent() : null;
        View saveButton = content.findViewById(saveId);

        Runnable updateOptionsSection = () -> {
            boolean needsOptions = isOptionType(type == null ? "" : type.getText().toString());
            if (needsOptions && optionAdapter.getItemCount() == 0) optionAdapter.addOption("");
            optionSection.setVisibility(needsOptions ? View.VISIBLE : View.GONE);
            positionFieldDialogFooter(content, cancelButton, cancelLabel, saveButton,
                    needsOptions ? optionSection.getId() : requiredLabelId,
                    needsOptions ? 16 : 36);
        };
        updateOptionsSection.run();

        if (type != null) {
            String[] typeNames = Arrays.stream(TipoCampoChecklist.values()).map(item -> item.nome).toArray(String[]::new);
            type.setAdapter(new ArrayAdapter<>(activity, R.layout.item_app_dropdown, typeNames));
            type.setOnItemClickListener((parent, row, position, id) -> updateOptionsSection.run());
            type.setOnClickListener(v -> type.showDropDown());
            View arrow = content.findViewById(arrowId);
            if (arrow != null) arrow.setOnClickListener(v -> type.showDropDown());
        }

        View.OnClickListener dismissDialog = v -> ((androidx.fragment.app.DialogFragment) owner).dismiss();
        if (cancelLabel != null) cancelLabel.setOnClickListener(dismissDialog);
        if (cancelButton != null) cancelButton.setOnClickListener(dismissDialog);
        if (saveButton != null) saveButton.setOnClickListener(v -> {
            if (name == null || name.getText().toString().trim().isEmpty()) {
                if (name != null) name.setError("Informe o nome do campo.");
                return;
            }
            if (type == null) return;
            TipoCampoChecklist selected;
            try { selected = TipoCampoChecklist.fromApi(type.getText().toString()); }
            catch (Exception invalid) { Toast.makeText(activity, "Selecione o tipo de campo.", Toast.LENGTH_SHORT).show(); return; }

            ChecklistField field = editing == null
                    ? new ChecklistField(UUID.randomUUID().toString(), name.getText().toString().trim(), selected, required[0], new ArrayList<>())
                    : editing;
            field.nome = name.getText().toString().trim();
            field.tipoCampo = selected;
            field.obrigatorio = required[0];
            field.opcoes = new ArrayList<>();
            if (selected == TipoCampoChecklist.RADIO || selected == TipoCampoChecklist.CHECK) {
                List<ChecklistOption> values = optionAdapter.optionValues();
                for (ChecklistOption option : values) {
                    String value = option.nome == null ? "" : option.nome.trim();
                    if (value.isEmpty()) {
                        Toast.makeText(activity, "Preencha ou remova todas as opções.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String optionId = option.id == null ? UUID.randomUUID().toString() : option.id;
                    field.opcoes.add(new ChecklistOption(optionId, value, false));
                }
                if (field.opcoes.isEmpty()) {
                    Toast.makeText(activity, "Adicione ao menos uma opção.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            ChecklistTemplate template = draft();
            if (template.campos == null) template.campos = new ArrayList<>();
            if (editing == null) template.campos.add(field);
            else {
                int index = args == null ? -1 : args.getInt("index", -1);
                if (index < 0 || index >= template.campos.size()) {
                    Toast.makeText(activity, "Campo não encontrado.", Toast.LENGTH_SHORT).show();
                    return;
                }
                template.campos.set(index, field);
            }
            saveDraft(template);
            ((androidx.fragment.app.DialogFragment) owner).dismiss();
            activity.refresh();
        });
    }

    private AutoCompleteTextView replaceTypeDropdown(View content, int inputId, int containerId, int arrowId) {
        View existing = content.findViewById(inputId);
        View container = content.findViewById(containerId);
        if (existing == null || !(existing.getParent() instanceof ViewGroup)) return null;
        ViewGroup parent = (ViewGroup) existing.getParent();
        ViewGroup.LayoutParams oldParams = existing.getLayoutParams();
        String value = existing instanceof TextView ? ((TextView) existing).getText().toString() : "";
        String hint = existing instanceof TextView ? ((TextView) existing).getHint().toString() : "Selecione um tipo de campo";
        androidx.appcompat.widget.AppCompatAutoCompleteTextView dropdown = new androidx.appcompat.widget.AppCompatAutoCompleteTextView(activity);
        dropdown.setId(inputId);
        dropdown.setTag("field:Tipo");
        dropdown.setText(value, false);
        dropdown.setHint(hint);
        dropdown.setTextSize(14);
        dropdown.setTextColor(androidx.core.content.ContextCompat.getColor(activity, R.color.ops_color_ff202020));
        dropdown.setHintTextColor(androidx.core.content.ContextCompat.getColor(activity, R.color.ops_color_ff637087));
        dropdown.setSingleLine(true);
        dropdown.setKeyListener(null);
        dropdown.setThreshold(0);
        dropdown.setBackgroundColor(Color.TRANSPARENT);
        dropdown.setPadding(dp(activity, 15), 0, dp(activity, 36), 0);
        dropdown.setGravity(Gravity.CENTER_VERTICAL);
        dropdown.setDropDownBackgroundDrawable(androidx.core.content.ContextCompat.getDrawable(activity, R.drawable.bg_app_popup_menu));
        dropdown.setDropDownVerticalOffset(dp(activity, 4));
        if (oldParams instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) oldParams;
            params.width = 0;
            params.height = 0;
            dropdown.setLayoutParams(params);
        } else dropdown.setLayoutParams(oldParams);
        int index = parent.indexOfChild(existing);
        parent.removeView(existing);
        parent.addView(dropdown, Math.max(0, index));
        resizeView(container, dp(activity, 42));

        View arrow = content.findViewById(arrowId);
        if (arrow != null && arrow.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) arrow.getLayoutParams();
            params.startToStart = -1;
            params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            params.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            params.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            params.topMargin = params.bottomMargin = 0;
            params.setMarginEnd(dp(activity, 15));
            arrow.setLayoutParams(params);
        }
        return dropdown;
    }

    private View createOptionSection(View content, OptionDraftAdapter adapter) {
        ViewGroup root = layoutRoot(content);
        LinearLayout section = vertical();
        section.setId(View.generateViewId());
        TextView heading = new TextView(activity);
        heading.setText("Opções");
        heading.setTextSize(14);
        heading.setTextColor(androidx.core.content.ContextCompat.getColor(activity, R.color.ops_color_ff130943));
        heading.setTypeface(activity.getResources().getFont(R.font.poppins_medium));
        section.addView(heading, new LinearLayout.LayoutParams(-1, dp(activity, 24)));

        RecyclerView optionList = new RecyclerView(activity);
        optionList.setLayoutManager(new LinearLayoutManager(activity));
        optionList.setNestedScrollingEnabled(false);
        optionList.setAdapter(adapter);
        LinearLayout.LayoutParams optionListParams = new LinearLayout.LayoutParams(-1, -2);
        optionListParams.topMargin = dp(activity, 12);
        section.addView(optionList, optionListParams);
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
                int from = source.getBindingAdapterPosition();
                int to = target.getBindingAdapterPosition();
                if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION || from == to) return false;
                adapter.move(from, to);
                return true;
            }
            @Override public void onSwiped(RecyclerView.ViewHolder holder, int direction) { }
        });
        adapter.setTouchHelper(helper);
        helper.attachToRecyclerView(optionList);

        TextView add = new TextView(activity);
        add.setText("+ Adicionar opção");
        add.setTextSize(14);
        add.setGravity(Gravity.CENTER);
        add.setTextColor(androidx.core.content.ContextCompat.getColor(activity, R.color.ops_color_ff153d7d));
        add.setBackgroundResource(R.drawable.bg_ops_70aaab1e67);
        add.setOnClickListener(v -> adapter.addOption(""));
        LinearLayout.LayoutParams addParams = new LinearLayout.LayoutParams(-1, dp(activity, 44));
        addParams.topMargin = dp(activity, 8);
        section.addView(add, addParams);

        TextView requiredLabel = content.findViewWithTag("text:Campo obrigatório");
        if (root instanceof androidx.constraintlayout.widget.ConstraintLayout && requiredLabel != null) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                    new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(0, -2);
            params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            params.topToBottom = requiredLabel.getId();
            params.topMargin = dp(activity, 12);
            params.setMarginStart(dp(activity, 35));
            params.setMarginEnd(dp(activity, 35));
            ((androidx.constraintlayout.widget.ConstraintLayout) root).addView(section, params);
        }
        section.setVisibility(View.GONE);
        return section;
    }

    private void alignRequiredControls(TextView label, TextView value, SwitchCompat toggle) {
        if (label == null || !(label.getParent() instanceof androidx.constraintlayout.widget.ConstraintLayout)) return;
        androidx.constraintlayout.widget.ConstraintLayout parent = (androidx.constraintlayout.widget.ConstraintLayout) label.getParent();
        if (toggle != null && toggle.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) toggle.getLayoutParams();
            params.width = dp(activity, 42);
            params.height = dp(activity, 28);
            params.startToStart = -1;
            params.startToEnd = -1;
            params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            params.topToTop = label.getId();
            params.bottomToBottom = label.getId();
            params.topMargin = params.bottomMargin = 0;
            params.setMarginEnd(dp(activity, 35));
            toggle.setLayoutParams(params);
        }
        if (value != null && toggle != null) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                    new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(-2, -2);
            params.endToStart = toggle.getId();
            params.topToTop = label.getId();
            params.bottomToBottom = label.getId();
            params.setMarginEnd(dp(activity, 8));
            value.setLayoutParams(params);
            value.setGravity(Gravity.CENTER_VERTICAL);
        }
    }

    private void positionFieldDialogFooter(View content, View cancelButton, View cancelLabel,
                                          View saveButton, int anchorId, int marginDp) {
        ViewGroup root = layoutRoot(content);
        if (!(root instanceof androidx.constraintlayout.widget.ConstraintLayout)
                || cancelButton == null || saveButton == null) return;
        androidx.constraintlayout.widget.ConstraintLayout parent =
                (androidx.constraintlayout.widget.ConstraintLayout) root;

        LinearLayout footer = parent.findViewWithTag("field:footer-row");
        if (footer == null) {
            if (cancelButton.getParent() == parent) parent.removeView(cancelButton);
            if (saveButton.getParent() == parent) parent.removeView(saveButton);
            footer = new LinearLayout(activity);
            footer.setTag("field:footer-row");
            footer.setId(View.generateViewId());
            footer.setOrientation(LinearLayout.HORIZONTAL);
            footer.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(0, dp(activity, 44), 1f);
            footer.addView(cancelButton, buttonParams);
            View gap = new View(activity);
            footer.addView(gap, new LinearLayout.LayoutParams(dp(activity, 12), 1));
            footer.addView(saveButton, new LinearLayout.LayoutParams(0, dp(activity, 44), 1f));

            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams footerParams =
                    new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(0, dp(activity, 44));
            footerParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            footerParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            footerParams.topToBottom = anchorId;
            footerParams.setMarginStart(dp(activity, 35));
            footerParams.setMarginEnd(dp(activity, 35));
            parent.addView(footer, footerParams);

            if (cancelLabel != null && cancelLabel.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams labelParams =
                        (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) cancelLabel.getLayoutParams();
                labelParams.width = 0;
                labelParams.height = 0;
                labelParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                labelParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                labelParams.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                labelParams.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                labelParams.leftMargin = labelParams.rightMargin = labelParams.topMargin = labelParams.bottomMargin = 0;
                cancelLabel.setLayoutParams(labelParams);
                if (cancelLabel instanceof TextView) ((TextView) cancelLabel).setGravity(Gravity.CENTER);
            }
        }

        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams footerParams =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) footer.getLayoutParams();
        footerParams.topToTop = -1;
        footerParams.topToBottom = anchorId;
        footerParams.bottomToBottom = -1;
        footerParams.topMargin = dp(activity, marginDp);
        footerParams.bottomMargin = 0;
        footer.setLayoutParams(footerParams);

        View bottomSpacer = parent.findViewWithTag("field:option-footer-spacer");
        if (bottomSpacer == null) {
            bottomSpacer = new View(activity);
            bottomSpacer.setTag("field:option-footer-spacer");
            bottomSpacer.setId(View.generateViewId());
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams spacerParams =
                    new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(0, dp(activity, 32));
            spacerParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            spacerParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            spacerParams.topToBottom = footer.getId();
            parent.addView(bottomSpacer, spacerParams);
        }
        bottomSpacer.setVisibility(View.VISIBLE);
    }

    private boolean isOptionType(String typeName) {
        try {
            TipoCampoChecklist type = TipoCampoChecklist.fromApi(typeName);
            return type == TipoCampoChecklist.RADIO || type == TipoCampoChecklist.CHECK;
        } catch (Exception ignored) { return false; }
    }

    private final class OptionDraftAdapter extends RecyclerView.Adapter<OptionDraftAdapter.OptionHolder> {
        private final List<ChecklistOption> values = new ArrayList<>();
        private ItemTouchHelper touchHelper;
        OptionDraftAdapter(List<ChecklistOption> initial) { values.addAll(initial); }
        void setTouchHelper(ItemTouchHelper helper) { touchHelper = helper; }
        void addOption(String value) {
            values.add(new ChecklistOption(UUID.randomUUID().toString(), value, false));
            notifyItemInserted(values.size() - 1);
        }
        List<ChecklistOption> optionValues() { return new ArrayList<>(values); }
        void move(int from, int to) { Collections.swap(values, from, to); notifyItemMoved(from, to); }
        @Override public OptionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout row = new LinearLayout(activity);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(activity, 8), 0, dp(activity, 8), 0);
            row.setBackgroundResource(R.drawable.bg_ops_70aaab1e67);
            TextView handle = new TextView(activity);
            handle.setText("≡");
            handle.setTextSize(21);
            handle.setGravity(Gravity.CENTER);
            handle.setContentDescription("Reordenar opção");
            row.addView(handle, new LinearLayout.LayoutParams(dp(activity, 36), -1));
            EditText input = new EditText(activity);
            input.setSingleLine(true);
            input.setHint("Título da opção");
            input.setTextSize(14);
            input.setIncludeFontPadding(true);
            input.setPadding(dp(activity, 8), 0, dp(activity, 8), 0);
            input.setBackgroundColor(Color.TRANSPARENT);
            row.addView(input, new LinearLayout.LayoutParams(0, -1, 1));
            TextView remove = new TextView(activity);
            remove.setText("×");
            remove.setTextSize(22);
            remove.setGravity(Gravity.CENTER);
            remove.setContentDescription("Remover opção");
            row.addView(remove, new LinearLayout.LayoutParams(dp(activity, 34), -1));
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(-1, dp(activity, 48));
            params.bottomMargin = dp(activity, 8);
            row.setLayoutParams(params);
            return new OptionHolder(row, handle, input, remove);
        }
        @Override public void onBindViewHolder(OptionHolder holder, int position) {
            if (holder.watcher != null) holder.input.removeTextChangedListener(holder.watcher);
            holder.input.setText(values.get(position).nome == null ? "" : values.get(position).nome);
            holder.watcher = new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    int current = holder.getBindingAdapterPosition();
                    if (current != RecyclerView.NO_POSITION && current < values.size()) values.get(current).nome = s.toString();
                }
                @Override public void afterTextChanged(android.text.Editable s) { }
            };
            holder.input.addTextChangedListener(holder.watcher);
            holder.handle.setOnTouchListener((view, event) -> {
                if (event.getActionMasked() == android.view.MotionEvent.ACTION_DOWN && touchHelper != null) {
                    touchHelper.startDrag(holder);
                    return true;
                }
                return false;
            });
            holder.remove.setOnClickListener(v -> {
                int current = holder.getBindingAdapterPosition();
                if (current != RecyclerView.NO_POSITION) {
                    values.remove(current);
                    notifyItemRemoved(current);
                }
            });
        }
        @Override public int getItemCount() { return values.size(); }
        final class OptionHolder extends RecyclerView.ViewHolder {
            final View handle; final EditText input; final View remove;
            android.text.TextWatcher watcher;
            OptionHolder(View row, View handle, EditText input, View remove) {
                super(row); this.handle = handle; this.input = input; this.remove = remove;
            }
        }
    }
    private static void resizeView(View view, int height) {
        if (view == null || view.getLayoutParams() == null) return;
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        view.setLayoutParams(params);
    }

    private static void moveConstraintTopMargin(View view, int margin) {
        if (view == null || !(view.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) return;
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) view.getLayoutParams();
        params.topMargin = margin;
        view.setLayoutParams(params);
    }

    private SwitchCompat replaceRequiredSwitch(View content, int switchId, boolean checked) {
        View original = content.findViewById(switchId);
        if (original instanceof SwitchCompat) {
            SwitchCompat existing = (SwitchCompat) original;
            existing.setChecked(checked);
            return existing;
        }
        if (original == null || !(original.getParent() instanceof ViewGroup)) return null;
        ViewGroup parent = (ViewGroup) original.getParent();
        ViewGroup.LayoutParams originalParams = original.getLayoutParams();
        SwitchCompat toggle = new SwitchCompat(activity);
        toggle.setId(switchId);
        toggle.setShowText(false);
        toggle.setTextOn("");
        toggle.setTextOff("");
        toggle.setChecked(checked);
        toggle.setContentDescription("Campo obrigatório");
        toggle.setMinWidth(dp(activity, 42));
        toggle.setMinimumHeight(dp(activity, 28));
        if (originalParams instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) originalParams;
            params.width = dp(activity, 42);
            params.height = dp(activity, 28);
            toggle.setLayoutParams(params);
        } else toggle.setLayoutParams(originalParams);
        int index = parent.indexOfChild(original);
        parent.removeView(original);
        parent.addView(toggle, Math.max(0, index));
        return toggle;
    }

    private void bindModels(View content) {
        ChecklistTemplate template = draft();
        EditText search = content.findViewWithTag("field:Pesquisar");
        android.widget.ImageView searchIcon = content.findViewById(R.id.ops_5717_8157);
        if (searchIcon != null) {
            searchIcon.setImageResource(R.drawable.ic_search_18);
            searchIcon.setColorFilter(androidx.core.content.ContextCompat.getColor(activity, R.color.ops_color_ff0b013c));
            searchIcon.setVisibility(View.VISIBLE);
            searchIcon.bringToFront();
        }
        View back = content.findViewWithTag("text:Voltar"), next = content.findViewWithTag("action:Próximo");
        clickTag(content, "image:arrow-left", v -> activity.back());
        if (back != null) {
            back.setOnClickListener(v -> activity.back());
        }
        int[] mockIds = {R.id.ops_5717_8160, R.id.ops_5717_8173, R.id.ops_5717_8172, R.id.ops_5717_8161, R.id.ops_5717_8162, R.id.ops_5717_8163, R.id.ops_5717_8164, R.id.ops_5717_8165, R.id.ops_5717_8166, R.id.ops_5717_8167, R.id.ops_5717_8168, R.id.ops_5717_8169, R.id.ops_5717_8170, R.id.ops_5717_8171};
        for (int id : mockIds) {
            View mock = content.findViewById(id);
            if (mock != null) {
                mock.setVisibility(View.GONE);
            }
        }
        LinearLayout list = vertical();
        list.setTag("model:list");
        list.setId(View.generateViewId());
        attachBelow(content, list, R.id.ops_5717_8149, 22);
        list.setPadding(dp(activity, 30), 0, dp(activity, 30), dp(activity, 100));
        alignWizardFooter(content, back, next);
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
                                activity.model.selected("checklist:model:" + model.id, false);
                                for (ChecklistEquipment equipment : model.equipamentos) {
                                    activity.model.selected("checklist:equipment:" + equipment.id, false);
                                }
                            }
                            activity.model.field("checklist", "clearModelSelection", "false");
                        }
                        renderModels(list, search == null ? "" : search.getText().toString());
                        if (search != null) {
                            search.addTextChangedListener(new android.text.TextWatcher() {
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                }

                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    renderModels(list, s.toString());
                                }

                                public void afterTextChanged(android.text.Editable e) {
                                }
                            });
                        }
                    }

                    public void failure(Throwable error) {
                        AppLoading.hide(loading);
                        toast(AutenticacaoViewModel.messageFrom(error));
                    }
                });
            }

            public void failure(Throwable error) {
                AppLoading.hide(loading);
                toast(AutenticacaoViewModel.messageFrom(error));
            }
        });
        if (next != null) {
            next.setOnClickListener(v -> {
                List<ChecklistEquipmentModel> selected = new ArrayList<>();
                for (EquipmentModelOption model : availableModels) {
                    if (modelSelected(model)) {
                        List<ChecklistEquipment> equipment = new ArrayList<>();
                        for (ChecklistEquipment item : model.equipamentos) {
                            if (equipmentSelected(model, item)) {
                                equipment.add(item);
                            }
                        }
                        if (!equipment.isEmpty()) {
                            selected.add(new ChecklistEquipmentModel(model.id, model.nome, equipment));
                        }
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

    private boolean modelSelected(EquipmentModelOption value) {
        if (activity.model.selected("checklist:model:" + value.id)) {
            return true;
        }
        ChecklistTemplate current = draft();
        if (current.modelos != null) {
            for (ChecklistEquipmentModel model : current.modelos) {
                if (model.modeloEquipamentoId == value.id) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean equipmentSelected(EquipmentModelOption model, ChecklistEquipment value) {
        if (activity.model.selected("checklist:equipment:" + value.id)) {
            return true;
        }
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

    private void renderModels(LinearLayout list, String filter) {
        list.removeAllViews();
        String query = filter == null ? "" : filter.toLowerCase(Locale.ROOT);
        for (EquipmentModelOption model : availableModels) {
            boolean modelMatches = model.nome != null && model.nome.toLowerCase(Locale.ROOT).contains(query);
            boolean equipmentMatches = false;
            if (!modelMatches && !query.isEmpty() && model.equipamentos != null) {
                for (ChecklistEquipment equipment : model.equipamentos) {
                    if (equipment.codigo != null && equipment.codigo.toLowerCase(Locale.ROOT).contains(query)) {
                        equipmentMatches = true;
                        break;
                    }
                }
            }
            if (!modelMatches && !equipmentMatches) {
                continue;
            }

            LinearLayout modelGroup = new LinearLayout(activity);
            modelGroup.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams groupParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            groupParams.bottomMargin = dp(activity, 12);
            modelGroup.setLayoutParams(groupParams);

            CheckBox modelBox = new CheckBox(activity);
            modelBox.setText(model.nome == null ? "" : model.nome);
            modelBox.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16);
            modelBox.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(activity, R.font.poppins_semibold));
            modelBox.setMaxLines(1);
            modelBox.setEllipsize(android.text.TextUtils.TruncateAt.END);
            modelBox.setChecked(modelSelected(model));
            modelGroup.addView(modelBox, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            modelBox.setOnCheckedChangeListener((button, checked) -> {
                activity.model.selected("checklist:model:" + model.id, checked);
                for (int i = 1; i < modelGroup.getChildCount(); i++) {
                    View row = modelGroup.getChildAt(i);
                    row.setVisibility(checked ? View.VISIBLE : View.GONE);
                }
            });

            if (model.equipamentos != null) for (ChecklistEquipment equipment : model.equipamentos) {
                if (!modelMatches && !query.isEmpty()
                        && (equipment.codigo == null || !equipment.codigo.toLowerCase(Locale.ROOT).contains(query))) {
                    continue;
                }
                CheckBox equipmentBox = new CheckBox(activity);
                equipmentBox.setText(equipment.codigo == null ? "" : equipment.codigo);
                equipmentBox.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 14);
                equipmentBox.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(activity, R.font.poppins_regular));
                equipmentBox.setMaxLines(1);
                equipmentBox.setEllipsize(android.text.TextUtils.TruncateAt.END);
                LinearLayout.LayoutParams equipmentParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                equipmentParams.setMarginStart(dp(activity, 28));
                modelGroup.addView(equipmentBox, equipmentParams);
                equipmentBox.setChecked(equipmentSelected(model, equipment));
                equipmentBox.setVisibility(modelBox.isChecked() ? View.VISIBLE : View.GONE);
                equipmentBox.setOnCheckedChangeListener((button, checked) -> activity.model.selected("checklist:equipment:" + equipment.id, checked));
            }
            list.addView(modelGroup);
        }
        if (list.getChildCount() == 0) {
            TextView empty = text("Não existem modelos disponíveis.", 15, false);
            list.addView(empty);
        }
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
        final String[] statusFilter = {"Todos"};
        loadChecklistEquipment(checklistId, adapter, empty, statusFilter, search.getText().toString());
        statusDropdown.setOnItemClickListener((parent, view, position, id) -> {
            statusFilter[0] = String.valueOf(parent.getItemAtPosition(position));
            loadChecklistEquipment(checklistId, adapter, empty, statusFilter, search.getText().toString());
        });
        search.addTextChangedListener(watcher(value -> loadChecklistEquipment(checklistId, adapter, empty, statusFilter, value)));
    }

    private void loadChecklistEquipment(String checklistId, InspectionEquipmentAdapter adapter, View empty, String[] statusFilter, String query) {
        String cacheKey = checklistId;
        if (inspectionEquipmentCache.containsKey(cacheKey)) {
            renderEquipment(inspectionEquipmentCache.get(cacheKey), adapter, empty, statusFilter, query);
            return;
        }
        Dialog loading = AppLoading.show(activity);
        vm.modelos(checklistId, new ChecklistViewModel.Callback<List<ChecklistEquipmentModel>>() {
            public void success(List<ChecklistEquipmentModel> models) {
                AppLoading.hide(loading);
                List<ChecklistEquipment> equipmentList = new ArrayList<>();
                if (models != null) {
                    for (ChecklistEquipmentModel model : models) {
                        if (model != null && model.equipamentos != null) {
                            for (ChecklistEquipment equipment : model.equipamentos) {
                                if (equipment != null) {
                                    equipment.modeloNome = model.nome;
                                    equipmentList.add(equipment);
                                }
                            }
                        }
                    }
                }
                inspectionEquipmentCache.put(cacheKey, equipmentList);
                renderEquipment(equipmentList, adapter, empty, statusFilter, query);
            }

            public void failure(Throwable error) {
                AppLoading.hide(loading);
                toast(AutenticacaoViewModel.messageFrom(error));
            }
        });
    }

    private void renderEquipment(List<ChecklistEquipment> values, InspectionEquipmentAdapter adapter, View empty, String[] statusFilter, String query) {
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
        if (empty != null) empty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
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
            title.setText("Inspecionar " + activeEquipment.codigo);
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
        answerContainer.setPadding(dp(activity, 26), 0, dp(activity, 26), dp(activity, 20));
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
        for (View action : new View[]{cancel, submit}) {
            if (action == null || !(action.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) continue;
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) action.getLayoutParams();
            params.topToTop = -1;
            params.topToBottom = fieldsId;
            params.bottomToTop = -1;
            params.bottomToBottom = -1;
            params.topMargin = dp(activity, 24);
            action.setLayoutParams(params);
        }
    }

    private void renderAnswer(ChecklistField field) {
        TextView label = text(field.nome + (Boolean.TRUE.equals(field.obrigatorio) ? " *" : ""), 16, true);
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
                input.setInputType(field.tipoCampo == TipoCampoChecklist.NUMERO ? InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                answerContainer.addView(input);
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
                rating.setMax(5);
                rating.setStepSize(1);
                answerContainer.addView(rating);
                answers.remove(field.id);
                rating.setOnRatingBarChangeListener((bar, value, user) -> {
                    if (value <= 0) answers.remove(field.id);
                    else answers.put(field.id, (int) value);
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
                inspectionEquipmentCache.clear();
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
            return value instanceof Number && ((Number) value).intValue() >= 1;
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
