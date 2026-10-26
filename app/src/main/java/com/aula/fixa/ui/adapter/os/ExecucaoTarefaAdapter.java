package com.aula.fixa.ui.adapter.os;
import android.view.LayoutInflater;import android.view.View;import android.view.ViewGroup;import android.widget.ImageView;import android.widget.TextView;
import androidx.annotation.NonNull;import androidx.recyclerview.widget.RecyclerView;import com.aula.fixa.R;import com.aula.fixa.domain.model.os.TarefaOS;import java.util.ArrayList;import java.util.List;
public final class ExecucaoTarefaAdapter extends RecyclerView.Adapter<ExecucaoTarefaAdapter.Holder>{
 public interface Listener{void toggle(TarefaOS t);void options(View v,TarefaOS t);}private final List<TarefaOS> rows=new ArrayList<>();private final Listener listener;
 public ExecucaoTarefaAdapter(Listener l){listener=l;}public void submit(List<TarefaOS> x){rows.clear();if(x!=null)rows.addAll(x);notifyDataSetChanged();}
 @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup p,int t){return new Holder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_ops_manager_planning_task,p,false));}
 @Override public void onBindViewHolder(@NonNull Holder h,int pos){TarefaOS t=rows.get(pos);boolean done="CONCLUIDA".equals(t.status);h.title.setText((done?"✓  ":"○  ")+(t.titulo==null?"":t.titulo));h.description.setText(t.descricao==null?"":t.descricao);h.item.setOnClickListener(v->listener.toggle(t));h.more.setOnClickListener(v->listener.options(v,t));}
 @Override public int getItemCount(){return rows.size();}
 static final class Holder extends RecyclerView.ViewHolder{final View item;final TextView title,description;final ImageView more;Holder(View v){super(v);item=v;title=v.findViewById(R.id.planningTaskTitle);description=v.findViewById(R.id.planningTaskDescription);more=v.findViewById(R.id.planningTaskMore);}}
}
