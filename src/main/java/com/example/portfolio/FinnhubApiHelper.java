package com.example.portfolio;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

public final class FinnhubApiHelper {
    private static final String API_KEY = "d6h1h21r01qnjncn0i3gd6h1h21r01qnjncn0i40";

    private String sendGet(String url) {
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
                Logger.log(LogLevel.WARNING, "Finnhub GET failed. Status=" + response.statusCode() + " url=" + url);
            } else if (response.body() == null || response.body().isBlank()) {
                Logger.log(LogLevel.WARNING, "Finnhub GET returned empty body. url=" + url);
            } else {
                Logger.log(LogLevel.INFO, "Finnhub GET ok. url=" + url);
            }

            return response.body();
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "Finnhub GET exception. url=" + url + " err=" + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    public String getQuote(String symbol) {
        try {
            String url = String.format("https://finnhub.io/api/v1/quote?symbol=%s&token=%s", symbol, API_KEY);
            return sendGet(url);
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getQuote failed for symbol=" + symbol + " err=" + e.getMessage());
            throw e;
        }
    }

    public String searchSymbol(String query) {
        try {
            String url = String.format("https://finnhub.io/api/v1/search?q=%s&token=%s", query, API_KEY);
            return sendGet(url);
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "searchSymbol failed for query=" + query + " err=" + e.getMessage());
            throw e;
        }
    }

    public String getStockProfile(String symbol) {
        try {
            String url = String.format("https://finnhub.io/api/v1/stock/profile2?symbol=%s&token=%s", symbol, API_KEY);
            return sendGet(url);
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getStockProfile failed for symbol=" + symbol + " err=" + e.getMessage());
            throw e;
        }
    }

    public String getCompanyNews(String symbol, LocalDate from, LocalDate to) {
        try {
            String url = String.format(
                    "https://finnhub.io/api/v1/company-news?symbol=%s&from=%s&to=%s&token=%s",
                    symbol, from, to, API_KEY
            );
            return sendGet(url);
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getCompanyNews failed for symbol=" + symbol + " from=" + from + " to=" + to + " err=" + e.getMessage());
            throw e;
        }
    }

    public String getRecommendationTrends(String symbol) {
        try {
            String url = String.format(
                    "https://finnhub.io/api/v1/stock/recommendation?symbol=%s&token=%s",
                    symbol, API_KEY
            );
            return sendGet(url);
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getRecommendationTrends failed for symbol=" + symbol + " err=" + e.getMessage());
            throw e;
        }
    }

    public String getBasicFinancials(String symbol) {
        try {
            String url = String.format(
                    "https://finnhub.io/api/v1/stock/metric?symbol=%s&metric=all&token=%s",
                    symbol, API_KEY
            );
            return sendGet(url);
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getBasicFinancials failed for symbol=" + symbol + " err=" + e.getMessage());
            throw e;
        }
    }

    public String getStockSymbols(String exchange) {
        try {
            String url = String.format(
                    "https://finnhub.io/api/v1/stock/symbol?exchange=%s&token=%s",
                    exchange, API_KEY
            );
            return sendGet(url);
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getStockSymbols failed for exchange=" + exchange + " err=" + e.getMessage());
            throw e;
        }
    }

    public String getForexSymbols(String exchange) {
        try {
            String url = String.format(
                    "https://finnhub.io/api/v1/forex/symbol?exchange=%s&token=%s",
                    exchange, API_KEY
            );
            return sendGet(url);
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getForexSymbols failed for exchange=" + exchange + " err=" + e.getMessage());
            throw e;
        }
    }

    public String getCryptoSymbols(String exchange) {
        try {
            String url = String.format(
                    "https://finnhub.io/api/v1/crypto/symbol?exchange=%s&token=%s",
                    exchange, API_KEY
            );
            return sendGet(url);
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getCryptoSymbols failed for exchange=" + exchange + " err=" + e.getMessage());
            throw e;
        }
    }

    public String getEtfProfile(String symbol) {
        try {
            String url = String.format(
                    "https://api.finnhub.io/api/v1/etf/profile?symbol=%s&token=%s",
                    symbol,
                    API_KEY
            );
            return sendGet(url);
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getEtfProfile failed for symbol=" + symbol + " err=" + e.getMessage());
            throw e;
        }
    }

    public String getStockCandles(String symbol, String resolution, long from, long to) {
        try {
            String url = String.format(
                    "https://finnhub.io/api/v1/stock/candle?symbol=%s&resolution=%s&from=%d&to=%d&token=%s",
                    symbol, resolution, from, to, API_KEY
            );
            return sendGet(url);
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getStockCandles failed for symbol=" + symbol + " res=" + resolution + " err=" + e.getMessage());
            throw e;
        }
    }

    public String getForexCandles(String symbol, String resolution, long from, long to) {
        try {
            String url = String.format(
                    "https://finnhub.io/api/v1/forex/candle?symbol=%s&resolution=%s&from=%d&to=%d&token=%s",
                    symbol, resolution, from, to, API_KEY
            );
            return sendGet(url);
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getForexCandles failed for symbol=" + symbol + " res=" + resolution + " err=" + e.getMessage());
            throw e;
        }
    }

    public String getCryptoCandles(String symbol, String resolution, long from, long to) {
        try {
            String url = String.format(
                    "https://finnhub.io/api/v1/crypto/candle?symbol=%s&resolution=%s&from=%d&to=%d&token=%s",
                    symbol, resolution, from, to, API_KEY
            );
            return sendGet(url);
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getCryptoCandles failed for symbol=" + symbol + " res=" + resolution + " err=" + e.getMessage());
            throw e;
        }
    }
}