package com.aula.fixa.infrastructure.remote.dto.jev;

import com.google.gson.annotations.SerializedName;

/** Resposta do endpoint de classificação JEV. */
public final class ClassificarProblemaResponse {
    @SerializedName("problema_classificado") public Boolean problemaClassificado;
    @SerializedName(value = "classificacao", alternate = {"categoria", "categoria_problema", "categoriaProblema", "classificacao_problema", "classification"})
    public String classificacao;
}
