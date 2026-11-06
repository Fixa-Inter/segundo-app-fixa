package com.aula.fixa.ui.config;

import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/** Abre o seletor de fotos padrão: galeria ou câmera. */
public final class SolicitacaoPhotoPicker {
    private final Fragment fragment;
    private final ActivityResultLauncher<String> gallery;
    private final ActivityResultLauncher<Uri> camera;
    private Consumer<List<Uri>> callback;
    private Uri cameraUri;

    public SolicitacaoPhotoPicker(@NonNull Fragment fragment) {
        this.fragment = fragment;
        gallery = fragment.registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), uris -> {
            if (callback != null) callback.accept(uris == null ? Collections.emptyList() : uris);
            callback = null;
        });
        camera = fragment.registerForActivityResult(new ActivityResultContracts.TakePicture(), saved -> {
            if (callback != null) callback.accept(Boolean.TRUE.equals(saved) && cameraUri != null
                    ? Collections.singletonList(cameraUri) : Collections.emptyList());
            callback = null;
            cameraUri = null;
        });
    }

    public void open(Consumer<List<Uri>> callback) {
        this.callback = callback;
        new AlertDialog.Builder(fragment.requireContext())
                .setItems(new String[]{"Escolher da galeria", "Tirar foto"}, (dialog, index) -> {
                    if (index == 0) gallery.launch("image/*");
                    else takePhoto();
                }).show();
    }

    public void openGallery(Consumer<List<Uri>> callback) {
        this.callback = callback;
        gallery.launch("image/*");
    }

    public void openCamera(Consumer<List<Uri>> callback) {
        this.callback = callback;
        takePhoto();
    }

    private void takePhoto() {
        try {
            File directory = fragment.requireContext().getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
            if (directory == null) throw new IOException("Armazenamento indisponível.");
            File file = File.createTempFile("fixa_solicitacao_", ".jpg", directory);
            cameraUri = FileProvider.getUriForFile(fragment.requireContext(),
                    fragment.requireContext().getPackageName() + ".fileProvider", file);
            camera.launch(cameraUri);
        } catch (IOException | SecurityException error) {
            callback = null;
            android.widget.Toast.makeText(fragment.requireContext(), "Não foi possível abrir a câmera.", android.widget.Toast.LENGTH_LONG).show();
        }
    }
}
