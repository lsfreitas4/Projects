package com.auction.ledger.network.kademlia;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.security.*;

public class NodeIdentity {
    private static final Logger logger = LoggerFactory.getLogger(NodeIdentity.class);

    private final KeyPair keyPair;
    private final KademliaId nodeId;
    private final long powNonce;

    private NodeIdentity(KeyPair keyPair, KademliaId nodeId, long powNonce) {
        this.keyPair = keyPair;
        this.nodeId = nodeId;
        this.powNonce = powNonce;
    }

    public long getNonce() {
        return powNonce;
    }

    /**
     * Gera uma nova identidade criptográfica resolvendo o puzzle Proof-of-Work
     * para mitigar ataques Sybil.
     */
    public static NodeIdentity generateWithPoW(int difficultyZeros) {
        try {
            logger.info("A gerar par de chaves ECDSA para a identidade do nó...");
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            keyGen.initialize(new java.security.spec.ECGenParameterSpec("secp256r1"), new java.security.SecureRandom());
            KeyPair pair = keyGen.generateKeyPair();
            byte[] pubKeyBytes = pair.getPublic().getEncoded();

            logger.info("A resolver puzzle PoW para o Node ID (Dificuldade: {} bits)...", difficultyZeros);
            MessageDigest digest = MessageDigest.getInstance("SHA-256", "BC");
            
            long nonce = 0;
            byte[] hash;
            
            long startTime = System.currentTimeMillis();
            while (true) {
                digest.reset();
                digest.update(pubKeyBytes);
                
                // Anexar o nonce ao hash
                ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
                buffer.putLong(nonce);
                digest.update(buffer.array());
                
                hash = digest.digest();
                
                if (checkLeadingZeros(hash, difficultyZeros)) {
                    break;
                }
                nonce++;
            }
            long duration = System.currentTimeMillis() - startTime;
            
            // Os primeiros 20 bytes do hash SHA-256 formam o nosso ID Kademlia de 160 bits
            byte[] idBytes = new byte[KademliaId.ID_LENGTH];
            System.arraycopy(hash, 0, idBytes, 0, KademliaId.ID_LENGTH);
            KademliaId generatedId = new KademliaId(idBytes);
            
            logger.info("Identidade gerada com sucesso em {} ms!", duration);
            logger.info("Nonce encontrado: {}", nonce);
            
            return new NodeIdentity(pair, generatedId, nonce);

        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar identidade do nó", e);
        }
    }

    private static boolean checkLeadingZeros(byte[] hash, int difficultyBits) {
        int bytesToCheck = difficultyBits / 8;
        int remainingBits = difficultyBits % 8;

        for (int i = 0; i < bytesToCheck; i++) {
            if (hash[i] != 0) return false;
        }

        if (remainingBits > 0) {
            byte mask = (byte) (0xFF << (8 - remainingBits));
            if ((hash[bytesToCheck] & mask) != 0) return false;
        }
        return true;
    }

    public KeyPair getKeyPair() { return keyPair; }
    public KademliaId getNodeId() { return nodeId; }
    public long getPowNonce() { return powNonce; }
}
