package com.aula.fixa.ui.view.operations;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.R;
import com.aula.fixa.adapter.auth.AutenticacaoViewModel;
import com.aula.fixa.adapter.solicitacao.SolicitacaoGestorViewModel;
import com.aula.fixa.adapter.solicitacao.SolicitacaoGestorViewModelFactory;
import com.aula.fixa.adapter.tecnico.TecnicoViewModel;
import com.aula.fixa.adapter.tecnico.TecnicoViewModelFactory;
import com.aula.fixa.domain.enums.StatusProblema;
import com.aula.fixa.ui.config.AppLoading;
import com.aula.fixa.ui.config.operations.ScreenCatalog;
import org.json.JSONObject;

/** Diálogos do protótipo com rolagem, teclado e ciclo de vida nativos. */
public final class OperationsDialogFragment extends DialogFragment {
    public static OperationsDialogFragment create(String key) {
        return create(key, null);
    }

    public static OperationsDialogFragment create(String key, Bundle extras) {
        OperationsDialogFragment fragment = new OperationsDialogFragment();
        Bundle args = extras == null ? new Bundle() : new Bundle(extras);
        args.putString("screen", key);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull @Override public Dialog onCreateDialog(Bundle state) {
        String key = requireArguments().getString("screen");
        boolean creatingPlanningTask = key.equals("manager_create_task");
        int layout = creatingPlanningTask ? R.layout.dialog_ops_manager_edit_task
                : ScreenCatalog.get(requireContext()).layout(requireContext(), key);
        View content = getLayoutInflater().inflate(layout, null, false);
        content.setLayoutParams(new android.view.ViewGroup.LayoutParams(-1, -2));
        if (key.equals("manager_create_category") || key.equals("manager_edit_category")
                || key.equals("manager_delete_category")) {
            com.aula.fixa.ui.view.equipamento.CategoriaEquipamentoGestorFluxo.bindDialog(this, content, key);
        } else if (key.equals("manager_create_model") || key.equals("manager_edit_model")
                || key.equals("manager_delete_model")) {
            com.aula.fixa.ui.view.equipamento.ModeloEquipamentoGestorFluxo.bindDialog(this, content, key);
        } else if (key.equals("manager_create_device") || key.equals("manager_edit_device")
                || key.equals("manager_delete_device")) {
            com.aula.fixa.ui.view.equipamento.EquipamentoGestorFluxo.bindDialog(this, content, key);
        } else if (key.equals("manager_edit_competency")) {
            bindEditCompetency(content);
        } else if (key.equals("manager_reject_request")) {
            bindRejectRequest(content);
        } else if (creatingPlanningTask) {
            TextView title = (TextView) content.findViewWithTag("text:Editar tarefa");
            if (title != null) title.setText(R.string.ops_text_criar_tarefa_0a2e86);
            EditText taskTitle = (EditText) content.findViewWithTag("field:Título da tarefa");
            if (taskTitle != null) {
                taskTitle.setText("");
                taskTitle.setHint(R.string.ops_text_coloque_o_nome_cbd678);
            }
            EditText taskDescription = (EditText) content.findViewWithTag("field:Descrição");
            if (taskDescription != null) {
                taskDescription.setText("");
                taskDescription.setHint(R.string.ops_text_coloque_a_descricao_da_tarefa_c6d230);
            }
            View cancelLabel = content.findViewWithTag("text:Cancelar");
            if (cancelLabel != null && cancelLabel.getParent() instanceof View) {
                ((View) cancelLabel.getParent()).setOnClickListener(v -> dismissNow());
            }
            com.aula.fixa.ui.view.task.PlanejamentoTarefasFluxo.bindDialog(
                    (OperationsActivity) requireActivity(), content, key, this);
        } else if (key.equals("manager_create_field") || key.equals("manager_edit_field")
                || key.equals("manager_create_choice_field") || key.equals("manager_edit_choice_field")
                || key.equals("manager_delete_checklist")) {
            com.aula.fixa.ui.view.checklist.ChecklistFlow.bindDialog(this, content, key);
        } else {
            new OperationsBinder((OperationsActivity) requireActivity(), content, key, this).bind();
            com.aula.fixa.ui.view.task.PlanejamentoTarefasFluxo.bindDialog(
                    (OperationsActivity) requireActivity(), content, key, this);
        }
        boolean checklistFieldDialog = key.equals("manager_create_field") || key.equals("manager_edit_field")
                || key.equals("manager_create_choice_field") || key.equals("manager_edit_choice_field");
        if (!checklistFieldDialog) com.aula.fixa.ui.config.PopupFieldStyler.apply(content);
        if (key.equals("manager_request_approved")) {
            View plan = content.findViewById(R.id.ops_5717_7116);
            if (plan != null) plan.setOnClickListener(v -> redirectApprovedRequestToPlanning());
        }
        if (creatingPlanningTask || key.equals("manager_edit_task")) stylePlanningTaskPopup(content);
        if (key.equals("manager_create_category") || key.equals("manager_edit_category")) {
            styleCategoryFields(content, key.equals("manager_create_category"));
        }
        if (key.equals("manager_create_model") || key.equals("manager_edit_model")) {
            styleModelDialog(content, key.equals("manager_edit_model"));
        }
        if (key.startsWith("manager_task_pending") || key.startsWith("manager_task_completed")) {
            styleTaskInfoDialog(content, key.contains("obstacle"));
        }
        if (key.equals("manager_edit_competency")) {
            EditText value = content.findViewById(R.id.ops_5717_6051);
            EditText oldValue = content.findViewById(R.id.ops_5717_6057);
            int fieldHeight = getResources().getDimensionPixelSize(R.dimen.ops_dp_40_0);
            for (EditText field : new EditText[]{oldValue, value}) {
                if (field == null) continue;
                android.view.ViewGroup.LayoutParams params = field.getLayoutParams();
                params.height = fieldHeight;
                field.setLayoutParams(params);
                field.setGravity(Gravity.CENTER);
                field.setBackgroundResource(R.drawable.bg_app_competency_dialog_field);
            }
            content.setPadding(content.getPaddingLeft(), content.getPaddingTop(),
                    content.getPaddingRight(), 0);
            TextView title = content.findViewById(R.id.ops_5717_6053);
            if (title != null) {
                title.setTextColor(androidx.core.content.ContextCompat.getColor(
                        requireContext(), R.color.app_primary));
                title.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(
                        requireContext(), R.font.poppins_medium));
            }
            View cancel = content.findViewById(R.id.ops_5717_6050);
            View save = content.findViewById(R.id.ops_5717_6049);
            if (cancel instanceof TextView) {
                ((TextView) cancel).setTypeface(androidx.core.content.res.ResourcesCompat.getFont(
                        requireContext(), R.font.poppins_regular));
                ((TextView) cancel).setTextColor(androidx.core.content.ContextCompat.getColor(
                        requireContext(), R.color.app_primary_deep));
            }
            if (save instanceof TextView) {
                ((TextView) save).setTypeface(androidx.core.content.res.ResourcesCompat.getFont(
                        requireContext(), R.font.poppins_regular));
            }
            if (value != null) value.setHintTextColor(androidx.core.content.ContextCompat.getColor(
                    requireContext(), R.color.app_text_secondary));
        }
        boolean drawerContent = ScreenCatalog.get(requireContext()).screen(key).optBoolean("drawer");
        if (!drawerContent) {
            DialogContentScrollView scroll = new DialogContentScrollView(requireContext(),
                    Math.round(getResources().getDisplayMetrics().heightPixels * .88f));
            scroll.setFillViewport(false);
            scroll.setClipToPadding(false);
            scroll.setVerticalScrollBarEnabled(false);
            content.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
            scroll.addView(content);
            content = scroll;
        }
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(content).create();
        return dialog;
    }

