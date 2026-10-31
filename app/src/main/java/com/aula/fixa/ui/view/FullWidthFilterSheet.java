package com.aula.fixa.ui.view;

import android.view.ViewGroup;
import android.view.Window;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public abstract class FullWidthFilterSheet extends BottomSheetDialogFragment {
    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null) return;
        Window window = getDialog().getWindow();
        if (window != null) window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, window.getAttributes().height);
    }
}
