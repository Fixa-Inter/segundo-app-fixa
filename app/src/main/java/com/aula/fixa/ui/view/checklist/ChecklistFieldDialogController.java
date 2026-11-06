package com.aula.fixa.ui.view.checklist;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
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
import com.aula.fixa.domain.enums.TipoCampoChecklist;
import com.aula.fixa.domain.model.checklist.*;
import com.aula.fixa.ui.adapter.checklist.ChecklistOptionsAdapter;
import com.aula.fixa.ui.config.AppLoading;
import com.aula.fixa.ui.view.operations.OperationsActivity;
import com.aula.fixa.ui.view.operations.OperationsDialogFragment;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** Owns checklist field dialog binding and its input validation. */
final class ChecklistFieldDialogController {
    private static final String DRAFT = "templateDraft";
    private final OperationsActivity activity;
    private final ChecklistViewModel vm;
    private final Gson gson = new Gson();

    ChecklistFieldDialogController(Fragment owner) {
        activity = (OperationsActivity) owner.requireActivity();
        vm = new ViewModelProvider(owner, new ChecklistViewModelFactory(activity)).get(ChecklistViewModel.class);
    }

    public void bindDialog(Fragment owner, View content, String key) {
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
                        Toast.makeText(activity, R.string.ops_checklist_invalid, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Dialog loading = AppLoading.show(activity);
                    vm.excluir(id, new ChecklistViewModel.Callback<Void>() {
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
            try { old = gson.fromJson(args.getString("field"), ChecklistField.class); }
            catch (Exception ignored) { }
        }
        bindFieldDialog(owner, content, key, args, old);
    }

    private void bindFieldDialog(Fragment owner, View content, String key, Bundle args, ChecklistField editing) {
        EditText name = content.findViewWithTag("field:Nome do campo");
        if (name != null) {
            styleFieldName(name);
            if (editing != null) name.setText(editing.nome);
            else if (args != null && args.getString("fieldName") != null) name.setText(args.getString("fieldName"));
        }

        AutoCompleteTextView type = content.findViewWithTag("field:Tipo");
        if (type != null) {
            styleTypeDropdown(type);
            type.setKeyListener(null);
            type.setThreshold(0);
        }
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
        if (requiredValue != null) requiredValue.setText(initialRequired ? R.string.ops_text_sim_931345 : R.string.ops_text_nao_9bc947);
        SwitchCompat requiredSwitch = content.findViewWithTag("switch:required");
        if (requiredSwitch != null) styleRequiredSwitch(requiredSwitch, requiredLabel, requiredValue);
        if (requiredSwitch != null) requiredSwitch.setChecked(initialRequired);
        final boolean[] required = {initialRequired};
        if (requiredSwitch != null) requiredSwitch.setOnCheckedChangeListener((button, checked) -> {
            required[0] = checked;
            if (requiredValue != null) requiredValue.setText(checked ? R.string.ops_text_sim_931345 : R.string.ops_text_nao_9bc947);
        });
        if (requiredValue != null) requiredValue.setOnClickListener(v -> {
            required[0] = !required[0];
            requiredValue.setText(required[0] ? R.string.ops_text_sim_931345 : R.string.ops_text_nao_9bc947);
            if (requiredSwitch != null) requiredSwitch.setChecked(required[0]);
        });

        List<ChecklistOption> initialOptions = new ArrayList<>();
        if (editing != null && editing.opcoes != null) {
            for (ChecklistOption option : editing.opcoes) {
                if (option != null) initialOptions.add(new ChecklistOption(option.id, option.nome, false));
            }
        }
        ChecklistOptionsAdapter optionAdapter = new ChecklistOptionsAdapter(initialOptions);
        View optionSection = createOptionSection(content, optionAdapter);
        int requiredLabelId = requiredLabel == null ? View.NO_ID : requiredLabel.getId();
        View cancelLabel = content.findViewWithTag("text:Cancelar");
        View cancelButton = cancelLabel != null && cancelLabel.getParent() instanceof View
                ? (View) cancelLabel.getParent() : null;
        View saveButton = content.findViewWithTag("action:Salvar");

        Runnable updateOptionsSection = () -> {
            boolean needsOptions = isOptionType(type == null ? "" : type.getText().toString());
            if (needsOptions && optionAdapter.getItemCount() == 0) optionAdapter.addOption("");
            optionSection.setVisibility(needsOptions ? View.VISIBLE : View.GONE);
            positionFieldDialogFooter(content, cancelButton, saveButton,
                    needsOptions ? optionSection.getId() : requiredLabelId,
                    needsOptions ? R.dimen.ops_checklist_dialog_footer_compact_spacing : R.dimen.ops_checklist_dialog_footer_default_spacing);
        };
        updateOptionsSection.run();

        if (type != null) {
            String[] typeNames = Arrays.stream(TipoCampoChecklist.values()).map(item -> item.nome).toArray(String[]::new);
            type.setAdapter(new ArrayAdapter<>(activity, R.layout.item_app_dropdown, typeNames));
            type.setOnItemClickListener((parent, row, position, id) -> updateOptionsSection.run());
            type.setOnClickListener(v -> type.showDropDown());
            View arrow = type.getParent() instanceof ViewGroup
                    ? ((ViewGroup) type.getParent()).findViewWithTag("image:") : null;
            if (arrow != null) arrow.setOnClickListener(v -> type.showDropDown());
        }

        View.OnClickListener dismissDialog = v -> ((androidx.fragment.app.DialogFragment) owner).dismiss();
        if (cancelLabel != null) cancelLabel.setOnClickListener(dismissDialog);
        if (cancelLabel instanceof TextView) {
            ((TextView) cancelLabel).setGravity(android.view.Gravity.CENTER);
            ((TextView) cancelLabel).setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        if (cancelButton != null) cancelButton.setOnClickListener(dismissDialog);
        if (saveButton != null) saveButton.setOnClickListener(v -> {
            if (name == null || name.getText().toString().trim().isEmpty()) {
                if (name != null) name.setError(activity.getString(R.string.ops_checklist_field_name_required));
                return;
            }
            if (type == null) return;
            TipoCampoChecklist selected;
            try { selected = TipoCampoChecklist.fromApi(type.getText().toString()); }
            catch (Exception invalid) { Toast.makeText(activity, R.string.ops_checklist_field_type_required, Toast.LENGTH_SHORT).show(); return; }

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
                        Toast.makeText(activity, R.string.ops_checklist_options_incomplete, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String optionId = option.id == null ? UUID.randomUUID().toString() : option.id;
                    field.opcoes.add(new ChecklistOption(optionId, value, false));
                }
                if (field.opcoes.isEmpty()) {
                    Toast.makeText(activity, R.string.ops_checklist_option_required, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            ChecklistTemplate template = draft();
            if (template.campos == null) template.campos = new ArrayList<>();
            if (editing == null) template.campos.add(field);
            else {
                int index = args == null ? -1 : args.getInt("index", -1);
                if ((index < 0 || index >= template.campos.size()) && editing.id != null) {
                    for (int i = 0; i < template.campos.size(); i++) {
                        ChecklistField candidate = template.campos.get(i);
                        if (candidate != null && editing.id.equals(candidate.id)) {
                            index = i;
                            break;
                        }
                    }
                }
                if (index < 0 || index >= template.campos.size()) {
                    Toast.makeText(activity, R.string.ops_checklist_field_not_found, Toast.LENGTH_SHORT).show();
                    return;
                }
                template.campos.set(index, field);
            }
            saveDraft(template);
            ((androidx.fragment.app.DialogFragment) owner).dismiss();
            activity.refresh();
        });
    }

    private void styleFieldName(EditText field) {
        field.setBackgroundResource(R.drawable.bg_ops_category_input);
        field.setBackgroundTintList(null);
        field.setPaddingRelative(dp(15), 0, dp(12), 0);
        field.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.START);
        field.setTextColor(color(R.color.app_text_black));
        field.setHintTextColor(color(R.color.ops_color_ff637087));
        field.setTextSize(16);
        field.setIncludeFontPadding(true);
        field.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(activity, R.font.poppins_medium));
    }

    private void styleTypeDropdown(AutoCompleteTextView field) {
        field.setBackgroundColor(Color.TRANSPARENT);
        field.setPaddingRelative(dp(15), 0, dp(36), 0);
        field.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.START);
        field.setTextSize(16);
        field.setTextColor(color(R.color.ops_color_ff202020));
        field.setHintTextColor(color(R.color.ops_color_ff637087));
        field.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(activity, R.font.poppins_medium));
        field.setSingleLine(true);
        field.setDropDownVerticalOffset(dp(4));
        field.setDropDownBackgroundDrawable(androidx.core.content.ContextCompat.getDrawable(activity, R.drawable.bg_app_popup_menu));
        View parent = (View) field.getParent();
        if (parent instanceof ViewGroup) {
            View arrow = ((ViewGroup) parent).findViewWithTag("image:");
            if (arrow instanceof ImageView) {
                ViewGroup.LayoutParams params = arrow.getLayoutParams();
                params.width = dp(12);
                params.height = dp(7);
                if (params instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams constraints =
                            (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) params;
                    constraints.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                    constraints.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                    constraints.topMargin = 0;
                }
                arrow.setLayoutParams(params);
            }
        }
    }

    private void styleRequiredSwitch(SwitchCompat toggle, TextView label, TextView value) {
        int blue = color(R.color.app_primary);
        int gray = color(R.color.ops_color_ff637087);
        toggle.setShowText(false);
        toggle.setThumbTintList(new ColorStateList(new int[][]{
                new int[]{android.R.attr.state_checked}, new int[]{}
        }, new int[]{Color.WHITE, Color.WHITE}));
        toggle.setTrackTintList(new ColorStateList(new int[][]{
                new int[]{android.R.attr.state_checked}, new int[]{}
        }, new int[]{blue, gray}));
        ViewGroup.LayoutParams params = toggle.getLayoutParams();
        params.width = dp(42);
        params.height = dp(28);
        toggle.setLayoutParams(params);
        if (label != null) {
            label.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(activity, R.font.poppins_medium));
            label.setTextSize(14);
            label.setTextColor(color(R.color.ops_color_ff202020));
        }
        if (value != null) {
            value.setTypeface(androidx.core.content.res.ResourcesCompat.getFont(activity, R.font.poppins_medium));
            value.setTextSize(14);
            value.setTextColor(gray);
        }
    }

