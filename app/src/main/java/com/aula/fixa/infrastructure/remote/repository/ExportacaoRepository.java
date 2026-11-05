package com.aula.fixa.infrastructure.remote.repository;

import com.aula.fixa.infrastructure.auth.session.SessionStorage;
import com.aula.fixa.infrastructure.remote.api.ApiClient;
import com.aula.fixa.infrastructure.remote.dto.ChecklistFieldResponse;
import com.aula.fixa.infrastructure.remote.dto.ChecklistOptionResponse;
import com.aula.fixa.infrastructure.remote.dto.EquipamentoChecklistResponse;
import com.aula.fixa.infrastructure.remote.dto.ModeloChecklistResponse;
import com.aula.fixa.infrastructure.remote.dto.ModeloEquipamentoChecklistResponse;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Busca registros completos da API para geração de planilhas. */
public final class ExportacaoRepository {
    public static final String SOLICITACOES = "Solicitações";
    public static final String ORDENS_SERVICO = "Ordens de serviço";
    public static final String OCORRENCIAS = "Ocorrências";
    public static final String CHECKLISTS = "Checklists";

    private final SessionStorage session;

    public ExportacaoRepository(SessionStorage session) {
        this.session = session;
    }

    public List<Map<String, String>> listar(String tipo) throws Exception {
        if (SOLICITACOES.equals(tipo)) {
            Response<List<JsonObject>> response = ApiClient.solicitacoes(session)
                    .exportarUnidade(null, null, "dataCriacao", -1).execute();
            return rows(requireBody(response));
        }
        if (ORDENS_SERVICO.equals(tipo)) {
            Response<List<JsonObject>> response = ApiClient.ordensServico(session)
                    .exportar(null, null, null, null, null, null, null, null,
                            null, "dataCriacao", -1).execute();
            return rows(requireBody(response));
        }
        if (OCORRENCIAS.equals(tipo)) {
            Response<List<JsonObject>> response = ApiClient.ocorrencias(session).exportar().execute();
            return rows(requireBody(response));
        }
        if (CHECKLISTS.equals(tipo)) {
            Response<List<ModeloChecklistResponse>> response = ApiClient.modelosChecklist(session).listar().execute();
            if (response.code() == 404) return new ArrayList<>();
            return checklistRows(requireBody(response));
        }
        throw new IllegalArgumentException("Exportação indisponível para: " + tipo);
    }

    private List<Map<String, String>> checklistRows(List<ModeloChecklistResponse> checklists) {
        List<Map<String, String>> result = new ArrayList<>();
        for (ModeloChecklistResponse checklist : checklists) {
            if (checklist == null) continue;
            Map<String, String> row = new LinkedHashMap<>();
            row.put("id", text(checklist.id));
            row.put("nome", text(checklist.nome));
            row.put("periodicidadeDias", text(checklist.periodicidade));
            row.put("obrigatorio", text(checklist.obrigatorio));
            row.put("primeiraAbertura", text(checklist.primeiraAbertura));
            row.put("campos", checklistFields(checklist.campos));
            row.put("modelosEquipamento", checklistModels(checklist.modelos));
            result.add(row);
        }
        return result;
    }

    private String checklistFields(List<ChecklistFieldResponse> fields) {
        if (fields == null || fields.isEmpty()) return "";
        List<String> values = new ArrayList<>();
        for (ChecklistFieldResponse field : fields) {
            if (field == null) continue;
            List<String> options = new ArrayList<>();
            if (field.opcoes != null) for (ChecklistOptionResponse option : field.opcoes) {
                if (option != null && option.nome != null && !option.nome.trim().isEmpty()) options.add(option.nome);
            }
            String value = "Campo: " + text(field.nome)
                    + "; Tipo: " + text(field.tipoCampo)
                    + "; Obrigatório: " + text(field.obrigatorio);
            if (!options.isEmpty()) value += "; Opções: " + String.join(", ", options);
            values.add(value);
        }
        return String.join("\n", values);
    }

    private String checklistModels(List<ModeloEquipamentoChecklistResponse> models) {
        if (models == null || models.isEmpty()) return "";
        List<String> values = new ArrayList<>();
        for (ModeloEquipamentoChecklistResponse model : models) {
            if (model == null) continue;
            StringBuilder value = new StringBuilder("Modelo: ").append(text(model.nome))
                    .append("; ID modelo: ").append(text(model.modeloEquipamentoId));
            if (model.equipamentos != null) {
                long equipmentCount = model.equipamentos.stream().filter(java.util.Objects::nonNull).count();
                value.append("; Equipamentos: ").append(equipmentCount);
                for (EquipamentoChecklistResponse equipment : model.equipamentos) {
                    if (equipment == null) continue;
                    value.append("\n  Código: ").append(text(equipment.codigo))
                            .append("; ID equipamento: ").append(text(equipment.equipamentoId))
                            .append("; Prazo inspeção: ").append(text(equipment.prazoInspecao))
                            .append("; Status: ").append(equipment.status == null ? ""
                                    : Boolean.TRUE.equals(equipment.status) ? "Realizada" : "Pendente");
                }
            }
            values.add(value.toString());
        }
        return String.join("\n", values);
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private List<Map<String, String>> rows(List<JsonObject> items) {
        List<Map<String, String>> result = new ArrayList<>();
        for (JsonObject item : items) {
            if (item == null) continue;
            Map<String, String> row = new LinkedHashMap<>();
            for (Map.Entry<String, JsonElement> field : item.entrySet()) {
                JsonElement value = field.getValue();
                row.put(field.getKey(), value == null || value.isJsonNull() ? "" :
                        value.isJsonPrimitive() ? value.getAsString() : value.toString());
            }
            result.add(row);
        }
        return result;
    }

    private static <T> T requireBody(Response<T> response) throws Exception {
        if (!response.isSuccessful()) {
            String body = response.errorBody() == null ? "" : response.errorBody().string();
            throw new com.aula.fixa.infrastructure.remote.ApiException(response.code(), body);
        }
        if (response.body() == null) throw new java.io.IOException("A API retornou uma resposta vazia.");
        return response.body();
    }
}
