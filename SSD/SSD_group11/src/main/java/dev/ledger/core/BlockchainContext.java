package dev.ledger.core;

import dev.ledger.consensus.IConsensusEngine;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlockchainContext {
    private final List<Block> chain = new ArrayList<>();
    private final Map<String, Transaction> globalTransactionPool = new ConcurrentHashMap<>();
    private final Map<String, Long> accountNonces = new ConcurrentHashMap<>();
    private final IConsensusEngine consensusEngine;
    private final int difficulty;

    public BlockchainContext(IConsensusEngine consensusEngine, int difficulty) {
        this.consensusEngine = consensusEngine;
        this.difficulty = difficulty;
        createGenesisBlock();
    }

    private void createGenesisBlock() {
        List<Transaction> genesisTx = new ArrayList<>();
        Transaction systemTx = new Transaction("SYSTEM_ORCHESTRATOR", "GENESIS_LEDGER_INIT", 0);
        systemTx.setTransactionId("0000000000000000000000000000000000000000000000000000000000000000");
        systemTx.setSignature("00000000");
        genesisTx.add(systemTx);

        Block genesis = new Block("0", genesisTx);
        
        // --- CORREÇÃO: Forçar Timestamp e Nonce fixos para o Bloco Génese ---
        // Assim todos os nós do mundo gerarão exatamente a mesma Hash Inicial!
        genesis.setTimestamp(1700000000000L); 
        genesis.setNonce(0);
        // -------------------------------------------------------------------
        
        consensusEngine.sealBlock(genesis);
        chain.add(genesis);
    }

    public synchronized boolean addBlock(Block newBlock) {
        Block latestBlock = getLatestBlock();
        
        // Structure Validation Checks
        if (!newBlock.getPreviousHash().equals(latestBlock.getHash())) {
            return false; // Rejects alternative or outdated chain variations
        }
        if (!consensusEngine.validateConsensus(newBlock)) {
            return false; // Faulty consensus validation proof
        }
        if (!newBlock.getMerkleRoot().equals(MerkleEngine.getMerkleRoot(newBlock.getTransactions()))) {
            return false; // Root mismatch indicates modified transactions
        }

        // Validate Transaction Sequence & Nonces
        for (Transaction tx : newBlock.getTransactions()) {
            if (!tx.verifySignature() && !tx.getSenderPublicKey().equals("SYSTEM_ORCHESTRATOR")) {
                return false; // Rejects unauthenticated state changes
            }
            // Enforce Strict Incremental Nonces to prevent Replay Attacks
            long expectedNonce = accountNonces.getOrDefault(tx.getSenderPublicKey(), 0L);
            if (tx.getNonce() != expectedNonce) {
                return false; 
            }
        }

        // Update State
        for (Transaction tx : newBlock.getTransactions()) {
            globalTransactionPool.put(tx.getTransactionId(), tx);
            accountNonces.put(tx.getSenderPublicKey(), tx.getNonce() + 1);
        }

        chain.add(newBlock);
        return true;
    }

    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    public List<Block> getChain() { return Collections.unmodifiableList(chain); }
    public boolean isTransactionProcessed(String txId) { return globalTransactionPool.containsKey(txId); }
    public long getNextExpectedNonce(String pubKey) { return accountNonces.getOrDefault(pubKey, 0L); }
}