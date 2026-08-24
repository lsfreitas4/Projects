package com.auction.ledger.network;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Message {
    private static final ObjectMapper mapper = new ObjectMapper();

    public enum MessageType {
        // RPCs Kademlia (Parte A - O Novo Handshake e Routing)
        PING_REQ, PING_RES,
        STORE_REQ, STORE_RES,
        FIND_NODE_REQ, FIND_NODE_RES,
        FIND_VALUE_REQ, FIND_VALUE_RES,
        JOIN_REQ, JOIN_RES,
        
        // Mensagens de Gossip / Ledger (Parte B)
        NEW_TRANSACTION,
        NEW_BLOCK,
        CHAIN_REQUEST,
        CHAIN_RESPONSE
    }

    @JsonProperty("type")
    private MessageType type;

    @JsonProperty("payload")
    private String payload;

    public Message() {}

    public Message(MessageType type, String payload) {
        this.type = type;
        this.payload = payload;
    }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    // Utility to convert this message to a JSON string with a newline delimiter for Netty
    public String toJsonLine() {
        try {
            return mapper.writeValueAsString(this) + "\n";
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize message", e);
        }
    }

    public static Message fromJson(String json) {
        try {
            return mapper.readValue(json, Message.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize message", e);
        }
    }
}