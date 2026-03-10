package com.example.portfolio;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class PortfolioController {

    private final UserStore userStore;

    public PortfolioController(UserStore userStore) {
        this.userStore = userStore;
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