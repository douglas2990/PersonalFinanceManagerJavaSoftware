package org.example.domain.repository;


import org.example.infrastructure.database.ApiGastoRepository;
import org.example.infrastructure.database.SqliteGastoAdapter;

public class RepositoryFactory {
    // Mude para 'true' para usar a API, 'false' para SQLite
    private static final boolean USAR_API = true;

    public static GastoRepositoryAPI getRepository() {
        if (USAR_API) {
            return new ApiGastoRepository();
        }
        // Agora retorna o Adaptador, mantendo o seu SqliteGastoRepository intacto!
        return new SqliteGastoAdapter();
    }
}