package com.auction.ledger.network.kademlia;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

public class KBucket {
    private final LinkedList<NodeContact> nodes = new LinkedList<>();
    private static final int K = 20;

    public boolean contains(NodeContact node) {
        return nodes.stream().anyMatch(n -> n.getId().toHexString().equals(node.getId().toHexString()));
    }

    public void moveToEnd(NodeContact node) {
        nodes.removeIf(n -> n.getId().toHexString().equals(node.getId().toHexString()));
        nodes.addLast(node);
    }

    public boolean isFull() {
        return nodes.size() >= K;
    }

    public NodeContact getLru() {
        return nodes.isEmpty() ? null : nodes.getFirst();
    }

    public void remove(NodeContact node) {
        nodes.removeIf(n -> n.getId().toHexString().equals(node.getId().toHexString()));
    }

    public void add(NodeContact node) {
        nodes.addLast(node);
    }

    public List<NodeContact> getNodes() {
        return new ArrayList<>(nodes);
    }
}
