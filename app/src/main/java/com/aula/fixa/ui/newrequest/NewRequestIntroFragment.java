package com.aula.fixa.ui.newrequest;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.aula.fixa.R;
import com.aula.fixa.ui.common.AppText;
import com.aula.fixa.data.remote.cloudinary.CloudinaryUploadService;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

public class NewRequestIntroFragment extends Fragment {

    public NewRequestIntroFragment() {
        super(R.layout.fragment_new_request_intro);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppText.setHtml(view.findViewById(R.id.tvTitle), R.string.app_new_request_title);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            NewRequestViewModel form = new ViewModelProvider(NavHostFragment.findNavController(this).getViewModelStoreOwner(R.id.nav_graph_app)).get(NewRequestViewModel.class);
            for (String token : new java.util.ArrayList<>(form.photoDeleteTokens)) CloudinaryUploadService.excluir(token, new CloudinaryUploadService.ResultadoCallback() {
                public void onSuccess(String url, String publicId) { }
                public void onError(String message) { }
            });
            form.clear();
            Navigation.findNavController(v).navigateUp();
        });
        view.findViewById(R.id.btnContinue).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_intro_to_description));
    }
}
