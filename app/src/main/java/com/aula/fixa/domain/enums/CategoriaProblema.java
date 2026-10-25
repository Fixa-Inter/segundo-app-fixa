package com.aula.fixa.domain.enums;

/** Categorias aceitas pela API para classificar uma solicitação/OS. */
public enum CategoriaProblema {
    ELETRICA("Elétrica", 1),
    HIDRAULICA("Hidráulica", 2),
    MECANICA("Mecânica", 3),
    INFORMATICA("Informática", 4),
    MOBILIARIO("Mobiliário", 5),
    CLIMATIZACAO("Climatização", 6),
    REDE_CONECTIVIDADE("Rede e Conectividade", 7),
    SEGURANCA("Segurança", 8),
    ILUMINACAO("Iluminação", 9),
    LIMPEZA_CONSERVACAO("Limpeza e Conservação", 10);

    private final String nome;
    private final int id;

    CategoriaProblema(String nome, int id) { this.nome = nome; this.id = id; }
    public String getNome() { return nome; }
    public int getId() { return id; }
}
