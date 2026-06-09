package org.example.usecase;

import org.example.domain.entity.Gasto;
import org.example.domain.repository.GastoRepository;
import org.example.domain.repository.GastoRepositoryAPI;

import java.time.LocalDate;

public class GerenciarGastoUseCase {

    private final GastoRepository repository;



    public GerenciarGastoUseCase(GastoRepository repository) {
        this.repository = repository;
    }



    public void registrarGasto(Gasto gasto) {
        if (gasto.getTotalParcelas() > 1) {
            gerarParcelas(gasto);
        } else {
            repository.salvar(gasto);
        }
    }

    private void gerarParcelas(Gasto gastoBase) {
        for (int i = 0; i < gastoBase.getTotalParcelas(); i++) {
            // Cria uma nova instância para cada parcela somando os meses
            LocalDate dataParcela = gastoBase.getData().plusMonths(i);
            Gasto parcela = new Gasto(
                    gastoBase.getDescricao() + " (" + (i + 1) + "/" + gastoBase.getTotalParcelas() + ")",
                    gastoBase.getValor() / gastoBase.getTotalParcelas(),
                    dataParcela,
                    gastoBase.getCategoria(),
                    gastoBase.getMetodo(),
                    false, // Parcela não é recorrente mensal
                    gastoBase.getTotalParcelas(),
                    i + 1
            );
            repository.salvar(parcela);
        }
    }
}