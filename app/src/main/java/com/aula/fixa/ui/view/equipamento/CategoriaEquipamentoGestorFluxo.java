package com.aula.fixa.ui.view.equipamento;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.aula.fixa.R;
import com.aula.fixa.adapter.auth.AutenticacaoViewModel;
import com.aula.fixa.adapter.equipamento.CategoriaEquipamentoViewModel;
import com.aula.fixa.adapter.equipamento.CategoriaEquipamentoViewModelFactory;
import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.ui.adapter.equipamento.CategoriaEquipamentoAdapter;
import com.aula.fixa.ui.config.AppLoading;
import com.aula.fixa.ui.view.operations.OperationsActivity;
import com.aula.fixa.ui.view.operations.OperationsDialogFragment;
import com.aula.fixa.ui.view.operations.OperationsScreenFragment;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/** Fluxo de gerenciamento de categorias de equipamento do gestor. */
public final class CategoriaEquipamentoGestorFluxo {
    private final OperationsScreenFragment host;
    private final OperationsActivity activity;
    private CategoriaEquipamentoViewModel model;
    private CategoriaEquipamentoAdapter adapter;
    private RecyclerView list;
    private TextView empty;
    private View root;
    private Dialog loading;
    private String query = "";
    private final List<OpcaoCadastro> categories = new ArrayList<>();

    public CategoriaEquipamentoGestorFluxo(OperationsScreenFragment host, OperationsActivity activity) {
        this.host = host; this.activity = activity;
    }

