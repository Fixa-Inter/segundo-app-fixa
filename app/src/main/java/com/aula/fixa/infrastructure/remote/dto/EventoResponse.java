package com.aula.fixa.infrastructure.remote.dto;

/** Contrato de resposta de EventoOutputDTO da API. Datas ISO-8601 preservadas como texto. */
public final class EventoResponse {
    public Long id;
    public String titulo;
    public String descricao;
    public String localUnidade;
    public String descricaoLocal;
    public String dataHoraInicio;
    public String dataHoraFim;
    public String observacao;
    public String usuario;
    public String dataCriacao;
}
