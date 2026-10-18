package com.aula.fixa.ui.newrequest;

import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageView;
import android.view.ViewGroup;
import com.bumptech.glide.Glide;

import com.aula.fixa.R;
import com.aula.fixa.data.local.preferences.SessionStorage;
import com.aula.fixa.data.repository.RequestRepository;
import com.aula.fixa.data.remote.dto.CadastroOption;
import com.aula.fixa.data.remote.cloudinary.CloudinaryUploadService;

import java.util.List;
import java.io.File;
import java.io.IOException;

public class NewRequestPhotoFragment extends NewRequestStepFragment {
    private ActivityResultLauncher<String> picker;
    private ActivityResultLauncher<Uri> camera;
    private Uri pendingPhotoUri;
    private RecyclerView photosRecyclerView;

    public NewRequestPhotoFragment() {
        super(R.layout.fragment_new_request_photo, 3, R.id.action_photo_to_confirm, R.string.app_step_photo_title);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        photosRecyclerView = view.findViewById(R.id.rvPhotos);
        photosRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        photosRecyclerView.setAdapter(new PhotoAdapter());
        AutoCompleteTextView field = view.findViewById(R.id.actvDevice);
        new Thread(() -> {
            try {
                RequestRepository repository = new RequestRepository(new SessionStorage(requireContext()));
                List<CadastroOption> options = repository.options(repository.categorias());

                requireActivity().runOnUiThread(() -> {
                    field.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.item_app_dropdown, options));
                    field.setOnItemClickListener((p, v, pos, id) -> form.categoryId = options.get(pos).getId());
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> android.widget.Toast.makeText(requireContext(), e.getMessage(), android.widget.Toast.LENGTH_LONG).show());
            }
        }).start();

        picker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                upload(uri);
            }
        });

        camera = registerForActivityResult(new ActivityResultContracts.TakePicture(), captured -> {
            if (Boolean.TRUE.equals(captured) && pendingPhotoUri != null) {
                upload(pendingPhotoUri);
            } else {
                Toast.makeText(requireContext(), "Captura cancelada.", Toast.LENGTH_SHORT).show();
            }
        });

        field.setOnClickListener(v -> field.showDropDown());
        field.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                field.showDropDown();
            }
        });

        view.findViewById(R.id.btnUpload).setOnClickListener(v -> {
            if (form.photoUrls.size() >= 3) { Toast.makeText(requireContext(), "Você pode enviar no máximo 3 fotos.", Toast.LENGTH_SHORT).show(); return; }
            picker.launch("image/*");
        });
        view.findViewById(R.id.btnTakePhoto).setOnClickListener(v -> takePhoto());
    }

    private void upload(Uri uri) {
        try {
            CloudinaryUploadService.salvar(requireContext(), uri, CloudinaryUploadService.callback(new CloudinaryUploadService.ResultadoCallback() {
                public void onSuccess(String url, String publicId) {
                    form.photoUrls.add(url);
                    requireActivity().runOnUiThread(() -> {
                        if (photosRecyclerView != null && photosRecyclerView.getAdapter() != null) {
                            photosRecyclerView.getAdapter().notifyDataSetChanged();
                        }
                    });
                }
                @Override public void onDeleteToken(String token) { form.photoDeleteTokens.add(token); }

                public void onError(String message) {
                    requireActivity().runOnUiThread(() -> android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_LONG).show());
                }
            }));
        } catch (Exception e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void takePhoto() {
        if (form.photoUrls.size() >= 3) { Toast.makeText(requireContext(), "Você pode enviar no máximo 3 fotos.", Toast.LENGTH_SHORT).show(); return; }
        try {
            File directory = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            if (directory == null) {
                throw new IOException("Armazenamento indisponível.");
            }

            File file = File.createTempFile("fixa_", ".jpg", directory);
            pendingPhotoUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileProvider", file);

            camera.launch(pendingPhotoUri);
        } catch (IOException | ActivityNotFoundException | SecurityException e) {
            Toast.makeText(requireContext(), "Não foi possível abrir a câmera: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private final class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.Holder> {
        @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
            ImageView image = new ImageView(parent.getContext());
            image.setLayoutParams(new ViewGroup.LayoutParams(110, 110)); image.setScaleType(ImageView.ScaleType.CENTER_CROP); image.setPadding(4, 4, 4, 4);
            return new Holder(image);
        }
        @Override public void onBindViewHolder(@NonNull Holder holder, int position) { Glide.with(NewRequestPhotoFragment.this).load(form.photoUrls.get(position)).centerCrop().into((ImageView) holder.itemView); }
        @Override public int getItemCount() { return form.photoUrls.size(); }
        final class Holder extends RecyclerView.ViewHolder { Holder(@NonNull View item) { super(item); } }
    }

    @Override
    protected int currentDestinationId() {
        return R.id.newRequestPhotoFragment;
    }

    @Override
    protected void restoreForm(View root) {
        ((AutoCompleteTextView) root.findViewById(R.id.actvDevice)).setText(form.device, false);
    }

    @Override
    protected void saveForm(View root) {
        form.device = ((AutoCompleteTextView) root.findViewById(R.id.actvDevice)).getText().toString().trim();
    }
}
