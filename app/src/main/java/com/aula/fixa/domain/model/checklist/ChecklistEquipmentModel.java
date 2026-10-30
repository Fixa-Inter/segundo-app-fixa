package com.aula.fixa.domain.model.checklist;

import java.util.ArrayList;
import java.util.List;

public final class ChecklistEquipmentModel {
    public long modeloEquipamentoId;
    public String nome;
    public String categoria;
    public List<ChecklistEquipment> equipamentos = new ArrayList<>();
    public ChecklistEquipmentModel() { }
    public ChecklistEquipmentModel(long id, String nome, List<ChecklistEquipment> equipamentos) {
        this.modeloEquipamentoId = id; this.nome = nome == null ? "" : nome;
        this.equipamentos = equipamentos == null ? new ArrayList<>() : new ArrayList<>(equipamentos);
    }

    public ChecklistEquipmentModel(long id, String nome, String categoria, List<ChecklistEquipment> equipamentos) {
        this(id, nome, equipamentos);
        this.categoria = categoria == null ? "" : categoria;
    }
}
