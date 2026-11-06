package com.aula.fixa.application.planejamento;

import com.aula.fixa.domain.repository.planejamento.RecomendacaoCategoriaRepository;

/** Valida e solicita ao classificador a categoria de problema mais provável. */
public final class RecomendarCategoriaProblema {
    private final RecomendacaoCategoriaRepository repository;

    public RecomendarCategoriaProblema(RecomendacaoCategoriaRepository repository) {
        this.repository = repository;
    }

    public String executar(String titulo, String descricao, String categoriaEquipamento) throws Exception {
        if (vazio(titulo) || vazio(descricao) || vazio(categoriaEquipamento)) {
            throw new IllegalArgumentException("Dados insuficientes para recomendar a competência.");
        }
        return repository.classificar(titulo.trim(), descricao.trim(), categoriaEquipamento.trim());
    }

    private boolean vazio(String value) { return value == null || value.trim().isEmpty(); }
}
