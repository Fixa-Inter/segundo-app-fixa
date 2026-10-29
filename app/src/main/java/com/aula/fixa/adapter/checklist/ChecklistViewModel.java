package com.aula.fixa.adapter.checklist;

import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.ViewModel;
import com.aula.fixa.application.checklist.GerenciarChecklists;
import com.aula.fixa.domain.model.checklist.ChecklistEquipment;
import com.aula.fixa.domain.model.checklist.ChecklistEquipmentModel;
import com.aula.fixa.domain.model.checklist.ChecklistTemplate;
import com.aula.fixa.domain.model.checklist.EquipmentModelOption;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ChecklistViewModel extends ViewModel {
    public interface Callback<T> { void success(T value); void failure(Throwable error); }
    private final GerenciarChecklists useCase;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    public ChecklistViewModel(GerenciarChecklists useCase) { this.useCase = useCase; }
    public void listar(Callback<List<ChecklistTemplate>> cb) { run(useCase::listar, cb); }
    public void modelosDisponiveis(List<ChecklistTemplate> current, String editId, Callback<List<EquipmentModelOption>> cb) { run(() -> useCase.modelosDisponiveis(current, editId), cb); }
    public void modelos(String checklistId, Callback<List<ChecklistEquipmentModel>> cb) { run(() -> useCase.modelosDoChecklist(checklistId), cb); }
    public void equipamentos(String checklistId, long modelId, Callback<List<ChecklistEquipment>> cb) { run(() -> useCase.equipamentosDoModelo(checklistId, modelId), cb); }
    public void cadastrar(ChecklistTemplate value, Callback<ChecklistTemplate> cb) { run(() -> useCase.cadastrar(value), cb); }
    public void atualizar(ChecklistTemplate value, Callback<ChecklistTemplate> cb) { run(() -> useCase.atualizar(value), cb); }
    public void excluir(String id, Callback<Void> cb) { run(() -> { useCase.excluir(id); return null; }, cb); }
    public void inspecionar(String checklistId, long equipmentId, ChecklistTemplate template, Map<String,Object> answers, Callback<Void> cb) {
        run(() -> { useCase.inspecionar(checklistId, equipmentId, template, answers); return null; }, cb);
    }
    private <T> void run(Work<T> work, Callback<T> cb) { io.execute(() -> { try { T value = work.get(); main.post(() -> cb.success(value)); } catch (Throwable error) { main.post(() -> cb.failure(error)); } }); }
    @Override protected void onCleared() { io.shutdownNow(); }
    private interface Work<T> { T get() throws Exception; }
}
