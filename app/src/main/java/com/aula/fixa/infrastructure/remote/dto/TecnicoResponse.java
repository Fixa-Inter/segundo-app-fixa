package com.aula.fixa.infrastructure.remote.dto;

import java.math.BigDecimal;
import java.util.List;

/** Resposta da recomendação de técnicos (UsuarioOutputDTO). */
public final class TecnicoResponse {
    public Long id;
    public String nomeCompleto;
    public String cargo;
    public String tipoAcesso;
    public String urlFotoPerfil;
    public AptidaoResponse competenciaVencedora;
    public Long tarefasEmAndamento;
    public List<TurnoResponse> turnos;

    public static final class TurnoResponse {
        public Long id;
        public String nome;
        public Integer horaInicio;
        public Integer horaFim;
    }

    public static final class AptidaoResponse {
        public Long id;
        public String categoriaProblema;
        public BigDecimal nota;
        public Boolean estaAtivo;
    }
}
