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

    private final String baseUrlGastos = "http://192.168.15.31:5123/api/gastos";
    private final String baseUrlCategorias = "http://192.168.15.31:5123/api/categorias";
    private final String baseUrlMetodos = "http://192.168.15.31:5123/api/metodos";

    private final HttpClient httpClient = createInsecureClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public void salvar(Gasto gasto) {
        try {
            // Criamos o JSON dinamicamente para enviar os TEXTOS (strings) que o seu C# espera
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", gasto.getId());
            payload.put("descricao", gasto.getDescricao());
            payload.put("valor", gasto.getValor());
            payload.put("data", gasto.getData().toString() + "T00:00:00Z"); // Formato ISO para o C#
            payload.put("categoria", gasto.getCategoria().getNome()); // Envia o texto puro da categoria
            payload.put("metodo", gasto.getMetodo().getNome());       // Envia o texto puro do método
            payload.put("totalParcelas", gasto.getTotalParcelas());
            payload.put("parcelaAtual", gasto.getParcelaAtual());

            String json = objectMapper.writeValueAsString(payload);

            boolean ehAtualizacao = (gasto.getId() > 0);
            String url = ehAtualizacao ? baseUrlGastos + "/" + gasto.getId() : baseUrlGastos;

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json");

            if (ehAtualizacao) builder.PUT(HttpRequest.BodyPublishers.ofString(json));
            else builder.POST(HttpRequest.BodyPublishers.ofString(json));

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            System.out.println("Gasto salvo! Status HTTP da API: " + response.statusCode());
        } catch (Exception e) {
            System.err.println("Erro ao salvar gasto: " + e.getMessage());
        }
    }

    @Override
    public void removerGasto(int id) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrlGastos + "/" + id))
                    .DELETE()
                    .build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            System.err.println("Erro ao remover gasto: " + e.getMessage());
        }
    }

    @Override
    public List<Gasto> buscarPorMesEAno(int mes, int ano) {
        try {
            String url = baseUrlGastos + "?mes=" + mes + "&ano=" + ano;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) return new ArrayList<>();

            List<GastoResponseDto> dtos = objectMapper.readValue(response.body(), new TypeReference<>(){});
            List<Gasto> lista = new ArrayList<>();
            for (GastoResponseDto d : dtos) lista.add(converter(d));
            return lista;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private Gasto converter(GastoResponseDto dto) {
        // Como o DTO voltou a ser String, pegamos o texto puro vindo do C#
        String nomeCategoria = (dto.getCategoria() != null) ? dto.getCategoria() : "Sem Categoria";
        String nomeMetodo = (dto.getMetodo() != null) ? dto.getMetodo() : "Sem Pagamento";

        return new Gasto(
                dto.getId(),
                dto.getDescricao(),
                dto.getValor(),
                dto.getData().toLocalDate(),
                new Categoria(nomeCategoria),
                new MetodoPagamento(nomeMetodo, 0),
                false,
                dto.getTotalParcelas(),
                dto.getParcelaAtual()
        );
    }

    @Override public void atualizarGasto(Gasto g) { salvar(g); }
    @Override public List<Gasto> buscarTodos() { return new ArrayList<>(); }

    @Override
    public List<Categoria> buscarTodasCategorias() {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrlCategorias)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Lê o JSON da API usando a sua classe CategoriaApi
            List<CategoriaApi> dtos = objectMapper.readValue(response.body(), new TypeReference<>(){});

            // Converte para a estrutura de Domínio pura (apenas o Nome)
            List<Categoria> lista = new ArrayList<>();
            for (CategoriaApi d : dtos) {
                lista.add(new Categoria(d.getNome()));
            }
            return lista;
        } catch (Exception e) {
            System.err.println("Erro ao buscar categorias: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<MetodoPagamento> buscarTodosMetodos() {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrlMetodos)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Alterado: Agora lê o JSON usando a sua classe MetodoPagamentoApi
            List<MetodoPagamentoApi> dtos = objectMapper.readValue(response.body(), new TypeReference<>(){});

            // Converte para a estrutura de Domínio pura (Nome e dia de vencimento)
            List<MetodoPagamento> lista = new ArrayList<>();
            for (MetodoPagamentoApi d : dtos) {
                lista.add(new MetodoPagamento(d.getNome(), d.getDiaVencimento()));
            }
            return lista;
        } catch (Exception e) {
            System.err.println("Erro ao buscar métodos de pagamento: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void salvarCategoria(Categoria c) {
        try {
            // Transforma o Domínio puro na classe da API com o ID zerado (para o C# saber que é um novo registro)
            CategoriaApi apiObj = new CategoriaApi(0, c.getNome());
            String json = objectMapper.writeValueAsString(apiObj);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrlCategorias))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status do cadastro de categoria: " + response.statusCode());
        } catch (Exception e) {
            System.err.println("Erro ao salvar categoria: " + e.getMessage());
        }
    }

    @Override
    public void salvarMetodo(MetodoPagamento m) {
        try {
            // Transforma o Domínio puro na classe da API antes de enviar
            MetodoPagamentoApi apiObj = new MetodoPagamentoApi(0, m.getNome(), m.getDiaVencimento());
            String json = objectMapper.writeValueAsString(apiObj);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrlMetodos))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Status do cadastro de método: " + response.statusCode());
        } catch (Exception e) {
            System.err.println("Erro ao salvar método: " + e.getMessage());
        }
    }

    // --- Métodos auxiliares usando estritamente as suas classes CategoriaApi e MetodoPagamentoApi ---
    private int obterIdCategoriaPorNome(String nome) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrlCategorias)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            List<CategoriaApi> dtos = objectMapper.readValue(response.body(), new TypeReference<>(){});
            for (CategoriaApi c : dtos) {
                if (c.getNome().equalsIgnoreCase(nome)) return c.getId();
            }
        } catch (Exception e) {
            System.err.println("Erro ao resolver ID da categoria: " + e.getMessage());
        }
        return 1;
    }

    private int obterIdMetodoPorNome(String nome) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrlMetodos)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Alterado para usar MetodoPagamentoApi para fazer a busca do ID
            List<MetodoPagamentoApi> dtos = objectMapper.readValue(response.body(), new TypeReference<>(){});
            for (MetodoPagamentoApi m : dtos) {
                if (m.getNome().equalsIgnoreCase(nome)) return m.getId();
            }
        } catch (Exception e) {
            System.err.println("Erro ao resolver ID do método: " + e.getMessage());
        }
        return 1;
    }

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