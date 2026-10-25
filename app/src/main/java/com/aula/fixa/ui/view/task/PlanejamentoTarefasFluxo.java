package com.aula.fixa.ui.view.task;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.adapter.operations.OperationsViewModel;
import com.aula.fixa.ui.adapter.task.PlanejamentoTarefaAdapter;
import com.aula.fixa.ui.view.operations.OperationsActivity;
import com.aula.fixa.ui.view.operations.OperationsDialogFragment;
import com.aula.fixa.ui.view.operations.OperationsScreenFragment;

/** Coordena os eventos e o rascunho de tarefas do planejamento da OS. */
public final class PlanejamentoTarefasFluxo {
    private final OperationsScreenFragment host;
    private final OperationsActivity activity;
    private final View root;
    private final PlanejamentoTarefaAdapter adapter;

    public PlanejamentoTarefasFluxo(OperationsScreenFragment host, OperationsActivity activity, View root) {
        this.host = host;
        this.activity = activity;
        this.root = root;
        adapter = new PlanejamentoTarefaAdapter(this::showTaskOptions);
    }

    public void bind() {
        RecyclerView list = root.findViewById(R.id.ops_planning_tasks_list);
        if (list != null) {
            list.setLayoutManager(new LinearLayoutManager(host.requireContext()));
            list.setNestedScrollingEnabled(false);
            list.setAdapter(adapter);
        }
        View addCard = root.findViewById(R.id.ops_5717_8530);
        View addLabel = root.findViewWithTag("text:+ Adicionar tarefa");
        View.OnClickListener addTask = view -> openCreateTask();
        if (addCard != null) addCard.setOnClickListener(addTask);
        if (addLabel != null) addLabel.setOnClickListener(addTask);
        refresh();
    }

    public void maybeShowFirstTaskPrompt() {
        if (!host.isAdded() || host.getParentFragmentManager().isStateSaved()
                || activity.model.selected("planning:first_task_prompt_shown")) return;
        activity.model.selected("planning:first_task_prompt_shown", true);
        activity.open("manager_first_task");
    }

    public void refresh() {
        adapter.submitList(activity.model.entries("planning_tasks"));
    }

    private void openCreateTask() {
        activity.model.selectEntry("planning_tasks", "", "");
        activity.model.clearDraft("manager_create_task");
        activity.openName("create_task");
    }

    private void showTaskOptions(View anchor, Bundle task) {
        String id = task.getString("id", "");
        String title = task.getString("title", "");
        View content = LayoutInflater.from(anchor.getContext()).inflate(R.layout.popup_app_request_menu, null, false);
        int width = anchor.getResources().getDimensionPixelSize(R.dimen.app_popup_menu_width);
        PopupWindow menu = new PopupWindow(content, width, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        menu.setElevation(anchor.getResources().getDisplayMetrics().density * 6f);
        content.findViewById(R.id.tvPopupEdit).setOnClickListener(view -> {
            menu.dismiss();
            activity.model.selectEntry("planning_tasks", id, title);
            activity.model.clearDraft("manager_edit_task");
            activity.model.field("manager_edit_task", "Título da tarefa", title);
            activity.model.field("manager_edit_task", "Descrição", task.getString("description", ""));
            activity.openName("edit_task");
        });
        content.findViewById(R.id.tvPopupDelete).setOnClickListener(view -> {
            menu.dismiss();
            activity.model.selectEntry("planning_tasks", id, title);
            activity.open("manager_delete_task");
        });
        menu.setBackgroundDrawable(androidx.core.content.ContextCompat.getDrawable(
                anchor.getContext(), R.drawable.bg_app_popup_menu));
        menu.setOutsideTouchable(true);
        menu.showAsDropDown(anchor, -width + anchor.getWidth(), 0);
    }

    /** Substitui os eventos genéricos dos diálogos pela edição local do rascunho da OS. */
    public static void bindDialog(OperationsActivity activity, View content, String key,
                                  OperationsDialogFragment dialog) {
        switch (key) {
            case "manager_first_task":
                bindFirstTask(activity, content, dialog);
                break;
            case "manager_create_task":
            case "manager_edit_task":
                bindTaskForm(activity, content, key, dialog);
                break;
            case "manager_delete_task":
                bindDeleteTask(activity, content, dialog);
                break;
        }
    }

    private static void bindFirstTask(OperationsActivity activity, View content, OperationsDialogFragment dialog) {
        View createAutomatically = content.findViewWithTag("action:Criar");
        View createManually = content.findViewWithTag("text:Criar eu mesmo");
        if (createAutomatically != null) createAutomatically.setOnClickListener(view -> {
            String title = activity.model.field("planning_tasks", "source_title");
            String description = activity.model.field("planning_tasks", "source_description");
            if (title == null) title = "";
            if (description == null) description = "";
            activity.model.saveEntry("planning_tasks", "", title, description);
            dialog.dismissNow();
            activity.refresh();
        });
        if (createManually != null) createManually.setOnClickListener(view -> {
            dialog.dismissNow();
            activity.model.selectEntry("planning_tasks", "", "");
            activity.model.clearDraft("manager_create_task");
            activity.openName("create_task");
        });
    }

    private static void bindTaskForm(OperationsActivity activity, View content, String key,
                                    OperationsDialogFragment dialog) {
        EditText title = content.findViewWithTag("field:Título da tarefa");
        EditText description = content.findViewWithTag("field:Descrição");
        View save = content.findViewWithTag("action:Salvar");
        if (save == null || title == null || description == null) return;
        save.setOnClickListener(view -> {
            String taskTitle = title.getText().toString().trim();
            String taskDescription = description.getText().toString().trim();
            if (taskTitle.isEmpty()) {
                title.setError(activity.getString(R.string.ops_required));
                title.requestFocus();
                return;
            }
            if (taskDescription.isEmpty()) {
                description.setError(activity.getString(R.string.ops_required));
                description.requestFocus();
                return;
            }
            String id = "manager_create_task".equals(key) ? "" : activity.model.selectedId();
            activity.model.saveEntry("planning_tasks", id, taskTitle, taskDescription);
            dialog.dismissNow();
            activity.refresh();
        });
    }

    private static void bindDeleteTask(OperationsActivity activity, View content, OperationsDialogFragment dialog) {
        View delete = content.findViewWithTag("action:Excluir");
        if (delete == null) return;
        delete.setOnClickListener(view -> {
            activity.model.deleteEntry("planning_tasks", activity.model.selectedId());
            dialog.dismissNow();
            activity.refresh();
        });
    }
}
