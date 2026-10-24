package com.aula.fixa.ui.view.newrequest;

import com.aula.fixa.adapter.newrequest.SolicitacaoFormularioViewModel;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.lifecycle.ViewModelProvider;

import com.aula.fixa.R;
import com.aula.fixa.ui.config.AppText;
import com.google.android.material.button.MaterialButton;

/**
 * Base dos passos de "Abrir solicitação": configura o indicador (1..3) e o rodapé Voltar/Próximo.
 * Os três passos mostram "Próximo"; "Finalizar" fica na tela de confirmação.
 */
public abstract class NewRequestStepFragment extends Fragment {

    /** Level do ClipDrawable equivalente a 50% (máximo 10000). */
    private static final int HALF_LEVEL = 5000;

    private final int step;
    private final int nextAction;
    private final int titleRes;
    protected SolicitacaoFormularioViewModel form;

    protected NewRequestStepFragment(@LayoutRes int layout, int step, @IdRes int nextAction, @StringRes int titleRes) {
        super(layout);
        this.step = step;
        this.nextAction = nextAction;
        this.titleRes = titleRes;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController nav = NavHostFragment.findNavController(this);
        form = new ViewModelProvider(nav.getViewModelStoreOwner(nav.getGraph().getId())).get(SolicitacaoFormularioViewModel.class);
        AppText.setHtml(view.findViewById(R.id.tvTitle), titleRes);
        bindIndicator(view);
        restoreForm(view);

        MaterialButton next = view.findViewById(R.id.btnStepNext);
        next.setText(R.string.app_next);
        next.setOnClickListener(v -> {
            // Evita navegação dupla quando o botão é tocado duas vezes durante a transição.
            if (nav.getCurrentDestination() == null || nav.getCurrentDestination().getId() != currentDestinationId()) return;
            saveForm(view);
            if (!validateStep(view)) return;
            nav.navigate(nextAction);
        });
        view.findViewById(R.id.btnStepBack).setOnClickListener(v -> {
            saveForm(view);
            nav.navigateUp();
        });
    }

    /** Id do destino deste passo, para ignorar cliques repetidos durante a transição. */
    protected abstract int currentDestinationId();

    /** Preenche os campos com o que já foi digitado (ao voltar para o passo). */
    protected void restoreForm(View root) {}

    /** Copia os campos para o ViewModel antes de sair do passo. */
    protected void saveForm(View root) {}

    /** Validação obrigatória antes de avançar para o próximo passo. */
    protected boolean validateStep(View root) { return true; }

    protected boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    protected void showValidationMessage(String message) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show();
    }

    private void bindIndicator(View root) {
        View indicator = root.findViewById(R.id.stepIndicator);
        if (indicator == null) return;

        styleCircle(indicator, R.id.tvStep1, R.id.tvStep1Label, stateFor(1));
        styleCircle(indicator, R.id.tvStep2, R.id.tvStep2Label, stateFor(2));
        styleCircle(indicator, R.id.tvStep3, R.id.tvStep3Label, stateFor(3));

        // Linha após o passo: cheia (verde) se concluído, meia (azul) se é o atual, cinza se pendente.
        bindLine(indicator.findViewById(R.id.viewLine1), 1);
        bindLine(indicator.findViewById(R.id.viewLine2), 2);
    }

    private void bindLine(View line, int index) {
        if (step > index) {
            line.setBackgroundResource(R.drawable.bg_app_indicator_line_done);
        } else if (step == index) {
            line.setBackgroundResource(R.drawable.bg_app_indicator_line_progress);
            line.getBackground().setLevel(HALF_LEVEL);
        } else {
            line.setBackgroundResource(R.drawable.bg_app_indicator_line_pending);
        }
    }

    private int stateFor(int index) {
        return index < step ? 0 : index == step ? 1 : 2; // 0 concluído, 1 atual, 2 pendente
    }

    private void styleCircle(View root, @IdRes int circleId, @IdRes int labelId, int state) {
        @DrawableRes int bg = state == 0 ? R.drawable.bg_app_circle_done
                : state == 1 ? R.drawable.bg_app_circle_primary : R.drawable.bg_app_circle_muted;
        @ColorRes int labelColor = state == 0 ? R.color.app_status_done
                : state == 1 ? R.color.app_primary : R.color.app_text_muted;
        root.findViewById(circleId).setBackgroundResource(bg);
        ((TextView) root.findViewById(labelId))
                .setTextColor(ContextCompat.getColor(requireContext(), labelColor));
    }
}
