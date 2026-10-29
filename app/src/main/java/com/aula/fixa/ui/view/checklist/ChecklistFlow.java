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

import com.aula.fixa.R;
import com.aula.fixa.adapter.auth.AutenticacaoViewModel;
import com.aula.fixa.adapter.checklist.ChecklistViewModel;
import com.aula.fixa.adapter.checklist.ChecklistViewModelFactory;
import com.aula.fixa.domain.enums.TipoCampoChecklist;
import com.aula.fixa.domain.model.checklist.*;
import com.aula.fixa.ui.config.AppLoading;
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
                bindInspections(content, screen);
                break;
            case "manager_inspection_form":
            case "technician_inspection_form":
                bindInspectionForm(content);
                break;
        }
    }

    private void bindChecklistList(View content, String screen) {
        boolean canManage = activity.role().equals("manager");
        View fab = content.findViewWithTag("text:+");
        if (fab == null) {
            fab = content.findViewWithTag("action:Adicionar");
        }

        if (fab != null) {
            fab.setVisibility(canManage ? View.VISIBLE : View.GONE);
            fab.setOnClickListener(v -> startNew());
        }
        loadTemplates(content, screen);
        for (View card : new View[]{content.findViewById(R.id.ops_5717_7973), content.findViewById(R.id.ops_5717_7988), content.findViewById(R.id.ops_5717_10260), content.findViewById(R.id.ops_5717_10275)}) {
            if (card != null) {
                card.setVisibility(View.GONE);
            }
        }
        // Retain the existing card geometry/background while filling server data.
        ViewGroup root = layoutRoot(content);
        LinearLayout list = new LinearLayout(activity);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setTag("checklist:cards");
        attachBelow(content, list, screen.startsWith("technician") ? R.id.ops_5717_10217 : R.id.ops_5717_7930, 18);
        list.setPadding(dp(activity, 24), 0, dp(activity, 24), dp(activity, 100));
        content.setTag(R.id.ops_5717_7930, list);
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
            LinearLayout card = card(160, R.drawable.bg_ops_e04d888c51);
            card.setPadding(dp(activity, 18), dp(activity, 12), dp(activity, 14), dp(activity, 10));
            TextView title = text(template.nome, 18, true);
            title.setMaxLines(1);
            title.setEllipsize(android.text.TextUtils.TruncateAt.END);
            TextView menu = text("⋮", 25, true);
            LinearLayout header = row(title, menu);
            card.addView(header);
            TextView category = text(modelNames(template), 13, false);
            card.addView(category);
            TextView periodic = text("Periodicidade   " + (template.periodicidade == null ? "_" : template.periodicidade + " dias"), 13, false);
            card.addView(periodic);
            TextView status = text(status(template), 13, true);
            card.addView(status);
            TextView count = text("Modelos   " + (template.modelos == null ? 0 : template.modelos.size()), 13, false);
            card.addView(count);
            card.setOnClickListener(v -> selectChecklist(template));
            if (activity.role().equals("manager")) {
                menu.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(activity, menu);
                    popup.getMenu().add("Editar");
                    popup.getMenu().add("Excluir");
                    popup.setOnMenuItemClickListener(item -> {
                        if ("Editar".contentEquals(item.getTitle())) {
                            edit(template);
                        } else {
                            confirmDelete(template);
                        }
                        return true;
                    });
                    popup.show();
                });
            } else {
                menu.setVisibility(View.GONE);
            }
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, dp(activity, 160));
            cp.bottomMargin = dp(activity, 14);
            list.addView(card, cp);
        }
        if (templates.isEmpty()) {
            TextView empty = text("Não existem checklists cadastrados.", 16, false);
            empty.setGravity(Gravity.CENTER);
            list.addView(empty, new LinearLayout.LayoutParams(-1, dp(activity, 120)));
        }
    }

    private String modelNames(ChecklistTemplate t) {
        if (t.modelos == null || t.modelos.isEmpty()) {
            return "Modelos de equipamento";
        }
        List<String> names = new ArrayList<>();
        for (ChecklistEquipmentModel m : t.modelos) {
            names.add(m.nome);
        }
        return android.text.TextUtils.join(", ", names);
    }

    private String status(ChecklistTemplate t) {
        if (t.modelos == null || t.modelos.isEmpty()) {
            return "Em andamento";
        }
        for (ChecklistEquipmentModel m : t.modelos) {
            if (m.equipamentos != null) {
                for (ChecklistEquipment e : m.equipamentos) {
                    if (!isCompleted(e)) {
                        return "Em andamento";
                    }
                }
            }
        }
        return "Concluído";
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
            date.setOnClickListener(v -> chooseDate(date));
        }
        clickTag(content, "image:arrow-left", v -> activity.back());
        clickTag(content, "text:Voltar", v -> activity.back());
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
            LocalDate d = LocalDate.parse(target.getText().toString());
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
        LinearLayout list = vertical();
        list.setTag("field:list");
        list.setId(View.generateViewId());
        attachBelow(content, list, R.id.ops_5717_8041, 12);
        list.setPadding(dp(activity, 34), 0, dp(activity, 34), 0);
        renderFields(list, template);
        setBelow(content, add, list.getId(), 16);
        if (add != null) {
            add.setOnClickListener(v -> showFieldDialog(null, -1));
        }
        if (addLabel != null) {
            addLabel.setOnClickListener(v -> showFieldDialog(null, -1));
        }
        clickTag(content, "image:arrow-left", v -> activity.back());
        clickTag(content, "text:Voltar", v -> activity.back());
        setBelow(content, content.findViewWithTag("action:Próximo"), R.id.ops_5717_8078, 24);
        setBelow(content, content.findViewWithTag("text:Voltar"), R.id.ops_5717_8078, 24);
        clickTag(content, "action:Próximo", v -> {
            if (template.campos == null || template.campos.isEmpty()) {
                toast("Adicione ao menos um campo de inspeção.");
                return;
            }
            saveDraft(template);
            activity.open("manager_checklist_models");
        });
    }

    private void renderFields(LinearLayout list, ChecklistTemplate t) {
        list.removeAllViews();
        if (t.campos == null) {
            return;
        }
        for (int i = 0; i < t.campos.size(); i++) {
            final int index = i;
            ChecklistField f = t.campos.get(i);
            LinearLayout card = card(70, R.drawable.bg_ops_892fe1afbe);
            card.setPadding(dp(activity, 14), dp(activity, 10), dp(activity, 12), dp(activity, 8));
            TextView label = text(f.nome, 16, true), meta = text(f.tipoCampo.nome + (Boolean.TRUE.equals(f.obrigatorio) ? " · Obrigatório" : " · Opcional"), 12, false), menu = text("⋮", 23, true);
            LinearLayout details = vertical();
            details.addView(label);
            details.addView(meta);
            LinearLayout line = row(details, menu);
            card.addView(line);
            menu.setOnClickListener(v -> {
                PopupMenu p = new PopupMenu(activity, menu);
                p.getMenu().add("Editar");
                p.getMenu().add("Excluir");
                p.getMenu().add("↑ Mover para cima");
                p.getMenu().add("↓ Mover para baixo");
                p.setOnMenuItemClickListener(item -> {
                    String a = item.getTitle().toString();
                    if (a.startsWith("Editar")) {
                        showFieldDialog(f, index);
                    } else if (a.startsWith("Excluir")) {
                        t.campos.remove(index);
                        saveDraft(t);
                        renderFields(list, t);
                    } else {
                        int dest = a.startsWith("↑") ? index - 1 : index + 1;
                        if (dest >= 0 && dest < t.campos.size()) {
                            Collections.swap(t.campos, index, dest);
                            saveDraft(t);
                            renderFields(list, t);
                        }
                    }
                    return true;
                });
                p.show();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(activity, 70));
            lp.bottomMargin = dp(activity, 10);
            list.addView(card, lp);
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
            try {
                old = flow.gson.fromJson(args.getString("field"), ChecklistField.class);
            } catch (Exception ignored) {
            }
        }
        final ChecklistField editing = old;
        boolean choiceDialog = key.equals("manager_create_choice_field");
        int nameId = key.equals("manager_edit_field") ? R.id.ops_5717_8180 : choiceDialog ? R.id.ops_5717_8216 : R.id.ops_5717_8198;
        EditText name = content.findViewById(nameId);
        if (name != null) {
            if (editing != null) {
                name.setText(editing.nome);
            } else if (args != null && args.getString("fieldName") != null) {
                name.setText(args.getString("fieldName"));
            }
        }
        int typeId = key.equals("manager_edit_field") ? R.id.ops_5717_8183_input : choiceDialog ? R.id.ops_5717_8219_input : R.id.ops_5717_8201_input;
        TextView type = content.findViewById(typeId);
        String initialType = args == null ? null : args.getString("fieldType");
        if (type != null) {
            type.setText(editing != null ? editing.tipoCampo.nome : choiceDialog ? (initialType == null ? TipoCampoChecklist.CHECK.nome : initialType) : "Selecione um tipo de campo");
        }
        TextView requiredValue = content.findViewWithTag("text:Sim");
        final boolean[] required = {editing != null && Boolean.TRUE.equals(editing.obrigatorio) || (args != null && args.getBoolean("required", false))};
        if (requiredValue != null) {
            requiredValue.setText(required[0] ? "Sim" : "Não");
            requiredValue.setOnClickListener(v -> {
                required[0] = !required[0];
                requiredValue.setText(required[0] ? "Sim" : "Não");
            });
        }
        LinearLayout optionList = flow.vertical();
        List<EditText> optionInputs = new ArrayList<>();
        if (editing != null && editing.opcoes != null) {
            for (ChecklistOption option : editing.opcoes) {
                addOptionInput(activity, optionList, optionInputs, option.nome);
            }
        } else if (choiceDialog) {
            addOptionInput(activity, optionList, optionInputs, "");
        }
        if (choiceDialog || editing != null && editing.opcoes != null && !editing.opcoes.isEmpty()) {
            ViewGroup root = flow.layoutRoot(content);
            int optionsHeader = choiceDialog ? R.id.ops_5717_8225 : R.id.ops_5717_8183;
            int footerId = choiceDialog ? R.id.ops_5717_8213 : R.id.ops_5717_8177;
            optionList.setId(View.generateViewId());
            flow.attachBelow(content, optionList, optionsHeader, 4);
            flow.setBelow(content, content.findViewById(footerId), optionList.getId(), 18);
            View addOption = choiceDialog ? content.findViewWithTag("text:+ Adicionar Campo") : null;
            if (addOption != null) {
                View addOptionCard = content.findViewById(R.id.ops_5717_8223);
                flow.setBelow(content, addOptionCard, optionList.getId(), 8);
                flow.setBelow(content, content.findViewById(footerId), addOptionCard == null ? optionList.getId() : addOptionCard.getId(), 18);
                addOption.setOnClickListener(v -> addOptionInput(activity, optionList, optionInputs, ""));
            }
            TextView addMore = flow.text("+ Adicionar opção", 13, true);
            optionList.addView(addMore);
            addMore.setOnClickListener(v -> addOptionInput(activity, optionList, optionInputs, ""));
        }
        if (type != null) {
            type.setOnClickListener(v -> {
                String[] labels = new String[TipoCampoChecklist.values().length];
                for (int i = 0; i < labels.length; i++) {
                    labels[i] = TipoCampoChecklist.values()[i].nome;
                }
                new AlertDialog.Builder(activity).setItems(labels, (dialog, which) -> {
                    TipoCampoChecklist selected = TipoCampoChecklist.values()[which];
                    if (key.equals("manager_create_field") && (selected == TipoCampoChecklist.RADIO || selected == TipoCampoChecklist.CHECK)) {
                        Bundle next = new Bundle();
                        next.putString("fieldName", name == null ? "" : name.getText().toString());
                        next.putString("fieldType", selected.nome);
                        next.putBoolean("required", required[0]);
                        ((androidx.fragment.app.DialogFragment) owner).dismiss();
                        com.aula.fixa.ui.view.operations.OperationsDialogFragment.create("manager_create_choice_field", next).show(activity.getSupportFragmentManager(), "ops_choice_field_dialog");
                    } else {
                        type.setText(selected.nome);
                    }
                }).show();
            });
        }
        View cancel = content.findViewWithTag("text:Cancelar"), save = content.findViewWithTag("action:Salvar");
        if (cancel != null) {
            cancel.setOnClickListener(v -> ((androidx.fragment.app.DialogFragment) owner).dismiss());
        }
        if (save != null) {
            save.setOnClickListener(v -> {
                if (name == null || name.getText().toString().trim().isEmpty()) {
                    if (name != null) {
                        name.setError("Informe o nome do campo.");
                    }
                    return;
                }
                if (type == null || type.getText().toString().startsWith("Selecione")) {
                    Toast.makeText(activity, "Selecione o tipo de campo.", Toast.LENGTH_SHORT).show();
                    return;
                }
                TipoCampoChecklist selected;
                try {
                    selected = TipoCampoChecklist.fromApi(type.getText().toString());
                } catch (Exception invalid) {
                    Toast.makeText(activity, "Selecione o tipo de campo.", Toast.LENGTH_SHORT).show();
                    return;
                }
                ChecklistField field = editing == null ? new ChecklistField(UUID.randomUUID().toString(), name.getText().toString().trim(), selected, required[0], new ArrayList<>()) : editing;
                field.nome = name.getText().toString().trim();
                field.tipoCampo = selected;
                field.obrigatorio = required[0];
                field.opcoes = new ArrayList<>();
                if (selected == TipoCampoChecklist.RADIO || selected == TipoCampoChecklist.CHECK) {
                    for (int i = 0; i < optionInputs.size(); i++) {
                        String value = optionInputs.get(i).getText().toString().trim();
                        if (!value.isEmpty()) {
                            String optionId = editing != null && editing.opcoes != null && i < editing.opcoes.size() ? editing.opcoes.get(i).id : UUID.randomUUID().toString();
                            field.opcoes.add(new ChecklistOption(optionId, value, false));
                        }
                    }
                    if (field.opcoes.isEmpty()) {
                        Toast.makeText(activity, "Adicione ao menos uma opção.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                ChecklistTemplate template = flow.draft();
                if (template.campos == null) {
                    template.campos = new ArrayList<>();
                }
                if (editing == null) {
                    template.campos.add(field);
                } else {
                    int index = args == null ? -1 : args.getInt("index", -1);
                    if (index < 0 || index >= template.campos.size()) {
                        Toast.makeText(activity, "Campo não encontrado.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    template.campos.set(index, field);
                }
                flow.saveDraft(template);
                ((androidx.fragment.app.DialogFragment) owner).dismiss();
                activity.refresh();
            });
        }
    }

    private static void addOptionInput(Context context, LinearLayout container, List<EditText> inputs, String value) {
        EditText input = new EditText(context);
        input.setSingleLine(true);
        input.setHint("Título da opção");
        input.setText(value);
        inputs.add(input);
        container.addView(input, new LinearLayout.LayoutParams(-1, dp(context, 48)));
    }

    private void bindModels(View content) {
        ChecklistTemplate template = draft();
        EditText search = content.findViewWithTag("field:Pesquisar");
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
        attachBelow(content, list, R.id.ops_5717_8149, 12);
        list.setPadding(dp(activity, 30), 0, dp(activity, 30), dp(activity, 100));
        setBelow(content, next, list.getId(), 24);
        setBelow(content, back, list.getId(), 24);
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
            if (model.nome == null || !model.nome.toLowerCase(Locale.ROOT).contains(query)) {
                continue;
            }
            CheckBox modelBox = new CheckBox(activity);
            modelBox.setText(model.nome + " (" + model.equipamentos.size() + ")");
            modelBox.setChecked(modelSelected(model));
            list.addView(modelBox);
            modelBox.setOnCheckedChangeListener((button, checked) -> {
                activity.model.selected("checklist:model:" + model.id, checked);
                for (int i = 0; i < list.getChildCount(); i++) {
                    View row = list.getChildAt(i);
                    if (("equipment:" + model.id).equals(row.getTag())) {
                        row.setVisibility(checked ? View.VISIBLE : View.GONE);
                    }
                }
            });
            for (ChecklistEquipment equipment : model.equipamentos) {
                CheckBox equipmentBox = new CheckBox(activity);
                equipmentBox.setText(equipment.codigo);
                equipmentBox.setTag("equipment:" + model.id);
                equipmentBox.setChecked(equipmentSelected(model, equipment));
                equipmentBox.setVisibility(modelBox.isChecked() ? View.VISIBLE : View.GONE);
                equipmentBox.setOnCheckedChangeListener((button, checked) -> activity.model.selected("checklist:equipment:" + equipment.id, checked));
                list.addView(equipmentBox);
            }
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

    private void bindInspections(View content, String screen) {
        String checklistId = activity.model.field("checklist", "checklistId");
        if (checklistId == null || checklistId.isEmpty()) {
            toast("Selecione um checklist.");
            activity.back();
            return;
        }
        boolean tech = screen.startsWith("technician");
        int[] mockIds = {R.id.ops_5717_8358, R.id.ops_5717_8368, R.id.ops_5717_8378, R.id.ops_5717_8388, R.id.ops_5717_10337, R.id.ops_5717_10347, R.id.ops_5717_10357, R.id.ops_5717_10367, R.id.ops_5717_8314, R.id.ops_5717_8315, R.id.ops_5717_10293, R.id.ops_5717_10294};
        for (int id : mockIds) {
            View mock = content.findViewById(id);
            if (mock != null) {
                mock.setVisibility(View.GONE);
            }
        }
        TextView modelLabel = content.findViewWithTag("text:Modelo"), statusLabel = content.findViewWithTag("text:Status");
        LinearLayout list = vertical();
        attachBelow(content, list, tech ? R.id.ops_5717_10328 : R.id.ops_5717_8349, 12);
        list.setPadding(dp(activity, 30), 0, dp(activity, 30), dp(activity, 100));
        final List<ChecklistEquipmentModel>[] models = new List[]{new ArrayList<>()};
        final long[] selectedModel = {-1};
        final String[] statusFilter = {"Todos"};
        int modelControl = tech ? R.id.ops_5717_10327 : R.id.ops_5717_8348, statusControl = tech ? R.id.ops_5717_10328 : R.id.ops_5717_8349;
        View modelBox = content.findViewById(modelControl), statusBox = content.findViewById(statusControl);
        Dialog loading = AppLoading.show(activity);
        vm.modelos(checklistId, new ChecklistViewModel.Callback<List<ChecklistEquipmentModel>>() {
            public void success(List<ChecklistEquipmentModel> result) {
                AppLoading.hide(loading);
                models[0] = result == null ? new ArrayList<>() : result;
                if (models[0].isEmpty()) {
                    TextView empty = text("Não existem modelos para este checklist.", 15, false);
                    list.addView(empty);
                    return;
                }
                selectedModel[0] = models[0].get(0).modeloEquipamentoId;
                if (modelLabel != null) {
                    modelLabel.setText(models[0].get(0).nome);
                }
                activity.model.field("checklist", "equipmentModelName", models[0].get(0).nome);
                fetchEquipment(checklistId, selectedModel[0], list, statusFilter);
            }

            public void failure(Throwable error) {
                AppLoading.hide(loading);
                toast(AutenticacaoViewModel.messageFrom(error));
            }
        });
        if (modelBox != null) {
            modelBox.setOnClickListener(v -> {
                String[] names = new String[models[0].size()];
                for (int i = 0; i < names.length; i++) {
                    names[i] = models[0].get(i).nome;
                }
                new AlertDialog.Builder(activity).setItems(names, (dialog, which) -> {
                    selectedModel[0] = models[0].get(which).modeloEquipamentoId;
                    if (modelLabel != null) {
                        modelLabel.setText(names[which]);
                    }
                    activity.model.field("checklist", "equipmentModelName", names[which]);
                    fetchEquipment(checklistId, selectedModel[0], list, statusFilter);
                }).show();
            });
        }
        if (statusBox != null) {
            statusBox.setOnClickListener(v -> new AlertDialog.Builder(activity).setItems(new String[]{"Todos", "Pendente", "Realizada"}, (dialog, which) -> {
                statusFilter[0] = new String[]{"Todos", "Pendente", "Realizada"}[which];
                if (statusLabel != null) {
                    statusLabel.setText(statusFilter[0]);
                }
                fetchEquipment(checklistId, selectedModel[0], list, statusFilter);
            }).show());
        }
    }

    private void fetchEquipment(String checklistId, long modelId, LinearLayout list, String[] statusFilter) {
        if (modelId <= 0) {
            return;
        }
        Dialog loading = AppLoading.show(activity);
        vm.equipamentos(checklistId, modelId, new ChecklistViewModel.Callback<List<ChecklistEquipment>>() {
            public void success(List<ChecklistEquipment> values) {
                AppLoading.hide(loading);
                list.removeAllViews();
                if (values != null) {
                    for (ChecklistEquipment equipment : values) {
                        boolean done = isCompleted(equipment);
                        if (!statusFilter[0].equals("Todos") && ((done && !statusFilter[0].equals("Realizada")) || (!done && !statusFilter[0].equals("Pendente")))) {
                            continue;
                        }
                        LinearLayout card = card(100, R.drawable.bg_ops_9a20d877fd);
                        card.setPadding(dp(activity, 18), dp(activity, 14), dp(activity, 14), dp(activity, 12));
                        card.addView(text(equipment.codigo, 18, true));
                        card.addView(text(done ? "Inspeção realizada" : "Inspeção pendente", 14, false));
                        if (equipment.prazoInspecao != null) {
                            card.addView(text("Prazo: " + equipment.prazoInspecao, 12, false));
                        }
                        card.setAlpha(done ? .58f : 1f);
                        if (!done) {
                            card.setOnClickListener(v -> openInspection(equipment));
                        }
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(activity, 100));
                        params.bottomMargin = dp(activity, 10);
                        list.addView(card, params);
                    }
                }
                if (list.getChildCount() == 0) {
                    TextView empty = text("Não existem equipamentos neste status.", 15, false);
                    list.addView(empty);
                }
            }

            public void failure(Throwable error) {
                AppLoading.hide(loading);
                toast(AutenticacaoViewModel.messageFrom(error));
            }
        });
    }

    private void openInspection(ChecklistEquipment equipment) {
        String json = activity.model.field("checklist", "selectedTemplate");
        activeTemplate = json == null ? null : gson.fromJson(json, ChecklistTemplate.class);
        activity.model.field("checklist", "equipmentId", String.valueOf(equipment.id));
        activity.model.field("checklist", "equipmentCode", equipment.codigo);
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
        setBelow(content, footer, answerContainer.getId(), 22);
        setBelow(content, content.findViewWithTag("action:Enviar"), answerContainer.getId(), 22);
        clickTag(content, "image:arrow-left", v -> activity.back());
        clickTag(content, "text:Cancelar", v -> activity.back());
        clickTag(content, "action:Enviar", v -> submitInspection());
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
                    if (field.tipoCampo == TipoCampoChecklist.NUMERO && value.trim().isEmpty()) {
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
                rating.setStepSize(1);
                answerContainer.addView(rating);
                answers.remove(field.id);
                rating.setOnRatingBarChangeListener((bar, value, user) -> answers.put(field.id, (int) value));
                break;
            }
        }
    }

    private void submitInspection() {
        if (activeTemplate == null || activeEquipment == null) {
            return;
        }
        for (ChecklistField field : activeTemplate.campos) {
            if (field.tipoCampo == TipoCampoChecklist.NUMERO && answers.get(field.id) instanceof String) {
                try {
                    answers.put(field.id, Double.valueOf((String) answers.get(field.id)));
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
        com.aula.fixa.ui.view.operations.OperationsDialogFragment.create(field == null ? "manager_create_field" : "manager_edit_field", args).show(activity.getSupportFragmentManager(), "ops_dialog");
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
