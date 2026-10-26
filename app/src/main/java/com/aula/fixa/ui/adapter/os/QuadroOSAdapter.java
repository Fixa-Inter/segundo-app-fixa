package com.aula.fixa.ui.adapter.os;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.aula.fixa.R;
import com.aula.fixa.domain.model.os.OrdemServicoQuadro;
import java.util.ArrayList;
import java.util.List;

public final class QuadroOSAdapter extends RecyclerView.Adapter<QuadroOSAdapter.Holder> {
    public interface Listener { void onClick(OrdemServicoQuadro os); void onMenuClick(View anchor, OrdemServicoQuadro os); }
    private final List<OrdemServicoQuadro> items=new ArrayList<>(); private final Listener listener;
    public QuadroOSAdapter(Listener listener){this.listener=listener;}
    public void submit(List<OrdemServicoQuadro> data){items.clear();if(data!=null)items.addAll(data);notifyDataSetChanged();}
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup p,int t){return new Holder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_ops_manager_board_order,p,false));}
    @Override public void onBindViewHolder(@NonNull Holder h,int position){
        OrdemServicoQuadro os=items.get(position); String priority=priorityCode(os.prioridade);
        h.title.setText(os.titulo==null?"":os.titulo); h.category.setText(join(os.categoriaProblema,os.categoriaEquipamento));
        h.date.setText(formatDate(os.dataPrevista)); h.priority.setText(priority);
        int color=priority.equals("P0")?ContextCompat.getColor(h.itemView.getContext(),R.color.ops_color_ff96031a):priority.equals("P1")?Color.rgb(243,190,47):Color.rgb(54,119,214);
        GradientDrawable bg=(GradientDrawable)ContextCompat.getDrawable(h.itemView.getContext(),R.drawable.bg_ops_39dc043443).mutate();
        bg.setColor(color);h.priority.setBackground(bg);h.priority.setTextColor(Color.WHITE);
        String name=os.nomeUsuario==null||os.nomeUsuario.trim().isEmpty()?"Técnico não atribuído":"Atribuído para "+os.nomeUsuario+" (Técnico)";
        h.technician.setText(name); h.itemView.setOnClickListener(v->{if(listener!=null)listener.onClick(os);});
        h.menu.setOnClickListener(v->{if(listener!=null)listener.onMenuClick(v,os);});
    }
    private String join(String a,String b){if(a==null||a.isEmpty())return b==null?"":b;if(b==null||b.isEmpty())return a;return a+" • "+b;}
    private String priorityCode(String p){if(p==null)return "P2";String s=p.toUpperCase(java.util.Locale.ROOT);if(s.contains("ALTA")||s.equals("P0"))return "P0";if(s.contains("MEDIA")||s.contains("MÉDIA")||s.equals("P1"))return "P1";return "P2";}
    private String formatDate(String value){if(value==null||value.isEmpty())return "";try{return java.time.LocalDate.parse(value).format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy",java.util.Locale.ENGLISH));}catch(Exception ignored){return value;}}
    @Override public int getItemCount(){return items.size();}
    static final class Holder extends RecyclerView.ViewHolder{final TextView title,category,date,priority,technician;final View menu;Holder(View v){super(v);title=v.findViewById(R.id.orderTitle);category=v.findViewById(R.id.orderCategory);date=v.findViewById(R.id.orderDueDate);priority=v.findViewById(R.id.orderPriority);technician=v.findViewById(R.id.orderTechnician);menu=v.findViewById(R.id.orderMenu);}}
}
