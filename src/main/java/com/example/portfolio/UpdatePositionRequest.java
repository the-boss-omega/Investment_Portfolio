package com.example.portfolio;

public record UpdatePositionRequest(
        String provider,
        String symbol,
        String assetType,
        int quantity,
        float originalPrice,
        float currentPrice,
        String exchange
) {
}

