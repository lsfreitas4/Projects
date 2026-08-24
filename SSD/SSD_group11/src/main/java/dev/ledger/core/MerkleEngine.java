package dev.ledger.core;

import org.bouncycastle.util.encoders.Hex;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class MerkleEngine {
    
    public static String getMerkleRoot(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return Hex.toHexString(new byte[32]); // Sane default fallback
        }
        
        List<String> treeLayer = new ArrayList<>();
        for (Transaction tx : transactions) {
            treeLayer.add(tx.getTransactionId());
        }
        
        while (treeLayer.size() > 1) {
            if (treeLayer.size() % 2 != 0) {
                treeLayer.add(treeLayer.get(treeLayer.size() - 1));
            }
            List<String> newLayer = new ArrayList<>();
            for (int i = 0; i < treeLayer.size(); i += 2) {
                newLayer.add(hashStrings(treeLayer.get(i), treeLayer.get(i + 1)));
            }
            treeLayer = newLayer;
        }
        return treeLayer.get(0);
    }

    private static String hashStrings(String left, String right) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256", "BC");
            byte[] hash = digest.digest((left + right).getBytes(StandardCharsets.UTF_8));
            return Hex.toHexString(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}