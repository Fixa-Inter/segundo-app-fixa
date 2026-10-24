package com.aula.fixa.application.home;

import com.aula.fixa.domain.model.home.ManagerHomeData;
import com.aula.fixa.domain.repository.home.ManagerHomeRepository;

public final class LoadManagerHome {
    private final ManagerHomeRepository repository;
    public LoadManagerHome(ManagerHomeRepository repository) { this.repository = repository; }
    public ManagerHomeData executar() throws Exception { return repository.carregar(); }
}
