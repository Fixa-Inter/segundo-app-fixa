package com.aula.fixa.ui.view.operations;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.aula.fixa.ui.config.operations.ScreenCatalog;
import org.json.JSONObject;

/** Diálogos do protótipo com rolagem, teclado e ciclo de vida nativos. */
public final class OperationsDialogFragment extends DialogFragment {
    public static OperationsDialogFragment create(String key) {
        OperationsDialogFragment fragment = new OperationsDialogFragment();
        Bundle args = new Bundle(); args.putString("screen", key); fragment.setArguments(args);
        return fragment;
    }

    @NonNull @Override public Dialog onCreateDialog(Bundle state) {
        String key = requireArguments().getString("screen");
        View content = getLayoutInflater().inflate(ScreenCatalog.get(requireContext()).layout(requireContext(), key), null, false);
        content.setLayoutParams(new android.view.ViewGroup.LayoutParams(-1, -2));
        new OperationsBinder((OperationsActivity) requireActivity(), content, key, this).bind();
        com.aula.fixa.ui.view.task.PlanejamentoTarefasFluxo.bindDialog(
                (OperationsActivity) requireActivity(), content, key, this);
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(content).create();
        return dialog;
    }

    @Override public void onStart() {
        super.onStart();
        Window window = requireDialog().getWindow();
        if (window == null) return;
        JSONObject spec = ScreenCatalog.get(requireContext()).screen(requireArguments().getString("screen"));
        float scale = getResources().getDimension(com.aula.fixa.R.dimen.ops_content_width) / 412f;
        int width = Math.min(Math.round(spec.optInt("width") * scale), (int)(getResources().getDisplayMetrics().widthPixels * .94f));
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(width, spec.optBoolean("drawer") ? -1 : -2);
        if (spec.optBoolean("drawer")) window.setGravity(Gravity.START);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
