package serverSide;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Classe responsável pela autenticação e registo de utilizadores.
 * Os utilizadores são guardados no ficheiro "users.db" com a password encriptada em SHA-256.
 */
public class AuthenticationManager {

    private static final String USERS_FILE = "users.db"; // Caminho do ficheiro onde os utilizadores são guardados
    private static final Map<String, String> credentials = new HashMap<>(); // Mapa username → hashedPassword

    // Bloco estático: é executado uma vez à carga da classe e carrega os utilizadores existentes
    static {
        loadUsersFromFile();
    }

    /**
     * Regista um novo utilizador se ele ainda não existir.
     *
     * @param username nome do utilizador
     * @param password palavra-passe original (será encriptada)
     * @return true se o registo for bem-sucedido; false se o utilizador já existir
     */
    public static synchronized boolean registerUser(String username, String password) {
        if (credentials.containsKey(username)) return false;

        String hashed = hashPassword(password); // Encripta a password
        credentials.put(username, hashed);      // Guarda no mapa em memória
        saveUserToFile(username, hashed);       // Persiste no ficheiro
        return true;
    }

    /**
     * Valida o login de um utilizador com username e password.
     *
     * @param username nome do utilizador
     * @param password palavra-passe fornecida
     * @return true se as credenciais forem válidas; false caso contrário
     */
    public static boolean validateCredentials(String username, String password) {
        File db = new File(USERS_FILE);
        if (!db.exists()) return false;

        String hashedInput = hashPassword(password); // Encripta a password fornecida

        // Percorre o ficheiro linha a linha à procura de correspondência
        try (BufferedReader br = new BufferedReader(new FileReader(db))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(hashedInput)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Valida se o token recebido corresponde a um utilizador válido.
     *
     * @param token token recebido do cliente
     * @return true se o token for válido (pertence a algum utilizador)
     */
    public static boolean validateToken(String token) {
        return TokenManager.getUserByToken(token) != null;
    }

    /**
     * Aplica SHA-256 à palavra-passe recebida, transformando-a num hash seguro.
     *
     * @param password palavra-passe original
     * @return hash SHA-256 como string hexadecimal
     */
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes());
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash da password", e);
        }
    }

    /**
     * Converte um array de bytes num string hexadecimal.
     *
     * @param hash array de bytes
     * @return representação hexadecimal
     */
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash)
            hexString.append(String.format("%02x", b));
        return hexString.toString();
    }

    /**
     * Guarda um utilizador (username e password encriptada) no ficheiro.
     *
     * @param username nome do utilizador
     * @param hashedPassword password encriptada
     */
    private static void saveUserToFile(String username, String hashedPassword) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE, true))) {
            writer.write(username + ":" + hashedPassword);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Erro ao guardar utilizador: " + e.getMessage());
        }
    }

    /**
     * Carrega todos os utilizadores e respetivas passwords encriptadas do ficheiro para memória.
     */
    private static void loadUsersFromFile() {
        Path path = Paths.get(USERS_FILE);
        if (!Files.exists(path)) return;

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    credentials.put(parts[0], parts[1]); // username → hashedPassword
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar utilizadores: " + e.getMessage());
        }
    }
}