    private void redirectApprovedRequestToPlanning() {
        long requestId = getArguments() == null ? 0 : getArguments().getLong("requestId", 0);
        if (!isAdded() || requestId <= 0) return;
        OperationsActivity activity = (OperationsActivity) requireActivity();
        dismissAllowingStateLoss();
        activity.openPlanningIntro(requestId);
    }

    private void stylePlanningTaskPopup(View content) {
        for (int id : new int[]{R.id.ops_5717_8669, R.id.ops_5717_8672}) {
            EditText field = content.findViewById(id);
            if (field == null) continue;
            field.setBackgroundResource(R.drawable.bg_app_planning_task_dialog_field);
            field.setBackgroundTintList(null);
        }
        int bottom = Math.round(28 * getResources().getDisplayMetrics().density);
        content.setPadding(content.getPaddingLeft(), content.getPaddingTop(),
                content.getPaddingRight(), bottom);
    }

    private void styleCategoryFields(View content, boolean creating) {
        int[] ids = creating
                ? new int[]{R.id.ops_5717_7866, R.id.ops_category_description}
                : new int[]{R.id.ops_5717_7857};
        for (int id : ids) {
            EditText field = content.findViewById(id);
            if (field == null) continue;
            field.setBackgroundResource(R.drawable.bg_ops_category_input);
            field.setBackgroundTintList(null);
            field.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            field.setIncludeFontPadding(false);
        }
    }

