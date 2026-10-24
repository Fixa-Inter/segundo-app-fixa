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
import android.net.Uri;

import com.aula.fixa.adapter.home.ManagerHomeViewModel;
import com.aula.fixa.adapter.home.ManagerHomeViewModelFactory;
import com.aula.fixa.ui.config.DataChangeNotifier;
import com.aula.fixa.ui.config.AppLoading;
import com.aula.fixa.ui.config.operations.ScreenCatalog;
import com.aula.fixa.ui.view.solicitacao.ManagerRequestFiltersSheet;
import com.aula.fixa.ui.view.solicitacao.SolicitacaoGestorFluxo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

/**
 * Fragment nativo para uma tela do catálogo. O layout permanece editável em XML.
 */
public final class OperationsScreenFragment extends Fragment implements ManagerRequestFiltersSheet.Listener {
    private Dialog homeLoading;
    private boolean firstResume = true;
    private OperationsProfileFlow profileFlow;
    private Uri pendingProfileCameraUri;
    private SolicitacaoGestorFluxo solicitacaoGestorFluxo;
    private final ActivityResultLauncher<String> profilePhotoPicker = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && profileFlow != null) {
                    profileFlow.uploadPhoto(uri);
                }
            });
    private final ActivityResultLauncher<Uri> profileCamera = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), captured -> {
                if (Boolean.TRUE.equals(captured) && pendingProfileCameraUri != null && profileFlow != null) {
                    profileFlow.uploadPhoto(pendingProfileCameraUri);
                }
                pendingProfileCameraUri = null;
            });

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(com.aula.fixa.R.layout.fragment_ops_screen_host, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        render();
        DataChangeNotifier.observe(getViewLifecycleOwner(), ignored -> {
            String screen = requireArguments().getString("screen", "manager_home");
            boolean profile = screen.equals(((OperationsActivity) requireActivity()).role() + "_profile")
                    || screen.equals(((OperationsActivity) requireActivity()).role() + "_my_profile");
            if (ignored != null && getView() == view && ("manager_home".equals(screen) || profile)) {
                render();
            }
        });
    }

    public void render() {
        if (getView() == null) {
            return;
        }
        android.widget.FrameLayout host = (android.widget.FrameLayout) getView();
        String screen = requireArguments().getString("screen", "manager_home");
        host.removeAllViews();
        int screenLayout = "manager_request_details".equals(screen)
                ? com.aula.fixa.R.layout.fragment_new_request_confirm
                : ScreenCatalog.get(requireContext()).layout(requireContext(), screen);
        View content = getLayoutInflater().inflate(screenLayout, host, false);
        host.addView(content);
        if ("manager_home".equals(screen)) {
            content.setVisibility(View.INVISIBLE);
            if (homeLoading != null) {
                AppLoading.hide(homeLoading);
            }
            homeLoading = AppLoading.show(requireContext());
        } else if (homeLoading != null) {
            AppLoading.hide(homeLoading);
            homeLoading = null;
        }
        OperationsBinder binder = new OperationsBinder((OperationsActivity) requireActivity(), content, screen, null);
        binder.bind();
        if ("manager_requests".equals(screen)) {
            solicitacaoGestorFluxo = new SolicitacaoGestorFluxo(this, (OperationsActivity) requireActivity());
            solicitacaoGestorFluxo.bindList(content);
        } else if ("manager_request_details".equals(screen)) {
            solicitacaoGestorFluxo = new SolicitacaoGestorFluxo(this, (OperationsActivity) requireActivity());
            solicitacaoGestorFluxo.bindDetails(content);
        } else {
            solicitacaoGestorFluxo = null;
        }
        if (screen.endsWith("profile") || screen.endsWith("my_profile") || screen.endsWith("edit_profile")) {
            OperationsProfileFlow.PhotoActions photoActions = new OperationsProfileFlow.PhotoActions() {
                @Override
                public void escolher() {
                    profilePhotoPicker.launch("image/*");
                }

                @Override
                public void capturar() {
                    capturarFotoPerfil();
                }
            };
            profileFlow = new OperationsProfileFlow(this, (OperationsActivity) requireActivity(),
                    content, screen, photoActions);
            profileFlow.bind();
        } else {
            profileFlow = null;
        }
        if ("manager_home".equals(screen)) {
            loadManagerHome(host, content);
        }
    }

    private void capturarFotoPerfil() {
        try {
            java.io.File directory = requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
            if (directory == null) {
                throw new java.io.IOException("Armazenamento indisponível.");
            }
            java.io.File image = java.io.File.createTempFile("fixa_perfil_", ".jpg", directory);
            pendingProfileCameraUri = androidx.core.content.FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileProvider", image);
            profileCamera.launch(pendingProfileCameraUri);
        } catch (java.io.IOException | android.content.ActivityNotFoundException
                 | SecurityException error) {
            android.widget.Toast.makeText(requireContext(), "Não foi possível abrir a câmera.",
                    android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private void loadManagerHome(android.widget.FrameLayout host, View content) {
        ManagerHomeViewModel model = new ViewModelProvider(this,
                new ManagerHomeViewModelFactory(requireContext())).get(ManagerHomeViewModel.class);
        model.carregar(new ManagerHomeViewModel.Callback() {
            @Override
            public void aoCarregar(com.aula.fixa.domain.model.home.ManagerHomeData data) {
                if (!isAdded() || getView() != host || host.indexOfChild(content) < 0) {
                    return;
                }
                try {
                    OperationsData.bindManagerHome((OperationsActivity) requireActivity(), content, data);
                    View home = content.findViewById(com.aula.fixa.R.id.opsManagerHomeContent);
                    if (home != null) {
                        home.setVisibility(View.VISIBLE);
                    }
                    content.setVisibility(View.VISIBLE);
                } finally {
                    AppLoading.hide(homeLoading);
                    homeLoading = null;
                }
            }

            @Override
            public void aoFalhar(Throwable erro) {
                if (!isAdded() || getView() != host || host.indexOfChild(content) < 0) {
                    return;
                }
                AppLoading.hide(homeLoading);
                homeLoading = null;
                content.setVisibility(View.VISIBLE);
                android.widget.Toast.makeText(requireContext(), com.aula.fixa.R.string.ops_home_load_error,
                        android.widget.Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onManagerRequestFilters(String period, String location, String category, String author) {
        if (solicitacaoGestorFluxo != null) {
            solicitacaoGestorFluxo.onFiltersChanged(period, location, category, author);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (firstResume) {
            firstResume = false;
            return;
        }
        if ("manager_requests".equals(requireArguments().getString("screen", "")) && getView() != null) {
            android.widget.FrameLayout host = (android.widget.FrameLayout) getView();
            if (host.getChildCount() > 0 && solicitacaoGestorFluxo != null) {
                solicitacaoGestorFluxo.refreshList(host.getChildAt(0));
            }
        }
        if (!"manager_home".equals(requireArguments().getString("screen", "manager_home")) || getView() == null) {
            return;
        }
        android.widget.FrameLayout host = (android.widget.FrameLayout) getView();
        if (host.getChildCount() > 0) {
            View content = host.getChildAt(0);
            content.setVisibility(View.INVISIBLE);
            if (homeLoading != null) {
                AppLoading.hide(homeLoading);
            }
            homeLoading = AppLoading.show(requireContext());
            loadManagerHome(host, content);
        }
    }

    @Override
    public void onDestroyView() {
        AppLoading.hide(homeLoading);
        homeLoading = null;
        if (solicitacaoGestorFluxo != null) {
            solicitacaoGestorFluxo.onDestroyView();
            solicitacaoGestorFluxo = null;
        }
        super.onDestroyView();
    }
}
