package com.aula.fixa.domain.model.checklist;

import java.util.ArrayList;
import java.util.List;

public final class EquipmentModelOption {
    public long id;
    public String nome;
    public List<ChecklistEquipment> equipamentos = new ArrayList<>();
    public EquipmentModelOption() { }
    public EquipmentModelOption(long id, String nome, List<ChecklistEquipment> equipamentos) {
        this.id = id; this.nome = nome == null ? "" : nome;
        this.equipamentos = equipamentos == null ? new ArrayList<>() : new ArrayList<>(equipamentos);
    }
}
