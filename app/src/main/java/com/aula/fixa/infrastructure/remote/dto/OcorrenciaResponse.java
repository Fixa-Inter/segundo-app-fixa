package com.aula.fixa.infrastructure.remote.dto;

import java.util.List;

public final class OcorrenciaResponse {
    public Long id, localUnidadeId, categoriaEquipamentoId;
    public String titulo, descricaoOcorrencia, categoriaProblema, prioridade, localUnidade,
            descricaoLocal, dataCriacao, categoriaEquipamento, nomeUsuario;
    public List<FotoResponse> fotos;
}
