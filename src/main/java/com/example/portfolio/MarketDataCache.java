package com.example.portfolio;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MarketDataCache {
    private static final long CACHE_TTL_MS = 15_000;
    private final FinnhubApiHelper api = new FinnhubApiHelper();
    private final Map<String, CachedEntry> cache = new ConcurrentHashMap<>();

    private record CachedEntry(String data, long timestamp) {
        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }
    }

    public String getQuote(String symbol) {
        return getCached("quote:" + symbol, () -> api.getQuote(symbol));
    }

    public String searchSymbol(String query) {
        return getCached("search:" + query, () -> api.searchSymbol(query));
    }

    public String getStockCandles(String symbol, String resolution, long from, long to) {
        String key = "candle:stock:" + symbol + ":" + resolution + ":" + from + ":" + to;
        return getCached(key, () -> api.getStockCandles(symbol, resolution, from, to));
    }

    public String getForexCandles(String symbol, String resolution, long from, long to) {
        String key = "candle:forex:" + symbol + ":" + resolution + ":" + from + ":" + to;
        return getCached(key, () -> api.getForexCandles(symbol, resolution, from, to));
    }

    public String getCryptoCandles(String symbol, String resolution, long from, long to) {
        String key = "candle:crypto:" + symbol + ":" + resolution + ":" + from + ":" + to;
        return getCached(key, () -> api.getCryptoCandles(symbol, resolution, from, to));
    }

    private String getCached(String key, java.util.function.Supplier<String> fetcher) {
        CachedEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            return entry.data();
        }
        String data = fetcher.get();
        cache.put(key, new CachedEntry(data, System.currentTimeMillis()));
        return data;
    }
}
