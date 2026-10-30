package com.aula.fixa.domain.repository;

import com.aula.fixa.domain.model.OpcaoCadastro;
import java.util.List;

public interface CadastroRepository {
    List<OpcaoCadastro> listarLocais() throws Exception;
    List<OpcaoCadastro> listarCategorias() throws Exception;
}
