package com.aula.fixa.ui.view.equipamento;

import android.app.Dialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.aula.fixa.R;
import com.aula.fixa.adapter.auth.AutenticacaoViewModel;
import com.aula.fixa.adapter.equipamento.EquipamentoViewModel;
import com.aula.fixa.adapter.equipamento.EquipamentoViewModelFactory;
import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.domain.model.equipamento.EquipamentoItem;
import com.aula.fixa.ui.adapter.equipamento.EquipamentoAdapter;
import com.aula.fixa.ui.config.AppLoading;
import com.aula.fixa.ui.view.operations.OperationsActivity;
import com.aula.fixa.ui.view.operations.OperationsDialogFragment;
import com.aula.fixa.ui.view.operations.OperationsScreenFragment;
import java.util.ArrayList;
import java.util.List;

/** Lista e manipula os equipamentos vinculados ao modelo selecionado. */
public final class EquipamentoGestorFluxo {
    private final OperationsScreenFragment host;
    private final OperationsActivity activity;
    private EquipamentoViewModel model;
    private EquipamentoAdapter adapter;
    private View root;
    private RecyclerView list;
    private TextView empty;
    private Dialog loading;
    private long modeloId;
    private List<EquipamentoItem> all = new ArrayList<>();
    private android.widget.FrameLayout floatingButton;

    public EquipamentoGestorFluxo(OperationsScreenFragment host, OperationsActivity activity) {
        this.host = host;
        this.activity = activity;
    }

