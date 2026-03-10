package com.example.portfolio;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/market")
public class MarketDataController {

    private final MarketDataCache cache;

    public MarketDataController(MarketDataCache cache) {
        this.cache = cache;
    }

    @GetMapping("/quote")
    public String getQuote(@RequestParam String symbol) {
        return cache.getQuote(symbol);
    }

    @GetMapping("/search")
    public String searchSymbol(@RequestParam String q) {
        return cache.searchSymbol(q);
    }

    @GetMapping("/candles")
    public String getCandles(@RequestParam String symbol,
                             @RequestParam(defaultValue = "D") String resolution,
                             @RequestParam long from,
                             @RequestParam long to,
                             @RequestParam(defaultValue = "stock") String type) {
        return switch (type.toLowerCase()) {
            case "forex" -> cache.getForexCandles(symbol, resolution, from, to);
            case "crypto" -> cache.getCryptoCandles(symbol, resolution, from, to);
            default -> cache.getStockCandles(symbol, resolution, from, to);
        };
    }
}
