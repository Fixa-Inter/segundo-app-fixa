package com.aula.fixa.ui.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.aula.fixa.R;
import com.aula.fixa.ui.common.AppDialogs;
import com.aula.fixa.ui.common.FragmentUi;
import com.aula.fixa.ui.common.DataChangeNotifier;
import com.aula.fixa.data.local.preferences.SessionStorage;
import com.aula.fixa.data.repository.RequestRepository;
import com.aula.fixa.data.repository.AuthRepository;
import com.aula.fixa.ui.auth.VerifyEmailActivity;
import com.google.gson.JsonObject;
import com.bumptech.glide.Glide;
import android.widget.ImageView;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.provider.MediaStore;
import android.os.Build;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import com.aula.fixa.data.remote.cloudinary.CloudinaryUploadService;
import java.io.File;
import java.io.IOException;

public class EditProfileFragment extends Fragment {

    private EditText name, birth, email, phone, password;
    private String initial;
    private String pendingPhotoUrl;
    private Uri pendingPhotoUri;
    private ActivityResultLauncher<String> picker;
    private ActivityResultLauncher<Uri> camera;

    public EditProfileFragment() {
        super(R.layout.fragment_edit_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) view.findViewById(R.id.tvPageTitle)).setText(R.string.app_edit_title);

