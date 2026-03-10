package com.example.portfolio;

import java.util.*;

public final class Account {

    private final Map<String, Position> positions = new HashMap<>();
    private final AccountProvider provider;
    private final String customProviderName;

    public Account(AccountProvider provider) {
        this(provider, null);
    }

    public Account(AccountProvider provider, String customProviderName) {
        try {
            this.provider = Objects.requireNonNull(provider, "provider must not be null");
            this.customProviderName = customProviderName;
        } catch (Exception e) {
            Logger.log(LogLevel.CRITICAL, "Failed to create Account (provider/customProviderName)");
            throw e;
        }
    }

    public AccountProvider getProvider() {
        return provider;
    }

    public String getProviderName() {
        try {
            if (provider == AccountProvider.OTHER && customProviderName != null) {
                return customProviderName;
            }
            return provider.name();
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getProviderName failed");
            throw e;
        }
    }

    public Collection<Position> getPositions() {
        try {
            return Collections.unmodifiableCollection(positions.values());
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getPositions failed");
            throw e;
        }
    }

    public Position getPosition(String symbol) {
        try {
            if (symbol == null) {
                Logger.log(LogLevel.WARNING, "getPosition called with null symbol");
                return null;
            }
            return positions.get(symbol);
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getPosition failed for symbol=" + symbol);
            throw e;
        }
    }

    public void addOrUpdatePosition(Position p) {
        try {
            Objects.requireNonNull(p, "position must not be null");
            String symbol = p.getSymbol();
            if (symbol == null || symbol.isBlank()) {
                Logger.log(LogLevel.WARNING, "addOrUpdatePosition called with empty symbol: " + p);
            }
            positions.put(symbol, p);
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "addOrUpdatePosition failed");
            throw e;
        }
    }

    public boolean removePosition(String symbol) {
        try {
            if (symbol == null) {
                Logger.log(LogLevel.WARNING, "removePosition called with null symbol");
                return false;
            }
            return positions.remove(symbol) != null;
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "removePosition failed for symbol=" + symbol);
            throw e;
        }
    }

    public float getTotalValue() {
        try {
            float sum = 0f;
            for (Position p : positions.values()) {
                try {
                    sum += p.getTotalValue();
                } catch (Exception inner) {
                    Logger.log(LogLevel.ERROR, "getTotalValue failed for position=" + safePos(p));
                    throw inner;
                }
            }
            return sum;
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getTotalValue failed");
            throw e;
        }
    }

    public float getTotalProfit() {
        try {
            float sum = 0f;
            for (Position p : positions.values()) {
                try {
                    sum += p.getProfit();
                } catch (Exception inner) {
                    Logger.log(LogLevel.ERROR, "getTotalProfit failed for position=" + safePos(p));
                    throw inner;
                }
            }
            return sum;
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getTotalProfit failed");
            throw e;
        }
    }

    public float getTotalProfitPercentage() {
        try {
            float cost = 0f;
            float value = 0f;
            for (Position p : positions.values()) {
                try {
                    cost += p.getQuantity() * p.getOriginalPrice();
                    value += p.getTotalValue();
                } catch (Exception inner) {
                    Logger.log(LogLevel.ERROR, "getTotalProfitPercentage failed for position=" + safePos(p));
                    throw inner;
                }
            }
            if (cost == 0f) return 0f;
            return (value - cost) / cost * 100f;
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getTotalProfitPercentage failed");
            throw e;
        }
    }

    private static String safePos(Position p) {
        try {
            return String.valueOf(p);
        } catch (Exception e) {
            return "<position toString failed>";
        }
    }
}