package com.aula.fixa.application.equipamento;

import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.domain.model.equipamento.ModeloEquipamentoItem;
import com.aula.fixa.domain.repository.equipamento.ModeloEquipamentoRepository;
import java.util.List;

public final class GerenciarModelosEquipamento {
    private final ModeloEquipamentoRepository repository;
    public GerenciarModelosEquipamento(ModeloEquipamentoRepository repository) { this.repository = repository; }
    public List<ModeloEquipamentoItem> listar() throws Exception { return repository.listar(); }
    public List<OpcaoCadastro> categorias() throws Exception { return repository.categorias(); }
    public List<OpcaoCadastro> marcas() throws Exception { return repository.marcas(); }
    public ModeloEquipamentoItem criar(String nome, String descricao, long categoriaId, long marcaId) throws Exception {
        validar(nome, descricao, categoriaId, marcaId);
        return repository.criar(nome.trim(), descricao.trim(), categoriaId, marcaId);
    }
    public ModeloEquipamentoItem atualizar(long id, String nome, String descricao, long categoriaId, long marcaId) throws Exception {
        if (id <= 0) throw new IllegalArgumentException("Modelo inválido.");
        validar(nome, descricao, categoriaId, marcaId);
        return repository.atualizar(id, nome.trim(), descricao.trim(), categoriaId, marcaId);
    }
    public void excluir(long id) throws Exception {
        if (id <= 0) throw new IllegalArgumentException("Modelo inválido.");
        repository.excluir(id);
    }
    private void validar(String nome, String descricao, long categoriaId, long marcaId) {
        if (nome == null || nome.trim().isEmpty()) throw new IllegalArgumentException("Informe o nome do modelo.");
        if (descricao == null || descricao.trim().isEmpty()) throw new IllegalArgumentException("Informe a descrição do modelo.");
        if (categoriaId <= 0 || marcaId <= 0) throw new IllegalArgumentException("Selecione categoria e marca.");
    }
}