        name = view.findViewById(R.id.etName);
        birth = view.findViewById(R.id.etBirth);
        email = view.findViewById(R.id.etEmail);
        phone = view.findViewById(R.id.etPhone);
        password = view.findViewById(R.id.etPassword);
        birth.setEnabled(false);
        email.setEnabled(false);
        password.setFocusable(false);
        password.setFocusableInTouchMode(false);
        password.setCursorVisible(false);
        password.setClickable(true);
        picker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) uploadProfilePhoto(uri, view);
        });
        camera = registerForActivityResult(new ActivityResultContracts.TakePicture(), captured -> {
            if (Boolean.TRUE.equals(captured) && pendingPhotoUri != null) {
                savePhotoToGallery(pendingPhotoUri);
                uploadProfilePhoto(pendingPhotoUri, view);
            }
        });
        view.findViewById(R.id.btnChangePhoto).setOnClickListener(v -> choosePhoto(view));
        loadProfile(view);
        password.setText("*************");
        password.setOnClickListener(v -> requestPasswordChange(view));
        initial = snapshot();

        view.findViewById(R.id.btnBack).setOnClickListener(v -> leave());
        view.findViewById(R.id.btnSave).setOnClickListener(v ->
                AppDialogs.showSaveConfirm(requireContext(), () -> saveProfile(view)));

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                leave();
            }
        });
    }

    private void loadProfile(View view) {
        android.content.Context appContext = requireContext().getApplicationContext();
        new Thread(() -> { try {
            JsonObject profile = new RequestRepository(new SessionStorage(appContext)).perfil();
            FragmentUi.runIfViewActive(this, view, () -> {
                name.setText(value(profile, "nomeCompleto")); birth.setText(value(profile, "dataNascimento")); email.setText(value(profile, "email")); phone.setText(value(profile, "cargo"));
                String url = value(profile, "urlFotoPerfil"); Glide.with(view).load(url.isEmpty() ? null : url).placeholder(R.drawable.img_avatar_115).error(R.drawable.img_avatar_115).circleCrop().into((ImageView) view.findViewById(R.id.ivAvatar)); initial = snapshot();
            });
        } catch (Exception e) { FragmentUi.runIfViewActive(this, view, () -> { name.setText(""); birth.setText(""); email.setText(""); phone.setText(""); initial = snapshot(); }); } }).start();
    }

    private String value(JsonObject profile, String key) { return profile.has(key) && !profile.get(key).isJsonNull() ? profile.get(key).getAsString() : ""; }

    private void saveProfile(View view) {
        android.content.Context appContext = requireContext().getApplicationContext();
        new Thread(() -> {
            try {
                new RequestRepository(new SessionStorage(appContext)).atualizarPerfil(
                        name.getText().toString().trim(), phone.getText().toString().trim(), pendingPhotoUrl);
                DataChangeNotifier.notifyChanged();
                FragmentUi.runIfViewActive(this, view, () -> Navigation.findNavController(view).navigateUp());
            } catch (Exception e) {
                FragmentUi.runIfViewActive(this, view, () -> android.widget.Toast.makeText(
                        view.getContext(), e.getMessage(), android.widget.Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void requestPasswordChange(View view) {
        String address = email.getText().toString().trim();
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(address).matches()) {
            Toast.makeText(view.getContext(), R.string.recovery_email_invalid, Toast.LENGTH_LONG).show();
            return;
        }
        password.setEnabled(false);
        android.content.Context appContext = requireContext().getApplicationContext();
        new Thread(() -> {
            try {
                new AuthRepository(new SessionStorage(appContext)).requestPasswordRecovery(address);
                FragmentUi.runIfViewActive(this, view, () -> {
                    password.setEnabled(true);
                    startActivity(VerifyEmailActivity.intent(requireContext(), address, true));
                });
            } catch (Exception error) {
                FragmentUi.runIfViewActive(this, view, () -> {
                    password.setEnabled(true);
                    Toast.makeText(view.getContext(), AuthRepository.messageFrom(error), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void choosePhoto(View view) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setItems(new String[]{"Escolher da galeria", "Tirar foto"}, (dialog, which) -> {
                    if (which == 0) picker.launch("image/*");
                    else takePhoto(view);
                }).show();
    }

    private void takePhoto(View view) {
        try {
            File directory = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (directory == null) throw new IOException("Armazenamento indisponível.");
            File file = File.createTempFile("fixa_perfil_", ".jpg", directory);
            pendingPhotoUri = FileProvider.getUriForFile(requireContext(),
                    requireContext().getPackageName() + ".fileProvider", file);
            camera.launch(pendingPhotoUri);
        } catch (IOException | ActivityNotFoundException | SecurityException e) {
            Toast.makeText(requireContext(), "Não foi possível abrir a câmera.", Toast.LENGTH_LONG).show();
        }
    }

    private void uploadProfilePhoto(Uri uri, View view) {
        try {
            CloudinaryUploadService.salvarPerfil(requireContext(), uri,
                    new CloudinaryUploadService.ResultadoCallback() {
                        @Override public void onSuccess(String url, String publicId) {
                            pendingPhotoUrl = url;
                            FragmentUi.runIfViewActive(EditProfileFragment.this, view, () -> Glide.with(view)
                                    .load(url).placeholder(R.drawable.img_avatar_115).error(R.drawable.img_avatar_115)
                                    .circleCrop().into((ImageView) view.findViewById(R.id.ivAvatar)));
                        }

                        @Override public void onError(String message) {
                            FragmentUi.runIfViewActive(EditProfileFragment.this, view, () -> Toast.makeText(view.getContext(), message, Toast.LENGTH_LONG).show());
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void savePhotoToGallery(Uri source) {
        new Thread(() -> {
            try {
                String fileName = "FIXA_perfil_" + System.currentTimeMillis() + ".jpg";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                    values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FIXA");
                    values.put(MediaStore.Images.Media.IS_PENDING, 1);
                    Uri destination = requireContext().getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    if (destination == null) throw new IOException("Não foi possível salvar a foto na galeria.");
                    copyPhoto(source, destination);
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    requireContext().getContentResolver().update(destination, values, null, null);
                } else {
                    File pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    if (!pictures.exists() && !pictures.mkdirs()) throw new IOException("Não foi possível acessar a galeria.");
                    File destination = new File(pictures, fileName);
                    try (InputStream input = requireContext().getContentResolver().openInputStream(source);
                         OutputStream output = new FileOutputStream(destination)) {
                        if (input == null) throw new IOException("Foto indisponível.");
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
                    }
                    android.media.MediaScannerConnection.scanFile(requireContext(),
                            new String[]{destination.getAbsolutePath()}, new String[]{"image/jpeg"}, null);
                }
            } catch (Exception ignored) {
                // A falha ao salvar na galeria não impede o upload nem o salvamento do perfil.
            }
        }).start();
    }

    private void copyPhoto(Uri source, Uri destination) throws IOException {
        try (InputStream input = requireContext().getContentResolver().openInputStream(source);
             OutputStream output = requireContext().getContentResolver().openOutputStream(destination)) {
            if (input == null || output == null) throw new IOException("Foto indisponível.");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) output.write(buffer, 0, read);
        }
    }

    /** Sem alterações sai direto; com alterações pergunta se salva (Figma "Salvar alterações?"). */
    private void leave() {
        Runnable exit = () -> Navigation.findNavController(requireView()).navigateUp();
        if (initial.equals(snapshot())) {
            exit.run();
        } else {
            AppDialogs.showDiscardConfirm(requireContext(),
                    () -> AppDialogs.showSaveConfirm(requireContext(), exit), exit);
        }
    }

    private String snapshot() {
        return name.getText() + "\u0000" + birth.getText() + "\u0000" + email.getText()
                + "\u0000" + phone.getText() + "\u0000" + password.getText();
    }
}
