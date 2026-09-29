package com.aula.fixa.ui.newrequest;

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
import androidx.navigation.Navigation;

import com.aula.fixa.R;
import com.aula.fixa.ui.common.AppText;
import com.google.android.material.button.MaterialButton;

/**
 * Base dos passos de "Abrir solicitação": configura o indicador (1..3) e o rodapé Voltar/Próximo.
 * O último passo mostra "Finalizar".
 */
public abstract class NewRequestStepFragment extends Fragment {

    private final int step;
    private final int nextAction;
    private final int titleRes;

    protected NewRequestStepFragment(@LayoutRes int layout, int step, @IdRes int nextAction, @StringRes int titleRes) {
        super(layout);
        this.step = step;
        this.nextAction = nextAction;
        this.titleRes = titleRes;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppText.setHtml(view.findViewById(R.id.tvTitle), titleRes);
        bindIndicator(view);

        MaterialButton next = view.findViewById(R.id.btnStepNext);
        next.setText(step == 3 ? R.string.app_finish : R.string.app_next);
        next.setOnClickListener(v -> Navigation.findNavController(v).navigate(nextAction));
        view.findViewById(R.id.btnStepBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void bindIndicator(View root) {
        View indicator = root.findViewById(R.id.stepIndicator);
        if (indicator == null) return;

        styleCircle(indicator, R.id.tvStep1, R.id.tvStep1Label, stateFor(1));
        styleCircle(indicator, R.id.tvStep2, R.id.tvStep2Label, stateFor(2));
        styleCircle(indicator, R.id.tvStep3, R.id.tvStep3Label, stateFor(3));

        // Linha 1: cheia quando o passo 1 está concluído; meia quando o 1 é o atual.
        indicator.findViewById(R.id.viewLine1).setBackgroundResource(
                step > 1 ? R.drawable.bg_app_indicator_line_done : R.drawable.bg_app_indicator_line_pending);
        indicator.findViewById(R.id.viewLine1Progress).setVisibility(step == 1 ? View.VISIBLE : View.INVISIBLE);
        indicator.findViewById(R.id.viewLine2).setBackgroundResource(
                step > 2 ? R.drawable.bg_app_indicator_line_done : R.drawable.bg_app_indicator_line_pending);
        indicator.findViewById(R.id.viewLine2Progress).setVisibility(step == 2 ? View.VISIBLE : View.INVISIBLE);
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
