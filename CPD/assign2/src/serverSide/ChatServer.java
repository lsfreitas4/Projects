package serverSide;

import sharedFiles.ProtocolConstants;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Classe principal do servidor de chat.
 * Cria um servidor SSL (com Java Secure Socket Layer) para aceitar conexões de clientes de forma segura.
 */
public class ChatServer {
    public static void main(String[] args) {
        final int PORT = ProtocolConstants.DEFAULT_PORT; // Porta onde o servidor irá escutar

        // Define o keystore com certificado SSL e sua senha
        System.setProperty("javax.net.ssl.keyStore", "serverkeystore.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");

        // Mapa de clientes atualmente conectados (username → ClientHandler)
        Map<String, ClientHandler> connectedClients = new HashMap<>();

        // Mapa das salas disponíveis (nome da sala → ChatRoom)
        Map<String, ChatRoom> chatRooms = new HashMap<>();

        // Locks para garantir sincronização em ambientes concorrentes
        ReentrantLock clientsLock = new ReentrantLock();
        ReentrantLock roomsLock = new ReentrantLock();

        try {
            // Cria o socket do servidor com suporte a SSL
            SSLServerSocketFactory sslFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            ServerSocket serverSocket = sslFactory.createServerSocket(PORT);

            System.out.println("Servidor seguro iniciado na porta " + PORT);

            // Loop infinito que aceita e trata novas conexões de clientes
            while (true) {
                Socket socket = serverSocket.accept(); // Espera nova conexão
                ClientHandler handler = new ClientHandler(socket, connectedClients, chatRooms, clientsLock, roomsLock);

                // Cria uma nova thread virtual (leve) para lidar com o cliente
                Thread.startVirtualThread(handler);
            }

        } catch (IOException e) {
            System.err.println("Erro no servidor SSL: " + e.getMessage());
        }
    }
}
