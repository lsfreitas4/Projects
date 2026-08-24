package com.auction.auction;

import dev.ledger.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TransactionPool {
    
    // O LOGGER VAI AQUI (Como um atributo da classe)
    private static final Logger logger = LoggerFactory.getLogger(TransactionPool.class);
    
    private final Map<String, Transaction> pool = new ConcurrentHashMap<>();
    private final Map<String, Long> accountNonces = new ConcurrentHashMap<>();

    public TransactionPool() {}

    public synchronized boolean addTransaction(Transaction tx) {
        // Agora podes usar o "logger" aqui dentro à vontade
        logger.info("A tentar adicionar TX: {} | Nonce: {} | Sender: {}", 
                     tx.getTransactionId(), tx.getNonce(), tx.getSenderPublicKey());

        if (!tx.verifySignature() && !tx.getSenderPublicKey().equals("SYSTEM_ORCHESTRATOR")) {
            logger.warn("REJEITADO: Assinatura inválida!");
            return false;
        }

        long expectedNonce = accountNonces.getOrDefault(tx.getSenderPublicKey(), 0L);
        if (tx.getNonce() < expectedNonce) {
            logger.warn("REJEITADO: Nonce inválido! Esperado {}, recebido {}", expectedNonce, tx.getNonce());
            return false; 
        }

        pool.put(tx.getTransactionId(), tx);
        logger.info("ACEITE: Transação na pool.");
        return true;
    }

    /**
     * Remove as transações da pool e atualiza o estado dos nonces após mineração.
     */
    public synchronized void processBlock(List<Transaction> minedTxs) {
        for (Transaction tx : minedTxs) {
            pool.remove(tx.getTransactionId());
            // Atualiza o nonce esperado para este sender ser (nonce atual + 1)
            accountNonces.put(tx.getSenderPublicKey(), tx.getNonce() + 1);
        }
    }

    /**
     * Recupera as transações mais prioritárias para inclusão no próximo bloco.
     * Ordena por nonce (garantindo sequência) e limite de tamanho.
     */
    public synchronized List<Transaction> getTransactionsForBlock(int limit) {
        return pool.values().stream()
                .sorted(Comparator.comparingLong(Transaction::getNonce))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public synchronized void clear() {
        pool.clear();
    }

    public int size() {
        return pool.size();
    }
    
    public Map<String, Transaction> getPool() {
        return Collections.unmodifiableMap(pool);
    }
}