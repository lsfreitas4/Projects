package clientSide;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Classe responsável por escutar mensagens recebidas do servidor
 * e exibi-las no terminal do cliente.
 */
public class MessageListener implements Runnable {

    private final BufferedReader inputStream;
    private final String currentUsername;

    /**
     * Construtor da classe MessageListener.
     *
     * @param inputStream      fluxo de entrada ligado ao servidor
     * @param currentUsername  nome do utilizador atual
     */
    public MessageListener(BufferedReader inputStream, String currentUsername) {
        this.inputStream = inputStream;
        this.currentUsername = currentUsername;
    }

    /**
     * Método principal da thread: escuta continuamente mensagens vindas do servidor.
     * Exibe mensagens de sistema ou de outros utilizadores no terminal do cliente.
     */
    @Override
    public void run() {
        try {
            String message;

            // Enquanto a thread não for interrompida e houver mensagens para ler:
            while (!Thread.currentThread().isInterrupted() && (message = inputStream.readLine()) != null) {

                if (message.startsWith("[System]") || message.startsWith("Room:")) {
                    System.out.println(message);
                } else if (message.contains(":")) {
                    String sender = message.split(":", 2)[0];
                    if (!sender.equals(currentUsername)) {
                        System.out.println(message); // Só imprime se for de outro utilizador
                    }
                }

                System.out.print("> ");
            }
        } catch (IOException e) {
            // Se a thread ainda não foi interrompida, exibe o erro
            if (!Thread.currentThread().isInterrupted()) {
                System.err.println("Erro ao ler mensagens do servidor: " + e.getMessage());
            }
        }
    }
}
