package org.example.infrastructure.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.domain.entity.*;
import org.example.domain.repository.GastoRepositoryAPI;

import javax.net.ssl.*;
import java.net.URI;
import java.net.http.*;
import java.security.cert.X509Certificate;
import java.util.*;

public class ApiGastoRepository implements GastoRepositoryAPI {

    private final String baseUrl = "https://localhost:7060/api/Gastos";
    private final HttpClient httpClient = createInsecureClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public void salvar(Gasto gasto) {
        try {
            // Prepara os IDs (você pode precisar ajustar a lógica de busca aqui)
            int metId = 1; // Ajuste conforme seu sistema
            int catId = 1; // Ajuste conforme seu sistema

            GastoDto dto = new GastoDto(
                    gasto.getId(), gasto.getDescricao(), gasto.getValor(), gasto.getData(),
                    catId, metId, gasto.isMensal(), gasto.getTotalParcelas(), gasto.getParcelaAtual()
            );

            String json = objectMapper.writeValueAsString(dto);

            // Lógica: Se ID > 0, é PUT (Atualizar). Se ID for 0, é POST (Criar).
            boolean ehAtualizacao = (gasto.getId() > 0);
            String url = ehAtualizacao ? baseUrl + "/" + gasto.getId() : baseUrl;

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json");

            if (ehAtualizacao) builder.PUT(HttpRequest.BodyPublishers.ofString(json));
            else builder.POST(HttpRequest.BodyPublishers.ofString(json));

            httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            System.out.println("Gasto " + (ehAtualizacao ? "atualizado" : "criado") + " com sucesso.");
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void removerGasto(int id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/" + id))
                    .DELETE()
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public List<Gasto> buscarPorMesEAno(int mes, int ano) {
        try {
            // Filtro via URL é fundamental para não trazer dados fantasma
            String url = baseUrl + "?mes=" + mes + "&ano=" + ano;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) return new ArrayList<>();

            List<GastoResponseDto> dtos = objectMapper.readValue(response.body(), new TypeReference<>(){});
            List<Gasto> lista = new ArrayList<>();
            for (GastoResponseDto d : dtos) lista.add(converter(d));
            return lista;
        } catch (Exception e) { return new ArrayList<>(); }
    }

    private Gasto converter(GastoResponseDto dto) {
        return new Gasto(dto.getId(), dto.getDescricao(), dto.getValor(), dto.getData().toLocalDate(),
                new Categoria(dto.getCategoria()), new MetodoPagamento(dto.getMetodo(), 0),
                false, dto.getTotalParcelas(), dto.getParcelaAtual());
    }

    // --- Métodos obrigatórios da interface (pode deixar vazio ou implementar futuramente) ---
    @Override public void atualizarGasto(Gasto g) { salvar(g); }
    @Override public List<Gasto> buscarTodos() { return new ArrayList<>(); }
    @Override public List<Categoria> buscarTodasCategorias() { return new ArrayList<>(); }
    @Override public List<MetodoPagamento> buscarTodosMetodos() { return new ArrayList<>(); }
    @Override public void salvarCategoria(Categoria c) {}
    @Override public void salvarMetodo(MetodoPagamento m) {}
    @Override public double buscarMetaFinal(String c, int m, int a) { return 0; }
    @Override public double buscarSomaGastosPorCategoria(String c, int m, int a) { return 0; }
    @Override public double buscarMetaAnual(String c, int a) { return 0; }
    @Override public double buscarMetaPorCategoria(String c, int m, int a) { return 0; }
    @Override public void salvarOuAtualizarMeta(String c, int m, int a, double v) {}
    @Override public void salvarOuAtualizarMetaAnual(String c, int a, double v) {}

    private HttpClient createInsecureClient() {
        try {
            TrustManager[] tm = new TrustManager[]{ new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                public void checkClientTrusted(X509Certificate[] c, String a) {}
                public void checkServerTrusted(X509Certificate[] c, String a) {}
            }};
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, tm, new java.security.SecureRandom());
            return HttpClient.newBuilder().sslContext(sc).build();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}