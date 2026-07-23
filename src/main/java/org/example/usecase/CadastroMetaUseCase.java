package org.example.usecase;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Locale;

public class CadastroMetaUseCase {

    // Como você está rodando no mesmo computador, pode usar o localhost
    private static final String BASE_URL = "http://localhost:5123/api/relatorios";

    // Usamos um ExecutorService para rodar requisições de rede fora da Thread Principal do JavaFX
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Este é o método que o seu Controller do JavaFX vai chamar ao clicar no botão "Salvar"
     */
    public void salvarMetasDoAno(int categoriaId, int ano, double valorMensal, boolean incluirDecimoTerceiro, Callback callback) {

        executor.execute(() -> {
            try {
                // O Java faz o trabalho sujo: Um loop de Janeiro (1) a Dezembro (12)
                for (int mes = 1; mes <= 12; mes++) {

                    double valorParaSalvar = valorMensal;

                    // 🧠 A MÁGICA DO 13º ACONTECE AQUI NO FRONTEND (JAVA):
                    // Se o usuário ativou o 13º E estamos no mês de Dezembro, dobramos a meta!
                    if (incluirDecimoTerceiro && mes == 12) {
                        valorParaSalvar = valorMensal * 2;
                    }

                    // Prepara o JSON exato que o seu C# (RequestConfigurarMeta) está esperando.
                    // Usamos String.format com Locale.US para garantir que o decimal use ponto (.) em vez de vírgula (,)
                    String jsonBody = String.format(Locale.US,
                            "{\"categoriaId\": %d, \"mesInicio\": %d, \"anoInicio\": %d, \"valorMeta\": %.2f}",
                            categoriaId, mes, ano, valorParaSalvar);

                    // Envia para o C#
                    enviarParaApiCSharp(jsonBody);
                }

                // Se o loop terminar sem erros, avisamos o Controller que deu sucesso
                if (callback != null) {
                    callback.onSucesso();
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (callback != null) {
                    callback.onErro("Erro ao salvar metas: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Método privado que faz a chamada HTTP real para o C# (Nativo do Java, sem bibliotecas externas)
     */
    private void enviarParaApiCSharp(String json) throws Exception {
        URL url = new URL(BASE_URL + "/salvar-meta-unica");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        // Escreve o JSON no corpo da requisição
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = json.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Lê a resposta do C# (200 OK)
        int responseCode = conn.getResponseCode();
        if (responseCode != 200 && responseCode != 201) {
            throw new Exception("Falha na API: Código " + responseCode);
        }

        conn.disconnect();
    }

    // Interface para comunicar o resultado de volta para o Controller do JavaFX
    public interface Callback {
        void onSucesso();
        void onErro(String mensagem);
    }
}