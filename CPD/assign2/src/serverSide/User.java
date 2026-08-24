package serverSide;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Classe que representa um utilizador autenticado no servidor.
 * Guarda informações como nome de utilizador, token de sessão, sala atual e socket de comunicação.
 */
public class User {

    private final String username;         // Nome do utilizador
    private String sessionToken;           // Token de sessão associado (autenticação)
    private String currentRoom;            // Nome da sala onde o utilizador está (pode ser null)
    private final Socket socket;           // Socket ligado ao cliente
    private final PrintWriter writer;      // Escritor para enviar mensagens ao cliente

    /**
     * Construtor: cria um utilizador com nome e socket associado.
     *
     * @param username nome do utilizador
     * @param socket socket ativo ligado ao cliente
     * @throws IOException se ocorrer erro ao obter o OutputStream do socket
     */
    public User(String username, Socket socket) throws IOException {
        this.username = username;
        this.socket = socket;
        this.writer = new PrintWriter(socket.getOutputStream(), true);
    }

    public String getUsername() {
        return username;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String token) {
        this.sessionToken = token;
    }

    public String getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(String roomName) {
        this.currentRoom = roomName;
    }

    /**
     * Envia uma mensagem ao cliente através do socket.
     *
     * @param message mensagem a ser enviada
     */
    public void sendMessage(String message) {
        writer.println(message);
    }
}
