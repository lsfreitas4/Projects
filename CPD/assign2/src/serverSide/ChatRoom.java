package serverSide;

import sharedFiles.ProtocolConstants;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Classe que representa uma sala de chat, podendo ser normal ou com IA.
 * Gere os utilizadores presentes, histórico de mensagens e integração com modelos de linguagem (via Ollama).
 */
public class ChatRoom {
    private final String roomName;
    private final List<User> users = new ArrayList<>();
    private final List<String> messageHistory = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final boolean isAIRoom;
    private final String aiPrompt;
    private final File historyFile;

    public ChatRoom(String roomName, boolean isAIRoom) {
        this(roomName, isAIRoom, "");
    }

    // Construtor para sala com prompt
    public ChatRoom(String roomName, boolean isAIRoom, String aiPrompt) {
        this.roomName = roomName;
        this.isAIRoom = isAIRoom;
        this.aiPrompt = aiPrompt;

        // Garante que a pasta "rooms/" existe
        File dir = new File("rooms");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.historyFile = new File(dir, roomName + ".txt");

        // Carrega histórico do ficheiro (se existir)
        loadHistoryFromFile();
    }

    public String getRoomName() {
        return roomName;
    }

    public boolean isAIRoom() {
        return isAIRoom;
    }

    /**
     * Limpa o histórico da sala: memória e ficheiro.
     */
    public synchronized void clearHistory() {
        lock.lock();
        try {
            messageHistory.clear();
            if (historyFile.exists()) {
                historyFile.delete();
            }
            historyFile.createNewFile();
        } catch (IOException e) {
            System.err.println("Error clearing history: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Adiciona um utilizador à sala e notifica os restantes.
     */
    public synchronized void addUser(User user, PrintWriter writer) {
        lock.lock();
        try {
            if (!users.contains(user)) {
                users.add(user);
                user.setCurrentRoom(roomName);

                String entryMessage = "[System] " + user.getUsername() + " entrou na sala";
                messageHistory.add(entryMessage);
                saveMessageToFile(entryMessage);

                for (User u : users) {
                    u.sendMessage(entryMessage);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove um utilizador da sala e notifica os restantes.
     */
    public synchronized void removeUser(User user) {
        lock.lock();
        try {
            if (users.remove(user)) {
                String exitMessage = "[System] " + user.getUsername() + " has left the room";
                messageHistory.add(exitMessage);
                saveMessageToFile(exitMessage);
                users.forEach(u -> u.sendMessage(exitMessage));
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Difunde uma mensagem para todos os utilizadores da sala.
     * Se for uma sala com IA, também obtém e envia resposta automática.
     */
    public synchronized void broadcastMessage(User sender, String content) {
        if (content == null || content.startsWith(ProtocolConstants.MESSAGE)) {
            return;
        }
        // Evita duplicatas consecutivas
        String formattedMessage = sender.getUsername() + ": " + content;
        if (messageHistory.isEmpty() || !messageHistory.get(messageHistory.size() - 1).equals(formattedMessage)) {
            messageHistory.add(formattedMessage);
            saveMessageToFile(formattedMessage);
            users.stream()
                    .filter(u -> !u.equals(sender))
                    .forEach(u -> u.sendMessage(formattedMessage));
        }

        if (isAIRoom) {
            try {
                String context = buildConversationContext();
                String botResponse = AIIntegration.getAIReponse(aiPrompt, context);

                if (botResponse != null) {
                    String formattedBot = "Bot: " + botResponse.trim();
                    if (messageHistory.isEmpty() || !messageHistory.get(messageHistory.size() - 1).equals(formattedBot)) {
                        messageHistory.add(formattedBot);
                        saveMessageToFile(formattedBot);
                        users.forEach(u -> u.sendMessage(formattedBot));
                    }
                } else {
                    users.forEach(u -> u.sendMessage("Bot: O serviço de IA não está disponível no momento."));
                }

            } catch (Exception e) {
                users.forEach(u -> u.sendMessage("Bot: Ocorreu um erro ao tentar responder. Tente novamente."));
            }
        }
    }

    private String buildConversationContext() {
        StringBuilder context = new StringBuilder();
        if (aiPrompt != null && !aiPrompt.isEmpty()) {
            context.append("System Prompt: ").append(aiPrompt).append("\n\n");
        }
        for (String msg : messageHistory) {
            context.append(msg).append("\n");
        }
        return context.toString();
    }

    private void saveMessageToFile(String message) {
        try (FileWriter writer = new FileWriter(historyFile, true)) {
            writer.write(message + "\n");
        } catch (IOException e) {
            System.err.println("Error saving message: " + e.getMessage());
        }
    }

    /**
     * Carrega o histórico do ficheiro para a memória.
     */
    private void loadHistoryFromFile() {
        if (!historyFile.exists()) return;

        lock.lock();
        try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!messageHistory.contains(line)) {
                    messageHistory.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading history: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }
    /**
     * Devolve a lista de utilizadores da sala.
     */
    public List<User> getUsers() {
        lock.lock();
        try {
            return new ArrayList<>(users);
        } finally {
            lock.unlock();
        }
    }
    /**
     * Devolve as últimas N mensagens da sala.
     */
    public synchronized List<String> getLastMessages(int n) {
        lock.lock();
        try {
            int start = Math.max(0, messageHistory.size() - n);
            return new ArrayList<>(messageHistory.subList(start, messageHistory.size()));
        } finally {
            lock.unlock();
        }
    }
}
