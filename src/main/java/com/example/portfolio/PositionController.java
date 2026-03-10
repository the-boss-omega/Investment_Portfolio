package com.example.portfolio;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/positions")
public class PositionController {

    private final UserStore userStore;
    private final TransactionStore transactionStore;

    public PositionController(UserStore userStore, TransactionStore transactionStore) {
        this.userStore = userStore;
        this.transactionStore = transactionStore;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addPosition(@RequestBody Map<String, Object> body,
                                                            HttpServletRequest request) {
        int userId = (int) request.getAttribute("userId");
        Portfolio portfolio = userStore.getPortfolio(userId);

        String accountProvider = (String) body.get("accountProvider");
        String symbol = (String) body.get("symbol");
        String assetType = (String) body.get("assetType");
        String exchange = (String) body.get("exchange");

        Number quantityNum = (Number) body.get("quantity");
        Number priceNum = (Number) body.get("price");

        if (accountProvider == null || symbol == null || assetType == null
                || quantityNum == null || priceNum == null) {
            return ResponseEntity.badRequest().body(Map.of("error",
                    "accountProvider, symbol, assetType, quantity, and price are required"));
        }

        int quantity = quantityNum.intValue();
        float price = priceNum.floatValue();

        if (quantity == 0 || price <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Quantity must be non-zero and price must be positive"));
        }

        // Find account
        Account targetAccount = null;
        for (Account a : portfolio.getAccounts()) {
            if (a.getProviderName().equalsIgnoreCase(accountProvider)) {
                targetAccount = a;
                break;
            }
        }

        if (targetAccount == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Account not found: " + accountProvider));
        }

        // Create asset
        FinancialAsset asset = switch (assetType.toUpperCase()) {
            case "STOCK" -> new Stock(symbol, price);
            case "ETF" -> new ETF(symbol, price);
            case "CRYPTO" -> new Crypto(symbol, price, exchange != null ? exchange : "UNKNOWN");
            case "FOREX" -> new Forex(symbol, price, exchange != null ? exchange : "UNKNOWN");
            default -> null;
        };

        if (asset == null) {
            return ResponseEntity.badRequest().body(Map.of("error",
                    "Invalid assetType. Use STOCK, ETF, CRYPTO, or FOREX"));
        }

        Position position = new Position(price, quantity, asset);
        targetAccount.addOrUpdatePosition(position);

        // Record transaction
        transactionStore.record(userId, accountProvider, symbol, assetType, "BUY", quantity, price);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Position added");
        result.put("symbol", symbol);
        result.put("quantity", quantity);
        result.put("price", price);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{accountProvider}/{symbol}")
    public ResponseEntity<Map<String, Object>> removePosition(@PathVariable String accountProvider,
                                                               @PathVariable String symbol,
                                                               HttpServletRequest request) {
        int userId = (int) request.getAttribute("userId");
        Portfolio portfolio = userStore.getPortfolio(userId);

        Account targetAccount = null;
        for (Account a : portfolio.getAccounts()) {
            if (a.getProviderName().equalsIgnoreCase(accountProvider)) {
                targetAccount = a;
                break;
            }
        }

        if (targetAccount == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Account not found"));
        }

        Position existing = targetAccount.getPosition(symbol.toUpperCase());
        if (existing == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Position not found: " + symbol));
        }

        float sellPrice = existing.getCurrentAssetPrice();
        int quantity = existing.getQuantity();
        String assetType = existing.getAsset().getClass().getSimpleName();

        targetAccount.removePosition(symbol.toUpperCase());

        // Record sell transaction
        transactionStore.record(userId, accountProvider, symbol.toUpperCase(), assetType, "SELL", quantity, sellPrice);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message", "Position sold");
        result.put("symbol", symbol.toUpperCase());
        result.put("soldAt", sellPrice);
        result.put("quantity", quantity);
        return ResponseEntity.ok(result);
    }
}
