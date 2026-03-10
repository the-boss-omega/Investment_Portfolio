package com.example.portfolio;

public final class Crypto extends FinancialAsset {
    private final String exchange;

    public Crypto(String symbol, float currentPrice, String exchange) {
        super(symbol, currentPrice);
        try {
            if (exchange == null || exchange.isBlank()) {
                Logger.log(LogLevel.WARNING, "Crypto created with null/blank exchange for symbol=" + symbol);
            }
            this.exchange = exchange;
        } catch (Exception e) {
            Logger.log(LogLevel.CRITICAL, "Failed to create Crypto for symbol=" + symbol);
            throw e;
        }
    }

    public String getExchange() {
        try {
            return exchange;
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getExchange failed for Crypto symbol=" + getSymbol());
            throw e;
        }
    }
}