package com.aula.fixa.infrastructure.remote.dto;
import java.util.List;
public final class ModeloChecklistRequest {
    public String id; public String nome; public Integer periodicidade; public String primeiraAbertura; public Boolean obrigatorio;
    public List<ChecklistFieldRequest> campos; public List<ModeloChecklistEquipmentRequest> modelos;
}
