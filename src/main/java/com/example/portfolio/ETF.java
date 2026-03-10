package com.example.portfolio;

public final class ETF extends FinancialAsset {
    public ETF(String symbol, float currentPrice) {
        super(symbol, currentPrice);
        try {
            if (symbol == null || symbol.isBlank()) {
                Logger.log(LogLevel.WARNING, "ETF created with null/blank symbol");
            }
        } catch (Exception e) {
            Logger.log(LogLevel.CRITICAL, "Failed to create ETF");
            throw e;
        }
    }
}