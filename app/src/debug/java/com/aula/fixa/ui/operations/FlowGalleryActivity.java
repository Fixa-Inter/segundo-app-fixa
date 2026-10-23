package com.aula.fixa.ui.operations;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import androidx.appcompat.app.AppCompatActivity;
import com.aula.fixa.R;
import java.util.*;

/** Galeria apenas de debug: permite revisar os fluxos sem conta ou dados reais. */
public final class FlowGalleryActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);setContentView(R.layout.activity_flow_gallery);
        List<Map<String,String>> groups=new ArrayList<>();
        List<List<Map<String,String>>> children=new ArrayList<>();
        for(String role:new String[]{"manager","technician"}) {
            Map<String,String> group=new HashMap<>();group.put("label",role.equals("manager")?"Gestor":"Técnico");groups.add(group);
            List<Map<String,String>> items=new ArrayList<>();
            for(var entry:ScreenCatalog.get(this).all().entrySet()) {
                if(!entry.getValue().optString("role").equals(role))continue;
                Map<String,String> item=new HashMap<>();item.put("key",entry.getKey());item.put("label",entry.getValue().optString("displayName",entry.getValue().optString("name")));items.add(item);
            }
            children.add(items);
        }
        ExpandableListView list=findViewById(R.id.opsGalleryList);
        list.setAdapter(new SimpleExpandableListAdapter(this,groups,android.R.layout.simple_expandable_list_item_1,new String[]{"label"},new int[]{android.R.id.text1},children,android.R.layout.simple_list_item_1,new String[]{"label"},new int[]{android.R.id.text1}));
        list.setOnChildClickListener((parent,view,group,child,id)->{startActivity(new Intent(this,FlowPreviewActivity.class).putExtra("screen",children.get(group).get(child).get("key")));return true;});
    }
}
