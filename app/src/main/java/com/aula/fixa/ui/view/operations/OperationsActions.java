package com.aula.fixa.ui.view.operations;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.view.ViewGroup;
import android.content.res.ColorStateList;
import android.graphics.Color;
import androidx.core.view.ViewCompat;

import com.aula.fixa.R;

import androidx.lifecycle.ViewModelProvider;

import com.aula.fixa.adapter.auth.SessaoViewModel;
import com.aula.fixa.adapter.auth.SessaoViewModelFactory;
import com.aula.fixa.adapter.operations.OperationsViewModel;
import com.aula.fixa.ui.config.operations.ScreenCatalog;
import com.aula.fixa.ui.view.auth.LoginActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

/**
 * Liga os controles do protótipo aos fluxos; regras separadas da renderização XML.
 */
public final class OperationsActions {
    private final OperationsActivity activity;
    private final OperationsBinder binder;
    private final String key, name;
    private final OperationsDialogFragment dialog;

    public OperationsActions(OperationsActivity activity, OperationsBinder binder, String key, OperationsDialogFragment dialog) {
        this.activity = activity;
        this.binder = binder;
        this.key = key;
        this.dialog = dialog;
        name = ScreenCatalog.get(activity).screen(key).optString("name");
    }

    private boolean has(String target) {
        return ScreenCatalog.get(activity).all().containsKey(activity.role() + "_" + target);
    }

    private void open(String target) {
        if (!has(target)) {
            return;
        }
        if (dialog != null) {
            dialog.dismissNow();
        }
        activity.openName(target);
    }

    private void back() {
        if (dialog != null) {
            dialog.dismiss();
        } else {
            activity.back();
        }
    }

