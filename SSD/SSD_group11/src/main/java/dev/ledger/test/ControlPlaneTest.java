package dev.ledger.test;

import dev.ledger.consensus.ProofOfWorkEngine;
import dev.ledger.core.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import com.auction.ledger.network.kademlia.KademliaId;
import com.auction.ledger.network.kademlia.NodeContact;
import com.auction.ledger.network.kademlia.NodeIdentity;
import com.auction.ledger.network.kademlia.RoutingTable;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Collections;

public class ControlPlaneTest {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== STARTING LEDGER SECURITY TESTING MATRICES ===");
        
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
        keyGen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
        KeyPair userA = keyGen.generateKeyPair();
        String pubKeyA = Hex.toHexString(userA.getPublic().getEncoded());

        ProofOfWorkEngine pow = new ProofOfWorkEngine(3);
        BlockchainContext ledger = new BlockchainContext(pow, 3);

        // Test 1: Validate Processing of an Authentic Transaction (COLEGA - MANTIDO INTACTO)
        System.out.print("Test 1: Legitimate Transaction Ingestion... ");
        Transaction tx1 = new Transaction(pubKeyA, "AUCTION_BID:ITEM_ID_7:450_USD", 0);
        tx1.signTransaction(userA.getPrivate());
        
        Block block1 = new Block(ledger.getLatestBlock().getHash(), Collections.singletonList(tx1));
        pow.sealBlock(block1);
        
        if (ledger.addBlock(block1)) {
            System.out.println("PASSED");
        } else {
            System.out.println("FAILED");
        }

        // Test 2: Detect and Reject a Replay Attack Event (COLEGA - MANTIDO INTACTO)
        System.out.print("Test 2: Anti-Replay Mitigation Check... ");
        Transaction txReplay = new Transaction(pubKeyA, "AUCTION_BID:ITEM_ID_7:450_USD", 0); // Reused nonce
        txReplay.signTransaction(userA.getPrivate());
        
        Block block2 = new Block(ledger.getLatestBlock().getHash(), Collections.singletonList(txReplay));
        pow.sealBlock(block2);
        
        if (!ledger.addBlock(block2)) {
            System.out.println("PASSED (Replay Block Successfully Blocked)");
        } else {
            System.out.println("FAILED (Exploit Vulnerability: Replay Accepted)");
        }

        System.out.println("\n=== STARTING KADEMLIA P2P TESTING MATRICES (PART A) ===");

        // Test 3: Kademlia Anti-Sybil Identity (PoW)
        System.out.print("Test 3: Anti-Sybil Identity Generation (PoW)... ");
        try {
            int difficultyBits = 8; // Exigimos 8 bits (1 byte inteiro de zeros)
            NodeIdentity identity = NodeIdentity.generateWithPoW(difficultyBits);
            byte firstByte = identity.getNodeId().getBytes()[0];
            
            if (firstByte == 0) {
                System.out.println("PASSED (Identity Hash starts with required leading zeros)");
            } else {
                System.out.println("FAILED");
            }
        } catch (Exception e) {
            System.out.println("FAILED (" + e.getMessage() + ")");
        }

        // Test 4: Kademlia XOR Distance Routing Sort
        System.out.print("Test 4: Kademlia XOR Distance Routing Sort... ");
        try {
            byte[] localBytes = new byte[20]; // ID Local é tudo zeros
            RoutingTable table = new RoutingTable(new KademliaId(localBytes));

            byte[] targetBytes = new byte[20]; targetBytes[19] = 1; // ID que vamos procurar
            KademliaId targetId = new KademliaId(targetBytes);

            byte[] farBytes = new byte[20]; farBytes[0] = (byte) 255; // Nó muito distante
            byte[] closeBytes = new byte[20]; closeBytes[19] = 2; // Nó muito próximo

            table.addContact(new NodeContact(new KademliaId(farBytes), "127.0.0.1", 8000));
            table.addContact(new NodeContact(new KademliaId(closeBytes), "127.0.0.1", 8001));

            // Pedir os nós mais próximos. O nó da porta 8001 deve vir primeiro!
            java.util.List<NodeContact> closest = table.findClosestNodes(targetId, 2);
            
            if (closest.get(0).getPort() == 8001) {
                System.out.println("PASSED (Closest node sorted correctly via XOR Distance)");
            } else {
                System.out.println("FAILED (Wrong sort order)");
            }
        } catch (Exception e) {
            System.out.println("FAILED (" + e.getMessage() + ")");
        }
        
        System.out.println("=== SYSTEM VERIFICATION COMPLETED ===");
    }
}
