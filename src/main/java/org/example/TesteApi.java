package org.example;

import org.example.domain.entity.Categoria;
import org.example.domain.entity.Gasto;
import org.example.domain.entity.MetodoPagamento;
import org.example.infrastructure.database.ApiGastoRepository;

import java.time.LocalDate;
import java.util.List;

public class TesteApi {
    public static void main(String[] args) {
        ApiGastoRepository repository = new ApiGastoRepository();

        // 1. Criar um objeto de teste
        Gasto novoGasto = new Gasto(
                0, // ID 0, a API deve gerar o ID no banco
                "Compra de Teste Java",
                99.99,
                LocalDate.now(),
                new Categoria(0, "Alimentação"),
                new MetodoPagamento("Cartão", 5),
                false,
                1,
                1
        );

        // 2. Salvar na API
        System.out.println("Tentando salvar o gasto...");
        repository.salvar(novoGasto);

        // 3. Buscar novamente para ver se veio no JSON
        System.out.println("Buscando lista atualizada...");
        List<Gasto> lista = repository.buscarTodos();

        if (lista.isEmpty()) {
            System.out.println("A lista ainda está vazia!");
        } else {
            System.out.println("Sucesso! Lista contem " + lista.size() + " itens:");
            for (Gasto g : lista) {
                System.out.println("- " + g.getDescricao() + ": R$ " + g.getValor());
            }
        }
    }
}