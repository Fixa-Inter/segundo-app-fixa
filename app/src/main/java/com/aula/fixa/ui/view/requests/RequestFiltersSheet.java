package com.aula.fixa.ui.view.requests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.AutoCompleteTextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.aula.fixa.R;
import com.aula.fixa.ui.config.FragmentUi;
import com.aula.fixa.ui.view.FullWidthFilterSheet;
import android.widget.ArrayAdapter;
import com.aula.fixa.adapter.cadastro.CadastroViewModel;
import com.aula.fixa.adapter.cadastro.CadastroViewModelFactory;
import com.aula.fixa.domain.model.OpcaoCadastro;
import java.util.List;
import java.util.Arrays;

/** Bottom sheet "Filtrar solicitações" (Figma "Filtros"). */
public class RequestFiltersSheet extends FullWidthFilterSheet {
    public interface Listener { void onFilters(String period, String location, String category, String status); }

    public static final String TAG = "RequestFiltersSheet";
    private CadastroViewModel viewModel;

    @Override
    public int getTheme() {
        return R.style.ThemeOverlay_FIXA_App_BottomSheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_app_request_filters, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, new CadastroViewModelFactory(requireContext()))
                .get(CadastroViewModel.class);
        setDefault(view, R.id.fieldPeriod, R.string.app_filter_period_default);
        setDefault(view, R.id.fieldLocation, R.string.app_filter_location_default);
        setDefault(view, R.id.fieldStatus, R.string.app_filter_status_default);
        setDefault(view, R.id.fieldCategory, R.string.app_filter_location_default);
        loadOptions(view);
        ((ImageView) view.findViewById(R.id.fieldPeriod).findViewById(R.id.ivFilterIcon))
                .setImageResource(R.drawable.ic_calendar_20);
        view.findViewById(R.id.btnApply).setOnClickListener(v -> {
            if (getTargetFragment() instanceof Listener) {
                Listener listener = (Listener) getTargetFragment();
                listener.onFilters(value(view, R.id.fieldPeriod), value(view, R.id.fieldLocation), value(view, R.id.fieldCategory), value(view, R.id.fieldStatus));
            }
            dismiss();
        });
        view.findViewById(R.id.btnClear).setOnClickListener(v -> {
            setDefault(view, R.id.fieldPeriod, R.string.app_filter_period_default);
            setDefault(view, R.id.fieldLocation, R.string.app_filter_location_default);
            setDefault(view, R.id.fieldStatus, R.string.app_filter_status_default);
            setDefault(view, R.id.fieldCategory, R.string.app_filter_location_default);
        });
    }

    private String value(View root, int id) { return ((AutoCompleteTextView) root.findViewById(id).findViewById(R.id.actvFilterValue)).getText().toString(); }

    private void loadOptions(View view) {
        final List<OpcaoCadastro>[] locations = new List[]{null};
        final List<OpcaoCadastro>[] categories = new List[]{null};
        final boolean[] completed = new boolean[2];
        Runnable showWhenReady = () -> {
            if (!completed[0] || !completed[1] || !isAdded() || getView() != view) return;
            setAdapter(view, R.id.fieldLocation, locations[0]);
            setAdapter(view, R.id.fieldCategory, categories[0]);
            setAdapter(view, R.id.fieldPeriod, Arrays.asList(new OpcaoCadastro(1, "Últimos 30 dias"), new OpcaoCadastro(2, "Últimos 3 meses"), new OpcaoCadastro(3, "Último ano"), new OpcaoCadastro(4, "Todos")));
            setAdapter(view, R.id.fieldStatus, Arrays.asList(new OpcaoCadastro(1, "Todas"), new OpcaoCadastro(2, "Pendente"), new OpcaoCadastro(3, "Aprovado"), new OpcaoCadastro(4, "Reprovado"), new OpcaoCadastro(5, "Concluído")));
        };
        viewModel.listarLocais(new CadastroViewModel.Callback<List<OpcaoCadastro>>() {
            @Override public void aoConcluir(List<OpcaoCadastro> values) { locations[0] = values; completed[0] = true; showWhenReady.run(); }
            @Override public void aoFalhar(Throwable error) { locations[0] = java.util.Collections.emptyList(); completed[0] = true; showWhenReady.run(); }
        });
        viewModel.listarCategorias(new CadastroViewModel.Callback<List<OpcaoCadastro>>() {
            @Override public void aoConcluir(List<OpcaoCadastro> values) { categories[0] = values; completed[1] = true; showWhenReady.run(); }
            @Override public void aoFalhar(Throwable error) { categories[0] = java.util.Collections.emptyList(); completed[1] = true; showWhenReady.run(); }
        });
    }

    private void setAdapter(View root, int id, List<OpcaoCadastro> values) {
        AutoCompleteTextView field = root.findViewById(id).findViewById(R.id.actvFilterValue);
        field.setAdapter(new ArrayAdapter<>(root.getContext(), R.layout.item_app_dropdown, values));
        field.setOnClickListener(v -> field.showDropDown());
        field.setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) field.showDropDown();
            return false;
        });
    }

    private void setDefault(View root, @IdRes int fieldId, int textRes) {
        AutoCompleteTextView field = root.findViewById(fieldId).findViewById(R.id.actvFilterValue);
        field.setText("");
        field.setHint(textRes);
    }
}
