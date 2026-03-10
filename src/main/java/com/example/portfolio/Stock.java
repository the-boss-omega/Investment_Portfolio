package com.example.portfolio;

public final class Stock extends FinancialAsset {
    public Stock(String symbol, float currentPrice) {
        super(symbol, currentPrice);
        try {
            if (symbol == null || symbol.isBlank()) {
                Logger.log(LogLevel.WARNING, "Stock created with null/blank symbol");
            }
        } catch (Exception e) {
            Logger.log(LogLevel.CRITICAL, "Failed to create Stock");
            throw e;
        }
    }
}