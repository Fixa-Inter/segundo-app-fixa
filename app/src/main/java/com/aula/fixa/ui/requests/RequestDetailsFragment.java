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
import com.aula.fixa.ui.common.FragmentUi;
import com.aula.fixa.ui.common.DataChangeNotifier;
import com.aula.fixa.ui.common.DashedLineDrawable;
import com.aula.fixa.domain.model.RequestItem;
import com.aula.fixa.domain.model.TimelineStep;
import com.aula.fixa.data.local.preferences.SessionStorage;
import com.aula.fixa.data.repository.RequestRepository;
import com.google.gson.JsonObject;

import java.util.List;

public class RequestDetailsFragment extends Fragment {

    public RequestDetailsFragment() {
        super(R.layout.fragment_request_details);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String title = getArguments() != null ? getArguments().getString("requestTitle", "") : "";
        long requestId = getArguments() != null ? getArguments().getLong("requestId", 0) : 0;
        int space = title.indexOf(' ');
        String html = space > 0
                ? "<b><font color=\"#153D7D\">" + title.substring(0, space) + "</font></b>" + title.substring(space)
                : title;
        AppText.setHtml(view.findViewById(R.id.tvTitle), html);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        view.findViewById(R.id.btnMore).setOnClickListener(v -> RequestMenuPopup.show(v,
                new RequestItem(title, "", "", RequestItem.Status.ANALYSIS)));

        RecyclerView list = view.findViewById(R.id.rvTimeline);
        if (requestId > 0) {
            loadDetails(view, list, requestId);
            DataChangeNotifier.observe(getViewLifecycleOwner(), ignored -> {
                if (getView() == view) loadDetails(view, list, requestId);
            });
        }

        list.setAdapter(new TimelineAdapter(java.util.Collections.emptyList()));
    }

    private void loadDetails(View view, RecyclerView list, long id) {
        new Thread(() -> {
            try {
                JsonObject item = new RequestRepository(new SessionStorage(requireContext())).detalhes(id);
            FragmentUi.runIfViewActive(this, view, () -> {
                    ((TextView) view.findViewById(R.id.tvTitle)).setText(item.get("titulo").getAsString());
                String status = item.has("status") ? item.get("status").getAsString() : "PENDENTE";
                boolean editable = status.equalsIgnoreCase("PENDENTE")
                        || status.equalsIgnoreCase("EM_ANALISE")
                        || status.equalsIgnoreCase("EM ANÁLISE")
                        || status.equalsIgnoreCase("EM_ANALISE");
                view.findViewById(R.id.btnMore).setVisibility(editable ? View.VISIBLE : View.GONE);
                if (editable) {
                    RequestItem current = new RequestItem(
                            id,
                            item.has("titulo") ? item.get("titulo").getAsString() : "",
                            item.has("localUnidade") && !item.get("localUnidade").isJsonNull() ? item.get("localUnidade").getAsString() : "",
                            "",
                            RequestItem.Status.ANALYSIS,
                            item.has("descricaoProblema") ? item.get("descricaoProblema").getAsString() : "",
                            item.has("categoriaEquipamento") && !item.get("categoriaEquipamento").isJsonNull() ? item.get("categoriaEquipamento").getAsString() : "",
                            0);
                    view.findViewById(R.id.btnMore).setOnClickListener(v -> RequestMenuPopup.show(v, current));
                }
                list.setAdapter(new TimelineAdapter(timeline(status)));
                });
            } catch (Exception e) {
                FragmentUi.runIfViewActive(this, view, () -> android.widget.Toast.makeText(view.getContext(), e.getMessage(), android.widget.Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private List<TimelineStep> timeline(String status) {
        List<TimelineStep> steps = new java.util.ArrayList<>();
        boolean refused = status.equalsIgnoreCase("REPROVADO") || status.equalsIgnoreCase("RECUSADO");
        boolean approved = status.equalsIgnoreCase("APROVADO");
        boolean concluded = status.equalsIgnoreCase("CONCLUIDO") || status.equalsIgnoreCase("CONCLUÍDO");
        steps.add(new TimelineStep(getString(R.string.app_details_step_opened), "", "", TimelineStep.State.DONE));
        steps.add(new TimelineStep(getString(R.string.app_details_step_analysis), "", "", refused || approved || concluded ? TimelineStep.State.DONE : TimelineStep.State.CURRENT));
        if (refused) {
            steps.add(new TimelineStep(getString(R.string.app_details_step_refused), "", "", TimelineStep.State.REFUSED));
            return steps;
        }
        steps.add(new TimelineStep(getString(R.string.app_details_step_approved), "", "", approved || concluded ? TimelineStep.State.DONE : TimelineStep.State.PENDING));
        steps.add(new TimelineStep(getString(R.string.app_details_step_done), "", "", concluded ? TimelineStep.State.DONE : approved ? TimelineStep.State.CURRENT : TimelineStep.State.PENDING));
        return steps;
    }

    /**
     * Timeline vertical (Figma "Detalhes"): concluída verde, atual azul, pendentes cinza sem card.
     */
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
                        refused.setVisibility(View.GONE);
                        card.setVisibility(View.VISIBLE);
                        card.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.app_status_done_soft));
                        title.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.app_status_done_text));
                        break;
                    case CURRENT:
                        dot.setBackgroundResource(R.drawable.bg_app_circle_approved);
                        dotInner.setVisibility(View.VISIBLE);
                        check.setVisibility(View.GONE);
                        refused.setVisibility(View.GONE);
                        card.setVisibility(View.VISIBLE);
                        card.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.app_status_approved_soft));
                        title.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.app_status_approved_text));
                        break;
                    case REFUSED:
                        dot.setBackgroundResource(R.drawable.bg_app_circle_refused);
                        dotInner.setVisibility(View.GONE);
                        check.setVisibility(View.GONE);
                        refused.setVisibility(View.VISIBLE);
                        card.setVisibility(View.VISIBLE);
                        card.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), R.color.app_status_refused_soft));
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

            private DashedLineDrawable dashedLine(TimelineStep.State state) {
                int color = state == TimelineStep.State.DONE ? R.color.app_status_done
                        : state == TimelineStep.State.REFUSED ? R.color.app_status_refused
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
