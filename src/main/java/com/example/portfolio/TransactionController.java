package com.example.portfolio;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionStore transactionStore;

    public TransactionController(TransactionStore transactionStore) {
        this.transactionStore = transactionStore;
    }

    @GetMapping
    public Map<String, Object> getTransactions(HttpServletRequest request,
                                                @RequestParam(required = false) String account,
                                                @RequestParam(required = false) String assetType,
                                                @RequestParam(required = false) String sortBy,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "50") int size) {
        int userId = (int) request.getAttribute("userId");
        List<Transaction> all = transactionStore.getTransactions(userId);

        // Filter
        List<Transaction> filtered = all.stream()
                .filter(t -> account == null || t.getAccountProvider().equalsIgnoreCase(account))
                .filter(t -> assetType == null || t.getAssetType().equalsIgnoreCase(assetType))
                .collect(Collectors.toList());

        // Sort
        Comparator<Transaction> comparator = Comparator.comparing(Transaction::getTimestamp).reversed();
        if ("symbol".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Transaction::getSymbol);
        } else if ("amount".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing((Transaction t) -> Math.abs(t.getQuantity() * t.getPrice())).reversed();
        }
        filtered.sort(comparator);

        // Paginate
        int total = filtered.size();
        int fromIdx = Math.min(page * size, total);
        int toIdx = Math.min(fromIdx + size, total);
        List<Transaction> pageItems = filtered.subList(fromIdx, toIdx);

        List<Map<String, Object>> items = pageItems.stream().map(t -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("action", t.getAction());
            m.put("symbol", t.getSymbol());
            m.put("assetType", t.getAssetType());
            m.put("quantity", t.getQuantity());
            m.put("price", t.getPrice());
            m.put("total", Math.abs(t.getQuantity() * t.getPrice()));
            m.put("accountProvider", t.getAccountProvider());
            m.put("timestamp", t.getTimestamp().toString());
            return m;
        }).toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("transactions", items);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }
}
