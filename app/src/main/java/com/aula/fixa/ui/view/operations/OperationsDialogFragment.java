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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.R;
import com.aula.fixa.adapter.auth.AutenticacaoViewModel;
import com.aula.fixa.adapter.solicitacao.SolicitacaoGestorViewModel;
import com.aula.fixa.adapter.solicitacao.SolicitacaoGestorViewModelFactory;
import com.aula.fixa.adapter.tecnico.TecnicoViewModel;
import com.aula.fixa.adapter.tecnico.TecnicoViewModelFactory;
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
        } else if (key.equals("manager_edit_competency")) {
            bindEditCompetency(content);
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

    private void bindEditCompetency(View content) {
        OperationsActivity activity = (OperationsActivity) requireActivity();
        Bundle args = requireArguments();
        TextView title = content.findViewById(R.id.ops_5717_6053);
        TextView oldValue = content.findViewById(R.id.ops_5717_6057);
        EditText newValue = content.findViewById(R.id.ops_5717_6051);
        View save = content.findViewById(R.id.ops_5717_6049);
        if (title != null) title.setText(args.getString("competenceName", "Competência"));
        if (oldValue != null) {
            String old = args.getString("oldScore", "_");
            oldValue.setText(old == null || old.trim().isEmpty() ? "_" : old);
            oldValue.setEnabled(false);
            oldValue.setFocusable(false);
        }
        if (newValue != null) {
            newValue.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                    | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                    | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
            newValue.setFilters(new android.text.InputFilter[]{(source, start, end, dest, dstart, dend) -> {
                String candidate = dest.subSequence(0, dstart).toString()
                        + source.subSequence(start, end) + dest.subSequence(dend, dest.length());
                if (candidate.isEmpty() || candidate.equals("-") || candidate.equals(".") || candidate.equals(",")) return null;
                if (!candidate.matches("-?\\d*(?:[.,]\\d{0,2})?")) return "";
                return null;
            }});
        }
        View cancel = content.findViewById(R.id.ops_5717_6050);
        if (cancel != null) cancel.setOnClickListener(v -> dismiss());
        if (save == null) return;
        save.setOnClickListener(v -> {
            if (newValue == null) return;
            String raw = newValue.getText().toString().trim().replace(',', '.');
            if (raw.isEmpty() || raw.equals(".") || raw.equals("-")) {
                newValue.setError("Informe um valor numérico.");
                return;
            }
            java.math.BigDecimal score;
            try { score = new java.math.BigDecimal(raw); }
            catch (NumberFormatException invalid) { newValue.setError("Informe um valor numérico válido."); return; }
            if (Math.max(0, score.stripTrailingZeros().scale()) > 2) {
                newValue.setError("Use no máximo duas casas decimais.");
                return;
            }
            long competenceId = args.getLong("competenceId", 0);
            if (competenceId <= 0) {
                Toast.makeText(activity, "Não foi possível identificar a competência.", Toast.LENGTH_LONG).show();
                return;
            }
            save.setEnabled(false);
            Dialog loading = AppLoading.show(requireContext());
            TecnicoViewModel model = new ViewModelProvider(this,
                    new TecnicoViewModelFactory(requireContext())).get(TecnicoViewModel.class);
            model.atualizarCompetencia(competenceId, score, new TecnicoViewModel.Callback<com.aula.fixa.domain.model.tecnico.CompetenciaTecnico>() {
                @Override public void success(com.aula.fixa.domain.model.tecnico.CompetenciaTecnico result) {
                    AppLoading.hide(loading);
                    if (!isAdded()) return;
                    dismiss();
                    Toast.makeText(activity, "Competência atualizada.", Toast.LENGTH_SHORT).show();
                    activity.refresh();
                }
                @Override public void failure(Throwable error) {
                    AppLoading.hide(loading);
                    if (!isAdded()) return;
                    save.setEnabled(true);
                    Toast.makeText(requireContext(), AutenticacaoViewModel.messageFrom(error), Toast.LENGTH_LONG).show();
                }
            });
        });
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
