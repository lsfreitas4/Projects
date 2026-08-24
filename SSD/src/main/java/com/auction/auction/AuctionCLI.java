package com.auction.auction;

import dev.ledger.core.BlockchainContext;
import dev.ledger.core.Transaction;
import com.auction.ledger.network.P2PClient;
import com.auction.ledger.network.Message;
import com.auction.ledger.network.kademlia.NodeIdentity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * Interface de Linha de Comando (CLI) para interagir com o sistema de leilões.
 * Permite emitir comandos que são transformados em transações assinadas criptograficamente.
 */
public class AuctionCLI {
    private static final Logger logger = LoggerFactory.getLogger(AuctionCLI.class);
    private final BlockchainContext blockchain;
    private final TransactionPool txPool;
    private final P2PClient p2pClient;
    private final NodeIdentity identity;
    private final AuctionManager auctionManager;
    private final String pubKeyHex;
    private final ObjectMapper mapper = new ObjectMapper();

    public AuctionCLI(BlockchainContext blockchain, TransactionPool txPool, P2PClient p2pClient, NodeIdentity identity) {
        this.blockchain = blockchain;
        this.txPool = txPool;
        this.p2pClient = p2pClient;
        this.identity = identity;
        this.auctionManager = new AuctionManager(blockchain);
        this.pubKeyHex = Hex.toHexString(identity.getKeyPair().getPublic().getEncoded());
    }

    public void start() {
        Thread cliThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("\n=================================================");
            System.out.println("   BEM-VINDO AO DECENTRALIZED AUCTION SYSTEM   ");
            System.out.println("=================================================");
            System.out.println("O teu Node ID: " + identity.getNodeId().toHexString());
            System.out.println("A tua Public Key (Truncada): " + pubKeyHex.substring(0, 16) + "...");

            while (!Thread.currentThread().isInterrupted()) {
                printMenu();
                String choice = scanner.nextLine().trim();

                try {
                    switch (choice) {
                        case "1":
                            handleCreateAuction(scanner);
                            break;
                        case "2":
                            handleStartAuction(scanner);
                            break;
                        case "3":
                            handlePlaceBid(scanner);
                            break;
                        case "4":
                            handleEndAuction(scanner);
                            break;
                        case "5":
                            // Força a leitura atualizada da Blockchain
                            auctionManager.refreshState();
                            System.out.println("\n--- LEILÕES ATIVOS NO LEDGER ---");
                            if (auctionManager.getAllAuctions().isEmpty()) {
                                System.out.println("Nenhum leilão registado na blockchain ainda.");
                            } else {
                                for (Auction a : auctionManager.getAllAuctions()) {
                                    System.out.println(a);
                                }
                            }
                            break;
                        case "6":
                            System.out.println("\nTransações pendentes na Mempool local: " + txPool.size());
                            break;
                        case "0":
                            System.out.println("A sair da CLI...");
                            return;
                        default:
                            System.out.println("Opção inválida. Tenta novamente.");
                    }
                } catch (Exception e) {
                    System.out.println("Erro ao executar comando: " + e.getMessage());
                }
            }
        });
        cliThread.setDaemon(true);
        cliThread.start();
    }

    private void printMenu() {
        System.out.println("\n[1] Criar Novo Leilão");
        System.out.println("[2] Iniciar Leilão");
        System.out.println("[3] Fazer Licitação (Bid)");
        System.out.println("[4] Terminar Leilão");
        System.out.println("[5] Listar Todos os Leilões (Sincronizar Estado)");
        System.out.println("[6] Ver Tamanho da Mempool (Pool)");
        System.out.println("[0] Sair");
        System.out.print("Escolha uma opção: ");
    }

    private void handleCreateAuction(Scanner scanner) throws Exception {
        System.out.print("Título do Item: ");
        String title = scanner.nextLine();
        System.out.print("Descrição: ");
        String description = scanner.nextLine();

        long nonce = calculateNextNonce();
        String auctionId = AuctionCommand.generateAuctionId(pubKeyHex, title, description, nonce);
        
        AuctionCommand cmd = AuctionCommand.createAuction(auctionId, title, description);
        broadcastAuctionTransaction(cmd, nonce);
        System.out.println("Transação de Criação enviada! Auction ID gerado: " + auctionId);
    }

    private void handleStartAuction(Scanner scanner) throws Exception {
        System.out.print("Insere o ID do Leilão para iniciar: ");
        String id = scanner.nextLine();

        long nonce = calculateNextNonce();
        AuctionCommand cmd = AuctionCommand.startAuction(id);
        broadcastAuctionTransaction(cmd, nonce);
        System.out.println("Comando START enviado para a rede.");
    }

    private void handlePlaceBid(Scanner scanner) throws Exception {
        System.out.print("Insere o ID do Leilão: ");
        String id = scanner.nextLine();
        System.out.print("Valor da Licitação (Inteiro): ");
        long amount = Long.parseLong(scanner.nextLine());

        long nonce = calculateNextNonce();
        AuctionCommand cmd = AuctionCommand.bid(id, amount);
        broadcastAuctionTransaction(cmd, nonce);
        System.out.println("Licitação de " + amount + " enviada para a rede.");
    }

    private void handleEndAuction(Scanner scanner) throws Exception {
        System.out.print("Insere o ID do Leilão para fechar: ");
        String id = scanner.nextLine();

        long nonce = calculateNextNonce();
        AuctionCommand cmd = AuctionCommand.endAuction(id);
        broadcastAuctionTransaction(cmd, nonce);
        System.out.println("Comando END enviado. O maior licitante será consolidado no próximo bloco.");
    }

    // --- HELPERS CRIPTOGRÁFICOS ---

    private long calculateNextNonce() {
        // Busca o último nonce confirmado no Ledger
        long nextNonce = blockchain.getNextExpectedNonce(pubKeyHex);
        // Soma as transações que já gerámos locais e que ainda estão na pool pendentes para evitar colisões
        long pendingInPool = txPool.getPool().values().stream()
                .filter(tx -> tx.getSenderPublicKey().equals(pubKeyHex))
                .count();
        return nextNonce + pendingInPool;
    }

    private void broadcastAuctionTransaction(AuctionCommand cmd, long nonce) throws Exception {
        // 1. Cria a transação embrulhando o payload estruturado em JSON
        Transaction tx = new Transaction(pubKeyHex, cmd.toPayload(), nonce);
        // 2. Assina a transação com a chave privada do nó
        tx.signTransaction(identity.getKeyPair().getPrivate());

        // 3. Adiciona à nossa própria pool local
        if (txPool.addTransaction(tx)) {
            // 4. Faz broadcast P2P (Gossip Protocol) para os vizinhos Kademlia
            Message msg = new Message(Message.MessageType.NEW_TRANSACTION, mapper.writeValueAsString(tx));
            p2pClient.broadcast(msg);
        } else {
            throw new RuntimeException("A transação foi rejeitada pela pool local (Verifica o teu nonce).");
        }
    }
}