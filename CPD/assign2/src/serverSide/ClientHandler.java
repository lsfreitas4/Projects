package serverSide;

import sharedFiles.ProtocolConstants;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * Classe responsável por lidar com a sessão de um cliente.
 * Trata autenticação, reconexão via token, envio e receção de mensagens e manipulação de salas.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Map<String, ClientHandler> connectedClients;
    private final Map<String, ChatRoom> chatRooms;
    private final Lock clientsLock;
    private final Lock roomsLock;

    private PrintWriter out;
    private BufferedReader in;
    private User user;

    public ClientHandler(Socket socket,
                         Map<String, ClientHandler> connectedClients,
                         Map<String, ChatRoom> chatRooms,
                         Lock clientsLock,
                         Lock roomsLock) {
        this.socket = socket;
        this.connectedClients = connectedClients;
        this.chatRooms = chatRooms;
        this.clientsLock = clientsLock;
        this.roomsLock = roomsLock;
    }

    /**
     * Método principal da thread. Trata a conexão do cliente até que ela seja encerrada.
     */
    @Override
    public void run() {
        try (
                socket;
                InputStream input = socket.getInputStream();
                OutputStream output = socket.getOutputStream()
        ) {
            in = new BufferedReader(new InputStreamReader(input));
            out = new PrintWriter(output, true);

            String line;
            while ((line = in.readLine()) != null) {
                processCommand(line.trim());
            }

        } catch (IOException e) {
            System.err.println("Erro na ligação com o cliente: " + e.getMessage());
        } finally {
            if (user != null) {
                if (user.getCurrentRoom() != null) {
                    ChatRoom room = chatRooms.get(user.getCurrentRoom());
                    if (room != null) room.removeUser(user);
                }
                FaultToleranceManager.unbindUser(user.getUsername());
                clientsLock.lock();
                try {
                    connectedClients.remove(user.getUsername());
                } finally {
                    clientsLock.unlock();
                }
            }
        }
    }

    /**
     * Decide qual handler executar com base no comando recebido.
     */
    private void processCommand(String line) {
        if (line.startsWith(ProtocolConstants.LOGIN)) {
            handleLogin(line);
        } else if (line.startsWith(ProtocolConstants.REGISTER)) {
            handleRegister(line);
        }else if (line.startsWith(ProtocolConstants.LOGOUT)) {
            handleLogout(line);
        } else if (line.startsWith("TOKEN")) {
            handleToken(line);
        } else if (line.startsWith(ProtocolConstants.JOIN_ROOM)) {
            handleJoinRoom(line);
        } else if (line.startsWith(ProtocolConstants.CREATE_ROOM)) {
            handleCreateRoom(line);
        } else if (line.startsWith(ProtocolConstants.MESSAGE)) {
            handleMessage(line);
        } else if (line.equalsIgnoreCase(ProtocolConstants.LEAVE_ROOM)) {
            handleLeaveRoom();
        } else {
            out.println(ProtocolConstants.INVALID_COMMAND);
        }
    }

    /**
     * Autenticação de um utilizador com username e password.
     */
    private void handleLogin(String line) {
        String[] parts = line.split(" ");
        if (parts.length != 3) {
            out.println(ProtocolConstants.ERROR + " Invalid LOGIN format.");
            return;
        }

        String username = parts[1];
        String password = parts[2];

        clientsLock.lock();
        try {
            if (connectedClients.containsKey(username)) {
                out.println(ProtocolConstants.ERROR + " Utilizador já está ligado noutro terminal.");
                return;
            }
        } finally {
            clientsLock.unlock();
        }

        boolean valid = AuthenticationManager.validateCredentials(username, password);
        if (valid) {
            try {
                user = new User(username, socket);
            } catch (IOException e) {
                out.println(ProtocolConstants.ERROR + " Erro ao criar o utilizador.");
                return;
            }

            String token = TokenManager.generateToken(user);
            user.setSessionToken(token);
            FaultToleranceManager.saveUserState(token, user);

            clientsLock.lock();
            try {
                connectedClients.put(username, this);
            } finally {
                clientsLock.unlock();
            }
            out.println(ProtocolConstants.SUCCESS + " " + token);
        } else {
            out.println(ProtocolConstants.ERROR + " Invalid credentials.");
        }
    }

    /**
     * Termina a sessão do utilizador (logout).
     */
    private void handleLogout(String line) {
        if (user == null) {
            return;
        }

        User loggingOutUser = user;
        user = null;

        if (loggingOutUser.getCurrentRoom() != null) {
            ChatRoom room = chatRooms.get(loggingOutUser.getCurrentRoom());
            if (room != null) {
                room.removeUser(loggingOutUser);
            }
        }

        TokenManager.removeToken(loggingOutUser.getSessionToken());
        FaultToleranceManager.unbindUser(loggingOutUser.getUsername());

        clientsLock.lock();
        try {
            connectedClients.remove(loggingOutUser.getUsername());
            System.out.println("Utilizador '" + loggingOutUser.getUsername() + "' fez logout. Sessão encerrada.");
        } finally {
            clientsLock.unlock();
        }

        out.println("SUCCESS Sessão terminada.");
    }

    /**
     * Registo de novo utilizador. Se for bem-sucedido, faz login automaticamente.
     */
    private void handleRegister(String line) {
        String[] parts = line.split(" ");
        if (parts.length != 3) {
            out.println(ProtocolConstants.ERROR + " Invalid REGISTER format.");
            return;
        }

        String username = parts[1];
        String password = parts[2];

        clientsLock.lock();
        try {
            if (connectedClients.containsKey(username)) {
                out.println(ProtocolConstants.ERROR + " Utilizador já está ligado noutro terminal.");
                return;
            }
        } finally {
            clientsLock.unlock();
        }

        boolean success = AuthenticationManager.registerUser(username, password);
        if (success) {
            handleLogin("LOGIN " + username + " " + password);
        } else {
            out.println(ProtocolConstants.ERROR + " Username already exists.");
        }
    }

    /**
     * Permite que um cliente se reconecte a partir de um token guardado.
     */
    private void handleToken(String line) {
        String[] parts = line.split(" ");
        if (parts.length != 2) {
            out.println("ERROR Formato inválido. Use: TOKEN <token>");
            return;
        }

        String token = parts[1];
        User previousState = TokenManager.getUserByToken(token);

        if (previousState == null) {
            out.println("ERROR Token inválido ou expirado.");
            return;
        }

        String username = previousState.getUsername();

        clientsLock.lock();
        try {
            if (connectedClients.containsKey(username)) {
                out.println("ERROR Sessão já ativa noutro terminal.");
                return;
            }

            user = new User(username, socket);
            user.setSessionToken(token);
            user.setCurrentRoom(previousState.getCurrentRoom());
            connectedClients.put(username, this);
        } catch (IOException e) {
            out.println("ERROR Erro ao restaurar utilizador.");
            return;
        } finally {
            clientsLock.unlock();
        }

        if (user.getCurrentRoom() != null) {
            ChatRoom room = chatRooms.get(user.getCurrentRoom());
            if (room == null) {
                room = new ChatRoom(user.getCurrentRoom(), false);
                chatRooms.put(user.getCurrentRoom(), room);
            }
            room.addUser(user, out);

            List<String> lastMessages = room.getLastMessages(6);
            for (String msg : lastMessages) {
                out.println(msg);
            }

            out.println("RECONNECTED_ROOM " + room.getRoomName());

        } else {
            out.println(ProtocolConstants.SUCCESS + " Reconnected as " + username);
        }
    }

    /**
     * Permite ao utilizador criar uma sala normal ou com IA.
     */
    private void handleCreateRoom(String line) {
        String[] parts = line.split(" ", 2);
        if (parts.length != 2) {
            out.println("Usage: CREATE_ROOM <room-name> (for normal room)");
            out.println("       CREATE_ROOM ai:<room-name>:<prompt> (for AI room)");
            return;
        }

        String input = parts[1];
        boolean isAIRoom = input.startsWith("ai:");
        String roomName;
        String prompt = "";

        if (isAIRoom) {
            String[] aiParts = input.split(":", 3);
            if (aiParts.length < 3) {
                out.println("Invalid AI room format. Use: ai:<room-name>:<prompt>");
                return;
            }
            roomName = aiParts[1];
            prompt = aiParts[2];
        } else {
            roomName = input;
        }

        roomsLock.lock();
        try {
            if (chatRooms.containsKey(roomName)) {
                out.println("Room '" + roomName + "' already exists");
                return;
            }

            ChatRoom newRoom = new ChatRoom(roomName, isAIRoom, prompt);
            chatRooms.put(roomName, newRoom);

            File f = new File("rooms/" + roomName + ".txt");
            if (!f.exists()) {
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    out.println("ERRO: Não foi possível criar o ficheiro da sala.");
                    return;
                }
            }
            out.println("SUCCESS Room '" + roomName + "' created" + (isAIRoom ? " (AI Room)" : ""));
        } finally {
            roomsLock.unlock();
        }
    }

    /**
     * Permite entrar numa sala. Se não existir, é criada.
     * Também permite listar todas as salas disponíveis.
     */
    private void handleJoinRoom(String line) {
        String[] parts = line.split(" ", 2);
        if (parts.length == 2 && parts[1].equals("LIST")) {
            // Handle room listing
            File folder = new File("rooms");
            String[] files = folder.list((dir, name) -> name.endsWith(".txt"));
            if (files == null || files.length == 0) {
                out.println("Nenhuma sala disponível.");
                return;
            }
            List<String> roomNames = Arrays.stream(files)
                    .map(f -> f.replace(".txt", ""))
                    .toList();
            out.println(String.join(", ", roomNames));
            return;
        }

        if (parts.length != 2) {
            out.println("ERRO: Use JOIN_ROOM <nome>");
            return;
        }

        String roomName = parts[1];
        roomsLock.lock();
        try {
            ChatRoom room = chatRooms.get(roomName);
            if (room == null) {
                // Create new room if it doesn't exist
                room = new ChatRoom(roomName, false, "");
                chatRooms.put(roomName, room);

                // Create history file
                File file = new File("rooms/" + roomName + ".txt");
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    out.println("ERRO: Não foi possível criar o ficheiro da sala.");
                    return;
                }
                out.println("SUCCESS Sala criada e entrada: " + roomName);
            } else {
                out.println("SUCCESS Entraste na sala " + roomName);
            }

            // Handle user joining
            if (user.getCurrentRoom() != null) {
                ChatRoom currentRoom = chatRooms.get(user.getCurrentRoom());
                if (currentRoom != null) {
                    currentRoom.removeUser(user);
                }
            }
            room.addUser(user, out);  // Note the added PrintWriter parameter
            user.setCurrentRoom(roomName);
        } finally {
            roomsLock.unlock();
        }
    }

    /**
     * Envia mensagem para a sala atual do utilizador.
     */
    private void handleMessage(String line) {
        String content = line.substring("MESSAGE ".length()).trim();
        if (user == null || user.getCurrentRoom() == null) {
            out.println("ERRO: Não estás em nenhuma sala.");
            return;
        }
        roomsLock.lock();
        try {
            ChatRoom room = chatRooms.get(user.getCurrentRoom());
            if (room != null) {
                room.broadcastMessage(user, content);
            }
        } finally {
            roomsLock.unlock();
        }
    }

    /**
     * Permite ao utilizador sair da sala atual.
     */
    private void handleLeaveRoom() {
        if (user == null || user.getCurrentRoom() == null) {
            out.println("ERRO: Não estás em nenhuma sala.");
            return;
        }
        roomsLock.lock();
        try {
            ChatRoom room = chatRooms.get(user.getCurrentRoom());
            if (room != null) {
                room.removeUser(user);
            }
            user.setCurrentRoom(null);
            out.println("LEFT_ROOM");
        } finally {
            roomsLock.unlock();
        }
    }
}
