package com.aula.fixa.ui.view.home;

import com.aula.fixa.domain.enums.StatusProblema;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.app.Dialog;
import android.content.Intent;

import androidx.annotation.ColorRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.ui.config.AppText;
import com.aula.fixa.ui.config.TouchTargets;
import com.aula.fixa.ui.config.AppLoading;
import com.aula.fixa.ui.config.FragmentUi;
import com.aula.fixa.ui.config.DataChangeNotifier;
import com.aula.fixa.domain.model.solicitacao.ProblemaItem;
import com.aula.fixa.domain.model.solicitacao.Problema;
import com.aula.fixa.domain.model.Perfil;
import com.aula.fixa.adapter.auth.SessaoViewModel;
import com.aula.fixa.adapter.auth.SessaoViewModelFactory;
import com.aula.fixa.adapter.perfil.PerfilViewModel;
import com.aula.fixa.adapter.perfil.PerfilViewModelFactory;
import com.aula.fixa.adapter.solicitacao.SolicitacaoViewModel;
import com.aula.fixa.adapter.solicitacao.SolicitacaoViewModelFactory;
import com.aula.fixa.ui.adapter.solicitacao.RequestAdapter;
import com.aula.fixa.ui.view.requests.RequestMenuPopup;
import com.aula.fixa.ui.view.auth.LoginActivity;
import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HomeFragment extends Fragment implements RequestAdapter.Listener {
    private SolicitacaoViewModel solicitacaoViewModel;
    private PerfilViewModel perfilViewModel;
    private SessaoViewModel sessaoViewModel;

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        solicitacaoViewModel = new ViewModelProvider(this,
                new SolicitacaoViewModelFactory(requireContext())).get(SolicitacaoViewModel.class);
        perfilViewModel = new ViewModelProvider(this,
                new PerfilViewModelFactory(requireContext())).get(PerfilViewModel.class);
        sessaoViewModel = new ViewModelProvider(this,
                new SessaoViewModelFactory(requireContext())).get(SessaoViewModel.class);

        TextView greeting = view.findViewById(R.id.tvGreeting);
        view.findViewById(R.id.homeRoot).setVisibility(View.GONE);
        loadHomeData(view, greeting);
        DataChangeNotifier.observe(getViewLifecycleOwner(), ignored -> {
            if (getView() == view) {
                view.findViewById(R.id.homeRoot).setVisibility(View.GONE);
                loadHomeData(view, greeting);
            }
        });

        RecyclerView list = view.findViewById(R.id.rvHomeRequests);
        RequestAdapter adapter = new RequestAdapter(this);
        list.setAdapter(adapter);

        TouchTargets.expand(view.findViewById(R.id.btnNotifications));
        TouchTargets.expand(view.findViewById(R.id.btnProfile));
        view.findViewById(R.id.btnNotifications).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_notifications));
        view.findViewById(R.id.btnProfile).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_profile));
        view.findViewById(R.id.ivAvatar).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_profile));
        view.findViewById(R.id.btnOpenRequest).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_home_to_new_request));
    }

    private void loadHomeData(View view, TextView greeting) {
        Dialog loading = AppLoading.show(view.getContext());
        if (!sessaoViewModel.tokenValido()) {
            sessaoViewModel.encerrar();
            FragmentUi.runIfViewActive(this, view, () -> {
                AppLoading.hide(loading);
                Intent intent = new Intent(view.getContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                view.getContext().startActivity(intent);
            });
            return;
        }

        final List<Problema>[] requests = new List[]{null};
        final Perfil[] profile = new Perfil[1];
        final Throwable[] failure = new Throwable[1];
        final boolean[] ready = new boolean[2];
        Runnable render = () -> {
            if (!ready[0] || !ready[1]) return;
            if (failure[0] != null || profile[0] == null || requests[0] == null) {
                showHomeError(view, loading);
                return;
            }
            List<ProblemaItem> items = new ArrayList<>();
            int done = 0, approved = 0, pending = 0, refused = 0;
            for (Problema problem : requests[0]) {
                if (problem.status.equalsIgnoreCase("APROVADO")) approved++;
                else if (problem.status.equalsIgnoreCase("REPROVADO")) refused++;
                else if (problem.status.equalsIgnoreCase("CONCLUIDO") || problem.status.equalsIgnoreCase("CONCLUÍDO")) done++;
                else if (problem.status.equalsIgnoreCase("PENDENTE")) pending++;
                items.add(new ProblemaItem(problem.id, problem.titulo, problem.local, problem.dataCriacao,
                        statusOf(problem.status), problem.descricao, problem.categoria, 0));
            }
            items.sort(Comparator.comparing(item -> item.date, Comparator.reverseOrder()));
            if (items.size() > 5) items = new ArrayList<>(items.subList(0, 5));
            List<ProblemaItem> finalItems = items;
            int p = pending, d = done, ap = approved, r = refused;
            FragmentUi.runIfViewActive(HomeFragment.this, view, () -> {
                AppLoading.hide(loading);
                view.findViewById(R.id.homeRoot).setVisibility(View.VISIBLE);
                view.findViewById(R.id.tvHomeMessage).setVisibility(View.GONE);
                AppText.setHtml(greeting, R.string.app_home_greeting, profile[0].nomeCompleto);
                loadProfilePhoto(view.findViewById(R.id.ivAvatar), profile[0].urlFotoPerfil);
                bindLegend(view, R.id.legendDone, R.string.app_home_status_done, d, R.color.app_status_done_chart);
                bindLegend(view, R.id.legendApproved, R.string.app_home_status_approved, ap, R.color.app_status_approved);
                bindLegend(view, R.id.legendAnalysis, R.string.app_home_status_analysis, p, R.color.app_status_analysis);
                bindLegend(view, R.id.legendRefused, R.string.app_home_status_refused, r, R.color.app_status_refused_chart);
                bindChart(view.findViewById(R.id.ivChart), d, ap, p, r);
                view.findViewById(R.id.layoutHomeRequestsEmpty).setVisibility(finalItems.isEmpty() ? View.VISIBLE : View.GONE);
                view.findViewById(R.id.rvHomeRequests).setVisibility(finalItems.isEmpty() ? View.GONE : View.VISIBLE);
                ((RequestAdapter) ((RecyclerView) view.findViewById(R.id.rvHomeRequests)).getAdapter()).submitList(finalItems);
            });
        };
        solicitacaoViewModel.listar(new SolicitacaoViewModel.Listener() {
            @Override public void aoCarregar(boolean carregando) { }
            @Override public void aoCriar(Problema problema) { }
            @Override public void aoListar(List<Problema> result) { requests[0] = result; ready[0] = true; render.run(); }
            @Override public void aoFalhar(Throwable erro) { failure[0] = erro; ready[0] = true; render.run(); }
        });
        perfilViewModel.obter(new PerfilViewModel.Callback<Perfil>() {
            @Override public void aoConcluir(Perfil result) { profile[0] = result; ready[1] = true; render.run(); }
            @Override public void aoFalhar(Throwable erro) { failure[0] = erro; ready[1] = true; render.run(); }
        });
    }

    private void showHomeError(View view, Dialog loading) {
        FragmentUi.runIfViewActive(this, view, () -> {
            AppLoading.hide(loading);
            View root = view.findViewById(R.id.homeRoot);
            for (int i = 0; i < ((android.view.ViewGroup) root).getChildCount(); i++) {
                View child = ((android.view.ViewGroup) root).getChildAt(i);
                child.setVisibility(child.getId() == R.id.tvHomeMessage ? View.VISIBLE : View.GONE);
            }
            ((TextView) view.findViewById(R.id.tvHomeMessage)).setText(R.string.app_home_load_error);
            root.setVisibility(View.VISIBLE);
        });
    }

    private void loadProfilePhoto(ImageView imageView, String url) {
        Glide.with(this)
                .load(url == null || url.trim().isEmpty() ? null : url)
                .placeholder(R.drawable.img_avatar_115)
                .fallback(R.drawable.img_avatar_115)
                .error(R.drawable.img_avatar_115)
                .circleCrop()
                .into(imageView);
    }

    private void bindChart(PieChart chart, int done, int approved, int analysis, int refused) {
        boolean hasRequests = done + approved + analysis + refused > 0;
        PieDataSet dataSet = new PieDataSet(Arrays.asList(
                new PieEntry(hasRequests ? done : 1), new PieEntry(approved),
                new PieEntry(analysis), new PieEntry(refused)), "");
        dataSet.setColors(hasRequests ? Arrays.asList(
                ContextCompat.getColor(requireContext(), R.color.app_status_done_chart),
                ContextCompat.getColor(requireContext(), R.color.app_status_approved),
                ContextCompat.getColor(requireContext(), R.color.app_status_analysis),
                ContextCompat.getColor(requireContext(), R.color.app_status_refused_chart))
                : java.util.Collections.singletonList(ContextCompat.getColor(requireContext(), R.color.app_chart_empty)));
        dataSet.setDrawValues(false);
        chart.setData(new PieData(dataSet));
        chart.setUsePercentValues(false);
        chart.setDrawEntryLabels(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(android.graphics.Color.TRANSPARENT);
        chart.setHoleRadius(66f);
        chart.setTransparentCircleRadius(68f);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setRotationEnabled(false);
        chart.setHighlightPerTapEnabled(false);
        chart.invalidate();
    }

    private StatusProblema statusOf(String value) {
        if (value.equalsIgnoreCase("APROVADO")) {
            return StatusProblema.APROVADO;
        }
        if (value.equalsIgnoreCase("REPROVADO")) {
            return StatusProblema.REPROVADO;
        }
        if (value.equalsIgnoreCase("CONCLUIDO") || value.equalsIgnoreCase("CONCLUÍDO")) {
            return StatusProblema.CONCLUIDO;
        }
        if (value.equalsIgnoreCase("OS_CRIADA")) {
            return StatusProblema.OS_CRIADA;
        }
        return StatusProblema.PENDENTE;
    }

    private void bindLegend(View root, @IdRes int rowId, int labelRes, int count, @ColorRes int color) {
        View row = root.findViewById(rowId);
        ((TextView) row.findViewById(R.id.tvLegendLabel)).setText(labelRes);
        ((TextView) row.findViewById(R.id.tvLegendValue))
                .setText(getResources().getQuantityString(R.plurals.app_home_requests_count, count, count));
        row.findViewById(R.id.viewLegendDot)
                .setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), color));
    }

    @Override
    public void onRequestClick(ProblemaItem item) {
        Bundle args = new Bundle();
        args.putLong("requestId", item.id);
        args.putString("requestTitle", item.title);
        Navigation.findNavController(requireView()).navigate(R.id.action_home_to_details, args);
    }

    @Override
    public void onRequestMenuClick(ProblemaItem item, View anchor) {
        RequestMenuPopup.show(anchor, item);
    }
}
