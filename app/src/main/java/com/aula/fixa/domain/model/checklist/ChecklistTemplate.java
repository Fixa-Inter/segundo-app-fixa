package com.aula.fixa.domain.model.checklist;

import java.util.ArrayList;
import java.util.List;

public final class ChecklistTemplate {
    public String id;
    public String nome;
    public Integer periodicidade;
    public String primeiraAbertura;
    public Boolean obrigatorio;
    public List<ChecklistField> campos = new ArrayList<>();
    public List<ChecklistEquipmentModel> modelos = new ArrayList<>();
    public ChecklistTemplate() { }
    public ChecklistTemplate(String id, String nome, Integer periodicidade, String primeiraAbertura, Boolean obrigatorio,
                             List<ChecklistField> campos, List<ChecklistEquipmentModel> modelos) {
        this.id = id; this.nome = nome; this.periodicidade = periodicidade; this.primeiraAbertura = primeiraAbertura;
        this.obrigatorio = obrigatorio;
        this.campos = campos == null ? new ArrayList<>() : new ArrayList<>(campos);
        this.modelos = modelos == null ? new ArrayList<>() : new ArrayList<>(modelos);
    }
}
