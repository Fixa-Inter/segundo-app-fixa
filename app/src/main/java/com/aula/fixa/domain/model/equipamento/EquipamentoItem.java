package com.aula.fixa.domain.model.equipamento;

public final class EquipamentoItem {
    public final long id;
    public final String codigo;
    public final String localUnidade;

    public EquipamentoItem(long id, String codigo, String localUnidade) {
        this.id = id;
        this.codigo = codigo == null ? "" : codigo;
        this.localUnidade = localUnidade == null ? "" : localUnidade;
    }
}
