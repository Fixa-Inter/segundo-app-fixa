package com.aula.fixa.ui.view.newrequest;

import android.content.ActivityNotFoundException;
import android.app.Dialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import com.bumptech.glide.Glide;

import com.aula.fixa.R;
import com.aula.fixa.ui.config.FragmentUi;
import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.adapter.foto.FotoViewModel;
import com.aula.fixa.adapter.foto.FotoViewModelFactory;
import com.aula.fixa.adapter.cadastro.CadastroViewModel;
import com.aula.fixa.adapter.cadastro.CadastroViewModelFactory;

import java.util.List;
import java.io.File;
import java.io.IOException;

public class NewRequestPhotoFragment extends NewRequestStepFragment {
    private ActivityResultLauncher<String> picker;
    private ActivityResultLauncher<Uri> camera;
    private Uri pendingPhotoUri;
    private RecyclerView photosRecyclerView;
    private View rootView;
    private FotoViewModel fotoViewModel;

    public NewRequestPhotoFragment() {
        super(R.layout.fragment_new_request_photo, 3, R.id.action_photo_to_confirm, R.string.app_step_photo_title);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fotoViewModel = new ViewModelProvider(this, new FotoViewModelFactory(requireContext())).get(FotoViewModel.class);
        CadastroViewModel cadastroViewModel = new ViewModelProvider(this,
                new CadastroViewModelFactory(requireContext())).get(CadastroViewModel.class);
        rootView = view;
        photosRecyclerView = view.findViewById(R.id.rvPhotos);
        photosRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        photosRecyclerView.setAdapter(new PhotoAdapter());
        viewUploadState();
        AutoCompleteTextView field = view.findViewById(R.id.actvDevice);
        cadastroViewModel.listarCategorias(new CadastroViewModel.Callback<List<OpcaoCadastro>>() {
            @Override public void aoConcluir(List<OpcaoCadastro> options) {
                FragmentUi.runIfViewActive(NewRequestPhotoFragment.this, view, () -> {
                    field.setAdapter(new ArrayAdapter<>(view.getContext(), R.layout.item_app_dropdown, options));
                    field.setOnItemClickListener((p, v, pos, id) -> form.categoryId = options.get(pos).getId());
                });
            }
            @Override public void aoFalhar(Throwable error) {
                FragmentUi.runIfViewActive(NewRequestPhotoFragment.this, view,
                        () -> android.widget.Toast.makeText(view.getContext(), error.getMessage(), android.widget.Toast.LENGTH_LONG).show());
            }
        });

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
            fotoViewModel.enviarSolicitacao(uri, new FotoViewModel.Callback() {
                public void aoSucesso(String url, String publicId) {
                    form.photoUrls.add(url);
                    FragmentUi.runIfViewActive(NewRequestPhotoFragment.this, rootView, () -> {
                        viewUploadState();
                        if (photosRecyclerView != null && photosRecyclerView.getAdapter() != null) {
                            photosRecyclerView.getAdapter().notifyDataSetChanged();
                        }
                    });
                }
                @Override public void aoReceberTokenExclusao(String token) { form.photoDeleteTokens.add(token); }

                public void aoFalhar(String message) {
                    FragmentUi.runIfViewActive(NewRequestPhotoFragment.this, rootView, () -> android.widget.Toast.makeText(rootView.getContext(), message, android.widget.Toast.LENGTH_LONG).show());
                }
            });
        } catch (Exception e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void viewUploadState() {
        View root = getView();
        if (root == null) return;
        root.findViewById(R.id.tvUpload).setVisibility(form.photoUrls.isEmpty() ? View.VISIBLE : View.GONE);
        photosRecyclerView.setVisibility(form.photoUrls.isEmpty() ? View.GONE : View.VISIBLE);
        if (form.photoUrls.isEmpty()) {
            root.findViewById(R.id.btnUpload).setBackgroundResource(R.drawable.bg_app_upload_area);
        } else {
            root.findViewById(R.id.btnUpload).setBackgroundColor(android.graphics.Color.TRANSPARENT);
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
        private static final int PHOTO = 0;
        private static final int ADD = 1;
        @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
            if (type == ADD) {
                ImageView add = new ImageView(parent.getContext());
                int photoSize = parent.getResources().getDimensionPixelSize(R.dimen.app_confirm_photo_size);
                int photoSpacing = parent.getResources().getDimensionPixelSize(R.dimen.app_confirm_photo_spacing);
                android.widget.LinearLayout.LayoutParams addParams = new android.widget.LinearLayout.LayoutParams(photoSize, photoSize);
                addParams.setMargins(0, 0, photoSpacing, 0);
                add.setLayoutParams(addParams);
                add.setBackgroundResource(R.drawable.bg_app_dialog_photo_add);
                add.setImageResource(R.drawable.ic_plus_24);
                add.setPadding(32, 32, 32, 32);
                return new Holder(add);
            }
            View image = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_photo_thumb, parent, false);
            return new Holder(image);
        }
        @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
            if (getItemViewType(position) == ADD) {
                holder.itemView.setOnClickListener(v -> picker.launch("image/*"));
            } else {
                Glide.with(NewRequestPhotoFragment.this).load(form.photoUrls.get(position)).centerCrop().into((ImageView) holder.itemView);
                holder.itemView.setOnClickListener(v -> showPhoto(form.photoUrls.get(position)));
            }
        }
        @Override public int getItemCount() { return form.photoUrls.isEmpty() ? 0 : form.photoUrls.size() + (form.photoUrls.size() < 3 ? 1 : 0); }
        @Override public int getItemViewType(int position) { return position < form.photoUrls.size() ? PHOTO : ADD; }
        final class Holder extends RecyclerView.ViewHolder { Holder(@NonNull View item) { super(item); } }
    }

    private void showPhoto(String url) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ImageView image = new ImageView(requireContext());
        image.setBackgroundColor(Color.BLACK);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        Glide.with(this).load(url).into(image);
        image.setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(image);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.black);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams params = window.getAttributes();
            params.dimAmount = 0.85f;
            window.setAttributes(params);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    protected int currentDestinationId() {
        return R.id.newRequestPhotoFragment;
    }

    @Override
    protected int operationsDestinationId() {
        return R.id.ops_new_request_photo;
    }

    @Override
    protected void restoreForm(View root) {
        ((AutoCompleteTextView) root.findViewById(R.id.actvDevice)).setText(form.device, false);
    }

    @Override
    protected void saveForm(View root) {
        form.device = ((AutoCompleteTextView) root.findViewById(R.id.actvDevice)).getText().toString().trim();
    }

    @Override
    public void onDestroyView() {
        rootView = null;
        photosRecyclerView = null;
        super.onDestroyView();
    }

    @Override
    protected boolean validateStep(View root) {
        if (form.categoryId <= 0 || !hasText(form.device)) {
            showValidationMessage("Selecione a categoria do equipamento.");
            return false;
        }
        if (form.photoUrls.isEmpty()) {
            showValidationMessage("Adicione pelo menos uma foto.");
            return false;
        }
        return true;
    }
}
