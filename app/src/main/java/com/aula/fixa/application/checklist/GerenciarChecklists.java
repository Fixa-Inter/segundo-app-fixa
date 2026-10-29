package com.aula.fixa.application.checklist;

import com.aula.fixa.domain.enums.TipoCampoChecklist;
import com.aula.fixa.domain.model.checklist.ChecklistEquipment;
import com.aula.fixa.domain.model.checklist.ChecklistEquipmentModel;
import com.aula.fixa.domain.model.checklist.ChecklistField;
import com.aula.fixa.domain.model.checklist.ChecklistOption;
import com.aula.fixa.domain.model.checklist.ChecklistTemplate;
import com.aula.fixa.domain.model.checklist.EquipmentModelOption;
import com.aula.fixa.domain.repository.checklist.ChecklistRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class GerenciarChecklists {
    private final ChecklistRepository repository;
    public GerenciarChecklists(ChecklistRepository repository) { this.repository = repository; }
    public List<ChecklistTemplate> listar() throws Exception { return repository.listar(); }
    public List<EquipmentModelOption> modelosDisponiveis(List<ChecklistTemplate> existentes, String editadoId) throws Exception {
        return repository.modelosDisponiveis(existentes, editadoId);
    }
    public List<ChecklistEquipmentModel> modelosDoChecklist(String checklistId) throws Exception { return repository.modelosDoChecklist(checklistId); }
    public List<ChecklistEquipment> equipamentosDoModelo(String checklistId, long modeloEquipamentoId) throws Exception {
        if (checklistId == null || checklistId.trim().isEmpty() || modeloEquipamentoId <= 0) throw new IllegalArgumentException("Selecione um modelo de equipamento.");
        return repository.equipamentosDoModelo(checklistId, modeloEquipamentoId);
    }
    public ChecklistTemplate cadastrar(ChecklistTemplate value) throws Exception { validarTemplate(value, false); return repository.cadastrar(value); }
    public ChecklistTemplate atualizar(ChecklistTemplate value) throws Exception { validarTemplate(value, true); return repository.atualizar(value); }
    public void excluir(String id) throws Exception {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("Checklist inválido.");
        repository.excluir(id);
    }
    public void inspecionar(String checklistId, long equipamentoId, ChecklistTemplate template, Map<String,Object> answers) throws Exception {
        if (checklistId == null || checklistId.trim().isEmpty() || equipamentoId <= 0) throw new IllegalArgumentException("Inspeção inválida.");
        if (answers == null) throw new IllegalArgumentException("Preencha os campos do checklist.");
        List<ChecklistField> fields = template == null || template.campos == null ? List.of() : template.campos;
        for (ChecklistField field : fields) {
            Object answer = answers.get(field.id);
            if (Boolean.TRUE.equals(field.obrigatorio) && !filled(field.tipoCampo, answer))
                throw new IllegalArgumentException("Preencha o campo obrigatório: " + field.nome + ".");
            if (answer != null && !compatible(field, answer)) throw new IllegalArgumentException("Revise a resposta do campo: " + field.nome + ".");
        }
        repository.cadastrarInspecao(checklistId, equipamentoId, answers);
    }
    private void validarTemplate(ChecklistTemplate value, boolean editing) {
        if (value == null) throw new IllegalArgumentException("Checklist inválido.");
        if (editing && (value.id == null || value.id.trim().isEmpty())) throw new IllegalArgumentException("Checklist inválido.");
        if (value.nome == null || value.nome.trim().isEmpty()) throw new IllegalArgumentException("Informe o nome do checklist.");
        if (value.periodicidade == null || value.periodicidade <= 0) throw new IllegalArgumentException("Informe uma periodicidade maior que zero.");
        if (value.primeiraAbertura == null || value.primeiraAbertura.trim().isEmpty()) throw new IllegalArgumentException("Informe a data da primeira abertura.");
        try { if (LocalDate.parse(value.primeiraAbertura).isBefore(LocalDate.now())) throw new IllegalArgumentException("A data da primeira abertura não pode estar no passado."); }
        catch (java.time.format.DateTimeParseException invalid) { throw new IllegalArgumentException("Informe uma data válida para a primeira abertura."); }
        if (value.campos == null || value.campos.isEmpty()) throw new IllegalArgumentException("Adicione ao menos um campo de inspeção.");
        for (ChecklistField field : value.campos) {
            if (field.nome == null || field.nome.trim().isEmpty() || field.tipoCampo == null) throw new IllegalArgumentException("Revise o nome e o tipo dos campos.");
            if ((field.tipoCampo == TipoCampoChecklist.RADIO || field.tipoCampo == TipoCampoChecklist.CHECK)
                    && (field.opcoes == null || field.opcoes.isEmpty())) throw new IllegalArgumentException("Adicione ao menos uma opção ao campo “" + field.nome + "”.");
            if (field.opcoes != null) for (ChecklistOption option : field.opcoes) if (option.nome == null || option.nome.trim().isEmpty()) throw new IllegalArgumentException("Preencha todas as opções dos campos.");
        }
    }
    private boolean filled(TipoCampoChecklist type, Object answer) {
        if (answer == null) return false;
        if (type == TipoCampoChecklist.TEXTO) return answer instanceof String && !((String) answer).trim().isEmpty();
        if (type == TipoCampoChecklist.NUMERO) return answer instanceof Number || (answer instanceof String && !((String) answer).trim().isEmpty());
        if (type == TipoCampoChecklist.RADIO || type == TipoCampoChecklist.CHECK) {
            if (!(answer instanceof Map)) return false;
            for (Object selected : ((Map<?, ?>) answer).values()) if (Boolean.TRUE.equals(selected)) return true;
            return false;
        }
        return true;
    }
    private boolean compatible(ChecklistField field, Object answer) {
        switch (field.tipoCampo) {
            case TEXTO: return answer instanceof String;
            case NUMERO:
                if (answer instanceof Number) return true;
                try { Double.parseDouble(String.valueOf(answer)); return true; } catch (NumberFormatException ignored) { return false; }
            case BOOLEANO: return answer instanceof Boolean;
            case AVALIACAO:
                if (!(answer instanceof Number)) return false;
                int rating = ((Number) answer).intValue(); return rating >= 1 && rating <= 5;
            case RADIO:
            case CHECK:
                if (!(answer instanceof Map)) return false;
                List<String> validIds = new ArrayList<>();
                if (field.opcoes != null) for (ChecklistOption option : field.opcoes) validIds.add(option.id);
                int selected = 0;
                for (Map.Entry<?,?> item : ((Map<?,?>) answer).entrySet()) {
                    if (!(item.getKey() instanceof String) || !validIds.contains(item.getKey()) || !(item.getValue() instanceof Boolean)) return false;
                    if (Boolean.TRUE.equals(item.getValue())) selected++;
                }
                return field.tipoCampo != TipoCampoChecklist.RADIO || selected <= 1;
            default: return false;
        }
    }
}
