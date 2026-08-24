package com.auction.ledger.network.kademlia;

import java.util.Objects;

public class NodeContact {
    private final KademliaId id;
    private final String ip;
    private final int port;

    public NodeContact(KademliaId id, String ip, int port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
    }

    public KademliaId getId() { return id; }
    public String getIp() { return ip; }
    public int getPort() { return port; }

    // Dois contactos são iguais se o ID for igual (mitiga nós mudarem de IP)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeContact that = (NodeContact) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "NodeContact{" +
                "id=" + (id != null ? id.toHexString().substring(0, 8) + "..." : "null") +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
