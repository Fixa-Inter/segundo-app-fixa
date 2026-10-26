package com.aula.fixa.ui.view.ocorrencia;

import android.app.Dialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.aula.fixa.R;
import com.aula.fixa.adapter.auth.AutenticacaoViewModel;
import com.aula.fixa.adapter.ocorrencia.OcorrenciaViewModel;
import com.aula.fixa.adapter.ocorrencia.OcorrenciaViewModelFactory;
import com.aula.fixa.domain.model.ocorrencia.OcorrenciaItem;
import com.aula.fixa.domain.model.ocorrencia.OcorrenciaFoto;
import com.aula.fixa.ui.adapter.ocorrencia.OcorrenciaListAdapter;
import com.aula.fixa.ui.adapter.ocorrencia.OcorrenciaEditPhotoAdapter;
import com.aula.fixa.ui.config.AppDialogs;
import com.aula.fixa.ui.config.AppLoading;
import com.aula.fixa.ui.view.operations.OperationsActivity;
import com.aula.fixa.ui.view.operations.OperationsScreenFragment;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/** Lista e detalhes das ocorrências do gestor, com filtragem local dos resultados da API. */
public final class OcorrenciaGestorFluxo {
    private final OperationsScreenFragment host; private final OperationsActivity activity;
    private OcorrenciaViewModel model; private View content; private RecyclerView list; private OcorrenciaListAdapter adapter;
    private final List<OcorrenciaItem> all=new ArrayList<>(); private Dialog loading;
    private String query="",period="",author="",location="",equipment="",category="",priority="";
    private com.aula.fixa.adapter.foto.FotoViewModel fotoModel;
    private OcorrenciaEditPhotoAdapter photoAdapter;
    private final List<OcorrenciaFoto> editPhotos=new ArrayList<>();
    private final List<Long> deletedPhotoIds=new ArrayList<>();
    private final List<String> addedPhotoUrls=new ArrayList<>();
    private List<com.aula.fixa.domain.model.OpcaoCadastro> localOptions=new ArrayList<>(),equipmentOptions=new ArrayList<>();
    private int pendingPhotoUploads;
    public OcorrenciaGestorFluxo(OperationsScreenFragment host,OperationsActivity activity){this.host=host;this.activity=activity;}
    public void bindList(View root){
        content=root; model=new ViewModelProvider(host,new OcorrenciaViewModelFactory(host.requireContext())).get(OcorrenciaViewModel.class);
        View back=root.findViewById(R.id.managerOccurrencesBack);if(back!=null)back.setOnClickListener(v->activity.back());
        EditText search=root.findViewById(R.id.managerOccurrencesSearch);if(search!=null)search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){query=s.toString();renderList();}public void afterTextChanged(Editable e){}});
        list=root.findViewById(R.id.managerOccurrencesList);list.setLayoutManager(new LinearLayoutManager(host.requireContext()));
        adapter=new OcorrenciaListAdapter(new OcorrenciaListAdapter.Listener(){public void onClick(OcorrenciaItem item){activity.openOccurrenceDetails(item.id,item.titulo);}public void onMenuClick(View anchor,OcorrenciaItem item){showMenu(anchor,item);}});list.setAdapter(adapter);
        View filter=root.findViewById(R.id.managerOccurrencesFilter);if(filter!=null)filter.setOnClickListener(v->showFilters());
        View fab=root.findViewById(R.id.managerOccurrencesFab);if(fab!=null)fab.setOnClickListener(v->{activity.model.clearDraft("manager_new_occurrence_form");activity.openName("occurrence_intro");});
        load();
    }
    public void refresh(){load();}

    /** Bind the manager's four-step occurrence creation form. */
    public void bindCreateStep(View root, String screen) {
        content = root;
        String scope = "manager_new_occurrence_form";
        if ("manager_occurrence_intro".equals(screen)) {
            EditText title = root.findViewById(R.id.occurrenceCreateTitle);
            EditText description = root.findViewById(R.id.occurrenceCreateDescription);
            title.setText(value(scope, "title"));
            description.setText(value(scope, "description"));
            root.findViewById(R.id.occurrenceCreateCancel).setOnClickListener(v -> activity.back());
            root.findViewById(R.id.occurrenceCreateNext).setOnClickListener(v -> {
                String t = title.getText().toString().trim(), d = description.getText().toString().trim();
                if (t.isEmpty() || d.isEmpty()) { title.setError("Preencha o título e a descrição."); return; }
                activity.model.field(scope, "title", t);
                activity.model.field(scope, "description", d);
                activity.openName("occurrence_service");
            });
            return;
        }
        if ("manager_occurrence_service".equals(screen)) {
            android.widget.AutoCompleteTextView local = editField(root, R.id.occurrenceCreateLocal);
            EditText locationDescription = root.findViewById(R.id.occurrenceCreateLocationDescription);
            locationDescription.setText(value(scope, "locationDescription"));
            setOptions(local, new ArrayList<>());
            com.aula.fixa.adapter.cadastro.CadastroViewModel catalogs = new ViewModelProvider(host,
                    new com.aula.fixa.adapter.cadastro.CadastroViewModelFactory(host.requireContext()))
                    .get(com.aula.fixa.adapter.cadastro.CadastroViewModel.class);
            catalogs.listarLocais(new com.aula.fixa.adapter.cadastro.CadastroViewModel.Callback<List<com.aula.fixa.domain.model.OpcaoCadastro>>() {
                public void aoConcluir(List<com.aula.fixa.domain.model.OpcaoCadastro> values) {
                    if (!host.isAdded()) return;
                    localOptions = values == null ? new ArrayList<>() : values;
                    List<String> names = new ArrayList<>(); for (com.aula.fixa.domain.model.OpcaoCadastro option : localOptions) names.add(option.getNome());
                    setOptions(local, names); local.setText(value(scope, "localName"), false);
                }
                public void aoFalhar(Throwable error) { showError(error); }
            });
            root.findViewById(R.id.occurrenceCreateBack).setOnClickListener(v -> activity.back());
            root.findViewById(R.id.occurrenceCreateNext).setOnClickListener(v -> {
                String name = local.getText().toString().trim(), description = locationDescription.getText().toString().trim();
                if (findId(localOptions, name) == null || description.isEmpty()) { local.setError("Selecione o local e informe a descrição."); return; }
                activity.model.field(scope, "localId", String.valueOf(findId(localOptions, name)));
                activity.model.field(scope, "localName", name);
                activity.model.field(scope, "locationDescription", description);
                activity.openName("occurrence_location");
            });
            return;
        }
        if ("manager_occurrence_location".equals(screen)) {
            android.widget.AutoCompleteTextView equipment = editField(root, R.id.occurrenceCreateEquipment);
            android.widget.AutoCompleteTextView category = editField(root, R.id.occurrenceCreateCategory);
            android.widget.AutoCompleteTextView priority = editField(root, R.id.occurrenceCreatePriority);
            setOptions(category, enumNames(com.aula.fixa.domain.enums.CategoriaProblema.values()));
            setOptions(priority, enumNames(com.aula.fixa.domain.enums.Prioridade.values()));
            category.setText(value(scope, "categoryName"), false); priority.setText(value(scope, "priorityName"), false);
            com.aula.fixa.adapter.cadastro.CadastroViewModel catalogs = new ViewModelProvider(host,
                    new com.aula.fixa.adapter.cadastro.CadastroViewModelFactory(host.requireContext()))
                    .get(com.aula.fixa.adapter.cadastro.CadastroViewModel.class);
            catalogs.listarCategorias(new com.aula.fixa.adapter.cadastro.CadastroViewModel.Callback<List<com.aula.fixa.domain.model.OpcaoCadastro>>() {
                public void aoConcluir(List<com.aula.fixa.domain.model.OpcaoCadastro> values) {
                    if (!host.isAdded()) return;
                    equipmentOptions = values == null ? new ArrayList<>() : values;
                    List<String> names = new ArrayList<>(); for (com.aula.fixa.domain.model.OpcaoCadastro option : equipmentOptions) names.add(option.getNome());
                    setOptions(equipment, names); equipment.setText(value(scope, "equipmentName"), false);
                }
                public void aoFalhar(Throwable error) { showError(error); }
            });
            root.findViewById(R.id.occurrenceCreateBack).setOnClickListener(v -> activity.back());
            root.findViewById(R.id.occurrenceCreateNext).setOnClickListener(v -> {
                String equipmentName = equipment.getText().toString().trim(), categoryName = category.getText().toString().trim(), priorityName = priority.getText().toString().trim();
                if (findId(equipmentOptions, equipmentName) == null || category(categoryName) == null || priority(priorityName) == null) {
                    Toast.makeText(host.requireContext(), R.string.ops_occurrence_select_all_fields, Toast.LENGTH_SHORT).show(); return;
                }
                activity.model.field(scope, "equipmentId", String.valueOf(findId(equipmentOptions, equipmentName)));
                activity.model.field(scope, "equipmentName", equipmentName);
                activity.model.field(scope, "category", category(categoryName).name()); activity.model.field(scope, "categoryName", categoryName);
                activity.model.field(scope, "priority", priority(priorityName).name()); activity.model.field(scope, "priorityName", priorityName);
                activity.openName("occurrence_result");
            });
            return;
        }
        android.widget.TextView photosCount = root.findViewById(R.id.occurrenceCreatePhotosCount);
        RecyclerView photosList = root.findViewById(R.id.occurrenceCreatePhotosList);
        photosList.setLayoutManager(new LinearLayoutManager(host.requireContext(), RecyclerView.HORIZONTAL, false));
        List<OcorrenciaFoto> selectedPhotos = createPhotos(scope);
        OcorrenciaEditPhotoAdapter createPhotoAdapter = new OcorrenciaEditPhotoAdapter(photo -> {
            selectedPhotos.remove(photo);
            org.json.JSONArray saved = new org.json.JSONArray(); for (OcorrenciaFoto remaining : selectedPhotos) saved.put(remaining.url);
            activity.model.field(scope, "photos", saved.toString());
            photosCount.setText(selectedPhotos.isEmpty() ? "Nenhuma foto adicionada" : host.getString(R.string.ops_photos_added, selectedPhotos.size()));
            ((OcorrenciaEditPhotoAdapter) photosList.getAdapter()).submit(selectedPhotos);
        });
        photosList.setAdapter(createPhotoAdapter); createPhotoAdapter.submit(selectedPhotos);
        photosCount.setText(selectedPhotos.isEmpty() ? "Nenhuma foto adicionada" : host.getString(R.string.ops_photos_added, selectedPhotos.size()));
        root.findViewById(R.id.occurrenceCreateBack).setOnClickListener(v -> activity.back());
        root.findViewById(R.id.occurrenceCreateAddPhotos).setOnClickListener(v -> activity.photos());
        root.findViewById(R.id.occurrenceCreateFinish).setOnClickListener(v -> activity.openName("occurrence_success"));
    }

    private String value(String scope, String field) {
        String value = activity.model.field(scope, field);
        return value == null ? "" : value;
    }

    private List<String> enumNames(Object[] options) {
        List<String> names = new ArrayList<>();
        for (Object option : options) {
            if (option instanceof com.aula.fixa.domain.enums.CategoriaProblema) names.add(((com.aula.fixa.domain.enums.CategoriaProblema) option).getNome());
            else if (option instanceof com.aula.fixa.domain.enums.Prioridade) names.add(((com.aula.fixa.domain.enums.Prioridade) option).getNome());
        }
        return names;
    }

    private int photoCount(String scope) {
        try { return new org.json.JSONArray(value(scope, "photos")).length(); }
        catch (Exception ignored) { return 0; }
    }

    private List<OcorrenciaFoto> createPhotos(String scope) {
        List<OcorrenciaFoto> photos = new ArrayList<>();
        try { org.json.JSONArray values = new org.json.JSONArray(value(scope, "photos"));
            for (int i = 0; i < values.length(); i++) photos.add(new OcorrenciaFoto(0, values.getString(i)));
        } catch (Exception ignored) { }
        return photos;
    }
    private void load(){if(model==null||content==null)return;content.setVisibility(View.INVISIBLE);AppLoading.hide(loading);loading=AppLoading.show(host.requireContext());model.listar(new OcorrenciaViewModel.Callback<List<OcorrenciaItem>>(){
        @Override public void success(List<OcorrenciaItem> values){if(!current())return;all.clear();if(values!=null)all.addAll(values);renderList();finish();}
        @Override public void failure(Throwable error){if(!current())return;all.clear();renderList();finish();Toast.makeText(host.requireContext(),AutenticacaoViewModel.messageFrom(error),Toast.LENGTH_LONG).show();}
    });}
    private void renderList(){if(adapter==null)return;String q=norm(query);List<OcorrenciaItem> visible=new ArrayList<>();for(OcorrenciaItem item:all){if(!matchesQuery(item,q)||!matches(item.autor,author)||!matches(item.localUnidade,location)||!matches(item.categoriaEquipamento,equipment)||!matches(item.categoriaProblema,category)||!matches(item.prioridade,priority)||!matchesPeriod(item.dataCriacao,period))continue;visible.add(item);}adapter.submit(visible);}
    private boolean matchesQuery(OcorrenciaItem i,String q){return q.isEmpty()||norm(i.titulo).contains(q);}
    private boolean matches(String value,String selected){return selected==null||selected.trim().isEmpty()||norm(value).equals(norm(selected));}
    private String norm(String value){return Normalizer.normalize(value==null?"":value,Normalizer.Form.NFD).replaceAll("\\p{M}+","").toLowerCase(java.util.Locale.ROOT).replace('_',' ').trim();}
    private boolean matchesPeriod(String raw,String selected){if(selected==null||selected.trim().isEmpty()||norm(selected).contains("todos")||norm(selected).contains("periodo"))return true;try{LocalDate date=parseDate(raw);String p=norm(selected);long days=p.contains("30")?30:p.contains("3 meses")?90:365;return !date.isBefore(LocalDate.now().minusDays(days));}catch(Exception ignored){return false;}}
    private LocalDate parseDate(String raw){try{return OffsetDateTime.parse(raw).toLocalDate();}catch(Exception ignored){}try{return LocalDateTime.parse(raw).toLocalDate();}catch(Exception ignored){}return LocalDate.parse(raw);}
    private void showFilters(){OcorrenciaFiltrosSheet sheet=new OcorrenciaFiltrosSheet();android.os.Bundle args=new android.os.Bundle();args.putStringArrayList("authors",distinct(0));args.putStringArrayList("locations",distinct(1));args.putStringArrayList("equipment",distinct(2));sheet.setArguments(args);sheet.setTargetFragment(host,0);sheet.show(host.getParentFragmentManager(),OcorrenciaFiltrosSheet.TAG);}
    private ArrayList<String> distinct(int field){TreeSet<String> values=new TreeSet<>(String.CASE_INSENSITIVE_ORDER);for(OcorrenciaItem i:all){String v=field==0?i.autor:field==1?i.localUnidade:i.categoriaEquipamento;if(v!=null&&!v.trim().isEmpty())values.add(v.trim());}return new ArrayList<>(values);}
    public void filters(String p,String a,String l,String e,String c,String pr){period=clean(p);author=clean(a);location=clean(l);equipment=clean(e);category=clean(c);priority=clean(pr);renderList();}
    private String clean(String v){return v==null||v.equalsIgnoreCase("Todos")||v.equalsIgnoreCase("Todas")||v.startsWith("Selecione")?"":v.trim();}
    private void showMenu(View anchor,OcorrenciaItem item){View menu=LayoutInflater.from(anchor.getContext()).inflate(R.layout.popup_app_request_menu,null,false);PopupWindow popup=new PopupWindow(menu,anchor.getResources().getDimensionPixelSize(R.dimen.app_popup_menu_width),ViewGroup.LayoutParams.WRAP_CONTENT,true);popup.setBackgroundDrawable(androidx.core.content.ContextCompat.getDrawable(anchor.getContext(),R.drawable.bg_app_popup_menu));popup.setElevation(dp(6));menu.findViewById(R.id.tvPopupEdit).setOnClickListener(v->{popup.dismiss();showEdit(item);});menu.findViewById(R.id.tvPopupDelete).setOnClickListener(v->{popup.dismiss();AppDialogs.showDeleteConfirm(anchor.getContext(),()->model.excluir(item.id,new OcorrenciaViewModel.Callback<Void>(){public void success(Void ignored){Toast.makeText(anchor.getContext(),R.string.ops_occurrence_deleted,Toast.LENGTH_SHORT).show();load();}public void failure(Throwable error){showError(error);}}));});popup.setOutsideTouchable(true);popup.showAsDropDown(anchor,-popup.getWidth()+anchor.getWidth(),0);}
    private void showEdit(OcorrenciaItem item){activity.openOccurrenceEdit(item);}
    public void bindEditInformation(View root){
        content=root;android.os.Bundle args=host.getArguments();if(args==null)return;
        View back=root.findViewById(R.id.occurrenceEditInfoBack);if(back!=null)back.setOnClickListener(v->activity.back());
        android.widget.AutoCompleteTextView local=editField(root,R.id.occurrenceEditFieldLocation),equipmentField=editField(root,R.id.occurrenceEditFieldEquipment),categoryField=editField(root,R.id.occurrenceEditFieldCategory),priorityField=editField(root,R.id.occurrenceEditFieldPriority);
        local.setHint(R.string.app_filter_location_default);equipmentField.setHint(R.string.app_filter_location_default);categoryField.setHint(R.string.app_filter_location_default);priorityField.setHint(R.string.app_filter_location_default);
        List<String> categories=new ArrayList<>(),priorities=new ArrayList<>();for(com.aula.fixa.domain.enums.CategoriaProblema c:com.aula.fixa.domain.enums.CategoriaProblema.values())categories.add(c.getNome());for(com.aula.fixa.domain.enums.Prioridade p:com.aula.fixa.domain.enums.Prioridade.values())priorities.add(p.getNome());
        setOptions(categoryField,categories);setOptions(priorityField,priorities);categoryField.setText(args.getString("categoryName",""),false);priorityField.setText(args.getString("priorityName",""),false);
        com.aula.fixa.adapter.cadastro.CadastroViewModel catalogs=new ViewModelProvider(host,new com.aula.fixa.adapter.cadastro.CadastroViewModelFactory(host.requireContext())).get(com.aula.fixa.adapter.cadastro.CadastroViewModel.class);
        catalogs.listarLocais(new com.aula.fixa.adapter.cadastro.CadastroViewModel.Callback<List<com.aula.fixa.domain.model.OpcaoCadastro>>(){public void aoConcluir(List<com.aula.fixa.domain.model.OpcaoCadastro> values){if(!host.isAdded())return;localOptions=values==null?new ArrayList<>():values;List<String> names=new ArrayList<>();for(com.aula.fixa.domain.model.OpcaoCadastro option:localOptions)names.add(option.getNome());setOptions(local,names);local.setText(args.getString("localName",""),false);}public void aoFalhar(Throwable error){showError(error);}});
        catalogs.listarCategorias(new com.aula.fixa.adapter.cadastro.CadastroViewModel.Callback<List<com.aula.fixa.domain.model.OpcaoCadastro>>(){public void aoConcluir(List<com.aula.fixa.domain.model.OpcaoCadastro> values){if(!host.isAdded())return;equipmentOptions=values==null?new ArrayList<>():values;List<String> names=new ArrayList<>();for(com.aula.fixa.domain.model.OpcaoCadastro option:equipmentOptions)names.add(option.getNome());setOptions(equipmentField,names);equipmentField.setText(args.getString("equipmentName",""),false);}public void aoFalhar(Throwable error){showError(error);}});
        root.findViewById(R.id.occurrenceEditInfoCancel).setOnClickListener(v->activity.back());
        root.findViewById(R.id.occurrenceEditInfoNext).setOnClickListener(v->{String locationName=local.getText().toString().trim(),equipmentName=equipmentField.getText().toString().trim(),categoryName=categoryField.getText().toString().trim(),priorityName=priorityField.getText().toString().trim();if(locationName.isEmpty()||equipmentName.isEmpty()||categoryName.isEmpty()||priorityName.isEmpty()){Toast.makeText(host.requireContext(),R.string.ops_occurrence_select_all_fields,Toast.LENGTH_SHORT).show();return;}Long localId=findId(localOptions,locationName),equipmentId=findId(equipmentOptions,equipmentName);com.aula.fixa.domain.enums.CategoriaProblema categoryValue=category(categoryName);com.aula.fixa.domain.enums.Prioridade priorityValue=priority(priorityName);if(localId==null||equipmentId==null||categoryValue==null||priorityValue==null){Toast.makeText(host.requireContext(),R.string.ops_occurrence_select_all_fields,Toast.LENGTH_SHORT).show();return;}activity.model.field("occurrence_edit","localId",String.valueOf(localId));activity.model.field("occurrence_edit","equipmentId",String.valueOf(equipmentId));activity.model.field("occurrence_edit","category",categoryValue.name());activity.model.field("occurrence_edit","priority",priorityValue.name());activity.openOccurrenceEditPhotos(args.getLong("occurrenceId"),args.getLongArray("photoIds"),args.getStringArrayList("photoUrls"));});
    }

    public void bindEditPhotos(View root){
        content=root;model=new ViewModelProvider(host,new OcorrenciaViewModelFactory(host.requireContext())).get(OcorrenciaViewModel.class);fotoModel=new ViewModelProvider(host,new com.aula.fixa.adapter.foto.FotoViewModelFactory(host.requireContext())).get(com.aula.fixa.adapter.foto.FotoViewModel.class);
        android.os.Bundle args=host.getArguments();if(args==null)return;long[] ids=args.getLongArray("photoIds");String[] urlArray=args.getStringArray("photoUrls");List<String> urls=urlArray==null?java.util.Collections.emptyList():java.util.Arrays.asList(urlArray);editPhotos.clear();deletedPhotoIds.clear();addedPhotoUrls.clear();if(ids!=null)for(int i=0;i<Math.min(ids.length,urls.size());i++)editPhotos.add(new OcorrenciaFoto(ids[i],urls.get(i)));
        RecyclerView photos=root.findViewById(R.id.occurrenceEditPhotosList);photos.setLayoutManager(new LinearLayoutManager(host.requireContext(),RecyclerView.HORIZONTAL,false));photoAdapter=new OcorrenciaEditPhotoAdapter(photo->{editPhotos.remove(photo);if(photo.id>0)deletedPhotoIds.add(photo.id);else addedPhotoUrls.remove(photo.url);photoAdapter.submit(editPhotos);});photos.setAdapter(photoAdapter);photoAdapter.submit(editPhotos);
        View back=root.findViewById(R.id.occurrenceEditPhotosBack);if(back!=null)back.setOnClickListener(v->activity.back());root.findViewById(R.id.occurrenceEditPhotosBackButton).setOnClickListener(v->activity.back());root.findViewById(R.id.occurrenceEditAddPhoto).setOnClickListener(v->host.selectOccurrenceEditPhotos());root.findViewById(R.id.occurrenceEditPhotosSave).setOnClickListener(v->saveOccurrenceEdit(args.getLong("occurrenceId")));
    }

    public void uploadNewPhotos(List<android.net.Uri> uris){if(uris==null||uris.isEmpty()||fotoModel==null)return;pendingPhotoUploads+=uris.size();updatePhotoSaveState();for(android.net.Uri uri:uris)fotoModel.enviarSolicitacao(uri,new com.aula.fixa.adapter.foto.FotoViewModel.Callback(){public void aoSucesso(String url,String publicId){if(!host.isAdded())return;host.requireActivity().runOnUiThread(()->{addedPhotoUrls.add(url);editPhotos.add(new OcorrenciaFoto(0,url));pendingPhotoUploads--;if(photoAdapter!=null)photoAdapter.submit(editPhotos);updatePhotoSaveState();});}public void aoReceberTokenExclusao(String token){}public void aoFalhar(String message){if(!host.isAdded())return;host.requireActivity().runOnUiThread(()->{pendingPhotoUploads--;updatePhotoSaveState();Toast.makeText(host.requireContext(),message,Toast.LENGTH_LONG).show();});}});}

    private void saveOccurrenceEdit(long id){if(pendingPhotoUploads>0){Toast.makeText(host.requireContext(),R.string.ops_occurrence_wait_photos,Toast.LENGTH_SHORT).show();return;}try{Long localId=Long.valueOf(activity.model.field("occurrence_edit","localId")),equipmentId=Long.valueOf(activity.model.field("occurrence_edit","equipmentId"));com.aula.fixa.domain.enums.CategoriaProblema category=com.aula.fixa.domain.enums.CategoriaProblema.valueOf(activity.model.field("occurrence_edit","category"));com.aula.fixa.domain.enums.Prioridade priority=com.aula.fixa.domain.enums.Prioridade.valueOf(activity.model.field("occurrence_edit","priority"));Dialog wait=AppLoading.show(host.requireContext());model.atualizar(id,localId,equipmentId,category,priority,new ArrayList<>(deletedPhotoIds),new ArrayList<>(addedPhotoUrls),new OcorrenciaViewModel.Callback<Void>(){public void success(Void ignored){AppLoading.hide(wait);Toast.makeText(host.requireContext(),R.string.ops_occurrence_edit_completed,Toast.LENGTH_SHORT).show();activity.finishOccurrenceEdit();}public void failure(Throwable error){AppLoading.hide(wait);showError(error);}});}catch(Exception error){Toast.makeText(host.requireContext(),R.string.ops_occurrence_select_all_fields,Toast.LENGTH_SHORT).show();}}
    private android.widget.AutoCompleteTextView editField(View root,int id){return root.findViewById(id).findViewById(R.id.actvFilterValue);}
    private void setOptions(android.widget.AutoCompleteTextView field,List<String> values){field.setAdapter(new android.widget.ArrayAdapter<>(host.requireContext(),R.layout.item_app_dropdown,values));field.setKeyListener(null);field.setOnClickListener(v->field.showDropDown());field.setOnTouchListener((v,event)->{if(event.getAction()==android.view.MotionEvent.ACTION_UP)field.showDropDown();return false;});}
    private Long findId(List<com.aula.fixa.domain.model.OpcaoCadastro> options,String name){if(options==null)return null;for(com.aula.fixa.domain.model.OpcaoCadastro option:options)if(option.getNome().equalsIgnoreCase(name))return option.getId();return null;}
    private com.aula.fixa.domain.enums.CategoriaProblema category(String value){for(com.aula.fixa.domain.enums.CategoriaProblema c:com.aula.fixa.domain.enums.CategoriaProblema.values())if(c.getNome().equalsIgnoreCase(value)||c.name().equalsIgnoreCase(value))return c;return null;}
    private com.aula.fixa.domain.enums.Prioridade priority(String value){for(com.aula.fixa.domain.enums.Prioridade p:com.aula.fixa.domain.enums.Prioridade.values())if(p.getNome().equalsIgnoreCase(value)||p.name().equalsIgnoreCase(value))return p;return null;}
    private void updatePhotoSaveState(){if(content!=null){View save=content.findViewById(R.id.occurrenceEditPhotosSave);if(save!=null)save.setEnabled(pendingPhotoUploads==0);}}

    public void bindDetails(View root){content=root;model=new ViewModelProvider(host,new OcorrenciaViewModelFactory(host.requireContext())).get(OcorrenciaViewModel.class);View back=root.findViewById(R.id.ops_5717_9112);if(back!=null)back.setOnClickListener(v->activity.back());android.widget.TextView heading=root.findViewById(R.id.ops_5717_9111);if(heading!=null)heading.setText(R.string.ops_occurrence_details_title);long id=host.getArguments()==null?0:host.getArguments().getLong("occurrenceId",0);if(id<=0){Toast.makeText(host.requireContext(),R.string.ops_occurrence_load_error,Toast.LENGTH_LONG).show();return;}root.setVisibility(View.INVISIBLE);loading=AppLoading.show(host.requireContext());model.detalhar(id,new OcorrenciaViewModel.Callback<OcorrenciaItem>(){public void success(OcorrenciaItem item){if(!current())return;bindDetailValues(root,item);finish();}public void failure(Throwable error){if(!current())return;finish();showError(error);}});}
    private void bindDetailValues(View root,OcorrenciaItem i){setText(root,R.id.ops_5717_9117,i.titulo);setText(root,R.id.ops_5717_9118,i.descricao);setText(root,R.id.ops_5717_9120,getString(R.string.ops_occurrence_equipment,i.categoriaEquipamento));setText(root,R.id.ops_5717_9119,getString(R.string.ops_occurrence_local,i.localUnidade));setText(root,R.id.ops_5717_9123,i.descricaoLocal);android.widget.ImageView first=root.findViewById(R.id.ops_5717_9124),second=root.findViewById(R.id.ops_5717_9125);loadPhoto(first,i.fotos.size()>0?i.fotos.get(0).url:null);if(i.fotos.size()>1)loadPhoto(second,i.fotos.get(1).url);else if(second!=null)second.setVisibility(View.GONE);}
    private String getString(int id,String value){return host.getString(id,value==null?"":value);}
    private void setText(View root,int id,CharSequence value){android.widget.TextView text=root.findViewById(id);if(text!=null)text.setText(value);}
    private void loadPhoto(android.widget.ImageView target,String url){if(target==null)return;if(url==null||url.trim().isEmpty()){target.setVisibility(View.GONE);return;}target.setVisibility(View.VISIBLE);com.bumptech.glide.Glide.with(host).load(url).centerCrop().placeholder(R.drawable.img_empty_results).error(R.drawable.img_empty_results).into(target);}
    private void showError(Throwable error){Toast.makeText(host.requireContext(),AutenticacaoViewModel.messageFrom(error),Toast.LENGTH_LONG).show();}
    private int dp(int value){return (int)(value*host.getResources().getDisplayMetrics().density+0.5f);}
    private boolean current(){return host.isAdded()&&host.getView() instanceof android.widget.FrameLayout&&((android.widget.FrameLayout)host.getView()).getChildCount()>0&&((android.widget.FrameLayout)host.getView()).getChildAt(0)==content;}
    private void finish(){AppLoading.hide(loading);loading=null;if(content!=null)content.setVisibility(View.VISIBLE);}
    public void clear(){AppLoading.hide(loading);loading=null;adapter=null;list=null;content=null;}
}
