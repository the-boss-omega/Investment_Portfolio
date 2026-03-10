package com.example.portfolio;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class Portfolio {
    private final Set<Account> accounts = new HashSet<>();
    private final User user;

    public Portfolio(User user) {
        try {
            this.user = Objects.requireNonNull(user, "user must not be null");
        } catch (Exception e) {
            Logger.log(LogLevel.CRITICAL, "Failed to create Portfolio: user is null");
            throw e;
        }
    }

    public User getUser() {
        return user;
    }

    public int getUserId() {
        try {
            return user.getId();
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getUserId failed");
            throw e;
        }
    }

    public String getUsername() {
        try {
            return user.getUsername();
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getUsername failed");
            throw e;
        }
    }

    public void addAccount(Account a) {
        try {
            accounts.add(Objects.requireNonNull(a, "account must not be null"));
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "addAccount failed");
            throw e;
        }
    }

    public boolean removeAccount(Account a) {
        try {
            if (a == null) {
                Logger.log(LogLevel.WARNING, "removeAccount called with null account");
                return false;
            }
            return accounts.remove(a);
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "removeAccount failed");
            throw e;
        }
    }

    public void clearAccounts() {
        try {
            accounts.clear();
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "clearAccounts failed");
            throw e;
        }
    }

    public Set<Account> getAccounts() {
        try {
            return Set.copyOf(accounts);
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getAccounts failed");
            throw e;
        }
    }

    public Set<Account> findAccountByProvider(AccountProvider provider) {
        try {
            if (provider == null) {
                Logger.log(LogLevel.WARNING, "findAccountByProvider called with null provider");
                return Set.of();
            }
            return accounts.stream()
                    .filter(a -> provider.equals(a.getProvider()))
                    .collect(Collectors.toUnmodifiableSet());
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "findAccountByProvider failed for provider=" + provider);
            throw e;
        }
    }

    public float getTotalValue() {
        try {
            float sum = 0f;
            for (Account a : accounts) {
                try {
                    sum += a.getTotalValue();
                } catch (Exception inner) {
                    Logger.log(LogLevel.ERROR, "getTotalValue failed for account=" + safeAcc(a));
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
            for (Account a : accounts) {
                try {
                    sum += a.getTotalProfit();
                } catch (Exception inner) {
                    Logger.log(LogLevel.ERROR, "getTotalProfit failed for account=" + safeAcc(a));
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
            float totalValue = 0f;
            float totalProfit = 0f;

            for (Account a : accounts) {
                try {
                    totalValue += a.getTotalValue();
                    totalProfit += a.getTotalProfit();
                } catch (Exception inner) {
                    Logger.log(LogLevel.ERROR, "getTotalProfitPercentage failed for account=" + safeAcc(a));
                    throw inner;
                }
            }

            float totalCost = totalValue - totalProfit;
            if (totalCost == 0f) return 0f;

            return (totalProfit / totalCost) * 100f;

        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "getTotalProfitPercentage failed");
            throw e;
        }
    }

    private static String safeAcc(Account a) {
        try {
            return String.valueOf(a);
        } catch (Exception e) {
            return "<account toString failed>";
        }
    }
}