package com.aula.fixa.domain.model.checklist;

import com.aula.fixa.domain.enums.TipoCampoChecklist;
import java.util.ArrayList;
import java.util.List;

public final class ChecklistField {
    public String id;
    public String nome;
    public TipoCampoChecklist tipoCampo;
    public Boolean obrigatorio;
    public List<ChecklistOption> opcoes = new ArrayList<>();
    public ChecklistField() { }
    public ChecklistField(String id, String nome, TipoCampoChecklist tipoCampo, Boolean obrigatorio, List<ChecklistOption> opcoes) {
        this.id = id; this.nome = nome; this.tipoCampo = tipoCampo; this.obrigatorio = obrigatorio;
        this.opcoes = opcoes == null ? new ArrayList<>() : new ArrayList<>(opcoes);
    }
}
