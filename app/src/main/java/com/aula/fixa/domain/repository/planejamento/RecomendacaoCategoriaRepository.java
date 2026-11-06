package com.aula.fixa.domain.repository.planejamento;

/** Contrato para consultar a categoria de problema recomendada pelo classificador JEV. */
public interface RecomendacaoCategoriaRepository {
    String classificar(String titulo, String descricao, String categoriaEquipamento) throws Exception;
}
