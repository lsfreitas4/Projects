package com.auction.ledger.network.kademlia;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Representa a Tabela de Roteamento Kademlia (XOR Distance).
 * Implementa lógica de gestão de KBuckets com política de atualização resiliente.
 */
public class RoutingTable {
    private final KademliaId localId;
    private final List<KBucket> buckets;

    public RoutingTable(KademliaId localId) {
        this.localId = localId;
        this.buckets = new ArrayList<>();
        // Kademlia padrão usa 160 bits para IDs
        for (int i = 0; i < 160; i++) {
            buckets.add(new KBucket());
        }
    }

    /**
     * Adiciona um contacto à tabela seguindo a regra:
     * 1. Se já existe, move para o fim (marca como mais recente/ativo).
     * 2. Se está cheio, tenta verificar saúde antes de remover (LRU).
     */
    public void addContact(NodeContact node) {
        if (node.getId().toHexString().equals(localId.toHexString())) return;
        
        int index = getBucketIndex(node.getId());
        KBucket bucket = buckets.get(index);

        if (bucket.contains(node)) {
            bucket.moveToEnd(node);
        } else if (bucket.isFull()) {
            // Lógica de resiliência: verificar se o nó LRU ainda responde
            NodeContact lru = bucket.getLru();
            if (pingNode(lru)) {
                // Se o nó respondeu, não o removemos. 
                // O novo nó é ignorado para preservar a integridade da tabela.
                return; 
            } else {
                // Se o nó não respondeu (fantasma), removemos e adicionamos o novo
                bucket.remove(lru);
                bucket.add(node);
            }
        } else {
            bucket.add(node);
        }
    }

    /**
     * Encontra os K contactos mais próximos de um ID alvo baseando-se na métrica XOR.
     */
    public List<NodeContact> findClosestNodes(KademliaId targetId, int k) {
        List<NodeContact> allKnownNodes = buckets.stream()
                .flatMap(b -> b.getNodes().stream())
                .collect(Collectors.toList());

        // Ordenação por proximidade matemática XOR
        allKnownNodes.sort((n1, n2) -> {
            KademliaId d1 = n1.getId().xorDistance(targetId);
            KademliaId d2 = n2.getId().xorDistance(targetId);
            return d1.compareTo(d2);
        });

        return allKnownNodes.stream().limit(k).collect(Collectors.toList());
    }

    private int getBucketIndex(KademliaId nodeId) {
        KademliaId dist = nodeId.xorDistance(localId);
        // O índice é determinado pelo prefixo comum (primeiro bit a 1)
        return Math.min(dist.firstSetBit(), 159);
    }

    private boolean pingNode(NodeContact node) {
        // Método que interage com o P2PClient para verificar se o nó ainda vive
        // Em produção, esta função chamaria o RPC de ping do Kademlia
        return true; 
    }
}
