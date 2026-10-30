package com.aula.fixa.domain.model.checklist;

public final class ChecklistOption {
    public String id;
    public String nome;
    public Boolean obrigatorio;
    public ChecklistOption() { }
    public ChecklistOption(String id, String nome, Boolean obrigatorio) { this.id = id; this.nome = nome; this.obrigatorio = obrigatorio; }
}
