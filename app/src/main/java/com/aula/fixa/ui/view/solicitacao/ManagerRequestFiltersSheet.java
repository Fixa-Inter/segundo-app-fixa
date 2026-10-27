package com.aula.fixa.ui.view.solicitacao;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.R;
import com.aula.fixa.adapter.cadastro.CadastroViewModel;
import com.aula.fixa.adapter.cadastro.CadastroViewModelFactory;
import com.aula.fixa.domain.model.OpcaoCadastro;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Filtros de solicitações do gestor, com a mesma apresentação da tela de solicitante. */
public final class ManagerRequestFiltersSheet extends BottomSheetDialogFragment {
    public interface Listener {
        void onManagerRequestFilters(String period, String location, String category, String author);
    }

    public static final String TAG = "ManagerRequestFiltersSheet";

    @Override public int getTheme() { return R.style.ThemeOverlay_FIXA_App_BottomSheet; }

    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_ops_manager_request_filters, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setDefault(view, R.id.fieldPeriod, R.string.app_filter_period_default);
        setDefault(view, R.id.fieldLocation, R.string.app_filter_location_default);
        setDefault(view, R.id.fieldCategory, R.string.app_filter_location_default);
        setDefault(view, R.id.fieldAuthor, R.string.app_filter_author_default);
        ((ImageView) view.findViewById(R.id.fieldPeriod).findViewById(R.id.ivFilterIcon))
                .setImageResource(R.drawable.ic_calendar_20);

        AutoCompleteTextView author = field(view, R.id.fieldAuthor);
        author.setThreshold(0);
        author.setKeyListener(null);
        author.setCursorVisible(false);

        loadOptions(view);
        ArrayList<String> authors = getArguments() == null
                ? new ArrayList<>() : getArguments().getStringArrayList(ARG_AUTHORS);
        setStringAdapter(view, R.id.fieldAuthor, authors == null ? Collections.emptyList() : authors);
        view.findViewById(R.id.btnApply).setOnClickListener(v -> {
            Fragment target = getTargetFragment();
            if (target instanceof Listener) {
                ((Listener) target).onManagerRequestFilters(
                        value(view, R.id.fieldPeriod), value(view, R.id.fieldLocation),
                        value(view, R.id.fieldCategory), value(view, R.id.fieldAuthor));
            }
            dismiss();
        });
        view.findViewById(R.id.btnClear).setOnClickListener(v -> {
            setDefault(view, R.id.fieldPeriod, R.string.app_filter_period_default);
            setDefault(view, R.id.fieldLocation, R.string.app_filter_location_default);
            setDefault(view, R.id.fieldCategory, R.string.app_filter_location_default);
            setDefault(view, R.id.fieldAuthor, R.string.app_filter_author_default);
            Fragment target = getTargetFragment();
            if (target instanceof Listener) {
                ((Listener) target).onManagerRequestFilters("", "", "", "");
            }
        });
    }

    private static final String ARG_AUTHORS = "authors";

    public void setAuthorOptions(List<String> authors) {
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_AUTHORS, new ArrayList<>(authors == null ? Collections.emptyList() : authors));
        setArguments(args);
    }

    private void loadOptions(View view) {
        CadastroViewModel model = new ViewModelProvider(this,
                new CadastroViewModelFactory(requireContext())).get(CadastroViewModel.class);
        setAdapter(view, R.id.fieldPeriod, Arrays.asList(
                new OpcaoCadastro(1, "Últimos 30 dias"), new OpcaoCadastro(2, "Últimos 3 meses"),
                new OpcaoCadastro(3, "Último ano"), new OpcaoCadastro(4, "Todos")));
        model.listarLocais(new CadastroViewModel.Callback<List<OpcaoCadastro>>() {
            @Override public void aoConcluir(List<OpcaoCadastro> values) { setAdapterIfActive(view, R.id.fieldLocation, values); }
            @Override public void aoFalhar(Throwable error) { setAdapterIfActive(view, R.id.fieldLocation, Collections.emptyList()); }
        });
        model.listarCategorias(new CadastroViewModel.Callback<List<OpcaoCadastro>>() {
            @Override public void aoConcluir(List<OpcaoCadastro> values) { setAdapterIfActive(view, R.id.fieldCategory, values); }
            @Override public void aoFalhar(Throwable error) { setAdapterIfActive(view, R.id.fieldCategory, Collections.emptyList()); }
        });
    }

    private void setAdapterIfActive(View view, int id, List<OpcaoCadastro> values) {
        if (!isAdded() || getView() != view) return;
        setAdapter(view, id, values);
    }

    private void setAdapter(View view, int id, List<OpcaoCadastro> values) {
        AutoCompleteTextView field = field(view, id);
        field.setAdapter(new ArrayAdapter<>(view.getContext(), R.layout.item_app_dropdown, values));
        field.setOnClickListener(v -> field.showDropDown());
        field.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) field.showDropDown();
            return false;
        });
    }

    private void setStringAdapter(View view, int id, List<String> values) {
        AutoCompleteTextView field = field(view, id);
        field.setAdapter(new ArrayAdapter<>(view.getContext(), R.layout.item_app_dropdown, values));
        field.setOnClickListener(v -> field.showDropDown());
        field.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) field.showDropDown();
            return false;
        });
    }

    private AutoCompleteTextView field(View root, int id) {
        return root.findViewById(id).findViewById(R.id.actvFilterValue);
    }

    private String value(View root, int id) { return field(root, id).getText().toString(); }

    private void setDefault(View root, int id, int hint) {
        AutoCompleteTextView field = field(root, id);
        field.setText("");
        field.setHint(hint);
    }
}
