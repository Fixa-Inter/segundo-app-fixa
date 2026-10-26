package com.aula.fixa.ui.view.os;

import android.app.Dialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.aula.fixa.R;
import com.aula.fixa.adapter.auth.AutenticacaoViewModel;
import com.aula.fixa.adapter.os.QuadroOSViewModel;
import com.aula.fixa.adapter.os.QuadroOSViewModelFactory;
import com.aula.fixa.domain.model.os.OrdemServicoQuadro;
import com.aula.fixa.ui.adapter.os.QuadroOSAdapter;
import com.aula.fixa.ui.config.AppLoading;
import com.aula.fixa.ui.view.operations.OperationsActivity;
import com.aula.fixa.ui.view.operations.OperationsScreenFragment;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Coordena a busca, os filtros e os cards reais do board. */
public final class QuadroOSFluxo {
    private final OperationsScreenFragment host; private final OperationsActivity activity;
    private QuadroOSViewModel model; private QuadroOSAdapter adapter; private RecyclerView list;
    private final List<OrdemServicoQuadro> all=new ArrayList<>(); private View content; private Dialog loading;
    private String status,query="",equipment="",problem="",priority="",period="";
    public QuadroOSFluxo(OperationsScreenFragment host,OperationsActivity activity){this.host=host;this.activity=activity;}
    public void bind(View root){
        content=root; model=new ViewModelProvider(host,new QuadroOSViewModelFactory(host.requireContext())).get(QuadroOSViewModel.class);
        TextView heading=root.findViewById(R.id.ops_5717_8533);if(heading!=null)heading.setText("Quadro de OSs");
        View back=root.findViewWithTag("image:arrow-left");if(back!=null)back.setOnClickListener(v->activity.back());
        TextView late=root.findViewById(R.id.ops_5717_8588);if(late!=null)late.setText("Atrasadas");
        int[] ids={R.id.ops_5717_8584,R.id.ops_5717_8581,R.id.ops_5717_8578,R.id.ops_5717_8575,R.id.ops_5717_8587};
        int[] labelIds={0,R.id.ops_5717_8582,R.id.ops_5717_8579,R.id.ops_5717_8576,R.id.ops_5717_8588};
        String[] statuses={null,"Não Iniciada","Em andamento","Concluída","Atrasada"};
        for(int i=0;i<ids.length;i++){View chip=root.findViewById(ids[i]);if(chip!=null){ViewGroup.LayoutParams p=chip.getLayoutParams();p.height=dp(38);chip.setLayoutParams(p);String value=statuses[i];View.OnClickListener select=v->{status=value;updateStatusChips(root,value,ids,labelIds);renderList();};chip.setClickable(true);chip.setOnClickListener(select);if(labelIds[i]!=0){View label=root.findViewById(labelIds[i]);if(label!=null)label.setOnClickListener(select);}}}
        updateStatusChips(root,null,ids,labelIds);
        EditText search=root.findViewById(R.id.ops_5717_8593);if(search!=null)search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){query=s.toString();renderList();}public void afterTextChanged(Editable e){}});
        ConstraintLayout parent=(ConstraintLayout)heading.getParent();
        list=new RecyclerView(host.requireContext());list.setId(View.generateViewId());list.setNestedScrollingEnabled(false);list.setLayoutManager(new LinearLayoutManager(host.requireContext()));
        adapter=new QuadroOSAdapter(new QuadroOSAdapter.Listener(){@Override public void onClick(OrdemServicoQuadro os){activity.openOrderDetails(os.id,os.titulo);}@Override public void onMenuClick(View anchor,OrdemServicoQuadro os){new ExecucaoOSFluxo(host,activity,content,os.id).showOrderMenu(anchor,QuadroOSFluxo.this::load);}});list.setAdapter(adapter);parent.addView(list);
        ConstraintLayout.LayoutParams lp=new ConstraintLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT);lp.topToBottom=R.id.ops_5717_8592;lp.startToStart=ConstraintLayout.LayoutParams.PARENT_ID;lp.endToEnd=ConstraintLayout.LayoutParams.PARENT_ID;lp.setMargins(0,dp(15),0,dp(90));list.setLayoutParams(lp);
        View funnel=root.findViewById(R.id.ops_5717_8595);if(funnel!=null)funnel.setOnClickListener(v->showFilters());load();
    }
    private void load(){if(model==null)return;content.setVisibility(View.INVISIBLE);AppLoading.hide(loading);loading=AppLoading.show(host.requireContext());model.carregar(new QuadroOSViewModel.Callback(){
        @Override public void success(List<OrdemServicoQuadro> values){if(!current())return;all.clear();all.addAll(values);renderList();finish();}
        @Override public void failure(Throwable error){if(!current())return;all.clear();renderList();finish();Toast.makeText(host.requireContext(),AutenticacaoViewModel.messageFrom(error),Toast.LENGTH_LONG).show();}
    });}
    public void refresh(){load();}
    private void renderList(){if(adapter==null)return;String q=norm(query);List<OrdemServicoQuadro> result=new ArrayList<>();for(OrdemServicoQuadro os:all){if(status!=null&&!norm(status).equals(norm(os.status)))continue;if(!q.isEmpty()&&!norm(os.titulo).contains(q)&&!norm(os.descricao).contains(q)&&!norm(os.categoriaProblema).contains(q)&&!norm(os.categoriaEquipamento).contains(q)&&!norm(os.nomeUsuario).contains(q))continue;if(!matches(os.categoriaEquipamento,equipment)||!matches(os.categoriaProblema,problem)||!matches(os.prioridade,priority)||!matchesPeriod(os.dataPrevista,period))continue;result.add(os);}adapter.submit(result);}
    private void showFilters(){QuadroOSFiltersSheet sheet=new QuadroOSFiltersSheet();sheet.setTargetFragment(host,0);sheet.show(host.getParentFragmentManager(),QuadroOSFiltersSheet.TAG);}
    public void filters(String e,String p,String pr,String pe,String st){equipment=clean(e);problem=clean(p);priority=clean(pr);period=clean(pe);if(st!=null&&!st.trim().isEmpty())status=clean(st);View root=content;if(root!=null){int[] ids={R.id.ops_5717_8584,R.id.ops_5717_8581,R.id.ops_5717_8578,R.id.ops_5717_8575,R.id.ops_5717_8587};int[] labelIds={0,R.id.ops_5717_8582,R.id.ops_5717_8579,R.id.ops_5717_8576,R.id.ops_5717_8588};updateStatusChips(root,status,ids,labelIds);}renderList();}
    private void updateStatusChips(View root,String selected,int[] ids,int[] labelIds){int activeBg=R.drawable.bg_ops_c27365e123,inactiveBg=R.drawable.bg_ops_11863650ea;int activeText=ContextCompat.getColor(host.requireContext(),R.color.ops_color_ffffffff),inactiveText=ContextCompat.getColor(host.requireContext(),R.color.ops_color_ff0b013c);for(int i=0;i<ids.length;i++){View chip=root.findViewById(ids[i]);boolean active=(selected==null&&i==0)||(selected!=null&&i>0&&norm(selected).equals(norm(new String[]{"","Não Iniciada","Em andamento","Concluída","Atrasada"}[i])));if(chip==null)continue;chip.setBackgroundResource(active?activeBg:inactiveBg);if(chip instanceof TextView)((TextView)chip).setTextColor(active?activeText:inactiveText);if(labelIds[i]!=0){View label=root.findViewById(labelIds[i]);if(label instanceof TextView)((TextView)label).setTextColor(active?activeText:inactiveText);}}}
    private String clean(String s){return s==null||s.equalsIgnoreCase("Todos")||s.equalsIgnoreCase("Todas")?"":s;}
    private String norm(String v){return v==null?"":Normalizer.normalize(v,Normalizer.Form.NFD).replaceAll("\\p{M}+","").toLowerCase(java.util.Locale.ROOT).replace('_',' ').replace('-',' ').replaceAll("\\be\\b", " ").replaceAll("[^a-z0-9 ]"," ").replaceAll("\\s+"," ").trim();}
    private boolean matches(String value,String filter){return filter==null||filter.trim().isEmpty()||norm(value).equals(norm(filter));}
    private boolean matchesPeriod(String raw,String selected){if(selected==null||selected.trim().isEmpty()||norm(selected).startsWith("todos"))return true;try{LocalDate date=parseDate(raw);String periodName=norm(selected);long days=periodName.contains("30")?30:periodName.contains("3 meses")?90:365;return !date.isBefore(LocalDate.now().minusDays(days));}catch(Exception e){return false;}}
    private LocalDate parseDate(String raw){try{return LocalDate.parse(raw);}catch(Exception ignored){try{return java.time.OffsetDateTime.parse(raw).toLocalDate();}catch(Exception ignoredAgain){return java.time.LocalDateTime.parse(raw).toLocalDate();}}}
    private int dp(int n){return (int)(n*host.getResources().getDisplayMetrics().density+0.5f);}
    private boolean current(){return host.isAdded()&&host.getView() instanceof android.widget.FrameLayout&&((android.widget.FrameLayout)host.getView()).getChildAt(0)==content;}
    private void finish(){AppLoading.hide(loading);loading=null;content.setVisibility(View.VISIBLE);}
    public void clear(){AppLoading.hide(loading);loading=null;adapter=null;list=null;}
}
