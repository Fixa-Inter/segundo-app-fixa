package com.aula.fixa.infrastructure.remote.dto;

/** Contrato de resposta compatível com UsuarioOutputDTO da API. */
public final class PerfilResponse {
    public Long id;
    public String nomeCompleto;
    public String email;
    public String cargo;
    public String tipoAcesso;
    public String urlFotoPerfil;
    public String dataNascimento;
    public String cnpjUnidade;
}
