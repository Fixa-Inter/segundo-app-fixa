package com.aula.fixa.ui.requests;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.ui.common.AppText;
import com.aula.fixa.ui.common.DashedLineDrawable;
import com.aula.fixa.domain.model.RequestItem;
import com.aula.fixa.domain.model.TimelineStep;

import java.util.Arrays;
import java.util.List;

public class RequestDetailsFragment extends Fragment {

    public RequestDetailsFragment() {
        super(R.layout.fragment_request_details);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String title = getArguments() != null ? getArguments().getString("requestTitle", "") : "";
        int space = title.indexOf(' ');
        String html = space > 0
                ? "<b><font color=\"#153D7D\">" + title.substring(0, space) + "</font></b>" + title.substring(space)
                : title;
        AppText.setHtml(view.findViewById(R.id.tvTitle), html);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        view.findViewById(R.id.btnMore).setOnClickListener(v -> RequestMenuPopup.show(v,
                new RequestItem(title, "", "", RequestItem.Status.ANALYSIS)));

        RecyclerView list = view.findViewById(R.id.rvTimeline);
        list.setAdapter(new TimelineAdapter(Arrays.asList(
                new TimelineStep(getString(R.string.app_details_step_opened), "20/08", "15:30", TimelineStep.State.DONE),
                new TimelineStep(getString(R.string.app_details_step_analysis), "20/08", "15:30", TimelineStep.State.CURRENT),
                new TimelineStep(getString(R.string.app_details_step_approved), "", "", TimelineStep.State.PENDING),
                new TimelineStep(getString(R.string.app_details_step_done), "", "", TimelineStep.State.PENDING)
        )));
    }

    /** Timeline vertical (Figma "Detalhes"): concluída verde, atual azul, pendentes cinza sem card. */
    static class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {

        private final List<TimelineStep> steps;

        TimelineAdapter(List<TimelineStep> steps) {
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
            private final View dot, dotInner, check, card, line;
            private final TextView date, title, time;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                dot = itemView.findViewById(R.id.viewDot);
                dotInner = itemView.findViewById(R.id.viewDotInner);
                check = itemView.findViewById(R.id.ivDotCheck);
                card = itemView.findViewById(R.id.cardStep);
                line = itemView.findViewById(R.id.viewLine);
                date = itemView.findViewById(R.id.tvStepDate);
                title = itemView.findViewById(R.id.tvStepTitle);
                time = itemView.findViewById(R.id.tvStepTime);
            }

            void bind(TimelineStep step, boolean last) {
                line.setVisibility(last ? View.INVISIBLE : View.VISIBLE);
                line.setBackground(dashedLine(step.state));
                date.setText(step.date);
                title.setText(step.title);
                time.setText(step.time);
                switch (step.state) {
                    case DONE:
                        dot.setBackgroundResource(R.drawable.bg_app_circle_done);
                        dotInner.setVisibility(View.GONE);
                        check.setVisibility(View.VISIBLE);
                        card.setVisibility(View.VISIBLE);
                        card.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.app_status_done_soft));
                        title.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.app_status_done_text));
                        break;
                    case CURRENT:
                        dot.setBackgroundResource(R.drawable.bg_app_circle_approved);
                        dotInner.setVisibility(View.VISIBLE);
                        check.setVisibility(View.GONE);
                        card.setVisibility(View.VISIBLE);
                        card.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.app_status_approved_soft));
                        title.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.app_status_approved_text));
                        break;
                    default:
                        dot.setBackgroundResource(R.drawable.bg_app_circle_muted);
                        dotInner.setVisibility(View.GONE);
                        check.setVisibility(View.GONE);
                        // Figma: passo pendente é só o ponto cinza; o card some mas mantém a altura da etapa.
                        card.setVisibility(View.INVISIBLE);
                }
            }

            private DashedLineDrawable dashedLine(TimelineStep.State state) {
                int color = state == TimelineStep.State.DONE ? R.color.app_status_done
                        : state == TimelineStep.State.CURRENT ? R.color.app_primary_deep
                        : R.color.app_status_pending;
                Resources res = itemView.getResources();
                return new DashedLineDrawable(ContextCompat.getColor(itemView.getContext(), color),
                        res.getDimension(R.dimen.app_details_timeline_line_stroke),
                        res.getDimension(R.dimen.app_details_timeline_line_dash),
                        res.getDimension(R.dimen.app_details_timeline_line_dash));
            }
        }
    }
}
