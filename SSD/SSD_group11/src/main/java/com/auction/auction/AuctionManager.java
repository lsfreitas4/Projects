package com.auction.auction;

import dev.ledger.core.Block;
import dev.ledger.core.BlockchainContext;
import dev.ledger.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Motor de estado dos leilões.
 * Reconstrói o estado atual lendo as transações consolidadas no Ledger.
 * Baseado na arquitetura do ficheiro `auction.rs` do projeto de referência.
 */
public class AuctionManager {
    private static final Logger logger = LoggerFactory.getLogger(AuctionManager.class);
    private final BlockchainContext blockchain;
    
    // O estado em memória de todos os leilões
    private final Map<String, Auction> activeAuctions = new HashMap<>();

    public AuctionManager(BlockchainContext blockchain) {
        this.blockchain = blockchain;
    }

    /**
     * Varrer a Blockchain desde o bloco Génese para reconstruir o estado de forma imutável.
     */
    public void refreshState() {
        activeAuctions.clear(); // Limpa o estado em memória
        
        for (Block block : blockchain.getChain()) {
            for (Transaction tx : block.getTransactions()) {
                
                // Ignorar transações que não são da aplicação de leilão
                AuctionCommand cmd = AuctionCommand.fromPayload(tx.getPayload());
                if (cmd == null) continue;

                processCommand(cmd, tx.getSenderPublicKey());
            }
        }
        logger.info("Estado dos Leilões sincronizado. {} leilões encontrados.", activeAuctions.size());
    }

    /**
     * Aplica a lógica de negócio de cada comando lido da Blockchain.
     */
    private void processCommand(AuctionCommand cmd, String senderKey) {
        String auctionId = cmd.getId();

        switch (cmd.getType()) {
            case CREATE:
                if (!activeAuctions.containsKey(auctionId)) {
                    Auction newAuction = new Auction(auctionId, cmd.getTitle(), cmd.getDescription(), senderKey);
                    activeAuctions.put(auctionId, newAuction);
                }
                break;

            case START:
                Auction auctionToStart = activeAuctions.get(auctionId);
                // Só o dono pode iniciar o leilão
                if (auctionToStart != null && auctionToStart.getOwnerPublicKey().equals(senderKey)) {
                    auctionToStart.setStatus(Auction.Status.ACTIVE);
                }
                break;

            case END:
                Auction auctionToEnd = activeAuctions.get(auctionId);
                // Só o dono pode terminar o leilão
                if (auctionToEnd != null && auctionToEnd.getOwnerPublicKey().equals(senderKey)) {
                    auctionToEnd.setStatus(Auction.Status.ENDED);
                }
                break;

            case BID:
                Auction auctionToBid = activeAuctions.get(auctionId);
                
                if (auctionToBid == null) break;
                
                // Regras de validação do Lance (Bid) - Semelhante ao auction.rs:
                // 1. Tem de estar ativo
                // 2. O dono não pode licitar no próprio leilão
                // 3. O valor tem de ser maior que o lance atual
                boolean isActive = auctionToBid.getStatus() == Auction.Status.ACTIVE;
                boolean isNotOwner = !auctionToBid.getOwnerPublicKey().equals(senderKey);
                boolean isHigherBid = cmd.getAmount() > auctionToBid.getHighestBid();

                if (isActive && isNotOwner && isHigherBid) {
                    auctionToBid.registerBid(cmd.getAmount(), senderKey);
                } else {
                    logger.debug("Lance rejeitado durante reconstrução de estado. Leilão: {}, Valor: {}", auctionId, cmd.getAmount());
                }
                break;
        }
    }

    public Collection<Auction> getAllAuctions() {
        return activeAuctions.values();
    }
}