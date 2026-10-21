package com.aula.fixa.ui.home;

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
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.ui.common.AppText;
import com.aula.fixa.ui.common.TouchTargets;
import com.aula.fixa.ui.common.AppLoading;
import com.aula.fixa.ui.common.FragmentUi;
import com.aula.fixa.ui.common.DataChangeNotifier;
import com.aula.fixa.domain.model.RequestItem;
import com.aula.fixa.ui.requests.RequestAdapter;
import com.aula.fixa.ui.requests.RequestMenuPopup;
import com.aula.fixa.data.local.preferences.SessionStorage;
import com.aula.fixa.data.repository.RequestRepository;
import com.aula.fixa.ui.auth.LoginActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.nio.charset.StandardCharsets;
import android.util.Base64;
import org.json.JSONObject;

public class HomeFragment extends Fragment implements RequestAdapter.Listener {

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        android.content.Context appContext = requireContext().getApplicationContext();
        Dialog loading = AppLoading.show(view.getContext());
        new Thread(() -> {
            try {
                SessionStorage session = new SessionStorage(appContext);
                String token = session.getToken();
                if (!isTokenValid(token)) {
                    session.clear();
                    FragmentUi.runIfViewActive(this, view, () -> {
                        AppLoading.hide(loading);
                        Intent intent = new Intent(view.getContext(), LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        view.getContext().startActivity(intent);
                    });
                    return;
                }
                RequestRepository repository = new RequestRepository(session);
                JsonArray response = repository.minhas();
                JsonObject profile = repository.perfil();
                List<RequestItem> items = new ArrayList<>();
                int done = 0, approved = 0, analysis = 0, refused = 0;
                for (var element : response) {
                    JsonObject item = element.getAsJsonObject();
                    String status = item.get("status").getAsString();
                    if (status.equalsIgnoreCase("APROVADO")) {
                        approved++;
                    } else if (status.equalsIgnoreCase("REPROVADO")) {
                        refused++;
                    } else if (status.equalsIgnoreCase("CONCLUIDO")) {
                        done++;
                    } else {
                        analysis++;
                    }
                    items.add(new RequestItem(item.get("id").getAsLong(), item.get("titulo").getAsString(), item.has("localUnidade") ? item.get("localUnidade").getAsString() : "", item.has("dataCriacao") ? item.get("dataCriacao").getAsString() : "", statusOf(status), item.has("descricaoProblema") ? item.get("descricaoProblema").getAsString() : "", item.has("categoriaEquipamento") ? item.get("categoriaEquipamento").getAsString() : "", 0));
                }
                items.sort(Comparator.comparing(item -> item.date, Comparator.reverseOrder()));
                if (items.size() > 5) {
                    items = new ArrayList<>(items.subList(0, 5));
                }
                String name = profile.has("nomeCompleto") ? profile.get("nomeCompleto").getAsString() : "";
                String profilePhotoUrl = profile.has("urlFotoPerfil") && !profile.get("urlFotoPerfil").isJsonNull()
                        ? profile.get("urlFotoPerfil").getAsString().trim() : "";
                List<RequestItem> finalItems = items;
                int a = analysis, d = done, ap = approved, r = refused;
                FragmentUi.runIfViewActive(this, view, () -> {
                    AppLoading.hide(loading);
                    view.findViewById(R.id.homeRoot).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.tvHomeMessage).setVisibility(View.GONE);
                    AppText.setHtml(greeting, R.string.app_home_greeting, name);
                    loadProfilePhoto(view.findViewById(R.id.ivAvatar), profilePhotoUrl);
                    bindLegend(view, R.id.legendDone, R.string.app_home_status_done, d, R.color.app_status_done_chart);
                    bindLegend(view, R.id.legendApproved, R.string.app_home_status_approved, ap, R.color.app_status_approved);
                    bindLegend(view, R.id.legendAnalysis, R.string.app_home_status_analysis, a, R.color.app_status_analysis);
                    bindLegend(view, R.id.legendRefused, R.string.app_home_status_refused, r, R.color.app_status_refused_chart);
                    bindChart(view.findViewById(R.id.ivChart), d, ap, a, r);
                    ((RecyclerView) view.findViewById(R.id.rvHomeRequests)).getAdapter().notifyDataSetChanged();
                    ((RequestAdapter) ((RecyclerView) view.findViewById(R.id.rvHomeRequests)).getAdapter()).submitList(finalItems);
                });
            } catch (Exception e) {
                FragmentUi.runIfViewActive(this, view, () -> {
                    AppLoading.hide(loading);
                    View root = view.findViewById(R.id.homeRoot);
                    for (int i = 0; i < ((android.view.ViewGroup) root).getChildCount(); i++) {
                        View child = ((android.view.ViewGroup) root).getChildAt(i);
                        child.setVisibility(child.getId() == R.id.tvHomeMessage ? View.VISIBLE : View.GONE);
                    }
                    ((TextView) view.findViewById(R.id.tvHomeMessage)).setText("Não foi possível carregar os dados.");
                    root.setVisibility(View.VISIBLE);
                });
            }
        }).start();
    }

    private boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) return false;
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;
            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP), StandardCharsets.UTF_8);
            long expiration = new JSONObject(payload).optLong("exp", 0L);
            return expiration > (System.currentTimeMillis() / 1000L);
        } catch (Exception ignored) {
            return false;
        }
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
        PieDataSet dataSet = new PieDataSet(Arrays.asList(
                new PieEntry(done), new PieEntry(approved),
                new PieEntry(analysis), new PieEntry(refused)), "");
        dataSet.setColors(Arrays.asList(
                ContextCompat.getColor(requireContext(), R.color.app_status_done_chart),
                ContextCompat.getColor(requireContext(), R.color.app_status_approved),
                ContextCompat.getColor(requireContext(), R.color.app_status_analysis),
                ContextCompat.getColor(requireContext(), R.color.app_status_refused_chart)));
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

    private RequestItem.Status statusOf(String value) {
        if (value.equalsIgnoreCase("APROVADO")) {
            return RequestItem.Status.APPROVED;
        }
        if (value.equalsIgnoreCase("REPROVADO")) {
            return RequestItem.Status.REFUSED;
        }
        if (value.equalsIgnoreCase("CONCLUIDO")) {
            return RequestItem.Status.DONE;
        }
        return RequestItem.Status.ANALYSIS;
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
    public void onRequestClick(RequestItem item) {
        Bundle args = new Bundle();
        args.putLong("requestId", item.id);
        args.putString("requestTitle", item.title);
        Navigation.findNavController(requireView()).navigate(R.id.action_home_to_details, args);
    }

    @Override
    public void onRequestMenuClick(RequestItem item, View anchor) {
        RequestMenuPopup.show(anchor, item);
    }
}
