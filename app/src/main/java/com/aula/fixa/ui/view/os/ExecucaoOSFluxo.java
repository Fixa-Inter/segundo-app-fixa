package com.aula.fixa.ui.view.os;

import android.app.Dialog;import android.view.Gravity;import android.view.View;import android.view.ViewGroup;import android.widget.PopupWindow;import android.widget.TextView;import android.widget.Toast;
import androidx.constraintlayout.widget.ConstraintLayout;import androidx.lifecycle.ViewModelProvider;import androidx.recyclerview.widget.LinearLayoutManager;import androidx.recyclerview.widget.RecyclerView;
import com.aula.fixa.R;import com.aula.fixa.adapter.auth.AutenticacaoViewModel;import com.aula.fixa.adapter.os.ExecucaoOSViewModel;import com.aula.fixa.adapter.os.ExecucaoOSViewModelFactory;import com.aula.fixa.domain.model.os.OrdemServicoQuadro;import com.aula.fixa.domain.model.os.TarefaOS;import com.aula.fixa.ui.adapter.os.ExecucaoTarefaAdapter;import com.aula.fixa.ui.config.AppLoading;import com.aula.fixa.ui.view.operations.OperationsActivity;import com.aula.fixa.ui.view.operations.OperationsScreenFragment;import java.util.List;

