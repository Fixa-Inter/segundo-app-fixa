package com.aula.fixa.ui.adapter.ocorrencia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.aula.fixa.R;
import com.aula.fixa.domain.model.ocorrencia.OcorrenciaFoto;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public final class OcorrenciaEditPhotoAdapter extends RecyclerView.Adapter<OcorrenciaEditPhotoAdapter.Holder> {
    public interface Listener { void onRemove(OcorrenciaFoto photo); }
    private final List<OcorrenciaFoto> photos=new ArrayList<>();
    private final Listener listener;
    public OcorrenciaEditPhotoAdapter(Listener listener){this.listener=listener;}
    public void submit(List<OcorrenciaFoto> values){photos.clear();if(values!=null)photos.addAll(values);notifyDataSetChanged();}
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent,int type){return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ops_manager_occurrence_edit_photo,parent,false));}
    @Override public void onBindViewHolder(@NonNull Holder holder,int position){OcorrenciaFoto photo=photos.get(position);Glide.with(holder.image).load(photo.url).centerCrop().placeholder(R.drawable.img_empty_results).error(R.drawable.img_empty_results).into(holder.image);holder.remove.setOnClickListener(v->{if(listener!=null)listener.onRemove(photo);});}
    @Override public int getItemCount(){return photos.size();}
    static final class Holder extends RecyclerView.ViewHolder {final ImageView image;final View remove;Holder(View v){super(v);image=v.findViewById(R.id.occurrenceEditPhotoImage);remove=v.findViewById(R.id.occurrenceEditPhotoRemove);}}
}
