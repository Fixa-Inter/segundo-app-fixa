package com.aula.fixa.ui.view.os;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.aula.fixa.R;
import com.aula.fixa.adapter.cadastro.CadastroViewModel;
import com.aula.fixa.adapter.cadastro.CadastroViewModelFactory;
import com.aula.fixa.domain.enums.CategoriaProblema;
import com.aula.fixa.domain.enums.Prioridade;
import com.aula.fixa.domain.model.OpcaoCadastro;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class QuadroOSFiltersSheet extends BottomSheetDialogFragment {
    public interface Listener { void onManagerOSFilters(String equipment,String problem,String priority,String period,String status); }
    public static final String TAG="ManagerOSFiltersSheet";
    @Override public int getTheme(){return R.style.ThemeOverlay_FIXA_App_BottomSheet;}
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater i,@Nullable ViewGroup c,@Nullable Bundle b){return i.inflate(R.layout.sheet_ops_manager_board_filters,c,false);}
    @Override public void onViewCreated(@NonNull View v,@Nullable Bundle b){
        defaultValue(v,R.id.fieldEquipment,"Todos");defaultValue(v,R.id.fieldProblem,"Todas");defaultValue(v,R.id.fieldPriority,"Todas");defaultValue(v,R.id.fieldPeriod,"Todos");defaultValue(v,R.id.fieldStatus,"Todos");
        ((ImageView)v.findViewById(R.id.fieldPeriod).findViewById(R.id.ivFilterIcon)).setImageResource(R.drawable.ic_calendar_20);
        List<String> problems=new ArrayList<>();for(CategoriaProblema x:CategoriaProblema.values())problems.add(x.getNome());
        List<String> priorities=new ArrayList<>();for(Prioridade x:Prioridade.values())priorities.add(x.getNome());
        setOptions(v,R.id.fieldProblem,problems);setOptions(v,R.id.fieldPriority,priorities);
        setOptions(v,R.id.fieldPeriod,Arrays.asList("Últimos 30 dias","Últimos 3 meses","Último ano","Todos"));
        setOptions(v,R.id.fieldStatus,Arrays.asList("Todos","Não Iniciada","Em andamento","Concluída","Atrasada"));
        CadastroViewModel vm=new ViewModelProvider(this,new CadastroViewModelFactory(requireContext())).get(CadastroViewModel.class);
        vm.listarCategorias(new CadastroViewModel.Callback<List<OpcaoCadastro>>(){public void aoConcluir(List<OpcaoCadastro> xs){if(isAdded())setOptions(v,R.id.fieldEquipment,names(xs));}public void aoFalhar(Throwable e){if(isAdded())setOptions(v,R.id.fieldEquipment,new ArrayList<>());}});
        v.findViewById(R.id.btnApply).setOnClickListener(x->{Fragment target=getTargetFragment();if(target instanceof Listener)((Listener)target).onManagerOSFilters(value(v,R.id.fieldEquipment),value(v,R.id.fieldProblem),value(v,R.id.fieldPriority),value(v,R.id.fieldPeriod),value(v,R.id.fieldStatus));dismiss();});
        v.findViewById(R.id.btnClear).setOnClickListener(x->{defaultValue(v,R.id.fieldEquipment,"Todos");defaultValue(v,R.id.fieldProblem,"Todas");defaultValue(v,R.id.fieldPriority,"Todas");defaultValue(v,R.id.fieldPeriod,"Todos");defaultValue(v,R.id.fieldStatus,"Todos");Fragment target=getTargetFragment();if(target instanceof Listener)((Listener)target).onManagerOSFilters("","","","","Todos");dismiss();});
    }
    private List<String> names(List<OpcaoCadastro> values){List<String> r=new ArrayList<>();if(values!=null)for(OpcaoCadastro x:values)r.add(x.getNome());return r;}
    private AutoCompleteTextView field(View root,int id){return root.findViewById(id).findViewById(R.id.actvFilterValue);}
    private void defaultValue(View root,int id,String hint){AutoCompleteTextView f=field(root,id);f.setText("");f.setHint(hint);}
    private String value(View root,int id){return field(root,id).getText().toString();}
    private void setOptions(View root,int id,List<String> values){AutoCompleteTextView f=field(root,id);f.setAdapter(new ArrayAdapter<>(root.getContext(),R.layout.item_app_dropdown,values));f.setOnClickListener(x->f.showDropDown());f.setOnTouchListener((x,e)->{if(e.getAction()==android.view.MotionEvent.ACTION_UP)f.showDropDown();return false;});}
}
