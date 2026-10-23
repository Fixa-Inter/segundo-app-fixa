package com.aula.fixa.application.operations;

import com.aula.fixa.domain.model.ManagerHomeData;
import com.aula.fixa.domain.repository.ManagerHomeRepository;

public final class LoadManagerHome {
    private final ManagerHomeRepository repository;
    public LoadManagerHome(ManagerHomeRepository repository) { this.repository = repository; }
    public ManagerHomeData executar() throws Exception { return repository.carregar(); }
}
