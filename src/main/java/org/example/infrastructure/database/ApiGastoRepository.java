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
            int categoriaId = obterIdCategoriaPorNome(gasto.getCategoria().getNome());
            int metodoId = obterIdMetodoPorNome(gasto.getMetodo().getNome());

            Map<String, Object> payload = new HashMap<>();
            payload.put("id", gasto.getId());
            payload.put("descricao", gasto.getDescricao());
            payload.put("valor", gasto.getValor());
            payload.put("data", gasto.getData().toString());

            // --- A CORREÇÃO ESTÁ AQUI: Enviamos os textos simples que o seu banco C# exige ---
            payload.put("categoria", gasto.getCategoria().getNome());
            payload.put("metodo", gasto.getMetodo().getNome());

            // Mantemos os IDs por precaução (caso você mude a estrutura do C# no futuro)
            payload.put("categoriaId", categoriaId);
            payload.put("CategoriaId", categoriaId);
            payload.put("metodoId", metodoId);
            payload.put("MetodoId", metodoId);
            payload.put("MetodoPagamentoId", metodoId);

            payload.put("totalParcelas", gasto.getTotalParcelas());
            payload.put("parcelaAtual", gasto.getParcelaAtual());

            String json = objectMapper.writeValueAsString(payload);
            System.out.println("-> Enviando JSON para C#: " + json);

            boolean ehAtualizacao = (gasto.getId() > 0);
            String url = ehAtualizacao ? baseUrlGastos + "/" + gasto.getId() : baseUrlGastos;

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json");

            if (ehAtualizacao) builder.PUT(HttpRequest.BodyPublishers.ofString(json));
            else builder.POST(HttpRequest.BodyPublishers.ofString(json));

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            System.out.println("-> Resposta do Salvamento C#: [" + response.statusCode() + "] " + response.body());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void atualizarGasto(Gasto g) { salvar(g); }

    @Override
    public void removerGasto(int id) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrlGastos + "/" + id)).DELETE().build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public List<Gasto> buscarTodos() {
        return buscarDeApi(baseUrlGastos);
    }

    @Override
    public List<Gasto> buscarPorMesEAno(int mes, int ano) {
        String url = baseUrlGastos + "?mes=" + mes + "&ano=" + ano;
        return buscarDeApi(url);
    }

    private List<Gasto> buscarDeApi(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) return new ArrayList<>();

            JsonNode root = objectMapper.readTree(response.body());
            List<Gasto> lista = new ArrayList<>();
            for (JsonNode node : root) {
                lista.add(converterJsonNode(node));
            }
            return lista;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // --- O PULO DO GATO: Processamento à prova de falhas ---
    private Gasto converterJsonNode(JsonNode node) {
        int id = getAsInt(node, "id", "Id", "ID");
        String descricao = getAsText(node, "descricao", "Descricao");
        double valor = getAsDouble(node, "valor", "Valor");

        String dataStr = getAsText(node, "data", "Data");
        java.time.LocalDate data = dataStr.isEmpty() ? java.time.LocalDate.now() : java.time.LocalDate.parse(dataStr.substring(0, 10));

        int totalParcelas = getAsInt(node, "totalParcelas", "TotalParcelas");
        int parcelaAtual = getAsInt(node, "parcelaAtual", "ParcelaAtual");

        // 1. Resolve a Categoria (Prioridade: String, depois busca por ID)
        Categoria cat = null;
        String nomeCat = getAsText(node, "categoria", "Categoria");
        if (!nomeCat.isEmpty()) {
            cat = new Categoria(nomeCat);
        } else {
            int catId = getAsInt(node, "categoriaId", "CategoriaId");
            if (catId > 0) cat = buscarCategoriaPorId(catId);
        }
        if (cat == null) cat = new Categoria("Sem Categoria");

        // 2. Resolve o Método (Prioridade: String, depois busca por ID)
        MetodoPagamento met = null;
        String nomeMet = getAsText(node, "metodo", "Metodo");
        if (!nomeMet.isEmpty()) {
            met = new MetodoPagamento(nomeMet, 0);
        } else {
            int metId = getAsInt(node, "metodoId", "MetodoId", "metodoPagamentoId", "MetodoPagamentoId");
            if (metId > 0) met = buscarMetodoPorId(metId);
        }
        if (met == null) met = new MetodoPagamento("Sem Pagamento", 0);

        return new Gasto(id, descricao, valor, data, cat, met, false, totalParcelas, parcelaAtual);
    }

    // --- FUNÇÕES CAÇADORAS (Ignoram Case Sensitive do JSON) ---
    private int getAsInt(JsonNode node, String... keys) {
        for (String k : keys) if (node.has(k) && !node.get(k).isNull()) return node.get(k).asInt();
        return 0;
    }
    private String getAsText(JsonNode node, String... keys) {
        for (String k : keys) if (node.has(k) && !node.get(k).isNull()) return node.get(k).asText();
        return "";
    }
    private double getAsDouble(JsonNode node, String... keys) {
        for (String k : keys) if (node.has(k) && !node.get(k).isNull()) return node.get(k).asDouble();
        return 0.0;
    }
    private JsonNode getAsNode(JsonNode node, String... keys) {
        for (String k : keys) if (node.has(k) && !node.get(k).isNull()) return node.get(k);
        return null;
    }

    // --- Restante do código ---
    @Override
    public List<Categoria> buscarTodasCategorias() {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrlCategorias)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return new ArrayList<>();

            List<CategoriaApi> dtos = objectMapper.readValue(response.body(), new TypeReference<>(){});
            return dtos.stream().map(d -> new Categoria(d.getNome())).toList();
        } catch (Exception e) { return new ArrayList<>(); }
    }

    @Override
    public Categoria buscarCategoriaPorId(int id) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrlCategorias)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            List<CategoriaApi> dtos = objectMapper.readValue(response.body(), new TypeReference<>(){});

            return dtos.stream().filter(c -> c.getId() == id).findFirst()
                    .map(c -> new Categoria(c.getNome())).orElse(new Categoria("Desconhecida"));
        } catch (Exception e) { return new Categoria("Erro"); }
    }

    @Override
    public List<MetodoPagamento> buscarTodosMetodos() {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrlMetodos)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return new ArrayList<>();

            List<MetodoPagamentoApi> dtos = objectMapper.readValue(response.body(), new TypeReference<>(){});
            return dtos.stream().map(d -> new MetodoPagamento(d.getNome(), d.getDiaVencimento())).toList();
        } catch (Exception e) { return new ArrayList<>(); }
    }

    @Override
    public MetodoPagamento buscarMetodoPorId(int id) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrlMetodos)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            List<MetodoPagamentoApi> dtos = objectMapper.readValue(response.body(), new TypeReference<>(){});

            return dtos.stream().filter(m -> m.getId() == id).findFirst()
                    .map(m -> new MetodoPagamento(m.getNome(), m.getDiaVencimento()))
                    .orElse(new MetodoPagamento("Desconhecido", 0));
        } catch (Exception e) { return new MetodoPagamento("Erro", 0); }
    }

    @Override
    public void salvarCategoria(Categoria c) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", 0);
            payload.put("nome", c.getNome());
            String json = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrlCategorias))
                    .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void salvarMetodo(MetodoPagamento m) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", 0);
            payload.put("nome", m.getNome());
            payload.put("diaVencimento", m.getDiaVencimento());
            String json = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrlMetodos))
                    .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private int obterIdCategoriaPorNome(String nome) {
        try {
            for (CategoriaApi c : obterTodasCategoriasApiRaw()) {
                if (c.getNome().trim().equalsIgnoreCase(nome.trim())) return c.getId();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 1;
    }

    private int obterIdMetodoPorNome(String nome) {
        try {
            for (MetodoPagamentoApi m : obterTodosMetodosApiRaw()) {
                if (m.getNome().trim().equalsIgnoreCase(nome.trim())) return m.getId();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 1;
    }

    private List<CategoriaApi> obterTodasCategoriasApiRaw() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrlCategorias)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), new TypeReference<>(){});
    }

    private List<MetodoPagamentoApi> obterTodosMetodosApiRaw() throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(baseUrlMetodos)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), new TypeReference<>(){});
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