package com.aula.fixa.domain.repository.os;

import com.aula.fixa.domain.model.os.OrdemServicoQuadro;
import java.util.List;
public interface QuadroOSRepository { List<OrdemServicoQuadro> listar() throws Exception; }
