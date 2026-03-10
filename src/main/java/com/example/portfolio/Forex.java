package com.example.portfolio;

public final class Forex extends FinancialAsset {
    private final String exchange;

    public Forex(String symbol, float currentPrice, String exchange) {
        super(symbol, currentPrice);
        try {
            if (exchange == null || exchange.isBlank()) {
                Logger.log(LogLevel.WARNING, "Forex created with null/blank exchange for symbol=" + symbol);
            }
            this.exchange = exchange;
        } catch (Exception e) {
            Logger.log(LogLevel.CRITICAL, "Failed to create Forex for symbol=" + symbol);
            throw e;
        }
    }

    public String getExchange() {
        try {
            return exchange;
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getExchange failed for Forex symbol=" + getSymbol());
            throw e;
        }
    }
}