package com.example.portfolio;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SampleDataConfig {

    private final UserStore userStore;
    private final TransactionStore transactionStore;
    private final PortfolioDatabaseFunctions databaseFunctions;

    public SampleDataConfig(UserStore userStore,
                            TransactionStore transactionStore,
                            PortfolioDatabaseFunctions databaseFunctions) {
        this.userStore = userStore;
        this.transactionStore = transactionStore;
        this.databaseFunctions = databaseFunctions;
    }

    @PostConstruct
    public void seedDemoUser() {
        seedMainDemoUser();
        seedDbCheckUser();
    }

    private void seedMainDemoUser() {
        User user = getOrCreateUser("Noam", "demo@demo.com", "demo123");
        int userId = user.getId();
        Portfolio portfolio = userStore.getPortfolio(userId);

        if (portfolio.getAccounts().isEmpty()) {
            Account leumi = new Account(AccountProvider.LEUMI);
            leumi.addOrUpdatePosition(new Position(150.00f, 25, new Stock("AAPL", 178.72f)));
            leumi.addOrUpdatePosition(new Position(310.00f, 10, new Stock("MSFT", 372.45f)));
            leumi.addOrUpdatePosition(new Position(120.50f, 30, new Stock("GOOG", 141.80f)));
            leumi.addOrUpdatePosition(new Position(420.00f, 8, new ETF("SPY", 502.30f)));
            leumi.addOrUpdatePosition(new Position(350.00f, 12, new ETF("QQQ", 438.15f)));
            addAccountAndPersist(userId, portfolio, leumi);

            Account hapoalim = new Account(AccountProvider.HAPOALIM);
            hapoalim.addOrUpdatePosition(new Position(245.00f, 15, new Stock("TSLA", 212.50f)));
            hapoalim.addOrUpdatePosition(new Position(130.00f, 20, new Stock("AMZN", 178.25f)));
            hapoalim.addOrUpdatePosition(new Position(50.00f, 40, new Stock("INTC", 32.60f)));
            addAccountAndPersist(userId, portfolio, hapoalim);

            Account crypto = new Account(AccountProvider.OTHER, "Binance");
            crypto.addOrUpdatePosition(new Position(42000.00f, 1, new Crypto("BTCUSDT", 67350.00f, "BINANCE")));
            crypto.addOrUpdatePosition(new Position(2800.00f, 5, new Crypto("ETHUSDT", 3420.00f, "BINANCE")));
            crypto.addOrUpdatePosition(new Position(0.58f, 5000, new Crypto("XRPUSDT", 0.62f, "BINANCE")));
            addAccountAndPersist(userId, portfolio, crypto);

            Account forex = new Account(AccountProvider.OTHER, "OANDA");
            forex.addOrUpdatePosition(new Position(1.0850f, 10000, new Forex("EURUSD", 1.0920f, "OANDA")));
            forex.addOrUpdatePosition(new Position(148.50f, -5000, new Forex("USDJPY", 150.20f, "OANDA")));
            addAccountAndPersist(userId, portfolio, forex);
        }

        if (transactionStore.getTransactions(userId).isEmpty()) {
            LocalDateTime base = LocalDateTime.now().minusDays(30);
            transactionStore.addAll(userId, List.of(
                    new Transaction(0, userId, "LEUMI", "AAPL", "Stock", "BUY", 25, 150.00f, base.plusDays(1)),
                    new Transaction(0, userId, "LEUMI", "MSFT", "Stock", "BUY", 10, 310.00f, base.plusDays(2)),
                    new Transaction(0, userId, "LEUMI", "GOOG", "Stock", "BUY", 30, 120.50f, base.plusDays(3)),
                    new Transaction(0, userId, "LEUMI", "SPY", "ETF", "BUY", 8, 420.00f, base.plusDays(4)),
                    new Transaction(0, userId, "LEUMI", "QQQ", "ETF", "BUY", 12, 350.00f, base.plusDays(5)),
                    new Transaction(0, userId, "HAPOALIM", "TSLA", "Stock", "BUY", 15, 245.00f, base.plusDays(6)),
                    new Transaction(0, userId, "HAPOALIM", "AMZN", "Stock", "BUY", 20, 130.00f, base.plusDays(7)),
                    new Transaction(0, userId, "HAPOALIM", "INTC", "Stock", "BUY", 40, 50.00f, base.plusDays(8)),
                    new Transaction(0, userId, "Binance", "BTCUSDT", "Crypto", "BUY", 1, 42000.00f, base.plusDays(9)),
                    new Transaction(0, userId, "Binance", "ETHUSDT", "Crypto", "BUY", 5, 2800.00f, base.plusDays(10)),
                    new Transaction(0, userId, "Binance", "XRPUSDT", "Crypto", "BUY", 5000, 0.58f, base.plusDays(11)),
                    new Transaction(0, userId, "OANDA", "EURUSD", "Forex", "BUY", 10000, 1.0850f, base.plusDays(12)),
                    new Transaction(0, userId, "OANDA", "USDJPY", "Forex", "BUY", -5000, 148.50f, base.plusDays(13))
            ));
        }
    }

    private void seedDbCheckUser() {
        User user = getOrCreateUser("DB Check", "dbcheck@demo.com", "dbcheck123");
        int userId = user.getId();
        Portfolio portfolio = userStore.getPortfolio(userId);

        if (portfolio.getAccounts().isEmpty()) {
            Account dbAccount = new Account(AccountProvider.LEUMI);
            dbAccount.addOrUpdatePosition(new Position(475.00f, 15, new Stock("NVDA", 520.00f)));
            addAccountAndPersist(userId, portfolio, dbAccount);
        }

        if (transactionStore.getTransactions(userId).isEmpty()) {
            transactionStore.addAll(userId, List.of(
                    new Transaction(0, userId, "LEUMI", "NVDA", "Stock", "BUY", 15, 475.00f,
                            LocalDateTime.now().minusDays(1))
            ));
        }
    }

    private User getOrCreateUser(String username, String email, String password) {
        User existing = userStore.findUserByEmail(email);
        return existing != null ? existing : userStore.register(username, email, password);
    }

    private void addAccountAndPersist(int userId, Portfolio portfolio, Account account) {
        portfolio.addAccount(account);
        databaseFunctions.saveAccount(userId, account);
    }
}
