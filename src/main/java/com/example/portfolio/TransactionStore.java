package com.example.portfolio;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TransactionStore {
    private final AtomicInteger nextId = new AtomicInteger(1);
    private final Map<Integer, List<Transaction>> userTransactions = new ConcurrentHashMap<>();

    public Transaction record(int userId, String accountProvider, String symbol,
                              String assetType, String action, int quantity, float price) {
        Transaction t = new Transaction(
                nextId.getAndIncrement(), userId, accountProvider, symbol,
                assetType, action, quantity, price, LocalDateTime.now()
        );
        userTransactions.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(t);
        return t;
    }

    public List<Transaction> getTransactions(int userId) {
        return userTransactions.getOrDefault(userId, List.of());
    }

    public void addAll(int userId, List<Transaction> transactions) {
        userTransactions.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).addAll(transactions);
    }
}
