package com.example.portfolio;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final UserStore userStore;

    public AccountController(UserStore userStore) {
        this.userStore = userStore;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createAccount(@RequestBody Map<String, String> body,
                                                              HttpServletRequest request) {
        int userId = (int) request.getAttribute("userId");
        Portfolio portfolio = userStore.getPortfolio(userId);

        String providerStr = body.get("provider");
        String customName = body.get("customName");

        if (providerStr == null || providerStr.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Provider is required"));
        }

        AccountProvider provider;
        try {
            provider = AccountProvider.valueOf(providerStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid provider. Use LEUMI, HAPOALIM, or OTHER"));
        }

        Account account;
        if (provider == AccountProvider.OTHER) {
            if (customName == null || customName.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Custom name required for OTHER provider"));
            }
            account = new Account(provider, customName);
        } else {
            account = new Account(provider);
        }

        portfolio.addAccount(account);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("provider", account.getProviderName());
        result.put("message", "Account created");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{providerName}")
    public ResponseEntity<Map<String, Object>> deleteAccount(@PathVariable String providerName,
                                                              HttpServletRequest request) {
        int userId = (int) request.getAttribute("userId");
        Portfolio portfolio = userStore.getPortfolio(userId);

        Account toRemove = null;
        for (Account a : portfolio.getAccounts()) {
            if (a.getProviderName().equalsIgnoreCase(providerName)) {
                toRemove = a;
                break;
            }
        }

        if (toRemove == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Account not found"));
        }

        portfolio.removeAccount(toRemove);
        return ResponseEntity.ok(Map.of("message", "Account deleted"));
    }
}
