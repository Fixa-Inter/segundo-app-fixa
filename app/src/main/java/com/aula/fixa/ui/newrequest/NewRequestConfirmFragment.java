package com.aula.fixa.ui.newrequest;

import android.os.Bundle;
import android.app.Dialog;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aula.fixa.R;
import com.aula.fixa.ui.common.AppText;
import com.aula.fixa.ui.common.FragmentUi;
import com.aula.fixa.ui.common.DataChangeNotifier;
import com.google.android.material.button.MaterialButton;
import com.aula.fixa.data.local.preferences.SessionStorage;
import com.aula.fixa.data.repository.RequestRepository;
import com.aula.fixa.data.remote.cloudinary.CloudinaryUploadService;
import com.bumptech.glide.Glide;

public class NewRequestConfirmFragment extends Fragment {

    public NewRequestConfirmFragment() {
        super(R.layout.fragment_new_request_confirm);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NewRequestViewModel form = new ViewModelProvider(NavHostFragment.findNavController(this)
                .getViewModelStoreOwner(R.id.nav_graph_app)).get(NewRequestViewModel.class);

        view.findViewById(R.id.btnBack).setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        view.findViewById(R.id.btnStepBack).setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        MaterialButton finish = view.findViewById(R.id.btnStepNext);
        android.content.Context appContext = requireContext().getApplicationContext();
        finish.setText(R.string.app_finish);
        finish.setOnClickListener(v -> {
            if (form.localId <= 0 || form.categoryId <= 0 || form.photoUrls.isEmpty()) {
                android.widget.Toast.makeText(requireContext(), "Selecione o local, a categoria e pelo menos uma foto.", android.widget.Toast.LENGTH_LONG).show();
                return;
            }

            finish.setEnabled(false);
            new Thread(() -> {
                try {
                    new RequestRepository(new SessionStorage(appContext)).criar(form.title, form.description, form.localId, form.locationDescription, form.categoryId, form.photoUrls);
                    DataChangeNotifier.notifyChanged();
                    FragmentUi.runIfViewActive(this, view, () -> {
                        form.clear();
                        Navigation.findNavController(v).navigate(R.id.action_confirm_to_success);
                    });
                } catch (Exception e) {
                    FragmentUi.runIfViewActive(this, view, () -> {
                        finish.setEnabled(true);
                        android.widget.Toast.makeText(view.getContext(), e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        });

        AppText.setHtml(view.findViewById(R.id.tvTitle), R.string.app_confirm_title);
        AppText.setHtml(view.findViewById(R.id.tvSummaryDescriptionLabel), R.string.app_confirm_description);

        ((TextView) view.findViewById(R.id.tvSummaryTitle)).setText(form.title);
        ((TextView) view.findViewById(R.id.tvSummaryDescription)).setText(form.description);
        ((TextView) view.findViewById(R.id.tvSummaryLocationDescription)).setText(form.locationDescription);

        AppText.setHtml(view.findViewById(R.id.tvSummaryDevice), R.string.app_confirm_device, form.device);
        AppText.setHtml(view.findViewById(R.id.tvSummaryLocation), R.string.app_confirm_location, form.location);

        RecyclerView photos = view.findViewById(R.id.rvSummaryPhotos);
        photos.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        photos.setAdapter(new PhotoAdapter(form.photoUrls));
        view.findViewById(R.id.tvSummaryImagesLabel).setVisibility(form.photoUrls.isEmpty() ? View.GONE : View.VISIBLE);
        photos.setVisibility(form.photoUrls.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void deletePendingPhotos(NewRequestViewModel form) {
        for (String token : new java.util.ArrayList<>(form.photoDeleteTokens)) {
            CloudinaryUploadService.excluir(token, new CloudinaryUploadService.ResultadoCallback() {
                @Override
                public void onSuccess(String url, String publicId) {
                }

                @Override
                public void onError(String message) {
                }
            });
        }
        form.clear();
    }

    static class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
        private final java.util.List<String> urls;

        PhotoAdapter(java.util.List<String> urls) {
            this.urls = urls;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_app_photo_thumb, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Glide.with(holder.itemView.getContext()).load(urls.get(position)).centerCrop().into((ImageView) holder.itemView);
            holder.itemView.setOnClickListener(v -> showPhoto(v.getContext(), urls.get(position)));
        }

        private void showPhoto(android.content.Context context, String url) {
            Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            ImageView image = new ImageView(context);
            image.setBackgroundColor(Color.BLACK);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            Glide.with(context).load(url).into(image);
            image.setOnClickListener(v -> dialog.dismiss());
            dialog.setContentView(image);
            dialog.show();
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(android.R.color.black);
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                WindowManager.LayoutParams params = window.getAttributes();
                params.dimAmount = 0.85f;
                window.setAttributes(params);
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            }
        }

        @Override
        public int getItemCount() {
            return urls.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}
