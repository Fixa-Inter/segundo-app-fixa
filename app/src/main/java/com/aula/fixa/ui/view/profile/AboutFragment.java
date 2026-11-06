package com.aula.fixa.ui.view.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.aula.fixa.R;
import com.aula.fixa.ui.config.AboutDocumentViewer;

public class AboutFragment extends Fragment {

    public AboutFragment() {
        super(R.layout.fragment_about);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) view.findViewById(R.id.tvPageTitle)).setText(R.string.app_about_title);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        bindDoc(view, R.id.docGuide, R.string.app_about_guide, "guia-de-uso.md");
        bindDoc(view, R.id.docPrivacy, R.string.app_about_privacy, "politica-de-privacidade.md");
        bindDoc(view, R.id.docTerms, R.string.app_about_terms, "termos-de-uso.md");
    }

    private void bindDoc(View root, @IdRes int id, @StringRes int title, String asset) {
        View card = root.findViewById(id);
        ((TextView) card.findViewById(R.id.tvDocTitle)).setText(title);
        card.setOnClickListener(v -> AboutDocumentViewer.show(requireContext(), getString(title), asset));
    }
}