    private void styleModelDialog(View content, boolean editing) {
        int titleId = editing ? R.id.ops_5717_7820 : R.id.ops_5717_7836;
        int nameLabelId = editing ? R.id.ops_5717_7827 : R.id.ops_5717_7843;
        int nameId = editing ? R.id.ops_5717_7825 : R.id.ops_5717_7841;
        int categoryLabelId = editing ? R.id.ops_5717_7831 : R.id.ops_5717_7847;
        int categoryContainerId = editing ? R.id.ops_5717_7828 : R.id.ops_5717_7844;
        int brandLabelId = editing ? R.id.ops_5717_7834 : R.id.ops_5717_7850;
        int brandId = editing ? R.id.ops_5717_7832 : R.id.ops_5717_7848;
        int descriptionLabelId = editing ? R.id.manager_model_description_label_edit
                : R.id.manager_model_description_label_create;
        int descriptionId = editing ? R.id.manager_model_description_edit : R.id.manager_model_description_create;
        int cancelButtonId = editing ? R.id.ops_5717_7822 : R.id.ops_5717_7838;
        int cancelTextId = editing ? R.id.ops_5717_7823 : R.id.ops_5717_7839;
        int saveButtonId = editing ? R.id.ops_5717_7821 : R.id.ops_5717_7837;

        View title = content.findViewById(titleId);
        View nameLabel = content.findViewById(nameLabelId);
        View name = content.findViewById(nameId);
        View categoryLabel = content.findViewById(categoryLabelId);
        View category = content.findViewById(categoryContainerId);
        View brandLabel = content.findViewById(brandLabelId);
        View brand = content.findViewById(brandId);
        View descriptionLabel = content.findViewById(descriptionLabelId);
        View description = content.findViewById(descriptionId);
        View cancel = content.findViewById(cancelButtonId);
        View cancelText = content.findViewById(cancelTextId);
        View save = content.findViewById(saveButtonId);
        if (title == null || nameLabel == null || name == null || categoryLabel == null || category == null
                || brandLabel == null || brand == null || descriptionLabel == null || description == null
                || cancel == null || save == null) return;

        View root = content instanceof android.view.ViewGroup
                && ((android.view.ViewGroup) content).getChildCount() > 0
                ? ((android.view.ViewGroup) content).getChildAt(0) : content;
        if (root instanceof androidx.constraintlayout.widget.ConstraintLayout) {
            ((androidx.constraintlayout.widget.ConstraintLayout) root).setMinimumHeight(dp(594));
        }
        setTopMargin(title, 24);
        setLabelStyle(title, 25, R.font.poppins_semibold);
        placeBelow(nameLabel, title, 16);
        placeBelow(name, nameLabel, 6);
        setHeight(name, dp(56));
        styleModelField(name);
        placeBelow(categoryLabel, name, 14);
        placeBelow(category, categoryLabel, 6);
        setHeight(category, dp(56));
        styleModelField(category);
        placeBelow(brandLabel, category, 14);
        placeBelow(brand, brandLabel, 6);
        setHeight(brand, dp(56));
        styleModelField(brand);
        placeBelow(descriptionLabel, brand, 14);
        placeBelow(description, descriptionLabel, 6);
        setHeight(description, dp(112));
        styleModelField(description);
        setActionRow(cancel, cancelText, save, description, dp(45), dp(12), dp(50));
    }

    private void styleModelField(View field) {
        if (field == null) return;
        field.setBackgroundResource(R.drawable.bg_ops_category_input);
        field.setBackgroundTintList(null);
    }

