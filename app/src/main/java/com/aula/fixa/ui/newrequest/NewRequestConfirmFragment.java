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
        view.findViewById(R.id.btnBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        view.findViewById(R.id.btnStepBack).setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        MaterialButton finish = view.findViewById(R.id.btnStepNext);
        finish.setText(R.string.app_finish);
        finish.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.action_confirm_to_success));

        AppText.setHtml(view.findViewById(R.id.tvTitle), R.string.app_confirm_title);
        AppText.setHtml(view.findViewById(R.id.tvSummaryDescriptionLabel), R.string.app_confirm_description);
        ((TextView) view.findViewById(R.id.tvSummaryTitle)).setText("Projetor quebrado");
        ((TextView) view.findViewById(R.id.tvSummaryDescription)).setText(
                "O projetor da sala T02 não liga e apresenta uma luz vermelha piscando.");
        AppText.setHtml(view.findViewById(R.id.tvSummaryDevice), R.string.app_confirm_device, "Projetor");
        AppText.setHtml(view.findViewById(R.id.tvSummaryLocation), R.string.app_confirm_location, "Sala T02 - 2º andar");

        RecyclerView photos = view.findViewById(R.id.rvSummaryPhotos);
        photos.setAdapter(new PhotoAdapter(new int[]{R.drawable.img_confirm_photo_1, R.drawable.img_confirm_photo_2}));
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
