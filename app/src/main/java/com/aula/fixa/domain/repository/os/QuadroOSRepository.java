package com.aula.fixa.domain.repository.os;

import com.aula.fixa.domain.model.os.OrdemServicoQuadro;
import java.util.List;
import com.aula.fixa.domain.model.FiltroBusca;
public interface QuadroOSRepository {
    List<OrdemServicoQuadro> listar() throws Exception;
    List<OrdemServicoQuadro> listar(FiltroBusca filtros) throws Exception;
}
