package serverSide;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Classe responsável por integrar o servidor com o modelo de IA (via Ollama API).
 * Faz pedidos HTTP POST com prompt e contexto, e extrai a resposta do modelo LLaMA.
 */
public class AIIntegration {
    private static final String OLLAMA_ENDPOINT = "http://127.0.0.1:11434/api/generate";
    private static final String MODEL_NAME = "llama3";


    /**
     * Envia um prompt e contexto para a IA e devolve a resposta gerada.
     *
     * @param prompt  instruções gerais para orientar o modelo
     * @param context conteúdo (ex: histórico de mensagens da sala) para o modelo continuar
     * @return resposta textual gerada pela IA
     */
    public static String getAIReponse(String prompt, String context) {
        // Algumas propriedades de sistema para evitar problemas com conexões HTTP persistentes
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("http.keepAlive", "false");

        try {
            System.out.println("[DEBUG] Connecting to Ollama...");
            URL url = new URL(OLLAMA_ENDPOINT);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "close");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(60000);
            conn.setDoOutput(true);

            // Monta o corpo do JSON com prompt + contexto
            String jsonPayload = String.format(
                    "{\"model\":\"%s\",\"prompt\":\"%s\\n\\n%s\",\"stream\":false}",
                    MODEL_NAME,
                    escape(prompt),
                    escape(context)
            );

            // Envia o corpo JSON na requisição
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Verifica código HTTP
            int code = conn.getResponseCode();

            if (code != 200) {
                System.err.println("[OLLAMA ERROR] HTTP response code: " + code);
                return null;
            }

            // Lê resposta completa da IA
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            // Extrai apenas o conteúdo textual da resposta JSON
            return extractResponseText(response.toString());

        } catch (Exception e) {
            System.err.println("[OLLAMA CONNECTION FAILURE] " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            // Restaura keepAlive para o comportamento padrão
            System.setProperty("http.keepAlive", "true");
        }
    }

    /**
     * Escapa caracteres especiais para enviar o prompt/contexto no JSON.
     */
    private static String escape(String input) {
        return input.replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    private static String extractResponseText(String json) {
        try {
            int start = json.indexOf("\"response\":\"");
            if (start == -1) return null;
            start += "\"response\":\"".length();
            int end = json.indexOf("\"", start);
            if (end == -1) return null;
            return json.substring(start, end).replace("\\n", "\n").replace("\\\"", "\"").trim();
        } catch (Exception e) {
            System.err.println("[OLLAMA PARSE ERROR] " + e.getMessage());
            return null;
        }
    }
}