package com.aula.fixa.ui.view.requests;

import com.aula.fixa.domain.enums.StatusProblema;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
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
        setStyledTitle(view.findViewById(R.id.tvTitle), title);

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
                    setStyledTitle(view.findViewById(R.id.tvTitle), item.titulo);
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

    private void setStyledTitle(TextView titleView, String value) {
        String title = value == null ? "" : value;
        SpannableString styled = new SpannableString(title);
        int wordEnd = title.indexOf(' ');
        if (wordEnd < 0) wordEnd = title.length();
        if (wordEnd > 0) {
            styled.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.app_primary)),
                    0, wordEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        titleView.setText(styled);
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
