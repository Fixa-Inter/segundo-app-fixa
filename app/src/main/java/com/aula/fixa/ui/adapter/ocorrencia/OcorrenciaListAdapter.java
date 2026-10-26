package com.aula.fixa.ui.adapter.ocorrencia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.aula.fixa.R;
import com.aula.fixa.domain.model.ocorrencia.OcorrenciaItem;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class OcorrenciaListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface Listener { void onClick(OcorrenciaItem item); void onMenuClick(View anchor, OcorrenciaItem item); }
    private static final int ITEM=0, EMPTY=1;
    private final List<OcorrenciaItem> items=new ArrayList<>();
    private final Listener listener;
    public OcorrenciaListAdapter(Listener listener){this.listener=listener;}
    public void submit(List<OcorrenciaItem> values){items.clear();if(values!=null)items.addAll(values);notifyDataSetChanged();}
    @Override public int getItemViewType(int position){return items.isEmpty()?EMPTY:ITEM;}
    @Override public int getItemCount(){return items.isEmpty()?1:items.size();}
    @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int type){
        View view=LayoutInflater.from(parent.getContext()).inflate(type==EMPTY?R.layout.item_ops_manager_occurrence_empty:R.layout.item_ops_manager_occurrence,parent,false);
        return type==EMPTY?new EmptyHolder(view):new Holder(view);
    }
    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder,int position){
        if(holder instanceof EmptyHolder)return;
        OcorrenciaItem item=items.get(position); Holder h=(Holder)holder;
        h.date.setText(formatDate(item.dataCriacao)); h.title.setText(item.titulo);
        String location=item.localUnidade;
        if(!item.descricaoLocal.isEmpty())location=location.isEmpty()?item.descricaoLocal:location+" - "+item.descricaoLocal;
        h.location.setText(location); h.author.setText(h.itemView.getContext().getString(R.string.ops_occurrence_registered_by,
                item.autor.isEmpty()?h.itemView.getContext().getString(R.string.ops_occurrence_unknown_author):item.autor));
        h.itemView.setOnClickListener(v->{if(listener!=null)listener.onClick(item);});
        h.menu.setOnClickListener(v->{if(listener!=null)listener.onMenuClick(v,item);});
    }
    private String formatDate(String raw){
        if(raw==null||raw.trim().isEmpty())return "";
        try{return OffsetDateTime.parse(raw).format(DateTimeFormatter.ofPattern("dd MMM yyyy",new Locale("pt","BR")));}catch(Exception ignored){}
        try{return LocalDateTime.parse(raw).format(DateTimeFormatter.ofPattern("dd MMM yyyy",new Locale("pt","BR")));}catch(Exception ignored){}
        try{return LocalDate.parse(raw).format(DateTimeFormatter.ofPattern("dd MMM yyyy",new Locale("pt","BR")));}catch(Exception ignored){}
        return raw;
    }
    static final class Holder extends RecyclerView.ViewHolder{
        final TextView date,title,location,author;final View menu;
        Holder(View v){super(v);date=v.findViewById(R.id.occurrenceDate);title=v.findViewById(R.id.occurrenceTitle);location=v.findViewById(R.id.occurrenceLocation);author=v.findViewById(R.id.occurrenceAuthor);menu=v.findViewById(R.id.occurrenceMenu);}
    }
    static final class EmptyHolder extends RecyclerView.ViewHolder{EmptyHolder(View v){super(v);}}
}
