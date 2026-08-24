package com.auction.auction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Representa os comandos possíveis da aplicação de Leilão.
 * Equivalente ao `enum AuctionCommand` no projeto em Rust.
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // Não inclui campos nulos no JSON para poupar espaço
public class AuctionCommand {

    public enum CommandType {
        CREATE, START, END, BID
    }

    private CommandType type;
    private String id;
    private String title;
    private String description;
    private Long amount; // Usamos Long para o dinheiro (equivalente ao u64 do Rust)

    private static final ObjectMapper mapper = new ObjectMapper();

    // Construtor vazio obrigatório para o Jackson (Deserialização)
    public AuctionCommand() {}

    private AuctionCommand(CommandType type, String id) {
        this.type = type;
        this.id = id;
    }

    // --- MÉTODOS DE FÁBRICA (FACTORY METHODS) ---

    public static AuctionCommand createAuction(String id, String title, String description) {
        AuctionCommand cmd = new AuctionCommand(CommandType.CREATE, id);
        cmd.title = title;
        cmd.description = description;
        return cmd;
    }

    public static AuctionCommand startAuction(String id) {
        return new AuctionCommand(CommandType.START, id);
    }

    public static AuctionCommand endAuction(String id) {
        return new AuctionCommand(CommandType.END, id);
    }

    public static AuctionCommand bid(String id, long amount) {
        AuctionCommand cmd = new AuctionCommand(CommandType.BID, id);
        cmd.amount = amount;
        return cmd;
    }

    // --- GERAÇÃO DE ID DETERMINÍSTICA (Como no Rust) ---
    
    /**
     * Gera um ID único para o leilão usando SHA-256 truncado,
     * exatamente como a função `generate_auction_id` do teu código em Rust.
     */
    public static String generateAuctionId(String publicKey, String title, String description, long nonce) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256", "BC");
            String data = publicKey + title + description + nonce;
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            // Retorna os primeiros 16 caracteres hexadecimais
            return Hex.toHexString(hash).substring(0, 16); 
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar Auction ID", e);
        }
    }

    // --- SERIALIZAÇÃO PARA O PAYLOAD DA TRANSAÇÃO ---

    /**
     * Transforma este comando numa string com o prefixo "AUCTION_" 
     * pronta para ser inserida no payload da Transaction.
     */
    public String toPayload() {
        try {
            return "AUCTION_" + mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Falha ao serializar AuctionCommand", e);
        }
    }

    /**
     * Tenta ler o payload de uma transação. Devolve null se não for um comando de leilão válido.
     */
    public static AuctionCommand fromPayload(String payload) {
        if (payload == null || !payload.startsWith("AUCTION_")) {
            return null;
        }
        try {
            String json = payload.substring(8); // Remove o "AUCTION_" (8 caracteres)
            return mapper.readValue(json, AuctionCommand.class);
        } catch (Exception e) {
            return null; // Ignora payloads mal formados
        }
    }

    // --- GETTERS E SETTERS (Obrigatórios para o Jackson) ---
    public CommandType getType() { return type; }
    public void setType(CommandType type) { this.type = type; }
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
}