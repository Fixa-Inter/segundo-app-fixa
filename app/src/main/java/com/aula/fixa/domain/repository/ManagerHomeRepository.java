package com.aula.fixa.domain.repository;

import com.aula.fixa.domain.model.ManagerHomeData;

public interface ManagerHomeRepository {
    ManagerHomeData carregar() throws Exception;
}
