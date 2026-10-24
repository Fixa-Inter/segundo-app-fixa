package com.aula.fixa.ui.view.newrequest;

import com.aula.fixa.adapter.newrequest.SolicitacaoFormularioViewModel;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.aula.fixa.R;
import com.aula.fixa.ui.config.AppText;
import com.aula.fixa.adapter.foto.FotoViewModel;
import com.aula.fixa.adapter.foto.FotoViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

public class NewRequestIntroFragment extends Fragment {

    public NewRequestIntroFragment() {
        super(R.layout.fragment_new_request_intro);
    }

    private FotoViewModel fotoViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fotoViewModel = new ViewModelProvider(this, new FotoViewModelFactory(requireContext())).get(FotoViewModel.class);
        AppText.setHtml(view.findViewById(R.id.tvTitle), R.string.app_new_request_title);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        view.findViewById(R.id.btnCancel).setOnClickListener(v -> {
            androidx.navigation.NavController nav = NavHostFragment.findNavController(this);
            SolicitacaoFormularioViewModel form = new ViewModelProvider(nav.getViewModelStoreOwner(nav.getGraph().getId())).get(SolicitacaoFormularioViewModel.class);
            for (String token : new java.util.ArrayList<>(form.photoDeleteTokens)) fotoViewModel.excluir(token);
            form.clear();
            Navigation.findNavController(v).navigateUp();
        });
        view.findViewById(R.id.btnContinue).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_intro_to_description));
    }
}
