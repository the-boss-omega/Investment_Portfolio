package com.example.portfolio;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PortfolioDatabaseFunctions {

    private final JdbcTemplate jdbcTemplate;

    public PortfolioDatabaseFunctions(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void createSchemaIfMissing() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL UNIQUE,
                    password_hash VARCHAR(255) NOT NULL,
                    salt VARCHAR(255) NOT NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS accounts (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    user_id BIGINT NOT NULL,
                    provider VARCHAR(50) NOT NULL,
                    custom_provider_name VARCHAR(255),
                    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS positions (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    account_id BIGINT NOT NULL,
                    symbol VARCHAR(20) NOT NULL,
                    asset_type VARCHAR(20) NOT NULL,
                    exchange_name VARCHAR(100),
                    quantity INT NOT NULL,
                    original_price DOUBLE NOT NULL,
                    current_price DOUBLE NOT NULL,
                    CONSTRAINT fk_positions_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
                )
                """);
    }

    public void saveAccount(int userId, Account account) {
        String provider = account.getProvider().name();
        String customProvider = account.getProvider() == AccountProvider.OTHER ? account.getProviderName() : null;

        Long existingId = findAccountId(userId, provider, customProvider);
        long accountId;

        if (existingId == null) {
            jdbcTemplate.update(
                    "INSERT INTO accounts (user_id, provider, custom_provider_name) VALUES (?, ?, ?)",
                    userId, provider, customProvider
            );
            Long insertedId = findAccountId(userId, provider, customProvider);
            if (insertedId == null) {
                throw new IllegalStateException("Failed to resolve saved account id for user=" + userId + ", provider=" + provider);
            }
            accountId = insertedId;
        } else {
            accountId = existingId;
            jdbcTemplate.update("DELETE FROM positions WHERE account_id = ?", accountId);
        }

        for (Position p : account.getPositions()) {
            String assetType = p.getAsset().getClass().getSimpleName().toUpperCase();
            String exchange = null;
            if (p.getAsset() instanceof Crypto crypto) {
                exchange = crypto.getExchange();
            } else if (p.getAsset() instanceof Forex forex) {
                exchange = forex.getExchange();
            }

            jdbcTemplate.update(
                    """
                    INSERT INTO positions (account_id, symbol, asset_type, exchange_name, quantity, original_price, current_price)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                    accountId,
                    p.getSymbol(),
                    assetType,
                    exchange,
                    p.getQuantity(),
                    p.getOriginalPrice(),
                    p.getCurrentAssetPrice()
            );
        }
    }

    public List<Account> loadAccounts(int userId) {
        List<Account> accounts = new ArrayList<>();
        List<AccountRow> accountRows = jdbcTemplate.query(
                "SELECT id, provider, custom_provider_name FROM accounts WHERE user_id = ?",
                (rs, rowNum) -> new AccountRow(
                        rs.getLong("id"),
                        rs.getString("provider"),
                        rs.getString("custom_provider_name")
                ),
                userId
        );

        for (AccountRow row : accountRows) {
            AccountProvider provider = AccountProvider.valueOf(row.provider());
            Account account = provider == AccountProvider.OTHER
                    ? new Account(provider, row.customProviderName())
                    : new Account(provider);

            jdbcTemplate.query(
                    """
                    SELECT symbol, asset_type, exchange_name, quantity, original_price, current_price
                    FROM positions WHERE account_id = ?
                    """,
                    rs -> {
                        FinancialAsset asset = toAsset(
                                rs.getString("asset_type"),
                                rs.getString("symbol"),
                                (float) rs.getDouble("current_price"),
                                rs.getString("exchange_name")
                        );

                        Position position = new Position(
                                (float) rs.getDouble("original_price"),
                                rs.getInt("quantity"),
                                asset
                        );
                        account.addOrUpdatePosition(position);
                    },
                    row.id()
            );

            accounts.add(account);
        }

        return accounts;
    }

    private Long findAccountId(int userId, String provider, String customProvider) {
        List<Long> ids = jdbcTemplate.query(
                """
                SELECT id FROM accounts
                WHERE user_id = ?
                  AND provider = ?
                  AND ((custom_provider_name IS NULL AND ? IS NULL) OR custom_provider_name = ?)
                """,
                (rs, rowNum) -> rs.getLong("id"),
                userId,
                provider,
                customProvider,
                customProvider
        );

        return ids.isEmpty() ? null : ids.getFirst();
    }

    private FinancialAsset toAsset(String assetType, String symbol, float currentPrice, String exchange) {
        return switch (assetType.toUpperCase()) {
            case "STOCK" -> new Stock(symbol, currentPrice);
            case "ETF" -> new ETF(symbol, currentPrice);
            case "CRYPTO" -> new Crypto(symbol, currentPrice, exchange);
            case "FOREX" -> new Forex(symbol, currentPrice, exchange);
            default -> new Stock(symbol, currentPrice);
        };
    }

    private record AccountRow(long id, String provider, String customProviderName) {
    }
}

