package dev.ledger.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;

public class Transaction {
    private String transactionId;
    private String senderPublicKey; // Hex encoded
    private String payload;         // Auction application specifics (Create, Bid)
    private long nonce;             // Anti-replay sequence counter
    private String signature;       // Hex encoded signature

    private static final ObjectMapper mapper = new ObjectMapper();

    public Transaction() {}

    public Transaction(String senderPublicKey, String payload, long nonce) {
        this.senderPublicKey = senderPublicKey;
        this.payload = payload;
        this.nonce = nonce;
        this.transactionId = calculateHash();
    }

    public String calculateHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256", "BC");
            String dataToHash = senderPublicKey + payload + nonce;
            byte[] hash = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
            return Hex.toHexString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Cryptographic isolation failure", e);
        }
    }

    public void signTransaction(PrivateKey privateKey) throws Exception {
        Signature dsa = Signature.getInstance("ECDSA", "BC");
        dsa.initSign(privateKey);
        dsa.update(transactionId.getBytes(StandardCharsets.UTF_8));
        byte[] realSignature = dsa.sign();
        this.signature = Hex.toHexString(realSignature);
    }

    public boolean verifySignature() {
        try {
            Signature dsa = Signature.getInstance("ECDSA", "BC");
            byte[] keyBytes = Hex.decode(senderPublicKey);
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
            
            dsa.initVerify(pubKey);
            dsa.update(transactionId.getBytes(StandardCharsets.UTF_8));
            return dsa.verify(Hex.decode(signature));
        } catch (Exception e) {
            return false;
        }
    }

    // Getters and Setters for Jackson Serialization
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getSenderPublicKey() { return senderPublicKey; }
    public void setSenderPublicKey(String senderPublicKey) { this.senderPublicKey = senderPublicKey; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public long getNonce() { return nonce; }
    public void setNonce(long nonce) { this.nonce = nonce; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}