package com.aula.fixa.adapter.checklist;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.ViewModel;
import com.aula.fixa.application.checklist.GerenciarChecklists;
import com.aula.fixa.domain.model.checklist.ChecklistEquipmentModel;
import com.aula.fixa.domain.model.checklist.ChecklistEquipment;
import com.aula.fixa.domain.model.checklist.ChecklistTemplate;
import com.aula.fixa.domain.model.checklist.EquipmentModelOption;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ChecklistViewModel extends ViewModel {
    public interface Callback<T> { void success(T value); void failure(Throwable error); }
    private final GerenciarChecklists useCase;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Map<String, List<ChecklistEquipment>> equipmentCache = new HashMap<>();
    public ChecklistViewModel(GerenciarChecklists useCase) { this.useCase = useCase; }
    public void listar(Callback<List<ChecklistTemplate>> cb) { run(useCase::listar, cb); }
    public void modelosDisponiveis(List<ChecklistTemplate> current, String editId, Callback<List<EquipmentModelOption>> cb) { run(() -> useCase.modelosDisponiveis(current, editId), cb); }
    public void modelos(String checklistId, Callback<List<ChecklistEquipment>> cb) {
        synchronized (equipmentCache) {
            List<ChecklistEquipment> cached = equipmentCache.get(checklistId);
            if (cached != null) {
                List<ChecklistEquipment> copy = new ArrayList<>(cached);
                main.post(() -> cb.success(copy));
                return;
            }
        }
        run(() -> {
            List<ChecklistEquipmentModel> models = useCase.modelosDoChecklist(checklistId);
            List<ChecklistEquipment> equipment = new ArrayList<>();
            if (models != null) for (ChecklistEquipmentModel model : models) {
                if (model == null || model.equipamentos == null) continue;
                for (ChecklistEquipment item : model.equipamentos) {
                    if (item == null) continue;
                    item.modeloNome = model.nome;
                    equipment.add(item);
                }
            }
            synchronized (equipmentCache) { equipmentCache.put(checklistId, new ArrayList<>(equipment)); }
            return equipment;
        }, cb);
    }
    /** Inspection and checklist mutations can change equipment status or membership. */
    public void invalidarCacheEquipamentos() { synchronized (equipmentCache) { equipmentCache.clear(); } }
    public void cadastrar(ChecklistTemplate value, Callback<ChecklistTemplate> cb) { invalidarCacheEquipamentos(); run(() -> useCase.cadastrar(value), cb); }
    public void atualizar(ChecklistTemplate value, Callback<ChecklistTemplate> cb) { invalidarCacheEquipamentos(); run(() -> useCase.atualizar(value), cb); }
    public void excluir(String id, Callback<Void> cb) { invalidarCacheEquipamentos(); run(() -> { useCase.excluir(id); return null; }, cb); }
    public void inspecionar(String checklistId, long equipmentId, ChecklistTemplate template, Map<String,Object> answers, Callback<Void> cb) {
        invalidarCacheEquipamentos();
        run(() -> { useCase.inspecionar(checklistId, equipmentId, template, answers); return null; }, cb);
    }
    private <T> void run(Work<T> work, Callback<T> cb) { io.execute(() -> { try { T value = work.get(); main.post(() -> cb.success(value)); } catch (Exception error) { main.post(() -> cb.failure(error)); } }); }
    @Override protected void onCleared() { io.shutdownNow(); }
    private interface Work<T> { T get() throws Exception; }
}
