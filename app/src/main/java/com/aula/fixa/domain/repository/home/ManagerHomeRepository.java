package com.aula.fixa.domain.repository.home;

import com.aula.fixa.domain.model.home.ManagerHomeData;

public interface ManagerHomeRepository {
    ManagerHomeData carregar() throws Exception;
}