    private int dp(int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }

    private int color(int resource) {
        return androidx.core.content.ContextCompat.getColor(activity, resource);
    }

    private View createOptionSection(View content, ChecklistOptionsAdapter adapter) {
        View section = content.findViewById(R.id.checklistFieldOptionsSection);
        RecyclerView optionList = section.findViewById(R.id.checklistOptionList);
        optionList.setLayoutManager(new LinearLayoutManager(activity));
        optionList.setNestedScrollingEnabled(false);
        optionList.setAdapter(adapter);
        ItemTouchHelper helper = new ItemTouchHelper(adapter.dragCallback());
        adapter.setTouchHelper(helper);
        helper.attachToRecyclerView(optionList);
        section.findViewById(R.id.checklistAddOption).setOnClickListener(v -> adapter.addOption(""));

        TextView requiredLabel = content.findViewWithTag("text:Campo obrigatório");
        if (requiredLabel != null && section.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) section.getLayoutParams();
            params.width = 0;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            params.topToBottom = requiredLabel.getId();
            params.topMargin = activity.getResources().getDimensionPixelSize(R.dimen.ops_checklist_option_list_spacing);
            section.setLayoutParams(params);
        }
        return section;
    }

    private void positionFieldDialogFooter(View content, View cancelButton,
                                          View saveButton, int anchorId, int marginResource) {
        ViewGroup root = layoutRoot(content);
        if (!(root instanceof androidx.constraintlayout.widget.ConstraintLayout)
                || cancelButton == null || saveButton == null) return;
        androidx.constraintlayout.widget.ConstraintLayout parent =
                (androidx.constraintlayout.widget.ConstraintLayout) root;

        LinearLayout footer = parent.findViewWithTag("field:footer-row");
        if (footer == null) {
            View cancelText = cancelButton.findViewWithTag("text:Cancelar");
            if (cancelText instanceof TextView
                    && cancelText.getLayoutParams() instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
                androidx.constraintlayout.widget.ConstraintLayout.LayoutParams textParams =
                        (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) cancelText.getLayoutParams();
                textParams.width = 0;
                textParams.height = 0;
                textParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                textParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                textParams.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                textParams.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
                textParams.setMarginStart(0);
                textParams.setMarginEnd(0);
                textParams.topMargin = 0;
                textParams.bottomMargin = 0;
                cancelText.setLayoutParams(textParams);
                ((TextView) cancelText).setGravity(android.view.Gravity.CENTER);
            }
            if (cancelButton.getParent() == parent) parent.removeView(cancelButton);
            if (saveButton.getParent() == parent) parent.removeView(saveButton);
            footer = new LinearLayout(activity);
            footer.setTag("field:footer-row");
            footer.setId(View.generateViewId());
            footer.setOrientation(LinearLayout.HORIZONTAL);

            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(0, activity.getResources().getDimensionPixelSize(R.dimen.ops_checklist_dialog_button_height), 1f);
            footer.addView(cancelButton, buttonParams);
            View gap = new View(activity);
            footer.addView(gap, new LinearLayout.LayoutParams(activity.getResources().getDimensionPixelSize(R.dimen.ops_checklist_dialog_button_gap), 1));
            footer.addView(saveButton, new LinearLayout.LayoutParams(0, activity.getResources().getDimensionPixelSize(R.dimen.ops_checklist_dialog_button_height), 1f));

            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams footerParams =
                    new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(0, activity.getResources().getDimensionPixelSize(R.dimen.ops_checklist_dialog_button_height));
            footerParams.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            footerParams.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            footerParams.topToBottom = anchorId;
            footerParams.setMarginStart(activity.getResources().getDimensionPixelSize(R.dimen.ops_checklist_dialog_footer_margin));
            footerParams.setMarginEnd(activity.getResources().getDimensionPixelSize(R.dimen.ops_checklist_dialog_footer_margin));
            parent.addView(footer, footerParams);


        }

        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams footerParams =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) footer.getLayoutParams();
        footerParams.topToTop = -1;
        footerParams.topToBottom = anchorId;
        footerParams.bottomToBottom = -1;
        footerParams.topMargin = activity.getResources().getDimensionPixelSize(marginResource);
        footerParams.bottomMargin = 0;
        footer.setLayoutParams(footerParams);

        View bottomSpacer = parent.findViewWithTag("field:option-footer-spacer");
        if (bottomSpacer == null) {
            bottomSpacer = new View(activity);
            bottomSpacer.setTag("field:option-footer-spacer");
            bottomSpacer.setId(View.generateViewId());
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams spacerParams =
                    new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(0, activity.getResources().getDimensionPixelSize(R.dimen.ops_checklist_dialog_footer_spacing));
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

    private ViewGroup layoutRoot(View content) {
        if (content instanceof androidx.core.widget.NestedScrollView) {
            androidx.core.widget.NestedScrollView scroll = (androidx.core.widget.NestedScrollView) content;
            if (scroll.getChildCount() > 0 && scroll.getChildAt(0) instanceof ViewGroup)
                return (ViewGroup) scroll.getChildAt(0);
        }
        View scroll = content.findViewById(R.id.checklistScroll);
        if (scroll instanceof androidx.core.widget.NestedScrollView
                && ((androidx.core.widget.NestedScrollView) scroll).getChildAt(0) instanceof ViewGroup)
            return (ViewGroup) ((androidx.core.widget.NestedScrollView) scroll).getChildAt(0);
        return content instanceof ViewGroup ? (ViewGroup) content : null;
    }

    private ChecklistTemplate draft() {
        String raw = activity.model.field("checklist", DRAFT);
        if (raw == null || raw.isEmpty()) return new ChecklistTemplate();
        try { return gson.fromJson(raw, ChecklistTemplate.class); }
        catch (Exception ignored) { return new ChecklistTemplate(); }
    }

    private void saveDraft(ChecklistTemplate value) { activity.model.field("checklist", DRAFT, gson.toJson(value)); }
    private void toast(String text) { Toast.makeText(activity, text, Toast.LENGTH_SHORT).show(); }
    private void showFieldDialog(ChecklistField field, int index) {
        Bundle args = new Bundle();
        if (field != null) args.putString("field", gson.toJson(field));
        args.putInt("index", index);
        String key = field == null ? "manager_create_field" : "manager_edit_field";
        OperationsDialogFragment.create(key, args).show(activity.getSupportFragmentManager(), "ops_dialog");
    }

}
