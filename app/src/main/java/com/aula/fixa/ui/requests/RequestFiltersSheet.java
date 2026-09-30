package com.aula.fixa.ui.requests;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aula.fixa.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/** Bottom sheet "Filtrar solicitações" (Figma "Filtros"). */
public class RequestFiltersSheet extends BottomSheetDialogFragment {

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
        view.findViewById(R.id.btnApply).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btnClear).setOnClickListener(v -> {
            setDefault(view, R.id.fieldPeriod, R.string.app_filter_period_default);
            setDefault(view, R.id.fieldLocation, R.string.app_filter_location_default);
            setDefault(view, R.id.fieldStatus, R.string.app_filter_status_default);
        });
    }

    private void setDefault(View root, @IdRes int fieldId, int textRes) {
        AutoCompleteTextView field = root.findViewById(fieldId).findViewById(R.id.actvFilterValue);
        field.setText("");
        field.setHint(textRes);
    }
}
