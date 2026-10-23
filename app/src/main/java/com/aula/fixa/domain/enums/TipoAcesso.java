package com.aula.fixa.domain.enums;

import java.util.Locale;

/** Tipos de acesso emitidos pela API no claim `role` do JWT. */
public enum TipoAcesso {
    SOLICITANTE,
    TECNICO,
    GESTOR,
    ADMINISTRADOR;

    public static TipoAcesso fromApi(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Tipo de acesso ausente");
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('É', 'E');
        return TipoAcesso.valueOf(normalized);
    }
}