/** Detalhe de execução da OS e ações persistidas de tarefa. */
public final class ExecucaoOSFluxo {
 private final OperationsScreenFragment host;private final OperationsActivity activity;private final View root;private final long orderId;private final ExecucaoOSViewModel vm;private Dialog loading;private RecyclerView list;private ExecucaoTarefaAdapter adapter;
 public ExecucaoOSFluxo(OperationsScreenFragment h,OperationsActivity a,View r,long id){host=h;activity=a;root=r;orderId=id;vm=new ViewModelProvider(h,new ExecucaoOSViewModelFactory(h.requireContext())).get(ExecucaoOSViewModel.class);}
 public void bind(){activity.model.field("execution","orderId",String.valueOf(orderId));View back=root.findViewWithTag("image:arrow-left");if(back!=null)back.setOnClickListener(v->activity.back());
  TextView heading=root.findViewById(R.id.ops_5717_8939);if(heading!=null)heading.setText("Execução da OS");
  View problem=root.findViewById(R.id.ops_5717_8950);if(problem!=null)problem.setOnClickListener(v->showOrderMenu(v));
  TextView title=root.findViewById(R.id.ops_5717_8962);TextView desc=root.findViewById(R.id.ops_5717_8963);
  list=new RecyclerView(host.requireContext());list.setId(View.generateViewId());list.setNestedScrollingEnabled(false);list.setLayoutManager(new LinearLayoutManager(host.requireContext()));
  adapter=new ExecucaoTarefaAdapter(new ExecucaoTarefaAdapter.Listener(){public void toggle(TarefaOS t){toggleTask(t);}public void options(View v,TarefaOS t){showTaskMenu(v,t);}});list.setAdapter(adapter);
  ConstraintLayout parent=(ConstraintLayout)root.findViewById(R.id.ops_5717_8943).getParent();
  for(int i=0;i<parent.getChildCount();i++){View child=parent.getChildAt(i);Object tag=child.getTag();if(child.getId()!=R.id.ops_5717_8950&&tag instanceof String&&((String)tag).startsWith("card:"))child.setVisibility(View.GONE);}
  TextView add=new TextView(host.requireContext());add.setId(View.generateViewId());add.setText("+  Adicionar tarefa");add.setTextColor(0xff142b57);add.setTextSize(15);add.setGravity(Gravity.CENTER);add.setBackgroundResource(R.drawable.bg_ops_6fc7f4db18);add.setOnClickListener(v->{activity.model.field("execution","orderId",String.valueOf(orderId));activity.model.selectEntry("execution_tasks","","");activity.model.clearDraft("manager_create_task");activity.openName("create_task");});
  parent.addView(add);ConstraintLayout.LayoutParams ap=new ConstraintLayout.LayoutParams(0,dp(45));ap.startToStart=ConstraintLayout.LayoutParams.PARENT_ID;ap.endToEnd=ConstraintLayout.LayoutParams.PARENT_ID;ap.topToBottom=R.id.ops_5717_8943;ap.setMargins(dp(30),dp(8),dp(30),dp(8));add.setLayoutParams(ap);
  parent.addView(list);ConstraintLayout.LayoutParams lp=new ConstraintLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT);lp.startToStart=ConstraintLayout.LayoutParams.PARENT_ID;lp.endToEnd=ConstraintLayout.LayoutParams.PARENT_ID;lp.topToBottom=add.getId();lp.bottomToTop=R.id.ops_5717_8945;lp.setMargins(dp(20),dp(8),dp(20),dp(12));list.setLayoutParams(lp);
  View cancel=root.findViewById(R.id.ops_5717_8945);if(cancel!=null)cancel.setOnClickListener(v->activity.back());View finish=root.findViewById(R.id.ops_5717_8944);if(finish!=null)finish.setOnClickListener(v->activity.openOrderCompletion(orderId));
  root.setVisibility(View.INVISIBLE);loading=AppLoading.show(host.requireContext());vm.detalhes(orderId,new ExecucaoOSViewModel.Callback<OrdemServicoQuadro>(){public void success(OrdemServicoQuadro os){if(!current())return;if(title!=null)title.setText(os.titulo);if(desc!=null)desc.setText(os.descricao);loadTasks();}public void failure(Throwable e){fail(e);}});
 }
 private void loadTasks(){vm.tarefas(orderId,new ExecucaoOSViewModel.Callback<List<TarefaOS>>(){public void success(List<TarefaOS> x){if(!current())return;adapter.submit(x);AppLoading.hide(loading);loading=null;root.setVisibility(View.VISIBLE);}public void failure(Throwable e){fail(e);}});}
 private void toggleTask(TarefaOS t){String next="CONCLUIDA".equals(t.status)?"EM_ANDAMENTO":"CONCLUIDA";vm.atualizar(new TarefaOS(t.id,t.titulo,t.descricao,next),new ExecucaoOSViewModel.Callback<Void>(){public void success(Void v){loadTasks();}public void failure(Throwable e){showError(e);}});}
 private void showTaskMenu(View anchor,TarefaOS t){View c=android.view.LayoutInflater.from(anchor.getContext()).inflate(R.layout.popup_app_request_menu,null,false);PopupWindow w=new PopupWindow(c,dp(160),ViewGroup.LayoutParams.WRAP_CONTENT,true);w.setBackgroundDrawable(androidx.core.content.ContextCompat.getDrawable(host.requireContext(),R.drawable.bg_app_popup_menu));w.setElevation(dp(6));c.findViewById(R.id.tvPopupEdit).setOnClickListener(v->{w.dismiss();activity.model.selectEntry("execution_tasks",String.valueOf(t.id),t.titulo);activity.model.field("execution_task","title",t.titulo);activity.model.field("execution_task","description",t.descricao);activity.model.field("execution_task","status",t.status);activity.openName("edit_task");});c.findViewById(R.id.tvPopupDelete).setOnClickListener(v->{w.dismiss();activity.model.selectEntry("execution_tasks",String.valueOf(t.id),t.titulo);activity.open("manager_delete_task");});w.setOutsideTouchable(true);w.showAsDropDown(anchor,-dp(150),0);}
 public void showOrderMenu(View anchor){showOrderMenu(anchor,activity::back);}
 public void showOrderMenu(View anchor,Runnable onDeleted){View menuContent=android.view.LayoutInflater.from(anchor.getContext()).inflate(R.layout.popup_app_request_menu,null,false);android.widget.PopupWindow popup=new android.widget.PopupWindow(menuContent,host.getResources().getDimensionPixelSize(R.dimen.app_popup_menu_width),ViewGroup.LayoutParams.WRAP_CONTENT,true);popup.setElevation(dp(6));menuContent.findViewById(R.id.tvPopupEdit).setOnClickListener(v->{popup.dismiss();prepararEdicaoOS();});menuContent.findViewById(R.id.tvPopupDelete).setOnClickListener(v->{popup.dismiss();com.aula.fixa.ui.config.AppDialogs.showDeleteConfirm(host.requireContext(),()->vm.excluirOS(orderId,new ExecucaoOSViewModel.Callback<Void>(){public void success(Void x){Toast.makeText(host.requireContext(),"OS excluída.",Toast.LENGTH_SHORT).show();onDeleted.run();}public void failure(Throwable e){showError(e);}}));});popup.showAsDropDown(anchor,-popup.getWidth()+anchor.getWidth(),0);}
 private void prepararEdicaoOS(){
  Dialog progress=AppLoading.show(host.requireContext());
  vm.detalhes(orderId,new ExecucaoOSViewModel.Callback<OrdemServicoQuadro>(){public void success(OrdemServicoQuadro os){
   com.aula.fixa.domain.enums.CategoriaProblema categoria=null;for(com.aula.fixa.domain.enums.CategoriaProblema c:com.aula.fixa.domain.enums.CategoriaProblema.values())if(c.getNome().equalsIgnoreCase(os.categoriaProblema)||c.name().equalsIgnoreCase(os.categoriaProblema))categoria=c;
   com.aula.fixa.domain.enums.Prioridade priority=null;for(com.aula.fixa.domain.enums.Prioridade p:com.aula.fixa.domain.enums.Prioridade.values())if(p.getNome().equalsIgnoreCase(os.prioridade)||p.name().equalsIgnoreCase(os.prioridade))priority=p;
   if(categoria==null||priority==null){AppLoading.hide(progress);showError(new IllegalStateException("Não foi possível preparar os dados desta OS para edição."));return;}
   final com.aula.fixa.domain.enums.CategoriaProblema selectedCategory=categoria;final com.aula.fixa.domain.enums.Prioridade selectedPriority=priority;
   activity.model.field("execution","editOrderId",String.valueOf(orderId));activity.model.field("plan_information","tecnicoId","");activity.model.field("plan_information","Competência",categoria.getNome());activity.model.field("plan_information","CompetênciaId",String.valueOf(categoria.getId()));activity.model.field("plan_information","Prioridade",priority.getNome());activity.model.field("plan_information","PrioridadeId",String.valueOf(priority.getId()));activity.model.field("plan_information","prazo",os.dataPrevista==null?"":os.dataPrevista);
   try{java.time.LocalDate date=java.time.LocalDate.parse(os.dataPrevista);activity.model.field("plan_information","mes",String.format(java.util.Locale.ROOT,"%04d-%02d",date.getYear(),date.getMonthValue()));}catch(Exception ignored){}
   com.aula.fixa.adapter.tecnico.TecnicoViewModel technicians=new ViewModelProvider(host,new com.aula.fixa.adapter.tecnico.TecnicoViewModelFactory(host.requireContext())).get(com.aula.fixa.adapter.tecnico.TecnicoViewModel.class);
   technicians.recomendar(selectedCategory.name(),new com.aula.fixa.adapter.tecnico.TecnicoViewModel.Callback<List<com.aula.fixa.domain.model.tecnico.TecnicoDisponivel>>(){
    public void success(List<com.aula.fixa.domain.model.tecnico.TecnicoDisponivel> values){
     AppLoading.hide(progress);
     Long assignedTechnicianId=findTechnicianId(values,os.nomeUsuario);
     activity.model.field("plan_information","tecnicoId",assignedTechnicianId==null?"":String.valueOf(assignedTechnicianId));
     activity.openPlanningStep("plan_information");
    }
    public void failure(Throwable error){AppLoading.hide(progress);showError(error);}
   });
  }public void failure(Throwable error){AppLoading.hide(progress);showError(error);}});
 }
 private Long findTechnicianId(List<com.aula.fixa.domain.model.tecnico.TecnicoDisponivel> technicians,String assignedName){
  String expected=normalizePersonName(assignedName);
  if(expected.isEmpty()||technicians==null)return null;
  for(com.aula.fixa.domain.model.tecnico.TecnicoDisponivel technician:technicians){
   if(technician!=null&&normalizePersonName(technician.nome).equals(expected))return technician.id;
  }
  return null;
 }
 private String normalizePersonName(String name){
  if(name==null)return "";
  return java.text.Normalizer.normalize(name,java.text.Normalizer.Form.NFD)
   .replaceAll("\\p{M}+","").trim().replaceAll("\\s+"," ").toLowerCase(java.util.Locale.ROOT);
 }
 private boolean current(){return host.isAdded()&&host.getView() instanceof android.widget.FrameLayout&&((android.widget.FrameLayout)host.getView()).getChildAt(0)==root;}
 private void fail(Throwable e){AppLoading.hide(loading);loading=null;root.setVisibility(View.VISIBLE);showError(e);}
 private void showError(Throwable e){if(host.isAdded())Toast.makeText(host.requireContext(),AutenticacaoViewModel.messageFrom(e),Toast.LENGTH_LONG).show();}
 private int dp(int x){return(int)(x*host.getResources().getDisplayMetrics().density+.5f);}
}
