package com.aula.fixa.ui.view.operations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.app.Dialog;
import com.aula.fixa.adapter.operations.ManagerHomeViewModel;
import com.aula.fixa.adapter.operations.ManagerHomeViewModelFactory;
import com.aula.fixa.ui.config.DataChangeNotifier;
import com.aula.fixa.ui.config.AppLoading;
import com.aula.fixa.ui.config.operations.ScreenCatalog;

/** Fragment nativo para uma tela do catálogo. O layout permanece editável em XML. */
public final class OperationsScreenFragment extends Fragment {
    private Dialog homeLoading;
    private boolean firstResume = true;
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(com.aula.fixa.R.layout.fragment_ops_screen_host, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        render();
        DataChangeNotifier.observe(getViewLifecycleOwner(), ignored -> {
            if (ignored != null && getView() == view
                    && "manager_home".equals(requireArguments().getString("screen", "manager_home")))
                render();
        });
    }

    public void render() {
        if (getView() == null) return;
        android.widget.FrameLayout host = (android.widget.FrameLayout) getView();
        String screen = requireArguments().getString("screen", "manager_home");
        host.removeAllViews();
        View content = getLayoutInflater().inflate(ScreenCatalog.get(requireContext()).layout(requireContext(), screen), host, false);
        host.addView(content);
        if ("manager_home".equals(screen)) {
            content.setVisibility(View.INVISIBLE);
            if (homeLoading != null) AppLoading.hide(homeLoading);
            homeLoading = AppLoading.show(requireContext());
        } else if (homeLoading != null) {
            AppLoading.hide(homeLoading);
            homeLoading = null;
        }
        new OperationsBinder((OperationsActivity) requireActivity(), content, screen, null).bind();
        if ("manager_home".equals(screen)) loadManagerHome(host, content);
    }

    private void loadManagerHome(android.widget.FrameLayout host, View content) {
        ManagerHomeViewModel model = new ViewModelProvider(this,
                new ManagerHomeViewModelFactory(requireContext())).get(ManagerHomeViewModel.class);
        model.carregar(new ManagerHomeViewModel.Callback() {
            @Override public void aoCarregar(com.aula.fixa.domain.model.ManagerHomeData data) {
                if (!isAdded() || getView() != host || host.indexOfChild(content) < 0) return;
                try {
                    OperationsData.bindManagerHome((OperationsActivity) requireActivity(), content, data);
                    View home = content.findViewById(com.aula.fixa.R.id.opsManagerHomeContent);
                    if (home != null) home.setVisibility(View.VISIBLE);
                    content.setVisibility(View.VISIBLE);
                } finally {
                    AppLoading.hide(homeLoading);
                    homeLoading = null;
                }
            }

            @Override public void aoFalhar(Throwable erro) {
                if (!isAdded() || getView() != host || host.indexOfChild(content) < 0) return;
                AppLoading.hide(homeLoading);
                homeLoading = null;
                content.setVisibility(View.VISIBLE);
                android.widget.Toast.makeText(requireContext(), com.aula.fixa.R.string.ops_home_load_error,
                        android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override public void onResume() {
        super.onResume();
        if (firstResume) { firstResume = false; return; }
        if (!"manager_home".equals(requireArguments().getString("screen", "manager_home")) || getView() == null) return;
        android.widget.FrameLayout host = (android.widget.FrameLayout) getView();
        if (host.getChildCount() > 0) {
            View content = host.getChildAt(0);
            content.setVisibility(View.INVISIBLE);
            if (homeLoading != null) AppLoading.hide(homeLoading);
            homeLoading = AppLoading.show(requireContext());
            loadManagerHome(host, content);
        }
    }

    @Override public void onDestroyView() {
        AppLoading.hide(homeLoading);
        homeLoading = null;
        super.onDestroyView();
    }
}