    public void bindList(View content) {
        root = content;
        model = new ViewModelProvider(host, new EquipamentoViewModelFactory(host.requireContext())).get(EquipamentoViewModel.class);
        modeloId = parse(activity.model.field("devices", "modelId"));
        if (modeloId <= 0 && "models".equals(activity.model.selectedScope())) {
            modeloId = parse(activity.model.selectedId());
            activity.model.field("devices", "modelId", String.valueOf(modeloId));
        }
        View title = content.findViewById(R.id.ops_5717_6556);
        View searchBox = content.findViewById(R.id.ops_5717_6589);
        EditText search = content.findViewById(R.id.ops_5717_6590);
        ViewGroup container = (ViewGroup) title.getParent();
        for (int i = container.getChildCount() - 1; i >= 0; i--) {
            View child = container.getChildAt(i);
            if (child.getTag() instanceof String && ((String) child.getTag()).startsWith("card:")) container.removeViewAt(i);
        }

        list = new RecyclerView(host.requireContext());
        list.setId(View.generateViewId());
        list.setNestedScrollingEnabled(false);
        list.setClipToPadding(false);
        ConstraintLayout.LayoutParams listParams = new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        listParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        listParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        listParams.topToBottom = searchBox.getId();
        listParams.setMarginStart(dp(31));
        listParams.setMarginEnd(dp(31));
        listParams.topMargin = dp(18);
        container.addView(list, listParams);
        list.setLayoutManager(new LinearLayoutManager(host.requireContext()));
        adapter = new EquipamentoAdapter(this::showMenu);
        list.setAdapter(adapter);

        empty = new TextView(host.requireContext());
        empty.setText("Nenhum equipamento cadastrado.");
        empty.setTextSize(15);
        empty.setTextColor(androidx.core.content.ContextCompat.getColor(host.requireContext(), R.color.ops_color_ff637087));
        empty.setGravity(android.view.Gravity.CENTER);
        ConstraintLayout.LayoutParams emptyParams = new ConstraintLayout.LayoutParams(0, dp(160));
        emptyParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        emptyParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        emptyParams.topToBottom = searchBox.getId();
        emptyParams.setMarginStart(dp(26));
        emptyParams.setMarginEnd(dp(26));
        emptyParams.topMargin = dp(18);
        container.addView(empty, emptyParams);

        View back = content.findViewWithTag("image:arrow-left");
        if (back != null) back.setOnClickListener(v -> activity.back());
        View fab = content.findViewById(R.id.ops_5717_6601);
        View plus = content.findViewById(R.id.ops_5717_6603);
        ViewGroup scroll = (ViewGroup) title.getParent();
        if (fab.getParent() instanceof ViewGroup) ((ViewGroup) fab.getParent()).removeView(fab);
        if (plus != null && plus.getParent() instanceof ViewGroup) ((ViewGroup) plus.getParent()).removeView(plus);
        floatingButton = new android.widget.FrameLayout(host.requireContext());
        floatingButton.setClickable(true);
        floatingButton.setFocusable(true);
        floatingButton.setContentDescription("Cadastrar equipamento");
        floatingButton.setElevation(dp(8));
        floatingButton.addView(fab, new android.widget.FrameLayout.LayoutParams(-1, -1));
        if (plus != null) floatingButton.addView(plus, new android.widget.FrameLayout.LayoutParams(dp(29), dp(29), android.view.Gravity.CENTER));
        View.OnClickListener create = v -> activity.open("manager_create_device");
        floatingButton.setOnClickListener(create);
        fab.setOnClickListener(create);
        if (plus != null) plus.setOnClickListener(create);
        ViewGroup screenHost = (ViewGroup) content.getParent();
        android.widget.FrameLayout.LayoutParams fabParams = new android.widget.FrameLayout.LayoutParams(dp(70), dp(70), android.view.Gravity.BOTTOM | android.view.Gravity.END);
        fabParams.setMargins(0, 0, dp(24), dp(24));
        screenHost.addView(floatingButton, fabParams);
        floatingButton.setVisibility(View.INVISIBLE);
        if (content instanceof ViewGroup) ((ViewGroup) content).setPadding(content.getPaddingLeft(), content.getPaddingTop(), content.getPaddingRight(), dp(96));
        if (search != null) search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s == null ? "" : s.toString()); }
            @Override public void afterTextChanged(Editable s) { }
        });
        content.setVisibility(View.INVISIBLE);
        loading = AppLoading.show(host.requireContext());
        load();
    }

    private void load() {
        if (modeloId <= 0) {
            showLoadFailure(new IllegalStateException("Não foi possível identificar o modelo de equipamento."));
            return;
        }
        model.listar(modeloId, new EquipamentoViewModel.Callback<List<EquipamentoItem>>() {
            @Override public void success(List<EquipamentoItem> values) {
                if (!host.isAdded() || root == null) return;
                all = values == null ? new ArrayList<>() : new ArrayList<>(values);
                filter("");
                finishLoading();
            }
            @Override public void failure(Throwable error) { showLoadFailure(error); }
        });
    }

    private void showLoadFailure(Throwable error) {
        if (!host.isAdded() || root == null) return;
        all.clear();
        adapter.submit(List.of());
        list.setVisibility(View.GONE);
        empty.setText("Não foi possível carregar os equipamentos. Toque para tentar novamente.");
        empty.setOnClickListener(v -> { root.setVisibility(View.INVISIBLE); loading = AppLoading.show(host.requireContext()); load(); });
        empty.setVisibility(View.VISIBLE);
        finishLoading();
        Toast.makeText(host.requireContext(), AutenticacaoViewModel.messageFrom(error), Toast.LENGTH_LONG).show();
    }

    private void finishLoading() {
        AppLoading.hide(loading);
        loading = null;
        root.setVisibility(View.VISIBLE);
        if (floatingButton != null) floatingButton.setVisibility(View.VISIBLE);
    }

    private void filter(String query) {
        String needle = query.trim().toLowerCase(java.util.Locale.ROOT);
        List<EquipamentoItem> filtered = new ArrayList<>();
        for (EquipamentoItem item : all) if (item.codigo.toLowerCase(java.util.Locale.ROOT).contains(needle)
                || item.localUnidade.toLowerCase(java.util.Locale.ROOT).contains(needle)) filtered.add(item);
        adapter.submit(filtered);
        boolean has = !filtered.isEmpty();
        list.setVisibility(has ? View.VISIBLE : View.GONE);
        empty.setText(all.isEmpty() ? "Nenhum equipamento cadastrado." : "Nenhum equipamento encontrado.");
        empty.setOnClickListener(null);
        empty.setVisibility(has ? View.GONE : View.VISIBLE);
    }

    private void showMenu(View anchor, EquipamentoItem item) {
        View menu = LayoutInflater.from(anchor.getContext()).inflate(R.layout.popup_app_request_menu, null, false);
        PopupWindow popup = new PopupWindow(menu, anchor.getResources().getDimensionPixelSize(R.dimen.app_popup_menu_width), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setBackgroundDrawable(androidx.core.content.ContextCompat.getDrawable(anchor.getContext(), R.drawable.bg_app_popup_menu));
        popup.setElevation(dp(6));
        popup.setOutsideTouchable(true);
        menu.findViewById(R.id.tvPopupEdit).setOnClickListener(v -> { popup.dismiss(); select(item); activity.open("manager_edit_device"); });
        menu.findViewById(R.id.tvPopupDelete).setOnClickListener(v -> { popup.dismiss(); select(item); activity.open("manager_delete_device"); });
        popup.showAsDropDown(anchor, -popup.getWidth() + anchor.getWidth(), 0);
    }

    private void select(EquipamentoItem item) {
        activity.model.selectEntry("devices", String.valueOf(item.id), item.codigo);
        activity.model.field("devices", "local", item.localUnidade);
    }

    public static void bindDialog(OperationsDialogFragment dialog, View content, String key) {
        OperationsActivity activity = (OperationsActivity) dialog.requireActivity();
        EquipamentoViewModel vm = new ViewModelProvider(dialog, new EquipamentoViewModelFactory(dialog.requireContext())).get(EquipamentoViewModel.class);
        if (key.equals("manager_delete_device")) {
            View cancel = content.findViewById(R.id.ops_5717_7916);
            View delete = content.findViewById(R.id.ops_5717_7914);
            TextView title = content.findViewById(R.id.ops_5717_7913);
            if (title != null) title.setText("Deseja excluir “" + activity.model.selectedTitle() + "”?");
            if (cancel != null) cancel.setOnClickListener(v -> dialog.dismiss());
            if (delete != null) delete.setOnClickListener(v -> {
                long id = parse(activity.model.selectedId());
                if (id <= 0) return;
                delete.setEnabled(false);
                Dialog wait = AppLoading.show(dialog.requireContext());
                vm.excluir(id, callback(dialog, activity, wait, delete, "Equipamento excluído."));
            });
            return;
        }
        boolean editing = key.equals("manager_edit_device");
        EditText code = content.findViewById(editing ? R.id.ops_5717_7891 : R.id.ops_5717_7904);
        TextView local = content.findViewById(editing ? R.id.ops_5717_7894_input : R.id.ops_5717_7907_input);
        View cancel = content.findViewById(editing ? R.id.ops_5717_7889 : R.id.ops_5717_7902);
        View save = content.findViewById(editing ? R.id.ops_5717_7887 : R.id.ops_5717_7900);
        if (editing) { code.setText(activity.model.selectedTitle()); local.setText(activity.model.field("devices", "local")); }
        prepareChoice(local);
        List<OpcaoCadastro>[] options = new List[]{new ArrayList<>()};
        boolean[] loaded = {false};
        vm.locais(new EquipamentoViewModel.Callback<List<OpcaoCadastro>>() {
            @Override public void success(List<OpcaoCadastro> values) { options[0] = values == null ? new ArrayList<>() : values; loaded[0] = true; }
            @Override public void failure(Throwable error) { loaded[0] = true; Toast.makeText(dialog.requireContext(), AutenticacaoViewModel.messageFrom(error), Toast.LENGTH_LONG).show(); }
        });
        local.setOnClickListener(v -> chooseLocal(dialog, local, options[0], loaded[0]));
        View arrow = content.findViewById(editing ? R.id.ops_5717_7897 : R.id.ops_5717_7910);
        if (arrow != null) arrow.setOnClickListener(v -> local.performClick());
        if (cancel != null) cancel.setOnClickListener(v -> dialog.dismiss());
        if (save == null) return;
        save.setOnClickListener(v -> {
            String value = code.getText().toString().trim();
            if (value.isEmpty()) { code.setError("Informe o identificador do equipamento."); return; }
            long localId = optionId(options[0], local.getText().toString());
            if (localId <= 0) { Toast.makeText(dialog.requireContext(), "Selecione um local da unidade.", Toast.LENGTH_SHORT).show(); return; }
            save.setEnabled(false);
            Dialog wait = AppLoading.show(dialog.requireContext());
            EquipamentoViewModel.Callback<EquipamentoItem> cb = new EquipamentoViewModel.Callback<>() {
                @Override public void success(EquipamentoItem item) { complete(dialog, activity, wait, save, null, editing ? "Equipamento atualizado." : "Equipamento criado."); }
                @Override public void failure(Throwable error) { complete(dialog, activity, wait, save, error, ""); }
            };
            if (editing) vm.atualizar(parse(activity.model.selectedId()), localId, value, cb);
            else vm.criar(parse(activity.model.field("devices", "modelId")), localId, value, cb);
        });
    }

    private static EquipamentoViewModel.Callback<Void> callback(OperationsDialogFragment dialog, OperationsActivity activity, Dialog wait, View button, String success) {
        return new EquipamentoViewModel.Callback<>() {
            @Override public void success(Void ignored) { complete(dialog, activity, wait, button, null, success); }
            @Override public void failure(Throwable error) { complete(dialog, activity, wait, button, error, success); }
        };
    }
    private static void complete(OperationsDialogFragment dialog, OperationsActivity activity, Dialog wait, View button, Throwable error, String message) {
        AppLoading.hide(wait);
        if (!dialog.isAdded()) return;
        if (error != null) { button.setEnabled(true); Toast.makeText(dialog.requireContext(), AutenticacaoViewModel.messageFrom(error), Toast.LENGTH_LONG).show(); return; }
        dialog.dismiss();
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        activity.refresh();
    }
    private static void prepareChoice(TextView field) {
        field.setFocusable(false);
        field.setClickable(true);
        if (field instanceof EditText) { ((EditText) field).setCursorVisible(false); ((EditText) field).setShowSoftInputOnFocus(false); }
    }
    private static void chooseLocal(OperationsDialogFragment dialog, TextView field, List<OpcaoCadastro> values, boolean loaded) {
        if (!loaded) { Toast.makeText(dialog.requireContext(), "Carregando locais…", Toast.LENGTH_SHORT).show(); return; }
        if (values == null || values.isEmpty()) { Toast.makeText(dialog.requireContext(), "Nenhum local disponível.", Toast.LENGTH_SHORT).show(); return; }
        String[] names = values.stream().map(OpcaoCadastro::getNome).toArray(String[]::new);
        View anchor = field.getParent() instanceof View ? (View) field.getParent() : field;
        ListPopupWindow popup = new ListPopupWindow(dialog.requireContext());
        popup.setAnchorView(anchor);
        popup.setWidth(anchor.getWidth());
        popup.setModal(true);
        popup.setAdapter(new ArrayAdapter<>(dialog.requireContext(), R.layout.item_app_dropdown, names));
        popup.setOnItemClickListener((parent, row, position, id) -> { field.setText(names[position]); popup.dismiss(); });
        popup.show();
    }
    private static long optionId(List<OpcaoCadastro> values, String name) {
        if (values != null) for (OpcaoCadastro value : values) if (value.getNome().equalsIgnoreCase(name.trim())) return value.getId();
        return 0;
    }
    private static long parse(String value) { try { return Long.parseLong(value); } catch (Exception ignored) { return 0; } }
    private int dp(int value) { return Math.round(value * host.getResources().getDisplayMetrics().density); }
}
