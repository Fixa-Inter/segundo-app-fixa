package com.aula.fixa.ui.view.equipamento;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ArrayAdapter;
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
import com.aula.fixa.adapter.equipamento.ModeloEquipamentoViewModel;
import com.aula.fixa.adapter.equipamento.ModeloEquipamentoViewModelFactory;
import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.domain.model.equipamento.ModeloEquipamentoItem;
import com.aula.fixa.ui.adapter.equipamento.ModeloEquipamentoAdapter;
import com.aula.fixa.ui.config.AppLoading;
import com.aula.fixa.ui.view.operations.OperationsActivity;
import com.aula.fixa.ui.view.operations.OperationsDialogFragment;
import com.aula.fixa.ui.view.operations.OperationsScreenFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Coordena listagem e dialogs de modelos de equipamento.
 */
public final class ModeloEquipamentoGestorFluxo {
    private final OperationsScreenFragment host;
    private final OperationsActivity activity;
    private ModeloEquipamentoViewModel model;
    private ModeloEquipamentoAdapter adapter;
    private View root;
    private RecyclerView list;
    private TextView empty;
    private android.widget.FrameLayout floatingButton;
    private Dialog loading;

    public ModeloEquipamentoGestorFluxo(OperationsScreenFragment host, OperationsActivity activity) {
        this.host = host;
        this.activity = activity;
    }

