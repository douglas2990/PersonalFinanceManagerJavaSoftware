package org.example.usecase;

import org.example.domain.entity.Gasto;
import org.example.domain.repository.GastoRepositoryAPI;

public class GerenciarGastoApiUseCase {

    private final GastoRepositoryAPI repository;

    // Esta usa a interface, sem tocar na classe do SQLite
    public GerenciarGastoApiUseCase(GastoRepositoryAPI repository) {
        this.repository = repository;
    }

    public void registrarGasto(Gasto gasto) {
        repository.salvar(gasto);
    }
}