package com.aula.fixa.ui.operations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/** Fragment nativo para uma tela do catálogo. O layout permanece editável em XML. */
public final class OperationsScreenFragment extends Fragment {
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(com.aula.fixa.R.layout.fragment_ops_screen_host, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        render();
    }

    public void render() {
        if (getView() == null) return;
        android.widget.FrameLayout host = (android.widget.FrameLayout) getView();
        String screen = requireArguments().getString("screen", "manager_home");
        host.removeAllViews();
        View content = getLayoutInflater().inflate(ScreenCatalog.get(requireContext()).layout(requireContext(), screen), host, false);
        host.addView(content);
        new OperationsBinder((OperationsActivity) requireActivity(), content, screen, null).bind();
    }
}
