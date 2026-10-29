package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.domain.enums.TipoCampoChecklist;
import com.aula.fixa.domain.model.checklist.ChecklistEquipment;
import com.aula.fixa.domain.model.checklist.ChecklistEquipmentModel;
import com.aula.fixa.domain.model.checklist.ChecklistField;
import com.aula.fixa.domain.model.checklist.ChecklistOption;
import com.aula.fixa.domain.model.checklist.ChecklistTemplate;
import com.aula.fixa.domain.model.checklist.EquipmentModelOption;
import com.aula.fixa.domain.repository.checklist.ChecklistRepository;
import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.ApiException;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.api.EquipamentoApi;
import com.aula.fixa.infrastructure.remote.api.InspecaoChecklistApi;
import com.aula.fixa.infrastructure.remote.api.ModeloChecklistApi;
import com.aula.fixa.infrastructure.remote.api.ModeloEquipamentoApi;
import com.aula.fixa.infrastructure.remote.dto.ChecklistFieldRequest;
import com.aula.fixa.infrastructure.remote.dto.ChecklistFieldResponse;
import com.aula.fixa.infrastructure.remote.dto.ChecklistOptionRequest;
import com.aula.fixa.infrastructure.remote.dto.ChecklistOptionResponse;
import com.aula.fixa.infrastructure.remote.dto.EquipamentoChecklistRequest;
import com.aula.fixa.infrastructure.remote.dto.EquipamentoChecklistResponse;
import com.aula.fixa.infrastructure.remote.dto.EquipamentoResponse;
import com.aula.fixa.infrastructure.remote.dto.ModeloChecklistEquipmentRequest;
import com.aula.fixa.infrastructure.remote.dto.ModeloChecklistRequest;
import com.aula.fixa.infrastructure.remote.dto.ModeloChecklistResponse;
import com.aula.fixa.infrastructure.remote.dto.ModeloEquipamentoChecklistResponse;
import com.aula.fixa.infrastructure.remote.dto.ModeloEquipamentoResponse;
import com.aula.fixa.infrastructure.remote.dto.InspecaoChecklistRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import retrofit2.Response;

public final class ChecklistRepositoryImpl implements ChecklistRepository {
    private final ModeloChecklistApi checklists;
    private final InspecaoChecklistApi inspecoes;
    private final ModeloEquipamentoApi modelosEquipamento;
    private final EquipamentoApi equipamentos;

    public ChecklistRepositoryImpl(SessionStorage session) {
        checklists = ApiClient.modelosChecklist(session);
        inspecoes = ApiClient.inspecoesChecklist(session);
        modelosEquipamento = ApiClient.modelosEquipamento(session);
        equipamentos = ApiClient.equipamentos(session);
    }

    @Override public List<ChecklistTemplate> listar() throws Exception {
        Response<List<ModeloChecklistResponse>> response = checklists.listar().execute();
        if (response.code() == 404) return new ArrayList<>();
        if (!response.isSuccessful()) throw apiError(response);
        List<ChecklistTemplate> result = new ArrayList<>();
        if (response.body() != null) for (ModeloChecklistResponse item : response.body()) if (item != null) result.add(map(item));
        return result;
    }

    @Override public List<EquipmentModelOption> modelosDisponiveis(List<ChecklistTemplate> existentes, String checklistEditadoId) throws Exception {
        Set<Long> usados = new HashSet<>();
        if (existentes != null) for (ChecklistTemplate template : existentes) {
            if (template == null || (checklistEditadoId != null && checklistEditadoId.equals(template.id)) || template.modelos == null) continue;
            for (ChecklistEquipmentModel model : template.modelos) if (model != null) usados.add(model.modeloEquipamentoId);
        }
        Response<List<ModeloEquipamentoResponse>> response = modelosEquipamento.listar().execute();
        if (response.code() == 404) return new ArrayList<>();
        if (!response.isSuccessful()) throw apiError(response);
        List<EquipmentModelOption> result = new ArrayList<>();
        if (response.body() == null) return result;
        for (ModeloEquipamentoResponse model : response.body()) {
            if (model == null || model.id == null || usados.contains(model.id)) continue;
            Response<List<EquipamentoResponse>> equipmentResponse = equipamentos.listar(model.id).execute();
            if (equipmentResponse.code() == 404) continue;
            if (!equipmentResponse.isSuccessful()) throw apiError(equipmentResponse);
            List<ChecklistEquipment> attached = new ArrayList<>();
            if (equipmentResponse.body() != null) for (EquipamentoResponse equipment : equipmentResponse.body()) {
                if (equipment != null && equipment.id != null) attached.add(new ChecklistEquipment(equipment.id, equipment.codigo, null, false));
            }
            if (!attached.isEmpty()) result.add(new EquipmentModelOption(model.id, model.nome, attached));
        }
        return result;
    }

    @Override public List<ChecklistEquipmentModel> modelosDoChecklist(String checklistId) throws Exception {
        Response<List<ModeloEquipamentoChecklistResponse>> response = inspecoes.listarModelos(checklistId).execute();
        if (response.code() == 404) return new ArrayList<>();
        if (!response.isSuccessful()) throw apiError(response);
        List<ChecklistEquipmentModel> result = new ArrayList<>();
        if (response.body() != null) for (ModeloEquipamentoChecklistResponse item : response.body()) if (item != null) result.add(map(item));
        return result;
    }