    public void bindList(View content) {
        root = content;
        model = new ViewModelProvider(host, new CategoriaEquipamentoViewModelFactory(host.requireContext()))
                .get(CategoriaEquipamentoViewModel.class);
        ViewGroup container = (ViewGroup) ((ViewGroup) content).getChildAt(0);
        View back = content.findViewById(R.id.ops_5717_6467);
        if (back != null) back.setOnClickListener(v -> activity.back());
        int searchId = R.id.ops_5717_6497, fabId = R.id.ops_5717_6509;
        RecyclerView recycler = new RecyclerView(host.requireContext());
        recycler.setId(View.generateViewId()); recycler.setClipToPadding(false);
        recycler.setPadding(0, 0, 0, dp(8));
        ConstraintLayout.LayoutParams listParams = new ConstraintLayout.LayoutParams(0, 0);
        listParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        listParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        listParams.topToBottom = searchId; listParams.bottomToTop = fabId;
        listParams.setMarginStart(dp(31)); listParams.setMarginEnd(dp(41));
        listParams.topMargin = dp(15); listParams.bottomMargin = dp(12);
        container.addView(recycler, listParams);
        recycler.setLayoutManager(new LinearLayoutManager(host.requireContext()));
        list = recycler;

        TextView emptyView = new TextView(host.requireContext());
        emptyView.setText("Nenhuma categoria cadastrada.");
        emptyView.setTextSize(14);
        emptyView.setTextColor(androidx.core.content.ContextCompat.getColor(host.requireContext(), R.color.app_text_secondary));
        emptyView.setGravity(android.view.Gravity.CENTER);
        ConstraintLayout.LayoutParams emptyParams = new ConstraintLayout.LayoutParams(0, 0);
        emptyParams.startToStart = listParams.startToStart; emptyParams.endToEnd = listParams.endToEnd;
        emptyParams.topToTop = listParams.topToTop; emptyParams.bottomToBottom = listParams.bottomToBottom;
        emptyParams.setMarginStart(listParams.getMarginStart()); emptyParams.setMarginEnd(listParams.getMarginEnd());
        emptyParams.topMargin = listParams.topMargin; emptyParams.bottomMargin = listParams.bottomMargin;
        container.addView(emptyView, emptyParams); empty = emptyView;

        adapter = new CategoriaEquipamentoAdapter(this::showMenu);
        recycler.setAdapter(adapter);
        EditText search = content.findViewById(R.id.ops_5717_6498);
        if (search != null) search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            public void onTextChanged(CharSequence s, int start, int before, int count) { query = s.toString(); render(); }
            public void afterTextChanged(android.text.Editable e) { }
        });
        View fab = content.findViewById(fabId), plus = content.findViewById(R.id.ops_5717_6511);
        View.OnClickListener create = v -> activity.open("manager_create_category");
        if (fab != null) fab.setOnClickListener(create);
        if (plus != null) plus.setOnClickListener(create);
        content.setVisibility(View.INVISIBLE);
        loading = AppLoading.show(host.requireContext());
        load();
    }

    private void load() {
        model.listar(new CategoriaEquipamentoViewModel.Callback<List<OpcaoCadastro>>() {
            @Override public void success(List<OpcaoCadastro> values) {
                if (!host.isAdded() || root == null) return;
                categories.clear(); if (values != null) categories.addAll(values);
                AppLoading.hide(loading); loading = null; root.setVisibility(View.VISIBLE); render();
            }
            @Override public void failure(Throwable error) {
                if (!host.isAdded() || root == null) return;
                categories.clear(); if (empty != null) empty.setText("Não foi possível carregar as categorias.");
                AppLoading.hide(loading); loading = null; root.setVisibility(View.VISIBLE); render();
                Toast.makeText(host.requireContext(), AutenticacaoViewModel.messageFrom(error), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void render() {
        if (adapter == null) return;
        String needle = normalize(query);
        List<OpcaoCadastro> visible = new ArrayList<>();
        for (OpcaoCadastro category : categories) if (normalize(category.getNome()).contains(needle)) visible.add(category);
        adapter.submit(visible);
        if (empty != null) empty.setVisibility(visible.isEmpty() ? View.VISIBLE : View.GONE);
        if (list != null) list.setVisibility(visible.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showMenu(View anchor, OpcaoCadastro category) {
        View menu = LayoutInflater.from(anchor.getContext()).inflate(R.layout.popup_app_request_menu, null, false);
        PopupWindow popup = new PopupWindow(menu, anchor.getResources().getDimensionPixelSize(R.dimen.app_popup_menu_width),
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setBackgroundDrawable(androidx.core.content.ContextCompat.getDrawable(anchor.getContext(), R.drawable.bg_app_popup_menu));
        popup.setElevation(dp(6)); popup.setOutsideTouchable(true);
        menu.findViewById(R.id.tvPopupEdit).setOnClickListener(v -> {
            popup.dismiss(); activity.model.selectEntry("categories", String.valueOf(category.getId()), category.getNome());
            activity.open("manager_edit_category");
        });
        menu.findViewById(R.id.tvPopupDelete).setOnClickListener(v -> {
            popup.dismiss(); activity.model.selectEntry("categories", String.valueOf(category.getId()), category.getNome());
            activity.open("manager_delete_category");
        });
        popup.showAsDropDown(anchor, -popup.getWidth() + anchor.getWidth(), 0);
    }

    public static void bindDialog(OperationsDialogFragment dialog, View content, String key) {
        OperationsActivity activity = (OperationsActivity) dialog.requireActivity();
        CategoriaEquipamentoViewModel model = new ViewModelProvider(dialog,
                new CategoriaEquipamentoViewModelFactory(dialog.requireContext())).get(CategoriaEquipamentoViewModel.class);
        if ("manager_create_category".equals(key)) {
            EditText name = content.findViewById(R.id.ops_5717_7866);
            EditText description = content.findViewById(R.id.ops_category_description);
            View cancel = content.findViewById(R.id.ops_5717_7864), save = content.findViewById(R.id.ops_5717_7862);
            cancel.setOnClickListener(v -> dialog.dismiss());
            save.setOnClickListener(v -> {
                String title = name.getText().toString().trim(), body = description.getText().toString().trim();
                if (title.isEmpty()) { name.setError("Informe o nome da categoria."); return; }
                if (body.isEmpty()) { description.setError("Informe a descrição da categoria."); return; }
                save.setEnabled(false); Dialog wait = AppLoading.show(dialog.requireContext());
                model.criar(title, body, new CategoriaEquipamentoViewModel.Callback<OpcaoCadastro>() {
                    @Override public void success(OpcaoCadastro value) { complete(dialog, activity, wait, save, "Categoria criada com sucesso."); }
                    @Override public void failure(Throwable error) { fail(dialog, wait, save, error); }
                });
            });
        } else if ("manager_edit_category".equals(key)) {
            EditText name = content.findViewById(R.id.ops_5717_7857);
            name.setText(activity.model.selectedTitle());
            View cancel = content.findViewById(R.id.ops_5717_7855), save = content.findViewById(R.id.ops_5717_7853);
            cancel.setOnClickListener(v -> dialog.dismiss());
            save.setOnClickListener(v -> {
                String title = name.getText().toString().trim();
                if (title.isEmpty()) { name.setError("Informe o nome da categoria."); return; }
                long id = selectedId(activity);
                save.setEnabled(false); Dialog wait = AppLoading.show(dialog.requireContext());
                model.atualizar(id, title, new CategoriaEquipamentoViewModel.Callback<OpcaoCadastro>() {
                    @Override public void success(OpcaoCadastro value) { complete(dialog, activity, wait, save, "Categoria atualizada com sucesso."); }
                    @Override public void failure(Throwable error) { fail(dialog, wait, save, error); }
                });
            });
        } else if ("manager_delete_category".equals(key)) {
            View cancel = content.findViewById(R.id.ops_5717_7874), remove = content.findViewById(R.id.ops_5717_7872);
            TextView message = content.findViewById(R.id.ops_5717_7871);
            if (message != null) message.setText("Deseja excluir “" + activity.model.selectedTitle() + "”? ");
            cancel.setOnClickListener(v -> dialog.dismiss());
            remove.setOnClickListener(v -> {
                long id = selectedId(activity); remove.setEnabled(false); Dialog wait = AppLoading.show(dialog.requireContext());
                model.excluir(id, new CategoriaEquipamentoViewModel.Callback<Void>() {
                    @Override public void success(Void value) { complete(dialog, activity, wait, remove, "Categoria excluída com sucesso."); }
                    @Override public void failure(Throwable error) { fail(dialog, wait, remove, error); }
                });
            });
        }
    }

    private static long selectedId(OperationsActivity activity) {
        try { return Long.parseLong(activity.model.selectedId()); }
        catch (Exception ignored) { return 0; }
    }
    private static void complete(OperationsDialogFragment dialog, OperationsActivity activity, Dialog wait, View button, String message) {
        AppLoading.hide(wait); button.setEnabled(true); dialog.dismiss();
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show(); activity.refresh();
    }
    private static void fail(OperationsDialogFragment dialog, Dialog wait, View button, Throwable error) {
        AppLoading.hide(wait); button.setEnabled(true);
        Toast.makeText(dialog.requireContext(), AutenticacaoViewModel.messageFrom(error), Toast.LENGTH_LONG).show();
    }
    private String normalize(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(java.util.Locale.ROOT).trim();
    }
    private int dp(int value) { return (int) (value * host.getResources().getDisplayMetrics().density + 0.5f); }
    public void clear() { AppLoading.hide(loading); loading = null; root = null; list = null; empty = null; adapter = null; }
}
