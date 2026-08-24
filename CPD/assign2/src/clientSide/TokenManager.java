package clientSide;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe responsável por gerir os tokens de autenticação persistentes por utilizador.
 * Os tokens são guardados num ficheiro local e usados para reconexões sem necessidade de senha.
 */
public class TokenManager {

    private static final String TOKEN_FILE = "tokens.txt"; // Ficheiro onde os tokens são guardados
    private static final Map<String, String> tokens = new HashMap<>(); // Mapa de username → token

    private static String currentUsername; // Utilizador atualmente autenticado
    private static String currentToken;    // Token do utilizador atual

    /**
     * Carrega os tokens do ficheiro 'tokens.txt' para memória.
     * O formato esperado de cada linha é: username:token
     */
    public static void loadTokens() {
        tokens.clear();
        Path path = Paths.get(TOKEN_FILE);
        if (!Files.exists(path)) return;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split(":");
                if (parts.length == 2) {
                    tokens.put(parts[0], parts[1]); // Guarda no mapa
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar tokens: " + e.getMessage());
        }
    }

    /**
     * Guarda os tokens em memória no ficheiro 'tokens.txt'.
     * Cada linha no ficheiro terá o formato: username:token
     */
    private static void saveTokens() {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(TOKEN_FILE))) {
            for (var entry : tokens.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine(); // Adiciona quebra de linha após cada entrada
            }
        } catch (IOException e) {
            System.err.println("Erro ao guardar tokens: " + e.getMessage());
        }
    }

    /**
     * Define o utilizador atualmente autenticado e tenta recuperar o token correspondente.
     *
     * @param username nome do utilizador atual
     */
    public static void setCurrentUser(String username) {
        currentUsername = username;
        currentToken = tokens.get(username);
    }

    /**
     * Retorna o nome do utilizador atual.
     */
    public static String getCurrentUsername() {
        return currentUsername;
    }

    /**
     * Retorna o token do utilizador atual.
     */
    public static String getCurrentToken() {
        return currentToken;
    }

    /**
     * Verifica se o utilizador atual tem um token válido associado.
     */
    public static boolean hasTokenForCurrentUser() {
        return currentToken != null;
    }

    /**
     * Armazena (ou atualiza) um token para determinado utilizador, define-o como atual
     * e persiste a alteração no ficheiro.
     *
     * @param username nome do utilizador
     * @param token    token de autenticação gerado pelo servidor
     */
    public static void storeTokenForUser(String username, String token) {
        tokens.put(username, token);
        currentUsername = username;
        currentToken = token;
        saveTokens();
    }

    /**
     * Remove o token associado a um utilizador específico.
     * Se o utilizador for o atual, também limpa o token da sessão.
     *
     * @param username nome do utilizador a remover
     */
    public static void clearToken(String username) {
        tokens.remove(username);
        if (username.equals(currentUsername)) {
            currentToken = null;
        }
        saveTokens();
    }
}
