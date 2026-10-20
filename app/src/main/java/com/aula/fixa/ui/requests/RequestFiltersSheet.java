package com.aula.fixa.ui.requests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.AutoCompleteTextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aula.fixa.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.widget.ArrayAdapter;
import com.aula.fixa.data.local.preferences.SessionStorage;
import com.aula.fixa.data.repository.RequestRepository;
import com.aula.fixa.data.remote.dto.CadastroOption;
import java.util.List;

/** Bottom sheet "Filtrar solicitações" (Figma "Filtros"). */
public class RequestFiltersSheet extends BottomSheetDialogFragment {
    public interface Listener { void onFilters(String period, String location, String category, String status); }

    public static final String TAG = "RequestFiltersSheet";

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
        new Thread(() -> {
            try {
                RequestRepository repository = new RequestRepository(new SessionStorage(requireContext()));
                List<CadastroOption> locations = repository.options(repository.locais());
                List<CadastroOption> categories = repository.options(repository.categorias());
                requireActivity().runOnUiThread(() -> {
                    setAdapter(view, R.id.fieldLocation, locations);
                    setAdapter(view, R.id.fieldCategory, categories);
                    setAdapter(view, R.id.fieldPeriod, List.of(new CadastroOption(1, "Últimos 30 dias"), new CadastroOption(2, "Últimos 3 meses"), new CadastroOption(3, "Último ano"), new CadastroOption(4, "Todos")));
                    setAdapter(view, R.id.fieldStatus, List.of(new CadastroOption(1, "Todas"), new CadastroOption(2, "Pendente"), new CadastroOption(3, "Aprovado"), new CadastroOption(4, "Reprovado"), new CadastroOption(5, "Concluído")));
                });
            } catch (Exception ignored) { }
        }).start();
    }

    private void setAdapter(View root, int id, List<CadastroOption> values) {
        AutoCompleteTextView field = root.findViewById(id).findViewById(R.id.actvFilterValue);
        field.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.item_app_dropdown, values));
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
