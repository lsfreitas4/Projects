package com.auction.ledger.network.kademlia;

import java.util.Arrays;

public class KademliaId implements Comparable<KademliaId> {
    public static final int ID_LENGTH = 20; // 160 bits
    private final byte[] bytes;

    public KademliaId(byte[] bytes) {
        if (bytes.length != ID_LENGTH) throw new IllegalArgumentException("ID must be 20 bytes");
        this.bytes = bytes.clone();
    }

    // Calcula a distância XOR entre este ID e outro (equivalente ao `xor_distance` do Rust)
    public KademliaId xorDistance(KademliaId other) {
        byte[] result = new byte[ID_LENGTH];
        for (int i = 0; i < ID_LENGTH; i++) {
            result[i] = (byte) (this.bytes[i] ^ other.bytes[i]);
        }
        return new KademliaId(result);
    }

    // Identifica o índice do K-Bucket (conta os zeros à esquerda do resultado XOR)
    public int getBucketIndex(KademliaId other) {
        KademliaId distance = this.xorDistance(other);
        for (int i = 0; i < ID_LENGTH; i++) {
            if (distance.bytes[i] != 0) {
                int leadingZeros = Integer.numberOfLeadingZeros(distance.bytes[i] & 0xFF) - 24;
                return (i * 8) + leadingZeros;
            }
        }
        return 159; // Se forem exatamente iguais
    }

    public byte[] getBytes() { return bytes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Arrays.equals(bytes, ((KademliaId) o).bytes);
    }

    @Override
    public int compareTo(KademliaId o) {
        return Arrays.compare(this.bytes, o.bytes);
    }

    public String toHexString() {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public int firstSetBit() {
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] != 0) {
                // Calcula o bit mais à esquerda que está a 1
                return (i * 8) + Integer.numberOfLeadingZeros(bytes[i] & 0xFF) - 24;
            }
        }
        return 160; // Se for o próprio ID
    }
}
