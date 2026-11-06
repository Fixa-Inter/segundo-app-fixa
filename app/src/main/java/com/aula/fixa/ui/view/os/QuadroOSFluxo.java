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
import java.util.ArrayList;
import java.util.List;

/** Coordena a busca, os filtros e os cards reais do board. */
public final class QuadroOSFluxo {
    private final OperationsScreenFragment host; private final OperationsActivity activity;
    private QuadroOSViewModel model; private QuadroOSAdapter adapter; private RecyclerView list;
    private final List<OrdemServicoQuadro> all=new ArrayList<>(); private View content; private Dialog loading;
    private String status,filterResponsible,query="",equipment="",problem="",priority="",period=""; private boolean loadFailed;
    private boolean technician;
    public QuadroOSFluxo(OperationsScreenFragment host,OperationsActivity activity){this.host=host;this.activity=activity;}
    public void bind(View root){
        content=root; technician="technician".equals(activity.role()); model=new ViewModelProvider(host,new QuadroOSViewModelFactory(host.requireContext(),technician)).get(QuadroOSViewModel.class);
        TextView heading=root.findViewById(technician?R.id.ops_5717_10470:R.id.ops_5717_8533);if(heading!=null){heading.setText(technician?"Suas ordens de serviço":"Quadro de OSs");if(!technician)heading.setTextColor(ContextCompat.getColor(host.requireContext(),R.color.ops_color_ff0b013c));}
        View back=root.findViewWithTag("image:arrow-left");if(back!=null)back.setOnClickListener(v->activity.back());
        TextView late=root.findViewById(technician?R.id.ops_5717_10525:R.id.ops_5717_8588);if(late!=null)late.setText("Atrasadas");
        int[] ids=technician?new int[]{R.id.ops_5717_10521,R.id.ops_5717_10518,R.id.ops_5717_10515,R.id.ops_5717_10512,R.id.ops_5717_10524}:new int[]{R.id.ops_5717_8584,R.id.ops_5717_8581,R.id.ops_5717_8578,R.id.ops_5717_8575,R.id.ops_5717_8587};
        int[] labelIds=technician?new int[]{0,R.id.ops_5717_10519,R.id.ops_5717_10516,R.id.ops_5717_10513,R.id.ops_5717_10525}:new int[]{0,R.id.ops_5717_8582,R.id.ops_5717_8579,R.id.ops_5717_8576,R.id.ops_5717_8588};
        String[] statuses={null,"Não iniciada","Em andamento","Concluída","Atrasada"};
        layoutStatusChips(root,ids);
        for(int i=0;i<ids.length;i++){View chip=root.findViewById(ids[i]);if(chip!=null){String value=statuses[i];View.OnClickListener select=v->{status=value;updateStatusChips(root,value,ids,labelIds);renderList();};chip.setClickable(true);chip.setOnClickListener(select);if(labelIds[i]!=0){View label=root.findViewById(labelIds[i]);if(label!=null)label.setOnClickListener(select);}}}
        updateStatusChips(root,null,ids,labelIds);
        EditText search=root.findViewById(technician?R.id.ops_5717_10530:R.id.ops_5717_8593);if(search!=null)search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){query=s.toString();renderList();}public void afterTextChanged(Editable e){}});
        ConstraintLayout parent=(ConstraintLayout)heading.getParent();
        list=new RecyclerView(host.requireContext());list.setId(View.generateViewId());list.setNestedScrollingEnabled(false);list.setLayoutManager(new LinearLayoutManager(host.requireContext()));
        adapter=new QuadroOSAdapter(new QuadroOSAdapter.Listener(){@Override public void onClick(OrdemServicoQuadro os){activity.openOrderDetails(os.id,os.titulo);}@Override public void onMenuClick(View anchor,OrdemServicoQuadro os){if(!technician)new ExecucaoOSFluxo(host,activity,content,os.id).showOrderMenu(anchor,QuadroOSFluxo.this::load);}},!technician);list.setAdapter(adapter);parent.addView(list);
        int anchor=technician?R.id.ops_5717_10529:R.id.ops_5717_8592;
        int sideMargin=host.getResources().getDimensionPixelSize(R.dimen.app_events_title_margin_start);
        ConstraintLayout.LayoutParams lp=new ConstraintLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT);lp.topToBottom=anchor;lp.startToStart=ConstraintLayout.LayoutParams.PARENT_ID;lp.endToEnd=ConstraintLayout.LayoutParams.PARENT_ID;lp.setMargins(sideMargin,dp(20),sideMargin,dp(90));list.setLayoutParams(lp);
        View funnel=root.findViewById(technician?R.id.technician_board_filter_button:R.id.ops_5717_8595);if(funnel!=null)funnel.setOnClickListener(v->showFilters());load();
    }
    private void load(){if(model==null)return;content.setVisibility(View.INVISIBLE);AppLoading.hide(loading);loading=AppLoading.show(host.requireContext());com.aula.fixa.domain.model.FiltroBusca filters=com.aula.fixa.domain.model.FiltroBusca.quadroOS(period,null,null,equipment,problem,priority,filterResponsible);model.carregar(filters,new QuadroOSViewModel.Callback(){
        @Override public void success(List<OrdemServicoQuadro> values){if(!current())return;loadFailed=false;all.clear();if(values!=null)all.addAll(values);renderList();finish();}
        @Override public void failure(Throwable error){if(!current())return;loadFailed=true;all.clear();renderList();finish();}
    });}
    public void refresh(){load();}
    private void renderList(){
        if(adapter==null)return;
        if(loadFailed)adapter.setEmptyState("Não foi possível carregar as ordens de serviço. Toque para tentar novamente.",this::load);
        else adapter.setEmptyState(null,null);
        List<OrdemServicoQuadro> filtered=new ArrayList<>();
        String term=normalize(query),selectedStatus=normalize(status);
        for(OrdemServicoQuadro order:all){
            boolean matchesText=term.isEmpty()||normalize(order.titulo).contains(term)||normalize(order.descricao).contains(term);
            String orderStatus=normalize(order.status);
            boolean matchesStatus=selectedStatus.isEmpty()||orderStatus.endsWith(selectedStatus);
            if(matchesText&&matchesStatus)filtered.add(order);
        }
        if(!loadFailed&&filtered.isEmpty())adapter.setEmptyState("Nenhuma ordem de serviço encontrada.",null);
        adapter.submit(filtered);
    }
    private void layoutStatusChips(View root,int[] ids){
        View row=root.findViewById(R.id.ops_status_row);
        if(row==null)return;
        int height=host.getResources().getDimensionPixelSize(R.dimen.app_requests_chip_height);
        int allWidth=host.getResources().getDimensionPixelSize(R.dimen.app_board_filter_all_width);
        int statusWidth=host.getResources().getDimensionPixelSize(R.dimen.app_requests_chip_min_width);
        int spacing=host.getResources().getDimensionPixelSize(R.dimen.app_requests_chip_spacing);
        ViewGroup.LayoutParams rowParams=row.getLayoutParams();if(!technician)rowParams.height=height;row.setLayoutParams(rowParams);
        ViewGroup track=(ViewGroup)((android.widget.HorizontalScrollView)row).getChildAt(0);
        if(track==null)return;
        int contentWidth=allWidth+4*statusWidth+4*spacing;
        ViewGroup.LayoutParams trackParams=track.getLayoutParams();trackParams.width=contentWidth;trackParams.height=height;track.setLayoutParams(trackParams);
        int start=0;
        for(int i=0;i<ids.length;i++){
            View chip=root.findViewById(ids[i]);if(chip==null)continue;
            int width=i==0?allWidth:statusWidth;
            ConstraintLayout.LayoutParams params=(ConstraintLayout.LayoutParams)chip.getLayoutParams();
            params.width=width;params.height=height;params.startToStart=ConstraintLayout.LayoutParams.PARENT_ID;params.setMarginStart(start);
            chip.setLayoutParams(params);start+=width+spacing;
        }
    }
    private void showFilters(){QuadroOSFiltersSheet sheet=new QuadroOSFiltersSheet();android.os.Bundle args=new android.os.Bundle();args.putString("equipment",equipment);args.putString("problem",problem);args.putString("priority",priority);args.putString("period",period);args.putString("responsible",filterResponsible==null?"":filterResponsible);sheet.setArguments(args);sheet.setTargetFragment(host,0);sheet.show(host.getParentFragmentManager(),QuadroOSFiltersSheet.TAG);}
    public void filters(String e,String p,String pr,String pe,String responsible){equipment=clean(e);problem=clean(p);priority=clean(pr);period=clean(pe);filterResponsible=responsible==null?null:clean(responsible);load();}
    private void updateStatusChips(View root,String selected,int[] ids,int[] labelIds){int activeBg=R.drawable.bg_ops_c27365e123,inactiveBg=R.drawable.bg_ops_11863650ea;int activeText=ContextCompat.getColor(host.requireContext(),R.color.ops_color_ffffffff),inactiveText=ContextCompat.getColor(host.requireContext(),R.color.ops_color_ff0b013c);String[] labels={"","Não iniciada","Em andamento","Concluída","Atrasada"};for(int i=0;i<ids.length;i++){View chip=root.findViewById(ids[i]);boolean active=(selected==null&&i==0)||(selected!=null&&i>0&&norm(selected).equals(norm(labels[i])));if(chip==null)continue;chip.setBackgroundResource(active?activeBg:inactiveBg);if(chip instanceof TextView)((TextView)chip).setTextColor(active?activeText:inactiveText);if(labelIds[i]!=0){View label=root.findViewById(labelIds[i]);if(label instanceof TextView)((TextView)label).setTextColor(active?activeText:inactiveText);}}}
    private String clean(String s){return s==null||s.equalsIgnoreCase("Todos")||s.equalsIgnoreCase("Todas")?"":s;}
    private String normalize(String value){return value==null?"":Normalizer.normalize(value,Normalizer.Form.NFD).replaceAll("\\p{M}+","").toLowerCase(java.util.Locale.ROOT).replace('_',' ').replace('-',' ').replaceAll("[^a-z0-9 ]"," ").replaceAll("\\s+"," ").trim();}
    private String norm(String v){return v==null?"":Normalizer.normalize(v,Normalizer.Form.NFD).replaceAll("\\p{M}+","").toLowerCase(java.util.Locale.ROOT).replace('_',' ').replace('-',' ').replaceAll("\\be\\b", " ").replaceAll("[^a-z0-9 ]"," ").replaceAll("\\s+"," ").trim();}
    private int dp(int n){return (int)(n*host.getResources().getDisplayMetrics().density+0.5f);}
    private boolean current(){return host.isAdded()&&host.getView() instanceof android.widget.FrameLayout&&((android.widget.FrameLayout)host.getView()).getChildAt(0)==content;}
    private void finish(){AppLoading.hide(loading);loading=null;content.setVisibility(View.VISIBLE);}
    public void clear(){AppLoading.hide(loading);loading=null;adapter=null;list=null;}
}