    private void styleTaskInfoDialog(View content, boolean hasObstacle) {
        View title = findTagged(content, "text:Informações da tarefa");
        View status = findTagged(content, "text:Status: Não concluída", "text:Status: Concluída");
        View taskLabel = findTagged(content, "text:Título da tarefa");
        View taskTitle = findTagged(content, "text:Verificar o funcionamento");
        View descriptionLabel = findTagged(content, "text:Descrição");
        View description = findTagged(content, "card:Diagnosticar o aparelho antes do reparo");
        View cancelText = findTagged(content, "text:Voltar");
        View action = findTagged(content, "action:Concluir tarefa", "action:Desfazer tarefa");
        if (title == null || status == null || taskLabel == null || taskTitle == null
                || descriptionLabel == null || description == null || cancelText == null || action == null) return;

        View cancel = cancelText.getParent() instanceof View ? (View) cancelText.getParent() : null;
        View taskTitleContainer = taskTitle.getParent() instanceof View ? (View) taskTitle.getParent() : null;
        if (cancel == null || taskTitleContainer == null) return;
        View root = content instanceof android.view.ViewGroup
                && ((android.view.ViewGroup) content).getChildCount() > 0
                ? ((android.view.ViewGroup) content).getChildAt(0) : content;
        if (root instanceof androidx.constraintlayout.widget.ConstraintLayout) {
            ((androidx.constraintlayout.widget.ConstraintLayout) root).setMinimumHeight(dp(hasObstacle ? 576 : 527));
        }

        setHorizontalMargins(title, 24, 24);
        setTopMargin(title, 24);
        setLabelStyle(title, 24, R.font.poppins_semibold);
        placeBelow(status, title, 12);
        setHorizontalMargins(status, 24, 24);
        placeBelow(taskLabel, status, 16);
        setHorizontalMargins(taskLabel, 24, 24);
        placeBelow(taskTitleContainer, taskLabel, 5);
        setHorizontalMargins(taskTitleContainer, 24, 24);
        setHeight(taskTitleContainer, dp(42));
        centerInsideParent(taskTitle);
        placeBelow(descriptionLabel, taskTitleContainer, 14);
        setHorizontalMargins(descriptionLabel, 24, 24);
        placeBelow(description, descriptionLabel, 6);
        setHorizontalMargins(description, 24, 24);
        setHeight(description, dp(168));
        setActionRow(cancel, cancelText, action, description);
    }

    private View findTagged(View root, String... tags) {
        for (String tag : tags) {
            View view = findTaggedRecursive(root, tag);
            if (view != null) return view;
        }
        return null;
    }

    private View findTaggedRecursive(View root, String tag) {
        if (tag.equals(root.getTag())) return root;
        if (!(root instanceof android.view.ViewGroup)) return null;
        android.view.ViewGroup group = (android.view.ViewGroup) root;
        for (int index = 0; index < group.getChildCount(); index++) {
            View found = findTaggedRecursive(group.getChildAt(index), tag);
            if (found != null) return found;
        }
        return null;
    }

    private void setHorizontalMargins(View view, int startDp, int endDp) {
        if (view == null || !(view.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) return;
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) view.getLayoutParams();
        params.setMarginStart(dp(startDp));
        params.setMarginEnd(dp(endDp));
        view.setLayoutParams(params);
    }

    private void centerInsideParent(View view) {
        if (view == null || !(view.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) return;
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) view.getLayoutParams();
        params.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        params.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        params.topToBottom = -1;
        params.bottomToTop = -1;
        params.topMargin = params.bottomMargin = 0;
        view.setLayoutParams(params);
        if (view instanceof TextView) ((TextView) view).setGravity(Gravity.CENTER_VERTICAL);
    }

    private void placeBelow(View view, View anchor, int marginDp) {
        if (view == null || anchor == null
                || !(view.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) return;
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) view.getLayoutParams();
        params.topToTop = -1;
        params.topToBottom = anchor.getId();
        params.bottomToBottom = -1;
        params.topMargin = dp(marginDp);
        view.setLayoutParams(params);
    }

    private void setTopMargin(View view, int marginDp) {
        if (view == null || !(view.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) return;
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) view.getLayoutParams();
        params.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        params.topToBottom = -1;
        params.topMargin = dp(marginDp);
        view.setLayoutParams(params);
    }

    private void setHeight(View view, int heightPx) {
        if (view == null || view.getLayoutParams() == null) return;
        android.view.ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = heightPx;
        view.setLayoutParams(params);
    }

    private void setActionRow(View cancel, View cancelText, View save, View description) {
        setActionRow(cancel, cancelText, save, description, dp(35), dp(12), dp(44));
    }

