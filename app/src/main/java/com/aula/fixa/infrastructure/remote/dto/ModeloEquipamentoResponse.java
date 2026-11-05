package com.aula.fixa.infrastructure.remote.dto;

import com.google.gson.annotations.SerializedName;

public final class ModeloEquipamentoResponse {
    public Long id;
    public String nome;
    public String descricao;
    public String marca;
    public String categoria;
    @SerializedName(value = "quantidadeAparelhos", alternate = {"quantidade_aparelhos", "quantidadeEquipamentos"})
    public Long quantidadeAparelhos;
}
