package dev.ledger.core;

import org.bouncycastle.util.encoders.Hex;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class Block {
    private String hash;
    private String previousHash;
    private String merkleRoot;
    private List<Transaction> transactions;
    private long timestamp;
    private long nonce;

    public Block() {
        this.transactions = new ArrayList<>();
    }

    public Block(String previousHash, List<Transaction> transactions) {
        this.previousHash = previousHash;
        this.transactions = transactions;
        this.timestamp = System.currentTimeMillis();
        this.merkleRoot = MerkleEngine.getMerkleRoot(transactions);
        this.hash = calculateHash();
    }

    public String calculateHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256", "BC");
            String data = previousHash + merkleRoot + timestamp + nonce;
            byte[] rawHash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Hex.toHexString(rawHash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void mineBlock(int difficulty) {
        this.merkleRoot = MerkleEngine.getMerkleRoot(transactions);
        String target = new String(new char[difficulty]).replace('\0', '0');
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
    }

    // Getters and Setters
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
    public String getPreviousHash() { return previousHash; }
    public void setPreviousHash(String previousHash) { this.previousHash = previousHash; }
    public String getMerkleRoot() { return merkleRoot; }
    public void setMerkleRoot(String merkleRoot) { this.merkleRoot = merkleRoot; }
    public List<Transaction> getTransactions() { return transactions; }
    public void setTransactions(List<Transaction> txs) { this.transactions = txs; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long ts) { this.timestamp = ts; }
    public long getNonce() { return nonce; }
    public void setNonce(long n) { this.nonce = n; }
}