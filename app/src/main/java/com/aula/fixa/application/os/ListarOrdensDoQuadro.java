package com.aula.fixa.application.os;

import com.aula.fixa.domain.model.os.OrdemServicoQuadro;
import com.aula.fixa.domain.repository.os.QuadroOSRepository;
import java.util.List;
public final class ListarOrdensDoQuadro {
    private final QuadroOSRepository repository;
    public ListarOrdensDoQuadro(QuadroOSRepository repository){this.repository=repository;}
    public List<OrdemServicoQuadro> executar()throws Exception{return repository.listar();}
}
