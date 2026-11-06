package com.aula.fixa.ui.view.chatbot;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.aula.fixa.R;

import java.util.List;

public class ChatbotFragment extends Fragment {
    private ChatbotFlow flow;
    private final ActivityResultLauncher<String> photoPicker = registerForActivityResult(
            new ActivityResultContracts.GetMultipleContents(), uris -> {
                if (flow != null && uris != null) flow.onPhotoUris(uris);
            });

    public ChatbotFragment() {
        super(R.layout.fragment_chatbot);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        flow = new ChatbotFlow(this, view, () -> photoPicker.launch("image/*"));
    }

    @Override public void onResume() {
        super.onResume();
        if (flow != null) flow.onResume();
    }

    @Override public void onDestroyView() {
        if (flow != null) { flow.close(); flow = null; }
        super.onDestroyView();
    }
}
