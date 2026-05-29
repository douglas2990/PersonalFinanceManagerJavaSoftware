package org.example.infrastructure.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.domain.entity.*;
import org.example.domain.repository.GastoRepository;

import javax.net.ssl.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class ApiGastoRepository implements GastoRepository {

    private final String baseUrl = "https://localhost:7060/api/Gastos";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public ApiGastoRepository() {
        this.httpClient = createInsecureClient();
    }

    @Override
    public List<Gasto> buscarTodos() {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrl)).GET().build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) return new ArrayList<>();

            List<GastoResponseDto> listaDtos = objectMapper.readValue(response.body(), new TypeReference<>(){});
            List<Gasto> listaGastos = new ArrayList<>();

            for (GastoResponseDto dto : listaDtos) {
                listaGastos.add(converterParaGasto(dto));
            }
            return listaGastos;

        } catch (Exception e) {
            System.err.println("Erro crítico ao processar lista de gastos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private Gasto converterParaGasto(GastoResponseDto dto) {
        Categoria cat = new Categoria(dto.getCategoria());
        MetodoPagamento met = new MetodoPagamento(dto.getMetodo(), 0);

        return new Gasto(
                dto.getId(),
                dto.getDescricao(),
                dto.getValor(),
                dto.getData().toLocalDate(),
                cat,
                met,
                false,
                dto.getTotalParcelas(),
                dto.getParcelaAtual()
        );
    }

    @Override
    public void salvar(Gasto gasto) {
        try {
            // Nota: Mantive sua lógica de busca de IDs
            int metId = obterIdDoMetodo(gasto.getMetodo());
            int catId = obterIdDaCategoria(gasto.getCategoria());

            // Usando a classe DTO original de envio (que você já tinha)
            GastoDto dto = new GastoDto(
                    gasto.getId(), gasto.getDescricao(), gasto.getValor(), gasto.getData(),
                    catId, metId, gasto.isMensal(), gasto.getTotalParcelas(), gasto.getParcelaAtual()
            );

            String json = objectMapper.writeValueAsString(dto);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status da resposta (Salvamento): " + response.statusCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Gasto> buscarPorMesEAno(int mes, int ano) { return new ArrayList<>(); }

    // Métodos auxiliares de ID mantidos conforme seu projeto
    private int obterIdDoMetodo(MetodoPagamento metodo) { return 1; }
    private int obterIdDaCategoria(Categoria categoria) { return 1; }

    private HttpClient createInsecureClient() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            return HttpClient.newBuilder().sslContext(sc).build();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao configurar cliente HTTP", e);
        }
    }
}