package com.aula.fixa.ui.adapter.solicitacao;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.fixa.R;
import com.aula.fixa.domain.model.solicitacao.FotoProblema;
import com.bumptech.glide.Glide;

import java.util.List;

/** Photo thumbnails shown in manager request details. */
public final class SolicitacaoGestorPhotoAdapter extends RecyclerView.Adapter<SolicitacaoGestorPhotoAdapter.Holder> {
    private final Fragment fragment;
    private final List<FotoProblema> photos;

    public SolicitacaoGestorPhotoAdapter(Fragment fragment, List<FotoProblema> photos) {
        this.fragment = fragment;
        this.photos = photos;
    }

    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_photo_thumb, parent, false);
        return new Holder(view);
    }

    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        Glide.with(fragment).load(photos.get(position).url).centerCrop().into(holder.image);
    }

    @Override public int getItemCount() { return photos == null ? 0 : photos.size(); }

    static final class Holder extends RecyclerView.ViewHolder {
        final ImageView image;
        Holder(@NonNull View view) {
            super(view);
            image = (ImageView) view;
        }
    }
}
