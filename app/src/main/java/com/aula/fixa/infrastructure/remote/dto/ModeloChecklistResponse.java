package com.aula.fixa.infrastructure.remote.dto;
import java.util.List;
public final class ModeloChecklistResponse {
    public String id; public String nome; public Integer periodicidade; public List<ChecklistFieldResponse> campos;
    public Boolean obrigatorio; public String primeiraAbertura; public List<ModeloEquipamentoChecklistResponse> modelos;
}
