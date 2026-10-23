package com.aula.fixa.infrastructure.remote.dto;

import java.util.List;

/** Contrato de resposta de ProblemaOutputDTO da API. */
public final class ProblemaResponse {
    public Long id;
    public String titulo;
    public String descricaoProblema;
    public String categoriaEquipamento;
    public String localUnidade;
    public String descricaoLocal;
    public String status;
    public String usuario;
    public String tipoAcesso;
    public String dataCriacao;
    public List<FotoResponse> fotos;
}
