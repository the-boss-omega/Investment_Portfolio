package com.example.portfolio;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class PortfolioController {

    private final UserStore userStore;
    private final PortfolioDatabaseFunctions databaseFunctions;

    public PortfolioController(UserStore userStore, PortfolioDatabaseFunctions databaseFunctions) {
        this.userStore = userStore;
        this.databaseFunctions = databaseFunctions;
    }

    @GetMapping("/portfolio")
    public Map<String, Object> getPortfolio(HttpServletRequest request) {
        int userId = (int) request.getAttribute("userId");
        Portfolio portfolio = userStore.getPortfolio(userId);
        User user = userStore.getUser(userId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("username", user.getUsername());
        result.put("userId", user.getId());
        result.put("email", userStore.getEmail(userId));
        result.put("totalValue", portfolio.getTotalValue());
        result.put("totalProfit", portfolio.getTotalProfit());
        result.put("totalProfitPct", portfolio.getTotalProfitPercentage());

        List<Map<String, Object>> accountList = new ArrayList<>();
        for (Account account : portfolio.getAccounts()) {
            accountList.add(serializeAccount(account));
        }
        result.put("accounts", accountList);
        return result;
    }

    @PostMapping("/db/save")
    public Map<String, Object> savePortfolioToDatabase(HttpServletRequest request) {
        int userId = (int) request.getAttribute("userId");
        Portfolio portfolio = userStore.getPortfolio(userId);
        int saved = 0;

        for (Account account : portfolio.getAccounts()) {
            databaseFunctions.saveAccount(userId, account);
            saved++;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "ok");
        result.put("userId", userId);
        result.put("savedAccounts", saved);
        return result;
    }

    @GetMapping("/db/accounts/{userId}")
    public List<Map<String, Object>> loadAccountsFromDatabase(@PathVariable int userId) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Account account : databaseFunctions.loadAccounts(userId)) {
            list.add(serializeAccount(account));
        }
        return list;
    }

    @GetMapping("/accounts")
    public List<Map<String, Object>> getAccounts(HttpServletRequest request) {
        int userId = (int) request.getAttribute("userId");
        Portfolio portfolio = userStore.getPortfolio(userId);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Account account : portfolio.getAccounts()) {
            list.add(serializeAccount(account));
        }
        return list;
    }

    @GetMapping("/accounts/{providerName}/positions")
    public List<Map<String, Object>> getPositionsByAccount(@PathVariable String providerName,
                                                            HttpServletRequest request) {
        int userId = (int) request.getAttribute("userId");
        Portfolio portfolio = userStore.getPortfolio(userId);

        for (Account account : portfolio.getAccounts()) {
            if (account.getProviderName().equalsIgnoreCase(providerName)) {
                return serializePositions(account);
            }
        }
        return List.of();
    }

    @PostMapping("/db/reload/{userId}")
    public Map<String, Object> reloadPortfolioFromDatabase(@PathVariable int userId) {
        Portfolio portfolio = userStore.getPortfolio(userId);
        portfolio.clearAccounts();
        List<Account> loadedAccounts = databaseFunctions.loadAccounts(userId);

        for (Account account : loadedAccounts) {
            portfolio.addAccount(account);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "ok");
        result.put("userId", userId);
        result.put("loadedAccounts", loadedAccounts.size());
        result.put("totalPositions", loadedAccounts.stream()
                .mapToInt(a -> a.getPositions().size())
                .sum());
        return result;
    }

    @PostMapping("/db/update-position")
    public Map<String, Object> updatePosition(@RequestBody UpdatePositionRequest request,
                                              HttpServletRequest httpRequest) {
        int userId = (int) httpRequest.getAttribute("userId");
        Portfolio portfolio = userStore.getPortfolio(userId);

        for (Account account : portfolio.getAccounts()) {
            if (account.getProviderName().equalsIgnoreCase(request.provider())) {
                Position existingPos = account.getPosition(request.symbol());
                if (existingPos != null) {
                    FinancialAsset asset = createAsset(request.assetType(), request.symbol(),
                            request.currentPrice(), request.exchange());
                    Position updatedPos = new Position(request.originalPrice(),
                            request.quantity(), asset);
                    account.addOrUpdatePosition(updatedPos);

                    databaseFunctions.saveAccount(userId, account);

                    return Map.of(
                            "status", "ok",
                            "message", "Position updated",
                            "symbol", request.symbol(),
                            "newQuantity", request.quantity(),
                            "newPrice", request.currentPrice()
                    );
                }
            }
        }

        return Map.of("status", "error", "message", "Position not found");
    }

    @PostMapping("/db/add-position")
    public Map<String, Object> addPosition(@RequestBody AddPositionRequest request,
                                           HttpServletRequest httpRequest) {
        int userId = (int) httpRequest.getAttribute("userId");
        Portfolio portfolio = userStore.getPortfolio(userId);

        for (Account account : portfolio.getAccounts()) {
            if (account.getProviderName().equalsIgnoreCase(request.provider())) {
                FinancialAsset asset = createAsset(request.assetType(), request.symbol(),
                        request.currentPrice(), request.exchange());
                Position newPos = new Position(request.originalPrice(), request.quantity(), asset);
                account.addOrUpdatePosition(newPos);

                databaseFunctions.saveAccount(userId, account);

                return Map.of(
                        "status", "ok",
                        "message", "Position added",
                        "symbol", request.symbol(),
                        "quantity", request.quantity(),
                        "provider", request.provider()
                );
            }
        }

        return Map.of("status", "error", "message", "Account not found");
    }

    @DeleteMapping("/db/position/{provider}/{symbol}")
    public Map<String, Object> deletePosition(@PathVariable String provider,
                                              @PathVariable String symbol,
                                              HttpServletRequest httpRequest) {
        int userId = (int) httpRequest.getAttribute("userId");
        Portfolio portfolio = userStore.getPortfolio(userId);

        for (Account account : portfolio.getAccounts()) {
            if (account.getProviderName().equalsIgnoreCase(provider)) {
                boolean removed = account.removePosition(symbol);
                if (removed) {
                    databaseFunctions.saveAccount(userId, account);

                    return Map.of(
                            "status", "ok",
                            "message", "Position deleted",
                            "symbol", symbol,
                            "provider", provider
                    );
                }
            }
        }

        return Map.of("status", "error", "message", "Position not found");
    }

    private FinancialAsset createAsset(String assetType, String symbol, float currentPrice, String exchange) {
        return switch (assetType.toUpperCase()) {
            case "STOCK" -> new Stock(symbol, currentPrice);
            case "ETF" -> new ETF(symbol, currentPrice);
            case "CRYPTO" -> new Crypto(symbol, currentPrice, exchange != null ? exchange : "");
            case "FOREX" -> new Forex(symbol, currentPrice, exchange != null ? exchange : "");
            default -> new Stock(symbol, currentPrice);
        };
    }

    static Map<String, Object> serializeAccount(Account account) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("provider", account.getProviderName());
        map.put("totalValue", account.getTotalValue());
        map.put("totalProfit", account.getTotalProfit());
        map.put("totalProfitPct", account.getTotalProfitPercentage());
        map.put("positionCount", account.getPositions().size());
        map.put("positions", serializePositions(account));
        return map;
    }

    static List<Map<String, Object>> serializePositions(Account account) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Position p : account.getPositions()) {
            Map<String, Object> pos = new LinkedHashMap<>();
            pos.put("symbol", p.getSymbol());
            pos.put("assetType", p.getAsset().getClass().getSimpleName());
            pos.put("quantity", p.getQuantity());
            pos.put("originalPrice", p.getOriginalPrice());
            pos.put("currentPrice", p.getCurrentAssetPrice());
            pos.put("totalValue", p.getTotalValue());
            pos.put("costBasis", p.getCostBasis());
            pos.put("profit", p.getProfit());
            pos.put("profitPct", p.getProfitPercentage());
            pos.put("direction", p.isLong() ? "LONG" : p.isShort() ? "SHORT" : "FLAT");
            if (p.getAsset() instanceof Crypto c) {
                pos.put("exchange", c.getExchange());
            } else if (p.getAsset() instanceof Forex f) {
                pos.put("exchange", f.getExchange());
            }
            list.add(pos);
        }
        return list;
    }
}