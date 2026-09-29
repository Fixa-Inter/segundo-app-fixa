package com.aula.fixa.ui.home;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
import com.aula.fixa.model.RequestItem;
import com.aula.fixa.ui.requests.RequestAdapter;
import com.aula.fixa.ui.requests.RequestMenuPopup;
import com.aula.fixa.ui.requests.SampleRequests;

public class HomeFragment extends Fragment implements RequestAdapter.Listener {

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView greeting = view.findViewById(R.id.tvGreeting);
        AppText.setHtml(greeting, R.string.app_home_greeting, "Ryan Cursino");

        bindLegend(view, R.id.legendDone, R.string.app_home_status_done, 3, R.color.app_status_done_chart);
        bindLegend(view, R.id.legendApproved, R.string.app_home_status_approved, 2, R.color.app_status_approved);
        bindLegend(view, R.id.legendAnalysis, R.string.app_home_status_analysis, 3, R.color.app_status_analysis);
        bindLegend(view, R.id.legendRefused, R.string.app_home_status_refused, 1, R.color.app_status_refused_chart);

        RecyclerView list = view.findViewById(R.id.rvHomeRequests);
        RequestAdapter adapter = new RequestAdapter(this);
        list.setAdapter(adapter);
        adapter.submitList(SampleRequests.home());

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
        args.putString("requestTitle", item.title);
        Navigation.findNavController(requireView()).navigate(R.id.action_home_to_details, args);
    }

    @Override
    public void onRequestMenuClick(RequestItem item, View anchor) {
        RequestMenuPopup.show(anchor, item);
    }
}
