package com.aula.fixa.ui.view.operations;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.R;
import com.aula.fixa.adapter.auth.AutenticacaoViewModel;
import com.aula.fixa.adapter.solicitacao.SolicitacaoGestorViewModel;
import com.aula.fixa.adapter.solicitacao.SolicitacaoGestorViewModelFactory;
import com.aula.fixa.domain.enums.StatusProblema;
import com.aula.fixa.ui.config.AppLoading;
import com.aula.fixa.ui.config.operations.ScreenCatalog;
import org.json.JSONObject;

/** Diálogos do protótipo com rolagem, teclado e ciclo de vida nativos. */
public final class OperationsDialogFragment extends DialogFragment {
    public static OperationsDialogFragment create(String key) {
        return create(key, null);
    }

    public static OperationsDialogFragment create(String key, Bundle extras) {
        OperationsDialogFragment fragment = new OperationsDialogFragment();
        Bundle args = extras == null ? new Bundle() : new Bundle(extras);
        args.putString("screen", key);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull @Override public Dialog onCreateDialog(Bundle state) {
        String key = requireArguments().getString("screen");
        View content = getLayoutInflater().inflate(ScreenCatalog.get(requireContext()).layout(requireContext(), key), null, false);
        content.setLayoutParams(new android.view.ViewGroup.LayoutParams(-1, -2));
        if (key.equals("manager_create_category") || key.equals("manager_edit_category")
                || key.equals("manager_delete_category")) {
            com.aula.fixa.ui.view.equipamento.CategoriaEquipamentoGestorFluxo.bindDialog(this, content, key);
        } else if (key.equals("manager_reject_request")) {
            bindRejectRequest(content);
        } else {
            new OperationsBinder((OperationsActivity) requireActivity(), content, key, this).bind();
            com.aula.fixa.ui.view.task.PlanejamentoTarefasFluxo.bindDialog(
                    (OperationsActivity) requireActivity(), content, key, this);
        }
        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(content).create();
        return dialog;
    }

    private void bindRejectRequest(View content) {
        OperationsActivity activity = (OperationsActivity) requireActivity();
        EditText reason = content.findViewById(R.id.ops_5717_7097);
        View cancel = content.findViewById(R.id.ops_5717_7096);
        View cancelText = content.findViewById(R.id.ops_5717_7100);
        View reject = content.findViewById(R.id.ops_5717_7095);
        cancel.setOnClickListener(v -> dismiss());
        cancelText.setOnClickListener(v -> dismiss());
        reject.setOnClickListener(v -> {
            String value = reason.getText().toString().trim();
            if (value.isEmpty()) {
                reason.setError("Informe o motivo da reprovação.");
                return;
            }
            long requestId = requireArguments().getLong("requestId", 0);
            if (requestId <= 0) {
                Toast.makeText(activity, "Solicitação inválida.", Toast.LENGTH_LONG).show();
                dismiss();
                return;
            }
            reject.setEnabled(false);
            Dialog loading = AppLoading.show(requireContext());
            SolicitacaoGestorViewModel model = new ViewModelProvider(this,
                    new SolicitacaoGestorViewModelFactory(requireContext()))
                    .get(SolicitacaoGestorViewModel.class);
            model.updateStatus(requestId, StatusProblema.REPROVADO, value,
                    new SolicitacaoGestorViewModel.Callback<com.aula.fixa.domain.model.solicitacao.SolicitacaoGestor>() {
                @Override public void success(com.aula.fixa.domain.model.solicitacao.SolicitacaoGestor result) {
                    AppLoading.hide(loading);
                    if (!isAdded()) return;
                    dismiss();
                    Toast.makeText(activity, "Solicitação reprovada.", Toast.LENGTH_SHORT).show();
                    activity.back();
                }
                @Override public void failure(Throwable error) {
                    AppLoading.hide(loading);
                    if (!isAdded()) return;
                    reject.setEnabled(true);
                    Toast.makeText(requireContext(), AutenticacaoViewModel.messageFrom(error),
                            Toast.LENGTH_LONG).show();
                }
            });
        });
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
