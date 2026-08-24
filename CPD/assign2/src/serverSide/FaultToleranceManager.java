package serverSide;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Classe responsável por gerir a tolerância a falhas do servidor,
 * permitindo restaurar o estado do utilizador após quedas de conexão.
 * Utiliza locking manual (ReentrantLock) para garantir acesso seguro ao mapa.
 */
public class FaultToleranceManager {

    // Mapeia tokens de sessão para o estado do utilizador correspondente
    private static final Map<String, User> tokenToUserState = new HashMap<>();

    // Lock para garantir sincronização entre threads concorrentes
    private static final ReentrantLock lock = new ReentrantLock();

    /**
     * Guarda o estado atual do utilizador associado a um token.
     *
     * @param token token de sessão (único por utilizador)
     * @param user  estado atual do utilizador (objeto User)
     */
    public static void saveUserState(String token, User user) {
        lock.lock();
        try {
            tokenToUserState.put(token, user);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Restaura o estado de um utilizador a partir de um token.
     *
     * @param token token de sessão
     * @return utilizador correspondente ao token, ou null se não existir
     */
    public static User restoreUserState(String token) {
        lock.lock();
        try {
            return tokenToUserState.get(token);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Wrapper para reconexão usando token. (Pode ser expandido futuramente.)
     *
     * @param token token de sessão
     * @return utilizador correspondente ao token
     */
    public static User handleReconnection(String token) {
        return restoreUserState(token);
    }

    /**
     * Invalida um token, removendo o estado do utilizador associado.
     *
     * @param token token a remover
     */
    public static void invalidateToken(String token) {
        lock.lock();
        try {
            tokenToUserState.remove(token);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove todos os tokens associados a um determinado nome de utilizador.
     * Usado em casos de logout ou reconexão.
     *
     * @param username nome do utilizador
     */
    public static void unbindUser(String username) {
        lock.lock();
        try {
            tokenToUserState.entrySet().removeIf(entry ->
                    entry.getValue().getUsername().equals(username)
            );
        } finally {
            lock.unlock();
        }
    }
}
