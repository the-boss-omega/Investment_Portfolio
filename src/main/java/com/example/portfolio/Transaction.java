package com.example.portfolio;

import java.time.LocalDateTime;

public class Transaction {
    private final int id;
    private final int userId;
    private final String accountProvider;
    private final String symbol;
    private final String assetType;
    private final String action;
    private final int quantity;
    private final float price;
    private final LocalDateTime timestamp;

    public Transaction(int id, int userId, String accountProvider, String symbol,
                       String assetType, String action, int quantity, float price, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.accountProvider = accountProvider;
        this.symbol = symbol;
        this.assetType = assetType;
        this.action = action;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getAccountProvider() { return accountProvider; }
    public String getSymbol() { return symbol; }
    public String getAssetType() { return assetType; }
    public String getAction() { return action; }
    public int getQuantity() { return quantity; }
    public float getPrice() { return price; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
