package com.aula.fixa.application.equipamento;

import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.domain.repository.equipamento.CategoriaEquipamentoRepository;
import java.util.List;

public final class GerenciarCategoriasEquipamento {
    private final CategoriaEquipamentoRepository repository;
    public GerenciarCategoriasEquipamento(CategoriaEquipamentoRepository repository) { this.repository = repository; }
    public List<OpcaoCadastro> listar() throws Exception { return repository.listar(); }
    public OpcaoCadastro criar(String nome, String descricao) throws Exception {
        requireText(nome, "Informe o nome da categoria.");
        requireText(descricao, "Informe a descrição da categoria.");
        return repository.criar(nome.trim(), descricao.trim());
    }
    public OpcaoCadastro atualizar(long id, String nome) throws Exception {
        if (id <= 0) throw new IllegalArgumentException("Categoria inválida.");
        requireText(nome, "Informe o nome da categoria.");
        return repository.atualizar(id, nome.trim());
    }
    public void excluir(long id) throws Exception {
        if (id <= 0) throw new IllegalArgumentException("Categoria inválida.");
        repository.excluir(id);
    }
    private void requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException(message);
    }
}