    @Override public List<ChecklistEquipment> equipamentosDoModelo(String checklistId, long modeloEquipamentoId) throws Exception {
        Response<List<EquipamentoChecklistResponse>> response = inspecoes.listarEquipamentos(checklistId, modeloEquipamentoId).execute();
        if (response.code() == 404) return new ArrayList<>();
        if (!response.isSuccessful()) throw apiError(response);
        List<ChecklistEquipment> result = new ArrayList<>();
        if (response.body() != null) for (EquipamentoChecklistResponse item : response.body()) if (item != null && item.equipamentoId != null)
            result.add(new ChecklistEquipment(item.equipamentoId, item.codigo, item.prazoInspecao, item.status));
        return result;
    }

    @Override public ChecklistTemplate cadastrar(ChecklistTemplate checklist) throws Exception {
        return one(checklists.cadastrar(request(checklist, false)).execute());
    }

    @Override public ChecklistTemplate atualizar(ChecklistTemplate checklist) throws Exception {
        return one(checklists.atualizar(request(checklist, true)).execute());
    }

    @Override public void excluir(String checklistId) throws Exception {
        Response<ModeloChecklistResponse> response = checklists.excluir(checklistId).execute();
        if (!response.isSuccessful()) throw apiError(response);
    }

    @Override public void cadastrarInspecao(String checklistId, long equipamentoId, Map<String, Object> dados) throws Exception {
        InspecaoChecklistRequest request = new InspecaoChecklistRequest();
        request.modeloChecklistId = checklistId;
        request.equipamentoId = equipamentoId;
        request.dados = dados;
        Response<?> response = inspecoes.cadastrar(request).execute();
        if (!response.isSuccessful()) throw apiError(response);
    }

    private ModeloChecklistRequest request(ChecklistTemplate source, boolean includeId) {
        ModeloChecklistRequest request = new ModeloChecklistRequest();
        request.id = includeId ? source.id : null;
        request.nome = source.nome;
        request.periodicidade = source.periodicidade;
        request.primeiraAbertura = source.primeiraAbertura;
        request.obrigatorio = source.obrigatorio == null ? Boolean.FALSE : source.obrigatorio;
        request.campos = new ArrayList<>();
        if (source.campos != null) for (ChecklistField field : source.campos) {
            ChecklistFieldRequest dto = new ChecklistFieldRequest();
            dto.id = includeId ? field.id : null;
            dto.nome = field.nome;
            dto.tipoCampo = field.tipoCampo.name();
            dto.obrigatorio = field.obrigatorio == null ? Boolean.FALSE : field.obrigatorio;
            dto.opcoes = new ArrayList<>();
            if (field.opcoes != null) for (ChecklistOption option : field.opcoes) {
                ChecklistOptionRequest optionDto = new ChecklistOptionRequest();
                optionDto.id = includeId ? option.id : null;
                optionDto.nome = option.nome;
                optionDto.obrigatorio = option.obrigatorio == null ? Boolean.FALSE : option.obrigatorio;
                dto.opcoes.add(optionDto);
            }
            request.campos.add(dto);
        }
        request.modelos = new ArrayList<>();
        if (source.modelos != null) for (ChecklistEquipmentModel model : source.modelos) {
            ModeloChecklistEquipmentRequest modelDto = new ModeloChecklistEquipmentRequest();
            modelDto.modeloEquipamentoId = model.modeloEquipamentoId;
            modelDto.equipamentos = new ArrayList<>();
            if (model.equipamentos != null) for (ChecklistEquipment equipment : model.equipamentos) modelDto.equipamentos.add(new EquipamentoChecklistRequest(equipment.id));
            request.modelos.add(modelDto);
        }
        return request;
    }

    private ChecklistTemplate one(Response<ModeloChecklistResponse> response) throws Exception {
        if (!response.isSuccessful()) throw apiError(response);
        if (response.body() == null) throw new java.io.IOException("A API retornou uma resposta vazia.");
        return map(response.body());
    }
    private ChecklistTemplate map(ModeloChecklistResponse item) {
        List<ChecklistField> fields = new ArrayList<>();
        if (item.campos != null) for (ChecklistFieldResponse field : item.campos) if (field != null) {
            List<ChecklistOption> options = new ArrayList<>();
            if (field.opcoes != null) for (ChecklistOptionResponse option : field.opcoes) if (option != null) options.add(new ChecklistOption(option.id, option.nome, option.obrigatorio));
            fields.add(new ChecklistField(field.id, field.nome, TipoCampoChecklist.fromApi(field.tipoCampo), field.obrigatorio, options));
        }
        List<ChecklistEquipmentModel> models = new ArrayList<>();
        if (item.modelos != null) for (ModeloEquipamentoChecklistResponse model : item.modelos) if (model != null) models.add(map(model));
        return new ChecklistTemplate(item.id, item.nome, item.periodicidade, item.primeiraAbertura, item.obrigatorio, fields, models);
    }
    private ChecklistEquipmentModel map(ModeloEquipamentoChecklistResponse item) {
        List<ChecklistEquipment> equipment = new ArrayList<>();
        if (item.equipamentos != null) for (EquipamentoChecklistResponse value : item.equipamentos) if (value != null && value.equipamentoId != null)
            equipment.add(new ChecklistEquipment(value.equipamentoId, value.codigo, value.prazoInspecao, value.status));
        return new ChecklistEquipmentModel(item.modeloEquipamentoId == null ? 0 : item.modeloEquipamentoId, item.nome, equipment);
    }
    private ApiException apiError(Response<?> response) throws Exception {
        return new ApiException(response.code(), response.errorBody() == null ? "" : response.errorBody().string());
    }
}
