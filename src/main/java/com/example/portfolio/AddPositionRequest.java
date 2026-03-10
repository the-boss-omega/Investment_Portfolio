package com.example.portfolio;

public record AddPositionRequest(
        String provider,
        String symbol,
        String assetType,
        int quantity,
        float originalPrice,
        float currentPrice,
        String exchange
) {
}

