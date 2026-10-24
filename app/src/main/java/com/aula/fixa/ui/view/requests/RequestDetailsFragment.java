package com.aula.fixa.ui.view.requests;

import com.aula.fixa.domain.enums.StatusProblema;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.ui.config.AppText;
import com.aula.fixa.ui.config.FragmentUi;
import com.aula.fixa.ui.config.DataChangeNotifier;
import com.aula.fixa.ui.config.DashedLineDrawable;
import com.aula.fixa.domain.model.solicitacao.ProblemaItem;
import com.aula.fixa.domain.model.solicitacao.EtapaLinhaTempo;
import com.aula.fixa.ui.adapter.solicitacao.TimelineAdapter;
import com.aula.fixa.adapter.cadastro.CadastroViewModel;
import com.aula.fixa.adapter.cadastro.CadastroViewModelFactory;
import com.aula.fixa.adapter.solicitacao.SolicitacaoViewModel;
import com.aula.fixa.adapter.solicitacao.SolicitacaoViewModelFactory;
import com.aula.fixa.domain.model.solicitacao.Problema;

import java.util.List;

public class RequestDetailsFragment extends Fragment {
    private SolicitacaoViewModel solicitacoes;
    private CadastroViewModel cadastros;

    public RequestDetailsFragment() {
        super(R.layout.fragment_request_details);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        solicitacoes = new ViewModelProvider(this, new SolicitacaoViewModelFactory(requireContext()))
                .get(SolicitacaoViewModel.class);
        cadastros = new ViewModelProvider(this, new CadastroViewModelFactory(requireContext()))
                .get(CadastroViewModel.class);

        String title = getArguments() != null ? getArguments().getString("requestTitle", "") : "";
        long requestId = getArguments() != null ? getArguments().getLong("requestId", 0) : 0;
        int space = title.indexOf(' ');
        String html = space > 0
                ? "<b><font color=\"#153D7D\">" + title.substring(0, space) + "</font></b>" + title.substring(space)
                : title;
        AppText.setHtml(view.findViewById(R.id.tvTitle), html);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        view.findViewById(R.id.btnMore).setOnClickListener(v -> RequestMenuPopup.show(v,
                new ProblemaItem(title, "", "", StatusProblema.PENDENTE), solicitacoes, cadastros));

        RecyclerView list = view.findViewById(R.id.rvTimeline);
        if (requestId > 0) {
            loadDetails(view, list, requestId);
            DataChangeNotifier.observe(getViewLifecycleOwner(), ignored -> {
                if (getView() == view) {
                    loadDetails(view, list, requestId);
                }
            });
        }

        list.setAdapter(new TimelineAdapter(java.util.Collections.emptyList()));
    }

    private void loadDetails(View view, RecyclerView list, long id) {
        solicitacoes.detalhar(id, new SolicitacaoViewModel.Callback<Problema>() {
            @Override public void aoConcluir(Problema item) {
                FragmentUi.runIfViewActive(RequestDetailsFragment.this, view, () -> {
                    ((TextView) view.findViewById(R.id.tvTitle)).setText(item.titulo);
                    String status = item.status.isEmpty() ? "PENDENTE" : item.status;
                    boolean editable = status.equalsIgnoreCase("PENDENTE");
                    view.findViewById(R.id.btnMore).setVisibility(editable ? View.VISIBLE : View.GONE);
                    if (editable) {
                        ProblemaItem current = new ProblemaItem(
                                id,
                                item.titulo,
                                item.local,
                                "",
                                StatusProblema.PENDENTE,
                                item.descricao,
                                item.categoria,
                                0);
                        view.findViewById(R.id.btnMore).setOnClickListener(v -> RequestMenuPopup.show(v, current, solicitacoes, cadastros));
                    }
                    list.setAdapter(new TimelineAdapter(timeline(status)));
                });
            }
            @Override public void aoFalhar(Throwable error) {
                FragmentUi.runIfViewActive(RequestDetailsFragment.this, view, () -> android.widget.Toast.makeText(
                        view.getContext(), error.getMessage(), android.widget.Toast.LENGTH_LONG).show());
            }
        });
    }

    private List<EtapaLinhaTempo> timeline(String status) {
        List<EtapaLinhaTempo> steps = new java.util.ArrayList<>();
        boolean refused = status.equalsIgnoreCase("REPROVADO") || status.equalsIgnoreCase("RECUSADO");
        boolean approved = status.equalsIgnoreCase("APROVADO");
        boolean concluded = status.equalsIgnoreCase("CONCLUIDO") || status.equalsIgnoreCase("CONCLUÍDO");
        steps.add(new EtapaLinhaTempo(getString(R.string.app_details_step_opened), "", "", StatusProblema.CONCLUIDO, false));
        steps.add(new EtapaLinhaTempo(getString(R.string.app_details_step_analysis), "", "",
                refused || approved || concluded ? StatusProblema.CONCLUIDO : StatusProblema.PENDENTE,
                !refused && !approved && !concluded));
        if (refused) {
            steps.add(new EtapaLinhaTempo(getString(R.string.app_details_step_refused), "", "", StatusProblema.REPROVADO, false));
            return steps;
        }
        steps.add(new EtapaLinhaTempo(getString(R.string.app_details_step_approved), "", "",
                approved || concluded ? StatusProblema.APROVADO : StatusProblema.PENDENTE, false));
        steps.add(new EtapaLinhaTempo(getString(R.string.app_details_step_done), "", "",
                concluded ? StatusProblema.CONCLUIDO : StatusProblema.PENDENTE, false));
        return steps;
    }

}
