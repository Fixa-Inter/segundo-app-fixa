package com.aula.fixa.ui.operations;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.aula.fixa.R;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Arrays;

/** Estado de apresentação local para a revisão dos fluxos, sem simular gravações na API. */
public final class OperationsData {
    private OperationsData(){}
    public static void bind(OperationsActivity activity,View root,JSONObject screen) {
        String key=screen.optString("key"), name=screen.optString("name"), scope=OperationsViewModel.scope(name);
        if(Arrays.asList("categories","models","devices","planning_tasks","checklist_fields").contains(name)) {
            JSONArray bindings=screen.optJSONArray("bindings");
            double bottom=160;
            for(int i=0;bindings!=null&&i<bindings.length();i++) {
                JSONObject b=bindings.optJSONObject(i);if(b==null||!b.optString("kind").equals("card"))continue;
                int id=activity.getResources().getIdentifier(b.optString("id"),"id",activity.getPackageName());
                View card=root.findViewById(id);if(card==null)continue;
                if(activity.model.deleted(b.optString("id")))card.setVisibility(View.GONE);
                for(Bundle entry:activity.model.entries(scope))if(b.optString("id").equals(entry.getString("id"))) {
                    TextView title=largest(card);if(title!=null)title.setText(entry.getString("title"));
                }
                JSONArray bounds=b.optJSONArray("bounds");bottom=Math.max(bottom,bounds.optDouble(1)+bounds.optDouble(3));
            }
            ConstraintLayout content=findContent(root);
            if(content!=null) {
                LinearLayout rows=new LinearLayout(activity);rows.setOrientation(LinearLayout.VERTICAL);rows.setId(View.generateViewId());
                ConstraintLayout.LayoutParams params=new ConstraintLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT);
                params.startToStart=ConstraintLayout.LayoutParams.PARENT_ID;params.endToEnd=ConstraintLayout.LayoutParams.PARENT_ID;
                params.topToTop=ConstraintLayout.LayoutParams.PARENT_ID;
                params.topMargin=(int)((bottom+16)*scale(activity));
                params.setMarginStart(activity.getResources().getDimensionPixelSize(R.dimen.ops_list_side));
                params.setMarginEnd(activity.getResources().getDimensionPixelSize(R.dimen.ops_list_side));
                content.addView(rows,params);
                for(Bundle entry:activity.model.entries(scope)) {
                    if(entry.getString("id","").startsWith("ops_"))continue;
                    View row=activity.getLayoutInflater().inflate(R.layout.item_ops_local_entry,rows,false);
                    ((TextView)row.findViewById(R.id.opsEntryTitle)).setText(entry.getString("title"));
                    ((TextView)row.findViewById(R.id.opsEntryDescription)).setText(entry.getString("description"));
                    row.setOnClickListener(v->{activity.model.selectEntry(scope,entry.getString("id"),entry.getString("title"));activity.openName(editor(scope));});
                    row.setOnLongClickListener(v->{activity.model.selectEntry(scope,entry.getString("id"),entry.getString("title"));activity.openName("delete_confirm");return true;});
                    rows.addView(row);
                }
            }
        }
        if(name.equals("occurrence_result")) photos(activity,root,screen);
        if(name.equals("occurrence_review"))review(activity,root,screen);
        if(name.startsWith("execution_")) {
            JSONArray bindings=screen.optJSONArray("bindings");
            for(int i=0;bindings!=null&&i<bindings.length();i++) {
                JSONObject item=bindings.optJSONObject(i);if(item==null||!item.optString("kind").equals("card"))continue;
                String id=item.optString("id");
                if(activity.model.selected("task:"+id)) {
                    View card=root.findViewById(activity.getResources().getIdentifier(id,"id",activity.getPackageName()));
                    if(card!=null){card.setSelected(true);card.setForeground(new android.graphics.drawable.ColorDrawable(androidx.core.content.ContextCompat.getColor(activity,R.color.ops_selected_overlay)));}
                }
            }
        }
    }
    private static void review(OperationsActivity activity,View root,JSONObject screen) {
        String role=activity.role(), title=activity.model.field(role+"_occurrence_service","Título"), description=activity.model.field(role+"_occurrence_service","Problema encontrado");
        String place=activity.model.field(role+"_occurrence_location","Local"),date=activity.model.field(role+"_occurrence_location","Data"),solution=activity.model.field(role+"_occurrence_result","Solução aplicada"),difficulty=activity.model.field(role+"_occurrence_result","Grau de dificuldade");
        JSONArray bindings=screen.optJSONArray("bindings");
        for(int i=0;bindings!=null&&i<bindings.length();i++) {
            JSONObject b=bindings.optJSONObject(i);if(b==null||!b.optString("kind").equals("text"))continue;
            View view=root.findViewById(activity.getResources().getIdentifier(b.optString("id"),"id",activity.getPackageName()));
            if(!(view instanceof TextView))continue;TextView text=(TextView)view;String original=b.optString("text");
            if(original.equals("Ar-condicionado quebrado")&&title!=null)text.setText(title);
            else if(original.startsWith("O ar-condicionado")&&description!=null)text.setText(description);
            else if(original.startsWith("Local:")&&place!=null)text.setText("Local: "+place+(date==null?"":" - "+date));
            else if(original.startsWith("Solução aplicada:")&&solution!=null)text.setText("Solução aplicada: "+solution);
            else if(original.startsWith("Dificuldade:")&&difficulty!=null)text.setText("Dificuldade: "+difficulty);
        }
    }
    private static float scale(OperationsActivity activity) {
        float dp=activity.getResources().getDisplayMetrics().widthPixels/activity.getResources().getDisplayMetrics().density;
        return activity.getResources().getDisplayMetrics().density*Math.min(dp,640f)/412f;
    }
    private static String editor(String scope) {
        switch(scope){case "categories":return "edit_category";case "models":return "edit_model";case "devices":return "edit_device";case "tasks":return "edit_task";default:return "edit_field";}
    }
    private static ConstraintLayout findContent(View root) {
        if(root instanceof ConstraintLayout)return (ConstraintLayout)root;
        if(root instanceof ViewGroup)for(int i=0;i<((ViewGroup)root).getChildCount();i++){ConstraintLayout found=findContent(((ViewGroup)root).getChildAt(i));if(found!=null)return found;}
        return null;
    }
    private static TextView largest(View root) {
        TextView result=root instanceof TextView?(TextView)root:null;
        if(root instanceof ViewGroup)for(int i=0;i<((ViewGroup)root).getChildCount();i++) {
            TextView candidate=largest(((ViewGroup)root).getChildAt(i));
            if(candidate!=null&&(result==null||candidate.getTextSize()>result.getTextSize()))result=candidate;
        }
        return result;
    }
    private static void photos(OperationsActivity activity,View root,JSONObject screen) {
        String value=activity.model.field(screen.optString("key"),"photos");if(value==null)return;
        try {
            JSONArray uris=new JSONArray(value);if(uris.length()==0)return;
            JSONArray bindings=screen.optJSONArray("bindings");
            for(int i=0;i<bindings.length();i++) {
                JSONObject binding=bindings.getJSONObject(i);
                if(!binding.optString("text").equals("Adicionar fotos"))continue;
                View selected=root.findViewById(activity.getResources().getIdentifier(binding.optString("id"),"id",activity.getPackageName()));
                if(selected==null)continue;
                View parent=selected.getParent() instanceof View?(View)selected.getParent():selected;
                while(!(parent instanceof ConstraintLayout)&&parent.getParent() instanceof View)parent=(View)parent.getParent();
                if(!(parent instanceof ConstraintLayout))continue;
                ConstraintLayout holder=(ConstraintLayout)parent;
                for(int c=0;c<holder.getChildCount();c++)holder.getChildAt(c).setVisibility(View.GONE);
                View strip=activity.getLayoutInflater().inflate(R.layout.view_ops_photos,holder,false);
                ConstraintLayout.LayoutParams params=new ConstraintLayout.LayoutParams(0,0);
                params.topToTop=0;params.bottomToBottom=0;params.startToStart=0;params.endToEnd=0;holder.addView(strip,params);
                LinearLayout row=strip.findViewById(R.id.opsPhotos);
                for(int u=0;u<uris.length();u++) {
                    ImageView thumb=(ImageView)activity.getLayoutInflater().inflate(R.layout.item_ops_photo,row,false);
                    Glide.with(activity).load(android.net.Uri.parse(uris.getString(u))).into(thumb);row.addView(thumb);
                }
                ImageView add=new ImageView(activity);int size=activity.getResources().getDimensionPixelSize(R.dimen.ops_touch_target);
                add.setLayoutParams(new LinearLayout.LayoutParams(size,size));add.setImageResource(R.drawable.figma_58b2a);
                add.setContentDescription(activity.getString(R.string.ops_add_photo));add.setOnClickListener(v->activity.photos());row.addView(add);
                break;
            }
        }catch(Exception ignored){ /* URI indisponível: o campo original continua utilizável. */ }
    }
}
