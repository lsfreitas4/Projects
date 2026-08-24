package com.auction.auction;

public class Auction {
    public enum Status {
        PENDING, ACTIVE, ENDED
    }

    private String id;
    private String title;
    private String description;
    private String ownerPublicKey;
    private Status status;
    private long highestBid;
    private String highestBidderPublicKey;

    public Auction(String id, String title, String description, String ownerPublicKey) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.ownerPublicKey = ownerPublicKey;
        this.status = Status.PENDING;
        this.highestBid = 0;
        this.highestBidderPublicKey = null;
    }

    // Getters e Setters básicos
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getOwnerPublicKey() { return ownerPublicKey; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public long getHighestBid() { return highestBid; }
    public String getHighestBidderPublicKey() { return highestBidderPublicKey; }
    
    public void registerBid(long amount, String bidderKey) {
        this.highestBid = amount;
        this.highestBidderPublicKey = bidderKey;
    }

    @Override
    public String toString() {
        return String.format("Leilão [%s] - %s | Estado: %s | Maior Lance: %d (por %s)", 
                id, title, status, highestBid, 
                (highestBidderPublicKey != null ? highestBidderPublicKey.substring(0, 8) + "..." : "Nenhum"));
    }
}