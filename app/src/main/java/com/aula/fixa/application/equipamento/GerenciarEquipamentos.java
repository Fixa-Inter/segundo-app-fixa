package com.aula.fixa.application.equipamento;

import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.domain.model.equipamento.EquipamentoItem;
import com.aula.fixa.domain.repository.equipamento.EquipamentoRepository;
import java.util.List;

public final class GerenciarEquipamentos {
    private final EquipamentoRepository repository;
    public GerenciarEquipamentos(EquipamentoRepository repository) { this.repository = repository; }
    public List<EquipamentoItem> listar(long modeloId) throws Exception {
        if (modeloId <= 0) throw new IllegalArgumentException("Modelo de equipamento inválido.");
        return repository.listar(modeloId);
    }
    public List<OpcaoCadastro> locais() throws Exception { return repository.locais(); }
    public EquipamentoItem criar(long modeloId, long localId, String codigo) throws Exception {
        validar(modeloId, localId, codigo);
        return repository.criar(modeloId, localId, codigo.trim());
    }
    public EquipamentoItem atualizar(long id, long localId, String codigo) throws Exception {
        if (id <= 0) throw new IllegalArgumentException("Equipamento inválido.");
        validarCampos(localId, codigo);
        return repository.atualizar(id, localId, codigo.trim());
    }
    public void excluir(long id) throws Exception {
        if (id <= 0) throw new IllegalArgumentException("Equipamento inválido.");
        repository.excluir(id);
    }
    private void validar(long modeloId, long localId, String codigo) {
        if (modeloId <= 0) throw new IllegalArgumentException("Modelo de equipamento inválido.");
        validarCampos(localId, codigo);
    }
    private void validarCampos(long localId, String codigo) {
        if (localId <= 0) throw new IllegalArgumentException("Selecione o local da unidade.");
        if (codigo == null || codigo.trim().isEmpty()) throw new IllegalArgumentException("Informe o identificador do equipamento.");
    }
}
