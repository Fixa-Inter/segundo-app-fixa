package com.aula.fixa.domain.enums;

import java.util.Locale;

public enum TipoCampoChecklist {
    TEXTO("Texto"), NUMERO("Número"), BOOLEANO("Sim ou Não"), RADIO("Uma Opção"), CHECK("Várias Opções"), AVALIACAO("Avaliação 1-5");
    public final String nome;
    TipoCampoChecklist(String nome) { this.nome = nome; }
    public static TipoCampoChecklist fromApi(String value) {
        if (value == null) throw new IllegalArgumentException("Tipo de campo ausente.");
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (TipoCampoChecklist type : values()) if (type.name().equals(normalized) || type.nome.equalsIgnoreCase(value.trim())) return type;
        if (normalized.equals("NÚMERO")) return NUMERO;
        return valueOf(normalized);
    }
}
