package com.aula.fixa.application.cadastro;

import com.aula.fixa.domain.model.OpcaoCadastro;
import com.aula.fixa.domain.repository.CadastroRepository;
import java.util.List;

public final class ListarLocais {
    private final CadastroRepository repository;
    public ListarLocais(CadastroRepository repository) { this.repository = repository; }
    public List<OpcaoCadastro> executar() throws Exception { return repository.listarLocais(); }
}
