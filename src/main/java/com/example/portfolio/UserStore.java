package com.example.portfolio;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserStore {
    private final JdbcTemplate jdbcTemplate;
    private final PortfolioDatabaseFunctions databaseFunctions;
    private final Map<Integer, Portfolio> portfolios = new ConcurrentHashMap<>();

    public UserStore(JdbcTemplate jdbcTemplate, PortfolioDatabaseFunctions databaseFunctions) {
        this.jdbcTemplate = jdbcTemplate;
        this.databaseFunctions = databaseFunctions;
    }

    public synchronized User register(String username, String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null || emailExists(normalizedEmail)) {
            return null;
        }

        String salt = generateSalt();
        String hash = hashPassword(password, salt);

        jdbcTemplate.update(
                "INSERT INTO users (username, email, password_hash, salt) VALUES (?, ?, ?, ?)",

                username,
                normalizedEmail,
                hash,
                salt
        );

        User user = findUserByEmail(normalizedEmail);
        if (user == null) {
            throw new IllegalStateException("User insert succeeded but lookup failed for email=" + normalizedEmail);
        }

        portfolios.put(user.getId(), new Portfolio(user));
        return user;
    }

    public User authenticate(String email, String password) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return null;
        }

        AuthRow row = findAuthRowByEmail(normalizedEmail);
        if (row == null) {
            return null;
        }

        String hash = hashPassword(password, row.salt());
        if (!hash.equals(row.passwordHash())) {
            return null;
        }

        return new User(row.username(), Math.toIntExact(row.id()));
    }

    public User getUser(int id) {
        List<User> users = jdbcTemplate.query(
                "SELECT id, username FROM users WHERE id = ?",
                (rs, rowNum) -> new User(rs.getString("username"), Math.toIntExact(rs.getLong("id"))),
                id
        );
        return users.isEmpty() ? null : users.get(0);
    }

    public User findUserByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return null;
        }

        List<User> users = jdbcTemplate.query(
                "SELECT id, username FROM users WHERE email = ?",
                (rs, rowNum) -> new User(rs.getString("username"), Math.toIntExact(rs.getLong("id"))),
                normalizedEmail
        );
        return users.isEmpty() ? null : users.get(0);
    }

    public Portfolio getPortfolio(int userId) {
        return portfolios.computeIfAbsent(userId, id -> {
            User user = getUser(id);
            if (user == null) {
                return null;
            }

            Portfolio portfolio = new Portfolio(user);
            for (Account account : databaseFunctions.loadAccounts(id)) {
                portfolio.addAccount(account);
            }
            return portfolio;
        });
    }

    public String getEmail(int userId) {
        List<String> emails = jdbcTemplate.query(
                "SELECT email FROM users WHERE id = ?",
                (rs, rowNum) -> rs.getString("email"),
                userId
        );
        return emails.isEmpty() ? null : emails.get(0);
    }

    public boolean emailExists(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return false;
        }

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?",
                Integer.class,
                normalizedEmail
        );
        return count != null && count > 0;
    }

    private AuthRow findAuthRowByEmail(String email) {
        List<AuthRow> rows = jdbcTemplate.query(
                "SELECT id, username, password_hash, salt FROM users WHERE email = ?",
                (rs, rowNum) -> new AuthRow(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("salt")
                ),
                email
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.toLowerCase();
    }

    private static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hash = md.digest(password.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return HexFormat.of().formatHex(salt);
    }

    private record AuthRow(long id, String username, String passwordHash, String salt) {
    }
}
