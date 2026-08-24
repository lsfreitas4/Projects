package serverSide;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Classe responsável pela gestão de tokens de autenticação de sessão.
 * Cada utilizador autenticado recebe um token com validade limitada (2 horas).
 * Garante unicidade dos tokens e controla sua expiração.
 */
public class TokenManager {

    private static final Map<String, SessionInfo> tokenToSession = new HashMap<>();
    private static final ReentrantLock lock = new ReentrantLock();

    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final long TOKEN_EXPIRATION_SECONDS = 7200; // 2 horas (em segundos)

    /**
     * Gera um token curto (6 caracteres alfanuméricos aleatórios).
     */
    private static String generateShortToken() {
        StringBuilder token = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            int index = RANDOM.nextInt(CHAR_POOL.length());
            token.append(CHAR_POOL.charAt(index));
        }
        return token.toString();
    }

    /**
     * Gera um token novo para um utilizador autenticado.
     * Substitui tokens anteriores do mesmo utilizador e guarda o momento de criação.
     *
     * @param user utilizador autenticado
     * @return token gerado
     */
    public static String generateToken(User user) {
        lock.lock();
        try {
            // Remove qualquer token anterior do mesmo utilizador
            tokenToSession.entrySet().removeIf(entry -> {
                boolean isSameUser = entry.getValue().user.getUsername().equals(user.getUsername());
                if (isSameUser) {
                    FaultToleranceManager.unbindUser(user.getUsername());
                    System.out.println("Token anterior removido para '" + user.getUsername() + "'");
                }
                return isSameUser;
            });

            // Garante unicidade do novo token
            String token;
            do {
                token = generateShortToken();
            } while (tokenToSession.containsKey(token));

            // Associa o token ao utilizador e marca a data/hora de criação
            tokenToSession.put(token, new SessionInfo(user, Instant.now()));
            System.out.println("Utilizador '" + user.getUsername() + "' autenticado com token: " + token);
            return token;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Recupera o utilizador associado a um token válido (não expirado).
     *
     * @param token token enviado pelo cliente
     * @return utilizador correspondente, ou null se for inválido ou expirado
     */
    public static User getUserByToken(String token) {
        lock.lock();
        try {
            SessionInfo session = tokenToSession.get(token);
            if (session == null) return null;

            // Verifica tempo de validade do token
            long age = Instant.now().getEpochSecond() - session.createdAt.getEpochSecond();
            if (age > TOKEN_EXPIRATION_SECONDS) {
                // Token expirou — remove e informa
                SessionInfo expiredSession = tokenToSession.remove(token);
                if (expiredSession != null) {
                    User expiredUser = expiredSession.user;
                    FaultToleranceManager.unbindUser(expiredUser.getUsername());
                    System.out.println("Token expirado para '" + expiredUser.getUsername() + "'. Sessão encerrada.");
                }
                return null;
            }

            return session.user;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove um token (usado em logout).
     *
     * @param token token a ser invalidado
     */
    public static void removeToken(String token) {
        lock.lock();
        try {
            tokenToSession.remove(token);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Classe interna que guarda o estado da sessão: utilizador e timestamp da criação.
     */
    private static class SessionInfo {
        User user;
        Instant createdAt;

        SessionInfo(User user, Instant createdAt) {
            this.user = user;
            this.createdAt = createdAt;
        }
    }
}
