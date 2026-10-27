package com.aula.fixa.application.tecnico;

import com.aula.fixa.domain.model.tecnico.TecnicoDisponivel;
import com.aula.fixa.domain.repository.tecnico.TecnicoRepository;
import java.util.List;

public final class ListarTecnicosRecomendados {
    private final TecnicoRepository repository;
    public ListarTecnicosRecomendados(TecnicoRepository repository) { this.repository = repository; }
    public List<TecnicoDisponivel> executar(String categoriaProblema) throws Exception {
        return repository.recomendar(categoriaProblema);
    }
    public List<TecnicoDisponivel> listar() throws Exception { return repository.listar(); }
}