    public Runnable action(JSONObject item, View view) {
        String text = item.optString("text"), kind = item.optString("kind"), asset = item.optString("asset"), icon = item.optString("name");
        if (asset.equals("eee40.svg") || icon.equals("arrow-left")) {
            return this::back;
        }
        if (asset.equals("76941.svg")) {
            return () -> open("notifications");
        }
        if (asset.equals("97246.svg")) {
            return () -> open("profile");
        }
        if (key.equals("manager_home") && item.optString("id").equals("ops_5717_7336")) {
            return () -> open("profile");
        }
        if (asset.equals("58b2a.svg") && item.optJSONArray("bounds").optDouble(0) > 200) {
            return this::create;
        }
        if (asset.equals("e9f2c.svg")) {
            return () -> open("edit_profile");
        }
        if (icon.contains("menu")) {
            return () -> open("chat_drawer");
        }
        if (kind.equals("card")) {
            if (name.equals("export") && java.util.Arrays.asList(
                    "Solicitações", "Ordens de serviço", "Ocorrências", "Checklists").contains(text)) {
                return () -> selectExportOption(view, text);
            }
            switch (name) {
                case "home":
                    return () -> open(text.contains("Manu") ? "chatbot" : activity.role().equals("manager") ? "planning" : "execution_pending");
                case "management":
                    return () -> menu(text);
                case "equipment":
                    return () -> menu(text);
                case "categories":
                    return () -> open("models");
                case "models":
                    return () -> open("devices");
                case "devices":
                    return () -> open("edit_device");
                case "checklists":
                    return () -> open("inspections");
                case "inspections":
                    return () -> open("inspection_form");
                case "requests":
                    return () -> open("request_details");
                case "planning":
                    return () -> open("planning_intro");
                case "occurrences":
                    return () -> open("occurrence_details");
                case "team":
                    return () -> open("member_details");
                case "member_details":
                    return () -> open("execution_pending");
                case "board":
                    return () -> open(activity.role().equals("manager") && text.contains("Ninguém") ? "assign_member" : "execution_pending");
                case "planning_tasks":
                    return () -> open("edit_task");
                case "checklist_fields":
                    return () -> open("edit_field");
                case "checklist_models":
                    return () -> select(view, "modelo:" + text);
                case "execution_pending":
                case "execution_completed":
                    return () -> open("task_pending");
                case "events":
                    return () -> open("event_details");
                case "assign_member":
                    return () -> select(view, "responsavel");
            }
        }
        if (kind.equals("image")) {
            double x = item.optJSONArray("bounds").optDouble(0), y = item.optJSONArray("bounds").optDouble(1), w = item.optJSONArray("bounds").optDouble(2);
            if (x > 310 && y > 100 && w <= 26 && has("edit_" + singular())) {
                return () -> {
                    selectAncestor(view);
                    open("edit_" + singular());
                };
            }
        }
        if (!kind.equals("text") && !kind.equals("button")) {
            return null;
        }
        if (key.equals("manager_assign_member") && text.trim().equals("Finalizar")) {
            return () -> {
                if (activity.model.field("plan_information", "tecnicoId") == null) {
                    android.widget.Toast.makeText(activity, "Selecione um técnico para continuar.",
                            android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                activity.openPlanningStep("planning_success");
            };
        }
        if (name.equals("export") && java.util.Arrays.asList("Solicitações", "Ordens de serviço", "Ocorrências", "Checklists").contains(text)) {
            return () -> selectExportOption(view, text);
        }
        if (text.contains("Teve algum impedimento?")) {
            return () -> open(name.startsWith("task_completed") ? "task_completed_obstacle" : "task_pending_obstacle");
        }
        switch (text.trim()) {
            case "Voltar":
                return this::back;
            case "Cancelar":
                return this::back;
            case "Continuar":
            case "Próximo":
                return this::next;
            case "Registrar":
                return () -> {
                    if (binder.validate()) {
                        open("occurrence_success");
                    }
                };
            case "Ir para a Home":
                return () -> open("home");
            case "Ir para o quadro":
                return () -> open("board");
            case "Registrar novo problema":
                return () -> open("occurrence_intro");
            case "Aprovar":
                return () -> open("approve_request");
            case "Recusar":
                return () -> open(name.equals("reject_request") ? "request_rejected" : "reject_request");
            case "Confirmar":
                return this::confirm;
            case "Planejar OS":
                return () -> open("planning_intro");
            case "Enviar":
                return () -> {
                    if (binder.validate()) {
                        open("send_inspection");
                    }
                };
            case "Finalizar":
                return () -> open(name.equals("assign_date") ? "confirm_assignment" : "finish_order");
            case "Concluir tarefa":
                return () -> {
                    activity.model.selected("task:" + activity.model.selectedId(), true);
                    activity.refresh();
                    open(name.endsWith("obstacle") ? "task_completed_obstacle" : "task_completed");
                };
            case "Desfazer tarefa":
                return () -> {
                    activity.model.selected("task:" + activity.model.selectedId(), false);
                    activity.refresh();
                    open(name.endsWith("obstacle") ? "task_pending_obstacle" : "task_pending");
                };
            case "Salvar":
            case "Criar":
                return this::save;
            case "Excluir":
                return () -> {
                    if (name.startsWith("delete_")) {
                        activity.model.deleteEntry(activity.model.selectedScope(), activity.model.selectedId());
                        back();
                        activity.refresh();
                    } else {
                        open("delete_confirm");
                    }
                };
            case "Editar":
                return () -> open("edit_" + singular());
            case "Meu perfil":
                return () -> open("my_profile");
            case "Editar perfil":
                return () -> open("edit_profile");
            case "Configurações":
                return () -> open("settings");
            case "FAQ":
                return () -> open("faq");
            case "Sobre o app":
                return () -> open("about");
            case "Exportar excel":
                return () -> open("export");
            case "Exportar":
                return activity::export;
            case "Manu":
            case "Converse com a Manu":
                return () -> open("chatbot");
            case "Sair da Manu":
                return () -> open("home");
            case "Voltar para login":
                return () -> {
                    activity.startActivity(new Intent(activity, LoginActivity.class));
                    activity.finish();
                };
            case "Sair":
                return () -> {
                    if (name.equals("logout_confirm")) {
                        new ViewModelProvider(activity, new SessaoViewModelFactory(activity)).get(SessaoViewModel.class).encerrar();
                        activity.startActivity(new Intent(activity, LoginActivity.class));
                        activity.finish();
                    } else {
                        open("logout_confirm");
                    }
                };
            case "+":
            case "+ Adicionar Campo":
            case "+ Adicionar tarefa":
                return this::create;
            case "Criar primeira tarefa automaticamente":
            case "Criar eu mesmo":
                return () -> open("create_task");
            case "Competências":
                return () -> open("competencies");
            case "Ver mais":
                return () -> {
                    String id = activity.model.field("manager_member_details", "tecnicoId");
                    try { if (id != null) activity.openCompetencies(Long.parseLong(id)); }
                    catch (NumberFormatException ignored) { }
                };
            case "Novo chat":
            case "Nova conversa":
            case "Ajuda com solicitação":
            case "Dúvidas sobre o app":
                return () -> open("chatbot");
            case "Adicionar fotos":
                return activity::photos;
            case "Sim":
            case "Não":
            case "Liga normalmente":
            case "Sem vazamentos":
            case "Sem ruídos":
            case "Filtros limpos":
                return () -> select(view, text);
            case "Autor":
            case "Profissional":
            case "Período":
            case "Modelo":
            case "Status":
            case "Área":
                return () -> filter(text);
            case "Concluídas":
            case "Em andam.":
            case "Não iniciadas":
            case "Todas":
            case "Recusadas":
                return () -> select(view, "estado");
            case "O que é o FIXA?":
                return () -> view.setSelected(!view.isSelected());
        }
        if (name.equals("assign_date") && text.matches("[0-9]{1,2}")) {
            return () -> {
                activity.model.field(key, "prazo", text + "/09/2026");
                select(view, "dia");
            };
        }
        if (name.equals("competencies") && text.matches("[0-9_]+/10")) {
            return () -> open("edit_competency");
        }
        return null;
    }

    public String description(JSONObject item) {
        String asset = item.optString("asset");
        if (asset.equals("eee40.svg")) {
            return activity.getString(R.string.ops_back);
        }
        if (asset.equals("76941.svg")) {
            return "Notificações";
        }
        if (asset.equals("97246.svg")) {
            return activity.getString(R.string.ops_nav_profile);
        }
        if (asset.equals("58b2a.svg")) {
            return "Adicionar";
        }
        return item.optString("name");
    }

    private void menu(String text) {
        switch (text) {
            case "Solicitações":
                open("requests");
                break;
            case "Planejamento":
                open("planning");
                break;
            case "Quadro":
                open("board");
                break;
            case "Ocorrências":
                open("occurrences");
                break;
            case "Categorias":
                open("categories");
                break;
            case "Modelos":
                open("models");
                break;
            case "Checklists":
                open("checklists");
                break;
        }
    }

    private String singular() {
        switch (name) {
            case "categories":
                return "category";
            case "models":
                return "model";
            case "devices":
                return "device";
            case "checklists":
                return "checklist";
            case "competencies":
                return "competency";
            default:
                return "request";
        }
    }

    private void create() {
        activity.model.selectEntry(OperationsViewModel.scope(name), "", "");
        String entity = singular();
        if (name.equals("planning_tasks")) {
            entity = "task";
        }
        if (name.equals("checklist_fields")) {
            entity = "field";
        }
        activity.model.clearDraft(activity.role() + "_create_" + entity);
        switch (name) {
            case "occurrences":
                open("occurrence_intro");
                break;
            case "checklists":
                open("checklist_intro");
                break;
            case "categories":
                open("create_category");
                break;
            case "models":
                open("create_model");
                break;
            case "devices":
                open("create_device");
                break;
            case "checklist_fields":
                open("create_field");
                break;
            case "planning_tasks":
                open("create_task");
                break;
            case "create_choice_field":
            case "edit_choice_field":
                open("create_option");
                break;
        }
    }

    private void next() {
        if (name.equals("planning_tasks") && activity.model.entries("planning_tasks").isEmpty()) {
            android.widget.Toast.makeText(activity,
                    "Adicione ao menos uma tarefa antes de continuar.",
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        if (name.equals("plan_information")
                && (activity.model.field("plan_information", "CompetênciaId") == null
                || activity.model.field("plan_information", "PrioridadeId") == null
                || empty(activity.model.field("plan_information", "mes"))
                || empty(activity.model.field("plan_information", "prazo")))) {
            android.widget.Toast.makeText(activity, R.string.ops_planning_required_fields, android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        if (!binder.validate()) {
            return;
        }
        switch (name) {
            case "occurrence_intro":
                open("occurrence_service");
                break;
            case "occurrence_service":
                open("occurrence_location");
                break;
            case "occurrence_location":
                open("occurrence_result");
                break;
            case "occurrence_result":
                open("occurrence_review");
                break;
            case "planning_intro":
                activity.openPlanningStep("plan_information");
                break;
            case "plan_information":
                activity.openPlanningStep("planning_tasks");
                break;
            case "planning_tasks":
                activity.openPlanningStep("assign_member");
                break;
            case "checklist_intro":
                open("checklist_information");
                break;
            case "checklist_information":
                open("checklist_fields");
                break;
            case "checklist_fields":
                open("checklist_models");
                break;
            case "checklist_models":
                open("checklist_success");
                break;
            case "assign_member":
                open("assign_date");
                break;
        }
    }

    private boolean empty(String value) { return value == null || value.trim().isEmpty(); }

    private void confirm() {
        switch (name) {
            case "approve_request":
                open("request_approved");
                break;
            case "send_inspection":
                open("inspection_success");
                break;
            case "finish_order":
                open("order_completed");
                break;
            case "confirm_assignment":
                open("assignment_success");
                break;
            default:
                back();
        }
    }

    private void save() {
        if (!binder.validate()) {
            return;
        }
        if (name.startsWith("create_") || name.startsWith("edit_")) {
            String title = "";
            for (String label : new String[]{"Nome", "Nome do campo", "Título da tarefa", "Título", "Identificador"}) {
                String value = activity.model.field(key, label);
                if (value != null && !value.trim().isEmpty()) {
                    title = value;
                    break;
                }
            }
            String description = activity.model.field(key, "Descrição");
            activity.model.saveEntry(OperationsViewModel.scope(name), name.startsWith("edit_") ? activity.model.selectedId() : "", title, description == null ? "" : description);
        }
        if (dialog != null) {
            dialog.dismissNow();
        } else {
            open("save_confirm");
        }
        activity.refresh();
    }

    private void selectAncestor(View view) {
        View candidate = view;
        while (candidate.getParent() instanceof View) {
            if (String.valueOf(candidate.getTag()).startsWith("card:")) {
                activity.model.selectEntry(OperationsViewModel.scope(name), activity.getResources().getResourceEntryName(candidate.getId()), String.valueOf(candidate.getTag()).substring(5));
                return;
            }
            candidate = (View) candidate.getParent();
        }
    }

    private void select(View view, String field) {
        if (name.equals("export") && field.startsWith("export:")) {
            for (String option : new String[]{"Solicitações", "Ordens de serviço", "Ocorrências", "Checklists"}) {
                if (!field.equals("export:" + option)) {
                    activity.model.selected(key + ":export:" + option, false);
                }
            }
        }
        boolean selected = !view.isSelected();
        view.setSelected(selected);
        activity.model.selected(key + ":" + field, selected);
        view.setAlpha(selected ? 1f : .65f);
        androidx.core.view.ViewCompat.setStateDescription(view, activity.getString(selected ? R.string.ops_selected : R.string.ops_unselected));
    }

    private void selectExportOption(View clicked, String option) {
        String stateKey = key + ":export:" + option;
        boolean selected = !activity.model.selected(stateKey);
        for (String candidate : new String[]{"Solicitações", "Ordens de serviço", "Ocorrências", "Checklists"}) {
            activity.model.selected(key + ":export:" + candidate, selected && candidate.equals(option));
        }

        View card = clicked;
        ViewGroup parent = card.getParent() instanceof ViewGroup ? (ViewGroup) card.getParent() : null;
        while (parent != null && !String.valueOf(card.getTag()).startsWith("card:")) {
            card = parent;
            parent = card.getParent() instanceof ViewGroup ? (ViewGroup) card.getParent() : null;
        }
        if (!String.valueOf(card.getTag()).startsWith("card:")) card = clicked;
        ViewGroup cardContainer = card.getParent() instanceof ViewGroup ? (ViewGroup) card.getParent() : null;
        if (cardContainer != null) {
            for (int i = 0; i < cardContainer.getChildCount(); i++) {
                View sibling = cardContainer.getChildAt(i);
                Object tag = sibling.getTag();
                if (tag instanceof String && ((String) tag).startsWith("card:")) {
                    boolean active = ((String) tag).equals("card:" + option);
                    sibling.setSelected(active && selected);
                    sibling.setAlpha(!selected || active ? 1f : 0.82f);
                    ViewCompat.setBackgroundTintList(sibling, active && selected
                            ? ColorStateList.valueOf(Color.rgb(237, 242, 255)) : null);
                    sibling.setElevation(active && selected ? dp(5) : dp(2));
                }
            }
        } else {
            card.setSelected(selected);
        }
    }

    private float dp(float value) {
        return value * activity.getResources().getDisplayMetrics().density;
    }

    public void choose(EditText input, String label) {
        String[] options;
        if (label.equals("Local")) {
            options = new String[]{"S07", "S08", "S09", "Sala S11", "Banheiro - 2° Andar"};
        } else if (label.equals("Campo obrigatório")) {
            options = new String[]{"Sim", "Não"};
        } else if (label.equals("Prioridade")) {
            options = new String[]{"P0", "P1", "P2", "P3"};
        } else if (label.equals("Competência")) {
            options = new String[]{"Hidráulica", "Elétrica", "Limpeza"};
        } else if (label.equals("Categoria")) {
            options = new String[]{"Ar condicionado", "Escada rolante", "Porta automática"};
        } else if (label.equals("Tipo")) {
            options = new String[]{"Sim ou não", "Várias opções"};
        } else {
            options = new String[]{"Fácil", "Média", "Difícil"};
        }
        new MaterialAlertDialogBuilder(activity).setTitle(label).setItems(options, (d, which) -> {
            input.setText(options[which]);
            if (label.equals("Tipo") && options[which].equals("Várias opções")) {
                open(name.equals("edit_field") ? "edit_choice_field" : "create_choice_field");
            }
        }).show();
    }

    public void date(EditText input) {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(activity, (picker, y, m, d) -> input.setText(String.format(new Locale("pt", "BR"), "%02d/%02d/%04d", d, m + 1, y)), now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void filter(String label) {
        String[] values = label.equals("Status") ? new String[]{"Todas", "Em andamento", "Concluídas", "Não iniciadas"} : label.equals("Período") ? new String[]{"Todas", "Setembro", "Out, 2026"} : new String[]{"Todas", "Ryan Cursino", "Genilson Santos", "Modelo institucional A", "Hidráulica"};
        new MaterialAlertDialogBuilder(activity).setTitle(label).setItems(values, (d, which) -> activity.model.field(key, label, values[which])).show();
    }
}
