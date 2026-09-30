package com.aula.fixa.ui.newrequest;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.ui.common.AppText;
import com.google.android.material.button.MaterialButton;

public class NewRequestConfirmFragment extends Fragment {

    public NewRequestConfirmFragment() {
        super(R.layout.fragment_new_request_confirm);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NewRequestViewModel form = new ViewModelProvider(NavHostFragment.findNavController(this)
                .getViewModelStoreOwner(R.id.nav_graph_app)).get(NewRequestViewModel.class);
        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        view.findViewById(R.id.btnStepBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        MaterialButton finish = view.findViewById(R.id.btnStepNext);
        finish.setText(R.string.app_finish);
        finish.setOnClickListener(v -> {
            form.clear();
            Navigation.findNavController(v).navigate(R.id.action_confirm_to_success);
        });

        AppText.setHtml(view.findViewById(R.id.tvTitle), R.string.app_confirm_title);
        AppText.setHtml(view.findViewById(R.id.tvSummaryDescriptionLabel), R.string.app_confirm_description);
        ((TextView) view.findViewById(R.id.tvSummaryTitle)).setText(form.title);
        ((TextView) view.findViewById(R.id.tvSummaryDescription)).setText(form.description);
        AppText.setHtml(view.findViewById(R.id.tvSummaryDevice), R.string.app_confirm_device, form.device);
        AppText.setHtml(view.findViewById(R.id.tvSummaryLocation), R.string.app_confirm_location, form.location);

        int[] drawables = new int[form.photos.size()];
        for (int i = 0; i < drawables.length; i++) drawables[i] = form.photos.get(i);
        RecyclerView photos = view.findViewById(R.id.rvSummaryPhotos);
        photos.setAdapter(new PhotoAdapter(drawables));
        view.findViewById(R.id.tvSummaryImagesLabel).setVisibility(drawables.length == 0 ? View.GONE : View.VISIBLE);
        photos.setVisibility(drawables.length == 0 ? View.GONE : View.VISIBLE);
    }

    static class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
        private final int[] drawables;

        PhotoAdapter(int[] drawables) {
            this.drawables = drawables;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_app_photo_thumb, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ((ImageView) holder.itemView).setImageResource(drawables[position]);
        }

        @Override
        public int getItemCount() {
            return drawables.length;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}
