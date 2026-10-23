package com.aula.fixa.adapter.operations;

import android.os.Bundle;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

/** Rascunhos da interface sobrevivem a navegação e recriação da Activity. */
public final class OperationsViewModel extends ViewModel {
    private final SavedStateHandle state;
    public OperationsViewModel(SavedStateHandle state) { this.state = state; }

    private String group(String screen) {
        String name = screen.replaceFirst("^(manager|technician)_", "");
        if (name.startsWith("occurrence_")) return "occurrence";
        if (name.startsWith("checklist_")) return "checklist";
        if (name.startsWith("planning_") || name.equals("plan_information")) return "planning";
        if (name.contains("profile")) return "profile";
        if(name.contains("field")&&!name.equals("checklist_fields"))return "field_draft:"+selectedId();
        if(name.startsWith("edit_"))return name+":"+selectedId();
        return name;
    }

    public String field(String screen, String label) {
        Bundle drafts = state.get("drafts");
        return drafts == null ? null : drafts.getString(group(screen) + ":" + label);
    }

    public void field(String screen, String label, String value) {
        Bundle old = state.get("drafts");
        Bundle drafts = old == null ? new Bundle() : new Bundle(old);
        drafts.putString(group(screen) + ":" + label, value);
        state.set("drafts", drafts);
    }

    public boolean selected(String key) { return Boolean.TRUE.equals(state.get(key)); }
    public void selected(String key, boolean selected) { state.set(key, selected); }
    public void clearDraft(String screen) {
        Bundle old=state.get("drafts");if(old==null)return;
        Bundle copy=new Bundle(old);String prefix=group(screen)+":";
        for(String key:new java.util.ArrayList<>(copy.keySet()))if(key.startsWith(prefix))copy.remove(key);
        state.set("drafts",copy);
    }

    public static String scope(String screen) {
        String value=screen.replaceFirst("^(manager|technician)_", "");
        if(value.contains("categor"))return "categories";
        if(value.contains("model")&&!value.startsWith("checklist_"))return "models";
        if(value.contains("device"))return "devices";
        if(value.contains("task") || value.startsWith("execution_"))return "tasks";
        if(value.contains("field"))return "fields";
        if(value.contains("option"))return "options";
        return value;
    }
    public void selectEntry(String scope,String id,String title) {
        state.set("selection_scope",scope);state.set("selection_id",id);state.set("selection_title",title);
    }
    public String selectedScope() { String v=state.get("selection_scope");return v==null?"":v; }
    public String selectedId() { String v=state.get("selection_id");return v==null?"":v; }
    public String selectedTitle() { String v=state.get("selection_title");return v==null?"":v; }
    public java.util.ArrayList<Bundle> entries(String scope) {
        java.util.ArrayList<Bundle> data=state.get("rows:"+scope);
        return data==null?new java.util.ArrayList<>():new java.util.ArrayList<>(data);
    }
    public void saveEntry(String scope,String id,String title,String description) {
        java.util.ArrayList<Bundle> data=entries(scope);
        String entryId=id.isEmpty()?java.util.UUID.randomUUID().toString():id;
        data.removeIf(row->entryId.equals(row.getString("id")));
        Bundle row=new Bundle();row.putString("id",entryId);row.putString("title",title);row.putString("description",description);
        data.add(row);state.set("rows:"+scope,data);
    }
    public void deleteEntry(String scope,String id) {
        java.util.ArrayList<Bundle> data=entries(scope);
        data.removeIf(row->id.equals(row.getString("id")));
        state.set("rows:"+scope,data);state.set("deleted:"+id,true);
    }
    public boolean deleted(String id) { return Boolean.TRUE.equals(state.get("deleted:"+id)); }
}
