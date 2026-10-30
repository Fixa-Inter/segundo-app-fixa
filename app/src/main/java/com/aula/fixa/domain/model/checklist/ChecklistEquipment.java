package com.aula.fixa.domain.model.checklist;

public final class ChecklistEquipment {
    public long id;
    public String codigo;
    public String modeloNome;
    public String prazoInspecao;
    public Boolean status;
    public ChecklistEquipment() { }
    public ChecklistEquipment(long id, String codigo, String prazoInspecao, Boolean status) {
        this.id = id; this.codigo = codigo == null ? "" : codigo; this.prazoInspecao = prazoInspecao; this.status = status;
    }
}