    public void bindList(View content) {
        root = content;
        model = new ViewModelProvider(host, new ModeloEquipamentoViewModelFactory(host.requireContext()))
                .get(ModeloEquipamentoViewModel.class);
        View title = content.findViewById(R.id.ops_5717_6723);
        View fab = content.findViewById(R.id.ops_5717_6728);
        ViewGroup container = (ViewGroup) title.getParent();
        for (int i = container.getChildCount() - 1; i >= 0; i--) {
            View child = container.getChildAt(i);
            Object tag = child.getTag();
            if (tag instanceof String && ((String) tag).startsWith("card:")) {
                container.removeViewAt(i);
            }
        }
        list = new RecyclerView(host.requireContext());
        list.setId(View.generateViewId());
        list.setClipToPadding(false);
        list.setPadding(0, 0, 0, dp(host, 8));
        ConstraintLayout.LayoutParams listParams = new ConstraintLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        listParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        listParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        listParams.topToBottom = title.getId();
        listParams.setMarginStart(dp(host, 26));
        listParams.setMarginEnd(dp(host, 26));
        listParams.topMargin = dp(host, 22);
        list.setNestedScrollingEnabled(false);
        list.setMinimumHeight(dp(host, 180));
        container.addView(list, listParams);
        list.setLayoutManager(new LinearLayoutManager(host.requireContext()));
        adapter = new ModeloEquipamentoAdapter(new ModeloEquipamentoAdapter.Listener() {
            @Override public void onMenu(View anchor, ModeloEquipamentoItem item) { showMenu(anchor, item); }
            @Override public void onSelect(ModeloEquipamentoItem item) {
                select(item);
                activity.open("manager_devices");
            }
        });
        list.setAdapter(adapter);

        empty = new TextView(host.requireContext());
        empty.setText("Nenhum modelo cadastrado.");
        empty.setTextSize(15);
        empty.setTextColor(androidx.core.content.ContextCompat.getColor(host.requireContext(), R.color.ops_color_ff637087));
        empty.setGravity(android.view.Gravity.CENTER);
        ConstraintLayout.LayoutParams emptyParams = new ConstraintLayout.LayoutParams(0, dp(host, 180));
        emptyParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        emptyParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        emptyParams.topToBottom = title.getId();
        emptyParams.setMarginStart(dp(host, 26));
        emptyParams.setMarginEnd(dp(host, 26));
        emptyParams.topMargin = dp(host, 22);
        container.addView(empty, emptyParams);

        View back = content.findViewWithTag("image:arrow-left");
        if (back != null) {
            back.setOnClickListener(v -> activity.back());
        }
        View.OnClickListener create = v -> activity.open("manager_create_model");
        View plus = content.findViewById(R.id.ops_5717_6730);
        container.removeView(fab);
        if (plus != null) container.removeView(plus);
        floatingButton = new android.widget.FrameLayout(host.requireContext());
        floatingButton.setClickable(true);
        floatingButton.setFocusable(true);
        floatingButton.setContentDescription("Criar modelo");
        floatingButton.setElevation(dp(host, 8));
        floatingButton.addView(fab, new android.widget.FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        fab.setOnClickListener(create);
        if (plus != null) {
            android.widget.FrameLayout.LayoutParams plusParams = new android.widget.FrameLayout.LayoutParams(
                    dp(host, 29), dp(host, 29), android.view.Gravity.CENTER);
            floatingButton.addView(plus, plusParams);
            plus.setOnClickListener(create);
        }
        floatingButton.setOnClickListener(create);
        android.widget.FrameLayout.LayoutParams floatingParams = new android.widget.FrameLayout.LayoutParams(
                dp(host, 70), dp(host, 70), android.view.Gravity.BOTTOM | android.view.Gravity.END);
        floatingParams.setMargins(0, 0, dp(host, 24), dp(host, 24));
        ViewGroup screenHost = (ViewGroup) content.getParent();
        screenHost.addView(floatingButton, floatingParams);
        floatingButton.setVisibility(View.INVISIBLE);
        if (content instanceof ViewGroup) {
            ViewGroup scrollContent = (ViewGroup) content;
            scrollContent.setClipToPadding(false);
            scrollContent.setPadding(scrollContent.getPaddingLeft(), scrollContent.getPaddingTop(),
                    scrollContent.getPaddingRight(), dp(host, 96));
        }

        content.setVisibility(View.INVISIBLE);
        loading = AppLoading.show(host.requireContext());
        load();
    }

    private void load() {
        model.listar(new ModeloEquipamentoViewModel.Callback<List<ModeloEquipamentoItem>>() {
            @Override
            public void success(List<ModeloEquipamentoItem> values) {
                if (!host.isAdded() || root == null) {
                    return;
                }
                adapter.submit(values);
                boolean hasItems = values != null && !values.isEmpty();
                list.setVisibility(hasItems ? View.VISIBLE : View.GONE);
                empty.setText("Nenhum modelo cadastrado.");
                empty.setVisibility(hasItems ? View.GONE : View.VISIBLE);
                AppLoading.hide(loading);
                loading = null;
                root.setVisibility(View.VISIBLE);
                if (floatingButton != null) floatingButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void failure(Throwable error) {
                if (!host.isAdded() || root == null) {
                    return;
                }
                adapter.submit(List.of());
                list.setVisibility(View.GONE);
                empty.setText("Não foi possível carregar os modelos. Toque para tentar novamente.");
                empty.setOnClickListener(v -> {
                    root.setVisibility(View.INVISIBLE);
                    loading = AppLoading.show(host.requireContext());
                    load();
                });
                empty.setVisibility(View.VISIBLE);
                AppLoading.hide(loading);
                loading = null;
                root.setVisibility(View.VISIBLE);
                if (floatingButton != null) floatingButton.setVisibility(View.VISIBLE);
                Toast.makeText(host.requireContext(), AutenticacaoViewModel.messageFrom(error), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showMenu(View anchor, ModeloEquipamentoItem item) {
        View menu = LayoutInflater.from(anchor.getContext()).inflate(R.layout.popup_app_request_menu, null, false);
        PopupWindow popup = new PopupWindow(menu, anchor.getResources().getDimensionPixelSize(R.dimen.app_popup_menu_width),
                ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popup.setBackgroundDrawable(androidx.core.content.ContextCompat.getDrawable(anchor.getContext(), R.drawable.bg_app_popup_menu));
        popup.setElevation(dp(host, 6));
        popup.setOutsideTouchable(true);
        menu.findViewById(R.id.tvPopupEdit).setOnClickListener(v -> {
            popup.dismiss();
            select(item);
            activity.open("manager_edit_model");
        });
        menu.findViewById(R.id.tvPopupDelete).setOnClickListener(v -> {
            popup.dismiss();
            select(item);
            activity.open("manager_delete_model");
        });
        popup.showAsDropDown(anchor, -popup.getWidth() + anchor.getWidth(), 0);
    }

    private void select(ModeloEquipamentoItem item) {
        activity.model.selectEntry("models", String.valueOf(item.id), item.nome);
        activity.model.field("devices", "modelId", String.valueOf(item.id));
        activity.model.field("models", "categoria", item.categoria);
        activity.model.field("models", "marca", item.marca);
        activity.model.field("models", "descricao", item.descricao);
    }

    public static void bindDialog(OperationsDialogFragment dialog, View content, String key) {
        OperationsActivity activity = (OperationsActivity) dialog.requireActivity();
        ModeloEquipamentoViewModel vm = new ViewModelProvider(dialog,
                new ModeloEquipamentoViewModelFactory(dialog.requireContext())).get(ModeloEquipamentoViewModel.class);
        if (key.equals("manager_delete_model")) {
            View cancel = content.findViewById(R.id.ops_5717_7881);
            View delete = content.findViewById(R.id.ops_5717_7880);
            TextView title = content.findViewById(R.id.ops_5717_7879);
            if (title != null) {
                title.setText("Deseja excluir “" + activity.model.selectedTitle() + "”?");
            }
            if (cancel != null) {
                cancel.setOnClickListener(v -> dialog.dismiss());
            }
            if (delete != null) {
                delete.setOnClickListener(v -> {
                    long id = parse(activity.model.selectedId());
                    if (id <= 0) {
                        return;
                    }
                    delete.setEnabled(false);
                    Dialog wait = AppLoading.show(dialog.requireContext());
                    vm.excluir(id, new ModeloEquipamentoViewModel.Callback<Void>() {
                        @Override
                        public void success(Void result) {
                            finish(dialog, activity, wait, delete, null, "Modelo excluído.");
                        }

                        @Override
                        public void failure(Throwable error) {
                            finish(dialog, activity, wait, delete, error, "Modelo excluído.");
                        }
                    });
                });
            }
            return;
        }
        boolean editing = key.equals("manager_edit_model");
        EditText name = content.findViewById(editing ? R.id.ops_5717_7825 : R.id.ops_5717_7841);
        EditText description = content.findViewById(editing ? R.id.manager_model_description_edit : R.id.manager_model_description_create);
        TextView category = content.findViewById(editing ? R.id.ops_5717_7830 : R.id.ops_5717_7844_input);
        EditText brand = content.findViewById(editing ? R.id.ops_5717_7832 : R.id.ops_5717_7848);
        View cancel = content.findViewById(editing ? R.id.ops_5717_7822 : R.id.ops_5717_7838);
        View save = content.findViewById(editing ? R.id.ops_5717_7821 : R.id.ops_5717_7837);
        if (editing) {
            name.setText(activity.model.selectedTitle());
            description.setText(activity.model.field("models", "descricao"));
            category.setText(activity.model.field("models", "categoria"));
            brand.setText(activity.model.field("models", "marca"));
        }
        prepareChoice(category);
        prepareChoice(brand);
        final List<OpcaoCadastro>[] options = new List[]{new ArrayList<>(), new ArrayList<>()};
        final boolean[] loaded = {false, false};
        vm.categorias(new ModeloEquipamentoViewModel.Callback<List<OpcaoCadastro>>() {
            @Override
            public void success(List<OpcaoCadastro> values) {
                options[0] = values == null ? new ArrayList<>() : values;
                loaded[0] = true;
            }

            @Override
            public void failure(Throwable error) {
                loaded[0] = true;
                Toast.makeText(dialog.requireContext(), AutenticacaoViewModel.messageFrom(error), Toast.LENGTH_LONG).show();
            }
        });
        vm.marcas(new ModeloEquipamentoViewModel.Callback<List<OpcaoCadastro>>() {
            @Override
            public void success(List<OpcaoCadastro> values) {
                options[1] = values == null ? new ArrayList<>() : values;
                loaded[1] = true;
            }

            @Override
            public void failure(Throwable error) {
                loaded[1] = true;
                Toast.makeText(dialog.requireContext(), AutenticacaoViewModel.messageFrom(error), Toast.LENGTH_LONG).show();
            }
        });
        category.setOnClickListener(v -> choose(dialog, category, options[0], loaded[0], "categoria"));
        brand.setOnClickListener(v -> choose(dialog, brand, options[1], loaded[1], "marca"));
        for (int arrowId : editing ? new int[]{R.id.ops_5717_7829} : new int[]{R.id.ops_5717_7845}) {
            View arrow = content.findViewById(arrowId);
            if (arrow != null) {
                arrow.setOnClickListener(v -> category.performClick());
            }
        }
        if (cancel != null) {
            cancel.setOnClickListener(v -> dialog.dismiss());
        }
        if (save == null) {
            return;
        }
        save.setOnClickListener(v -> {
            String n = name.getText().toString().trim(), d = description.getText().toString().trim();
            if (n.isEmpty()) {
                name.setError("Informe o nome do modelo.");
                return;
            }
            if (d.isEmpty()) {
                description.setError("Informe a descrição.");
                return;
            }
            long categoryId = optionId(options[0], category.getText().toString());
            long brandId = optionId(options[1], brand.getText().toString());
            if (categoryId <= 0) {
                category.setError("Selecione uma categoria.");
                return;
            }
            if (brandId <= 0) {
                brand.setError("Selecione uma marca.");
                return;
            }
            save.setEnabled(false);
            Dialog wait = AppLoading.show(dialog.requireContext());
            ModeloEquipamentoViewModel.Callback<ModeloEquipamentoItem> cb = new ModeloEquipamentoViewModel.Callback<>() {
                @Override
                public void success(ModeloEquipamentoItem value) {
                    finish(dialog, activity, wait, save, null, editing ? "Modelo atualizado." : "Modelo criado.");
                }

                @Override
                public void failure(Throwable error) {
                    AppLoading.hide(wait);
                    if (!dialog.isAdded()) {
                        return;
                    }
                    save.setEnabled(true);
                    Toast.makeText(dialog.requireContext(), AutenticacaoViewModel.messageFrom(error), Toast.LENGTH_LONG).show();
                }
            };
            if (editing) {
                vm.atualizar(parse(activity.model.selectedId()), n, d, categoryId, brandId, cb);
            } else {
                vm.criar(n, d, categoryId, brandId, cb);
            }
        });
    }

    private static void prepareChoice(TextView field) {
        if (field == null) {
            return;
        }
        field.setFocusable(false);
        field.setClickable(true);
        if (field instanceof EditText) {
            ((EditText) field).setCursorVisible(false);
            ((EditText) field).setShowSoftInputOnFocus(false);
        }
    }

    private static void choose(OperationsDialogFragment dialog, TextView field, List<OpcaoCadastro> values, boolean loaded, String label) {
        if (!loaded) {
            Toast.makeText(dialog.requireContext(), "Carregando opções…", Toast.LENGTH_SHORT).show();
            return;
        }
        if (values.isEmpty()) {
            Toast.makeText(dialog.requireContext(), "Nenhuma opção disponível para " + label + ".", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] names = values.stream().map(OpcaoCadastro::getNome).toArray(String[]::new);
        View anchor = field.getParent() instanceof View ? (View) field.getParent() : field;
        ListPopupWindow dropdown = new ListPopupWindow(dialog.requireContext());
        dropdown.setAnchorView(anchor);
        dropdown.setWidth(anchor.getWidth());
        dropdown.setModal(true);
        dropdown.setAdapter(new ArrayAdapter<>(dialog.requireContext(), R.layout.item_app_dropdown, names));
        dropdown.setOnItemClickListener((parent, row, position, id) -> {
            field.setText(names[position]);
            dropdown.dismiss();
        });
        dropdown.show();
    }

    private static long optionId(List<OpcaoCadastro> values, String name) {
        if (values != null) {
            for (OpcaoCadastro value : values) {
                if (value.getNome().equalsIgnoreCase(name.trim())) {
                    return value.getId();
                }
            }
        }
        return 0;
    }

    private static long parse(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static int dp(OperationsScreenFragment host, int value) {
        return Math.round(value * host.getResources().getDisplayMetrics().density);
    }

    private static void finish(OperationsDialogFragment dialog, OperationsActivity activity, Dialog loading, View button, Throwable error, String message) {
        AppLoading.hide(loading);
        if (!dialog.isAdded()) {
            return;
        }
        if (error != null) {
            button.setEnabled(true);
            Toast.makeText(dialog.requireContext(), AutenticacaoViewModel.messageFrom(error), Toast.LENGTH_LONG).show();
            return;
        }
        dialog.dismiss();
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        activity.refresh();
    }
}
