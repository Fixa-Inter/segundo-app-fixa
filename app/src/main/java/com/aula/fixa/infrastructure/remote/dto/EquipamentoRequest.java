package com.aula.fixa.infrastructure.remote.dto;

public final class EquipamentoRequest {
    public Long id;
    public Long usuarioId;
    public Long modeloEquipamentoId;
    public Long localUnidadeId;
    public String codigo;

    public static EquipamentoRequest criar(long usuarioId, long modeloId, long localId, String codigo) {
        EquipamentoRequest request = new EquipamentoRequest();
        request.usuarioId = usuarioId;
        request.modeloEquipamentoId = modeloId;
        request.localUnidadeId = localId;
        request.codigo = codigo;
        return request;
    }

    public static EquipamentoRequest atualizar(long id, long localId, String codigo) {
        EquipamentoRequest request = new EquipamentoRequest();
        request.id = id;
        request.localUnidadeId = localId;
        request.codigo = codigo;
        return request;
    }
}
