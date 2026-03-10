package com.example.portfolio;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class FinancialAsset {
    protected static final String API_KEY = "d6h1h21r01qnjncn0i3gd6h1h21r01qnjncn0i40";

    private final String symbol;
    private float currentPrice;

    protected FinancialAsset(String symbol, float currentPrice) {
        if (symbol == null || symbol.isBlank()) {
            Logger.log(LogLevel.ERROR, "FinancialAsset constructor: symbol is null/blank");
            throw new IllegalArgumentException("symbol must not be blank");
        }
        this.symbol = symbol.trim().toUpperCase();
        this.currentPrice = currentPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public float getCurrentPrice() {
        return currentPrice;
    }

    protected void setCurrentPrice(float currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void refreshPrice() {
        String url = String.format(
                "https://finnhub.io/api/v1/quote?symbol=%s&token=%s",
                symbol,
                API_KEY
        );

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status code: " + response.statusCode());
            System.out.println("Response body: " + response.body());

            if (response.statusCode() != 200) {
                Logger.log(LogLevel.WARNING, "Failed to fetch price for " + symbol + ". Status code: " + response.statusCode());
                return;
            }

            String body = response.body();
            if (body == null || body.isBlank()) {
                Logger.log(LogLevel.WARNING, "Failed to fetch price for " + symbol + ". Response body is empty.");
                return;
            }

            int cIndex = body.indexOf("\"c\":");
            if (cIndex == -1) {
                Logger.log(LogLevel.WARNING, "Failed to fetch price for " + symbol + ". Current price not found in response.");
                return;
            }

            String afterC = body.substring(cIndex + 4).trim();
            int commaIndex = afterC.indexOf(",");
            if (commaIndex == -1) {
                Logger.log(LogLevel.WARNING, "Failed to fetch price for " + symbol + ". Current price format is unexpected.");
                return;
            }

            String currentPriceText = afterC.substring(0, commaIndex).trim();
            if (currentPriceText.isEmpty() || currentPriceText.equals("null")) {
                Logger.log(LogLevel.WARNING, "Failed to fetch price for " + symbol + ". Current price is empty or null.");
                return;
            }

            float parsed = Float.parseFloat(currentPriceText);
            if (!Float.isFinite(parsed) || parsed < 0f) {
                Logger.log(LogLevel.WARNING, "Failed to fetch price for " + symbol + ". Parsed price is invalid: " + parsed);
                return;
            }

            currentPrice = parsed;
            Logger.log(LogLevel.INFO, "Updated price for " + symbol + " to " + currentPrice);

        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "Exception while refreshing price for " + symbol + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}