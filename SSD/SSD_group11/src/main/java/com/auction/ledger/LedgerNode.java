package com.auction.ledger;

import dev.ledger.core.BlockchainContext;
import dev.ledger.core.Block;
import dev.ledger.core.Transaction;
import dev.ledger.consensus.ProofOfWorkEngine;
import com.auction.ledger.config.LedgerConfig;
import com.auction.ledger.network.P2PServer;
import com.auction.ledger.network.P2PClient;
import com.auction.ledger.network.Message;
import com.auction.ledger.network.kademlia.KademliaService;
import com.auction.ledger.network.kademlia.NodeIdentity;
import com.auction.ledger.network.kademlia.RoutingTable;
import com.auction.auction.TransactionPool;
import com.auction.auction.AuctionCLI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.Security;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class LedgerNode {
    private static final Logger logger = LoggerFactory.getLogger(LedgerNode.class);
    
    // Instanciação global e thread-safe da Pool de Transações
    private static final TransactionPool txPool = new TransactionPool();

    public static void main(String[] args) {
        logger.info("Initializing Secure Distributed Ledger Node Architecture...");
        
        Security.addProvider(new BouncyCastleProvider());
        LedgerConfig config = LedgerConfig.get();
        
        // Arranque da Identidade Kademlia
        int networkPoWDifficulty = 12; 
        NodeIdentity myIdentity = NodeIdentity.generateWithPoW(networkPoWDifficulty);
        RoutingTable myRoutingTable = new RoutingTable(myIdentity.getNodeId());
        
        KademliaService kademliaService = 
                new com.auction.ledger.network.kademlia.KademliaService(myIdentity, myRoutingTable, networkPoWDifficulty);

        logger.info("Configured P2P Network Port: {}", config.p2pPort);
        
        ProofOfWorkEngine powEngine = new ProofOfWorkEngine(config.difficulty);
        BlockchainContext blockchain = new BlockchainContext(powEngine, config.difficulty);
        
        logger.info("Genesis block verified. Node tip hash is: {}", blockchain.getLatestBlock().getHash());
        
        // Iniciar Servidor passando a txPool (FIXED)
        P2PServer server = new P2PServer(config.p2pPort, blockchain, kademliaService, txPool);
        server.start();

        // Iniciar Cliente passando a tabela de roteamento
        P2PClient client = new P2PClient(myRoutingTable, blockchain, txPool);
        client.connectToPeers(config.peers, myIdentity);

        // Start async mining daemon thread linked to the TransactionPool
        if (config.miningEnabled == 1L) {
            Thread minerThread = new Thread(() -> {
                logger.info("Mining daemon started. Polling Transaction Pool for work...");
                ObjectMapper mapper = new ObjectMapper();
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(15000); // Aguarda transações acumularem na pool

                        // Extrai as transações prontas e ordenadas da Pool de Memória
                        List<Transaction> pendingTxs = txPool.getTransactionsForBlock(config.blockSizeLimit);
                        
                        // Se não há lances ou transações, o minerador descansa (poupa CPU/Energia)
                        if (pendingTxs.isEmpty()) {
                            continue;
                        }

                        Block newBlock = new Block(blockchain.getLatestBlock().getHash(), pendingTxs);
                        
                        logger.info("Mining new block with {} transactions...", pendingTxs.size());
                        powEngine.sealBlock(newBlock);
                        
                        if (blockchain.addBlock(newBlock)) {
                            // IMPORTANTE: Remove da pool as transações que foram eternizadas no bloco
                            txPool.processBlock(pendingTxs);
                            logger.info("Successfully mined and appended block: {}", newBlock.getHash());
                            
                            Message blockMsg = new Message(
                                Message.MessageType.NEW_BLOCK, 
                                mapper.writeValueAsString(newBlock)
                            );
                            
                            // 1. Envia para os peers a quem NÓS ligámos (Outbound)
                            client.broadcast(blockMsg);
                            
                            // 2. NOVA LINHA: Envia para os peers que se ligaram a NÓS (Inbound)
                            P2PServer.relayGossip(blockMsg, null);
                        }
                    }
                } catch (InterruptedException e) {
                    logger.info("Mining daemon shut down.");
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    logger.error("Miner error", e);
                }
            });
            minerThread.start();
        }

        // --- ARRANQUE DA CLI (Substitui o CountDownLatch passivo) ---
        logger.info("Instantiating Auction User Interface Layer...");
        AuctionCLI cli = new AuctionCLI(blockchain, txPool, client, myIdentity);
        cli.start();

        // Mantém a thread principal viva para monitorar o processo
        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}