package com.aula.fixa.ui.view.ocorrencia;

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
import com.aula.fixa.R;
import com.aula.fixa.domain.enums.CategoriaProblema;
import com.aula.fixa.domain.enums.Prioridade;
import com.aula.fixa.ui.view.FullWidthFilterSheet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class OcorrenciaFiltrosSheet extends FullWidthFilterSheet {
    public interface Listener { void onOccurrenceFilters(String period,String author,String location,String equipment,String category,String priority); }
    public static final String TAG="OcorrenciaFiltrosSheet";
    @Override public int getTheme(){return R.style.ThemeOverlay_FIXA_App_BottomSheet;}
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle state){return inflater.inflate(R.layout.sheet_ops_manager_occurrence_filters,container,false);}
    @Override public void onViewCreated(@NonNull View view,@Nullable Bundle state){super.onViewCreated(view,state);
        setDefault(view,R.id.fieldPeriod,R.string.app_filter_period_default);setDefault(view,R.id.fieldAuthor,R.string.app_filter_author_default);
        setDefault(view,R.id.fieldLocation,R.string.app_filter_location_default);setDefault(view,R.id.fieldEquipment,R.string.app_filter_location_default);
        setDefault(view,R.id.fieldCategory,R.string.app_filter_location_default);setDefault(view,R.id.fieldPriority,R.string.app_filter_location_default);
        ((ImageView)view.findViewById(R.id.fieldPeriod).findViewById(R.id.ivFilterIcon)).setImageResource(R.drawable.ic_calendar_20);
        List<String> periods=Arrays.asList(getString(R.string.app_filter_period_default),"Últimos 30 dias","Últimos 3 meses","Último ano","Todos");
        setAdapter(view,R.id.fieldPeriod,periods);setAdapter(view,R.id.fieldCategory,enumCategories());setAdapter(view,R.id.fieldPriority,enumPriorities());
        Bundle args=getArguments();setAdapter(view,R.id.fieldAuthor,args==null?new ArrayList<>():args.getStringArrayList("authors"));
        setAdapter(view,R.id.fieldLocation,args==null?new ArrayList<>():args.getStringArrayList("locations"));
        setAdapter(view,R.id.fieldEquipment,args==null?new ArrayList<>():args.getStringArrayList("equipment"));
        view.findViewById(R.id.btnApply).setOnClickListener(v->{Fragment target=getTargetFragment();if(target instanceof Listener)((Listener)target).onOccurrenceFilters(value(view,R.id.fieldPeriod),value(view,R.id.fieldAuthor),value(view,R.id.fieldLocation),value(view,R.id.fieldEquipment),value(view,R.id.fieldCategory),value(view,R.id.fieldPriority));dismiss();});
        view.findViewById(R.id.btnClear).setOnClickListener(v->{Fragment target=getTargetFragment();if(target instanceof Listener)((Listener)target).onOccurrenceFilters("","","","","","");dismiss();});
    }
    private List<String> enumCategories(){List<String> values=new ArrayList<>();for(CategoriaProblema c:CategoriaProblema.values())values.add(c.getNome());return values;}
    private List<String> enumPriorities(){List<String> values=new ArrayList<>();for(Prioridade p:Prioridade.values())values.add(p.getNome());return values;}
    private AutoCompleteTextView field(View root,int id){return root.findViewById(id).findViewById(R.id.actvFilterValue);}
    private String value(View root,int id){return field(root,id).getText().toString();}
    private void setDefault(View root,int id,int hint){AutoCompleteTextView f=field(root,id);f.setText("");f.setHint(hint);}
    private void setAdapter(View root,int id,List<String> values){AutoCompleteTextView f=field(root,id);List<String> safe=values==null?new ArrayList<>():values;f.setAdapter(new ArrayAdapter<>(root.getContext(),R.layout.item_app_dropdown,safe));f.setOnClickListener(v->f.showDropDown());f.setOnTouchListener((v,event)->{if(event.getAction()==android.view.MotionEvent.ACTION_UP)f.showDropDown();return false;});}
}
