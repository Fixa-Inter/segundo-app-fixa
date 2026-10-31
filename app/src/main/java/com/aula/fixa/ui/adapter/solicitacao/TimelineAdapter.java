package com.aula.fixa.ui.adapter.solicitacao;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.aula.fixa.R;
import com.aula.fixa.domain.enums.StatusProblema;
import com.aula.fixa.domain.model.solicitacao.EtapaLinhaTempo;
import com.aula.fixa.ui.config.DashedLineDrawable;
import java.util.List;
/**
 * Timeline vertical (Figma "Detalhes"): concluída verde, atual azul, pendentes cinza sem card.
 */
public final class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {

    private final List<EtapaLinhaTempo> steps;

    public TimelineAdapter(List<EtapaLinhaTempo> steps) {
        this.steps = steps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_timeline_step, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(steps.get(position), position == steps.size() - 1);
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final View dot, dotInner, check, refused, card, line;
        private final TextView date, title, time;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            dot = itemView.findViewById(R.id.viewDot);
            dotInner = itemView.findViewById(R.id.viewDotInner);
            check = itemView.findViewById(R.id.ivDotCheck);
            refused = itemView.findViewById(R.id.tvDotRefused);
            card = itemView.findViewById(R.id.cardStep);
            line = itemView.findViewById(R.id.viewLine);
            date = itemView.findViewById(R.id.tvStepDate);
            title = itemView.findViewById(R.id.tvStepTitle);
            time = itemView.findViewById(R.id.tvStepTime);
        }

        void bind(EtapaLinhaTempo step, boolean last) {
            line.setVisibility(last ? View.INVISIBLE : View.VISIBLE);
            line.setBackground(dashedLine(step.status, step.atual));
            date.setText(step.date);
            title.setText(step.title);
            time.setText(step.time);
            if (step.atual) {
                dot.setBackgroundResource(R.drawable.bg_app_circle_approved);
                dotInner.setVisibility(View.VISIBLE);
                check.setVisibility(View.GONE);
                refused.setVisibility(View.GONE);
                card.setVisibility(View.VISIBLE);
                card.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.app_timeline_approved_soft));
                title.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.app_status_approved_text));
                return;
            }
            switch (step.status) {
                case CONCLUIDO:
                    renderConcluido();
                    break;
                case APROVADO:
                    renderAprovado();
                    break;
                case REPROVADO:
                    dot.setBackgroundResource(R.drawable.bg_app_circle_refused);
                    dotInner.setVisibility(View.GONE);
                    check.setVisibility(View.GONE);
                    refused.setVisibility(View.VISIBLE);
                    card.setVisibility(View.VISIBLE);
                    card.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.app_timeline_refused_soft));
                    title.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.app_status_refused_text));
                    break;
                default:
                    dot.setBackgroundResource(R.drawable.bg_app_circle_muted);
                    dotInner.setVisibility(View.GONE);
                    check.setVisibility(View.GONE);
                    refused.setVisibility(View.GONE);
                    // Figma: passo pendente é só o ponto cinza; o card some mas mantém a altura da etapa.
                    card.setVisibility(View.INVISIBLE);
            }
        }

        private void renderAprovado() {
            // A aprovação conclui esta etapa, mas não representa a conclusão da solicitação.
            dot.setBackgroundResource(R.drawable.bg_app_circle_done);
            dotInner.setVisibility(View.GONE);
            check.setVisibility(View.VISIBLE);
            refused.setVisibility(View.GONE);
            card.setVisibility(View.VISIBLE);
            card.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.app_timeline_done_soft));
            title.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.app_status_done_text));
        }

        private void renderConcluido() {
            // Somente esta etapa sinaliza que a solicitação foi efetivamente concluída.
            dot.setBackgroundResource(R.drawable.bg_app_circle_done);
            dotInner.setVisibility(View.GONE);
            check.setVisibility(View.VISIBLE);
            refused.setVisibility(View.GONE);
            card.setVisibility(View.VISIBLE);
            card.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.app_timeline_done_soft));
            title.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.app_status_done_text));
        }

        private DashedLineDrawable dashedLine(StatusProblema status, boolean atual) {
            int color = atual ? R.color.app_primary_deep
                    : status == StatusProblema.CONCLUIDO || status == StatusProblema.APROVADO ? R.color.app_status_done
                    : status == StatusProblema.REPROVADO ? R.color.app_status_refused
                    : R.color.app_status_pending;
            Resources res = itemView.getResources();
            return new DashedLineDrawable(ContextCompat.getColor(itemView.getContext(), color),
                    res.getDimension(R.dimen.app_details_timeline_line_stroke),
                    res.getDimension(R.dimen.app_details_timeline_line_dash),
                    res.getDimension(R.dimen.app_details_timeline_line_dash));
        }
    }
}
