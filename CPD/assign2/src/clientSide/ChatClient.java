package clientSide;

import sharedFiles.ProtocolConstants;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = ProtocolConstants.DEFAULT_PORT;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final Scanner scanner = new Scanner(System.in);
    private volatile boolean running = true;

    public static void main(String[] args) {
        new ChatClient().start();
    }

    public void start() {
        try {
            // Configura SSL
            System.setProperty("javax.net.ssl.trustStore", "serverkeystore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "123456");

            // Cria socket SSL
            SSLSocketFactory sslFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = sslFactory.createSocket(SERVER_HOST, SERVER_PORT);

            // Inicializa streams
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Inicia menu de autenticação
            authenticateMenu();
        } catch (IOException e) {
            System.err.println("Erro de conexão com o servidor SSL: " + e.getMessage());
        } finally {
            closeResources();
            System.exit(0);
        }
    }


    private void authenticateMenu() throws IOException {
        while (true) {
            System.out.println("\n=== Autenticação ===");
            System.out.println("1. Login");
            System.out.println("2. Registar");
            System.out.println("3. Reconectar com token");
            System.out.println("4. Sair");
            System.out.print("Escolha: ");
            String option = scanner.nextLine().trim();
    
            switch (option) {
                case "1" -> {
                    System.out.println("\n===  LOGIN === ");
                    System.out.print("Nome de utilizador: ");
                    String username = scanner.nextLine().trim();
                    if (handleLogin(username)) return;
                }
                case "2" -> {
                    System.out.println("\n===  REGISTO ===");
                    System.out.print("Nome de utilizador: ");
                    String username = scanner.nextLine().trim();
                    if (handleRegister(username)) return;
                }
                case "3" -> {
                    System.out.print("Token: ");
                    String token = scanner.nextLine().trim();
                    out.println("TOKEN " + token);
    
                    String response;
                    while ((response = in.readLine()) != null) {
                        if (response.startsWith("RECONNECTED_ROOM")) {
                            String roomName = response.split(" ")[1];
                            System.out.println("Reconectado e entraste automaticamente na sala " + roomName);
                            startChatLoop();
                            return;
                        } else if (response.startsWith(ProtocolConstants.SUCCESS)) {
                            String[] parts = response.split(" ");
                            String username = parts[3];
                            System.out.println("Reconectado como " + username);
                            enterChatMenu();
                            return;
                        } else if (response.startsWith("ERROR")) {
                            System.out.println(response);
                            break;
                        } else {
                            System.out.println(response); 
                        }
                    }
                }
                case "4" -> {
                    System.out.println("A sair...");
                    return;
                }
                default -> System.out.println("Opção inválida.");
            }
        }
    }
    

    private boolean handleLogin(String username) throws IOException {
        //System.out.println("\n===  LOGIN === ");
    
        Console console = System.console();
        for (int i = 0; i < 3; i++) {
            String password;
            if (console != null) {
                char[] passwordChars = console.readPassword("Password: ");
                password = new String(passwordChars);
            } else {
                System.out.print("Password: ");
                password = scanner.nextLine().trim();
            }
    
            out.println("LOGIN " + username + " " + password);
            String response = in.readLine();
            if (response.startsWith(ProtocolConstants.SUCCESS)) {
                String token = response.split(" ")[1];
                TokenManager.storeTokenForUser(username, token);
                System.out.println("Bem-vindo, " + username + "! O teu token é: " + token);
                enterChatMenu();
                return true;
            } else {
                System.out.println("Tentativa " + (i + 1) + " falhou: " + response);
                
                if (response.contains("ligado noutro terminal")) {
                    return false;
                }
            }

        }
        System.out.println("Número máximo de tentativas atingido.");
        return false;
    }
    

    private boolean handleRegister(String username) throws IOException {
        //System.out.println("\n===  REGISTO ===");
    
        Console console = System.console();
        String password;
        if (console != null) {
            char[] passwordChars = console.readPassword("Password: ");
            password = new String(passwordChars);
        } else {
            System.out.print("Password: ");
            password = scanner.nextLine().trim();
        }
    
        out.println("REGISTER " + username + " " + password);
        String response = in.readLine();
        if (response.startsWith(ProtocolConstants.SUCCESS)) {
            String token = response.split(" ")[1];
            TokenManager.storeTokenForUser(username, token);
            System.out.println("Bem-vindo, " + username + "! O teu token é: " + token);
            enterChatMenu();
            return true;
        } else {
            System.out.println(response);
            return false;
        }
    }

    private void enterChatMenu() throws IOException {
        while (true) {
            System.out.println("\n=== Chat Rooms ===");
            System.out.println("1. Lista de salas disponíveis");
            System.out.println("2. Criar sala normal");
            System.out.println("3. Criar sala AI");
            System.out.println("4. Entrar numa sala existente");
            System.out.println("5. Sair");
            System.out.print("Escolha: ");

            String option = scanner.nextLine().trim();

            switch (option) {
                case "1" -> {
                    out.println("JOIN_ROOM LIST");
                    String response = in.readLine();
                    if (response.startsWith("SUCCESS") || response.startsWith("ERROR")) {
                        System.out.println(response);
                    } else {
                        String[] rooms = response.split(",\\s*");
                        if (rooms.length == 0 || response.contains("Nenhuma sala")) {
                            System.out.println("Nenhuma sala disponível.");
                        } else {
                            for (int i = 0; i < rooms.length; i++) {
                                System.out.println((i + 1) + ". " + rooms[i]);
                            }
                        }
                    }
                    System.out.println("0. Voltar");
                }
                case "2" -> {
                    System.out.print("Introduza o nome da sala normal: ");
                    String roomName = scanner.nextLine().trim();
                    out.println("CREATE_ROOM " + roomName);
                    String response = in.readLine();
                    System.out.println(response);
                }
                case "3" -> {
                    System.out.println("\nCriar Sala AI");
                    System.out.print("Nome da sala AI: ");
                    String roomName = scanner.nextLine().trim();
                    System.out.print("Prompt do AI (ex: 'Você é um assistente útil'): ");
                    String prompt = scanner.nextLine().trim();
                    out.println("CREATE_ROOM ai:" + roomName + ":" + prompt);
                    System.out.println(in.readLine());
                }
                case "4" -> {
                    System.out.println("\nOpções de entrada na sala:");
                    System.out.println("1. Digitar nome da sala");
                    System.out.println("2. Lista de salas disponíveis");
                    System.out.print("Escolha: ");
                    String joinOption = scanner.nextLine().trim();

                    if (joinOption.equals("1")) {
                        // Option 1: Direct room name entry
                        System.out.print("Digite o nome da sala (será criada se não existir): ");
                        String roomName = scanner.nextLine().trim();
                        out.println("JOIN_ROOM " + roomName);

                        String response;
                        while ((response = in.readLine()) != null) {
                            System.out.println(response);
                            if (response.startsWith("SUCCESS")) {
                                startChatLoop();
                                break;
                            } else if (response.startsWith("ERROR")) {
                                break;
                            }
                        }
                    } else if (joinOption.equals("2")) {
                        // Option 2: List and select from available rooms
                        out.println("JOIN_ROOM LIST");
                        String response = in.readLine();
                        String[] rooms = response.split(",\\s*");

                        if (rooms.length == 0 || response.contains("Nenhuma sala")) {
                            System.out.println("Nenhuma sala disponível.");
                            break;
                        }

                        System.out.println("\nSalas disponíveis:");
                        for (int i = 0; i < rooms.length; i++) {
                            System.out.println((i + 1) + ". " + rooms[i]);
                        }
                        System.out.println("0. Voltar");

                        System.out.print("Escolha o número da sala: ");
                        int choice;
                        try {
                            choice = Integer.parseInt(scanner.nextLine().trim());
                        } catch (NumberFormatException e) {
                            System.out.println("Opção inválida.");
                            break;
                        }

                        if (choice == 0) break;
                        if (choice < 1 || choice > rooms.length) {
                            System.out.println("Número de sala inválido.");
                            break;
                        }

                        String selectedRoom = rooms[choice - 1];
                        out.println("JOIN_ROOM " + selectedRoom);
                        boolean joined = false;

                        while ((response = in.readLine()) != null) {
                            System.out.println(response);
                            if (response.startsWith("SUCCESS")) {
                                joined = true;
                                break;
                            }
                        }

                        if (joined) {
                            startChatLoop();
                        }
                    } else {
                        System.out.println("Opção inválida.");
                    }
                }
                case "5" -> {
                    out.println(ProtocolConstants.LOGOUT);
                    String currentUser = TokenManager.getCurrentUsername();
                    if (currentUser != null) {
                        TokenManager.clearToken(currentUser);
                    }
                    return;
                }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private void startChatLoop() {
        System.out.println("Envia mensagens. Escreve '/sair' para sair da sala.");

        Thread listener = new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equals("LEFT_ROOM")) {
                        break;
                    }
                    System.out.println("\r" + message);
                    System.out.print("> ");
                }
            } catch (IOException e) {
                System.err.println("Erro ao ler mensagens: " + e.getMessage());
            }
        });

        listener.start();

        while (true) {
            System.out.print("> ");
            String message = scanner.nextLine();

            if (message.equalsIgnoreCase("/sair")) {
                out.println("LEAVE_ROOM");
                break;
            } else {
                out.println("MESSAGE " + message);
            }
        }

        try {
            listener.join();
        } catch (InterruptedException e) {
            System.err.println("Erro ao encerrar o listener: " + e.getMessage());
        }

        try {
            enterChatMenu();
        } catch (IOException e) {
            System.err.println("Erro ao voltar ao menu: " + e.getMessage());
        }
    }


    private void closeResources() {
        try {
            if (in != null) in.close();
        } catch (IOException e) {
            System.err.println("Erro ao fechar input: " + e.getMessage());
        }
        if (out != null) out.close();
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Erro ao fechar socket: " + e.getMessage());
        }
    }
}