    private void setActionRow(View cancel, View cancelText, View save, View description,
                              int side, int gap, int height) {
        if (!(cancel.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)
                || !(save.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)) return;
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams cancelParams =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) cancel.getLayoutParams();
        cancelParams.width = 0;
        cancelParams.height = height;
        cancelParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        cancelParams.endToStart = save.getId();
        cancelParams.startToEnd = -1;
        cancelParams.endToEnd = -1;
        cancelParams.setMarginStart(side);
        cancelParams.setMarginEnd(gap / 2);
        cancelParams.topToTop = -1;
        cancelParams.topToBottom = description.getId();
        cancelParams.topMargin = dp(20);
        cancelParams.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        cancelParams.bottomMargin = dp(24);
        cancelParams.horizontalChainStyle = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.CHAIN_SPREAD;
        cancel.setLayoutParams(cancelParams);

        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams saveParams =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) save.getLayoutParams();
        saveParams.width = 0;
        saveParams.height = height;
        saveParams.startToStart = -1;
        saveParams.startToEnd = cancel.getId();
        saveParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        saveParams.endToStart = -1;
        saveParams.setMarginStart(gap / 2);
        saveParams.setMarginEnd(side);
        saveParams.topToTop = cancel.getId();
        saveParams.topToBottom = -1;
        saveParams.bottomToBottom = cancel.getId();
        saveParams.topMargin = saveParams.bottomMargin = 0;
        save.setLayoutParams(saveParams);

        if (cancelText instanceof TextView) {
            TextView label = (TextView) cancelText;
            label.setGravity(Gravity.CENTER);
            label.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(requireContext(), R.font.poppins_medium));
            label.setTextSize(15);
            if (label.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams labelParams =
                        (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) label.getLayoutParams();
                labelParams.width = 0;
                labelParams.height = 0;
                labelParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                labelParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                labelParams.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                labelParams.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                labelParams.leftMargin = labelParams.rightMargin = labelParams.topMargin = labelParams.bottomMargin = 0;
                label.setLayoutParams(labelParams);
            }
        }
        cancel.setBackgroundResource(R.drawable.bg_ops_6fc7f4db18);
        save.setBackgroundTintList(null);
        if (save instanceof TextView) {
            ((TextView) save).setGravity(Gravity.CENTER);
            ((TextView) save).setTypeface(androidx.core.content.res.ResourcesCompat.getFont(requireContext(), R.font.poppins_medium));
            ((TextView) save).setTextSize(15);
        }
    }

    private void setLabelStyle(View view, int sp, int fontId) {
        if (view instanceof TextView) {
            TextView text = (TextView) view;
            text.setTextSize(sp);
            text.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(requireContext(), fontId));
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void bindEditCompetency(View content) {
        OperationsActivity activity = (OperationsActivity) requireActivity();
        Bundle args = requireArguments();
        TextView title = content.findViewById(R.id.ops_5717_6053);
        TextView oldValue = content.findViewById(R.id.ops_5717_6057);
        EditText newValue = content.findViewById(R.id.ops_5717_6051);
        View save = content.findViewById(R.id.ops_5717_6049);
        if (title != null) title.setText(args.getString("competenceName", "Competência"));
        if (oldValue != null) {
            String old = args.getString("oldScore", "_");
            oldValue.setText(old == null || old.trim().isEmpty() ? "_" : old);
            oldValue.setEnabled(false);
            oldValue.setFocusable(false);
        }
        if (newValue != null) {
            newValue.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                    | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                    | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
            newValue.setFilters(new android.text.InputFilter[]{(source, start, end, dest, dstart, dend) -> {
                String candidate = dest.subSequence(0, dstart).toString()
                        + source.subSequence(start, end) + dest.subSequence(dend, dest.length());
                if (candidate.isEmpty() || candidate.equals("-") || candidate.equals(".") || candidate.equals(",")) return null;
                if (!candidate.matches("-?\\d*(?:[.,]\\d{0,2})?")) return "";
                return null;
            }});
        }
        View cancel = content.findViewById(R.id.ops_5717_6050);
        if (cancel != null) cancel.setOnClickListener(v -> dismiss());
        if (save == null) return;
        save.setOnClickListener(v -> {
            if (newValue == null) return;
            String raw = newValue.getText().toString().trim().replace(',', '.');
            if (raw.isEmpty() || raw.equals(".") || raw.equals("-")) {
                newValue.setError("Informe um valor numérico.");
                return;
            }
            java.math.BigDecimal score;
            try { score = new java.math.BigDecimal(raw); }
            catch (NumberFormatException invalid) { newValue.setError("Informe um valor numérico válido."); return; }
            if (Math.max(0, score.stripTrailingZeros().scale()) > 2) {
                newValue.setError("Use no máximo duas casas decimais.");
                return;
            }
            long competenceId = args.getLong("competenceId", 0);
            if (competenceId <= 0) {
                Toast.makeText(activity, "Não foi possível identificar a competência.", Toast.LENGTH_LONG).show();
                return;
            }
            save.setEnabled(false);
            Dialog loading = AppLoading.show(requireContext());
            TecnicoViewModel model = new ViewModelProvider(this,
                    new TecnicoViewModelFactory(requireContext())).get(TecnicoViewModel.class);
            model.atualizarCompetencia(competenceId, score, new TecnicoViewModel.Callback<com.aula.fixa.domain.model.tecnico.CompetenciaTecnico>() {
                @Override public void success(com.aula.fixa.domain.model.tecnico.CompetenciaTecnico result) {
                    AppLoading.hide(loading);
                    if (!isAdded()) return;
                    dismiss();
                    Toast.makeText(activity, "Competência atualizada.", Toast.LENGTH_SHORT).show();
                    activity.refresh();
                }
                @Override public void failure(Throwable error) {
                    AppLoading.hide(loading);
                    if (!isAdded()) return;
                    save.setEnabled(true);
                    Toast.makeText(requireContext(), AutenticacaoViewModel.messageFrom(error), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void bindRejectRequest(View content) {
        OperationsActivity activity = (OperationsActivity) requireActivity();
        EditText reason = content.findViewById(R.id.ops_5717_7097);
        View cancel = content.findViewById(R.id.ops_5717_7096);
        View cancelText = content.findViewById(R.id.ops_5717_7100);
        View reject = content.findViewById(R.id.ops_5717_7095);
        cancel.setOnClickListener(v -> dismiss());
        cancelText.setOnClickListener(v -> dismiss());
        reject.setOnClickListener(v -> {
            String value = reason.getText().toString().trim();
            if (value.isEmpty()) {
                reason.setError("Informe o motivo da reprovação.");
                return;
            }
            long requestId = requireArguments().getLong("requestId", 0);
            if (requestId <= 0) {
                Toast.makeText(activity, "Solicitação inválida.", Toast.LENGTH_LONG).show();
                dismiss();
                return;
            }
            reject.setEnabled(false);
            Dialog loading = AppLoading.show(requireContext());
            SolicitacaoGestorViewModel model = new ViewModelProvider(this,
                    new SolicitacaoGestorViewModelFactory(requireContext()))
                    .get(SolicitacaoGestorViewModel.class);
            model.updateStatus(requestId, StatusProblema.REPROVADO, value,
                    new SolicitacaoGestorViewModel.Callback<com.aula.fixa.domain.model.solicitacao.SolicitacaoGestor>() {
                @Override public void success(com.aula.fixa.domain.model.solicitacao.SolicitacaoGestor result) {
                    AppLoading.hide(loading);
                    if (!isAdded()) return;
                    dismiss();
                    Toast.makeText(activity, "Solicitação reprovada.", Toast.LENGTH_SHORT).show();
                    activity.back();
                }
                @Override public void failure(Throwable error) {
                    AppLoading.hide(loading);
                    if (!isAdded()) return;
                    reject.setEnabled(true);
                    Toast.makeText(requireContext(), AutenticacaoViewModel.messageFrom(error),
                            Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    @Override public void onStart() {
        super.onStart();
        Window window = requireDialog().getWindow();
        if (window == null) return;
        JSONObject spec = ScreenCatalog.get(requireContext()).screen(requireArguments().getString("screen"));
        float scale = getResources().getDimension(com.aula.fixa.R.dimen.ops_content_width) / 412f;
        String key = requireArguments().getString("screen", "");
        int designWidth = key.startsWith("manager_task_pending") || key.startsWith("manager_task_completed")
                ? Math.max(spec.optInt("width"), 412) : spec.optInt("width");
        int width = Math.min(Math.round(designWidth * scale), (int)(getResources().getDisplayMetrics().widthPixels * .94f));
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(width, spec.optBoolean("drawer") ? -1 : -2);
        if (spec.optBoolean("drawer")) window.setGravity(Gravity.START);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private static final class DialogContentScrollView extends androidx.core.widget.NestedScrollView {
        private final int maxContentHeight;
        DialogContentScrollView(android.content.Context context, int maxContentHeight) {
            super(context);
            this.maxContentHeight = maxContentHeight;
        }
        @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int availableHeight = MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED
                    ? maxContentHeight : Math.min(maxContentHeight, MeasureSpec.getSize(heightMeasureSpec));
            int cappedHeightSpec = MeasureSpec.makeMeasureSpec(availableHeight, MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, cappedHeightSpec);
        }
    }
}
