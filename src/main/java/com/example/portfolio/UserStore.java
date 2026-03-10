package com.example.portfolio;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class UserStore {
    private final AtomicInteger nextId = new AtomicInteger(1);
    private final Map<Integer, User> users = new ConcurrentHashMap<>();
    private final Map<String, Integer> emailToId = new ConcurrentHashMap<>();
    private final Map<Integer, String> emails = new ConcurrentHashMap<>();
    private final Map<Integer, String> passwordHashes = new ConcurrentHashMap<>();
    private final Map<Integer, String> salts = new ConcurrentHashMap<>();
    private final Map<Integer, Portfolio> portfolios = new ConcurrentHashMap<>();

    public synchronized User register(String username, String email, String password) {
        if (emailToId.containsKey(email.toLowerCase())) {
            return null;
        }
        int id = nextId.getAndIncrement();
        User user = new User(username, id);
        String salt = generateSalt();
        String hash = hashPassword(password, salt);

        users.put(id, user);
        emailToId.put(email.toLowerCase(), id);
        emails.put(id, email.toLowerCase());
        passwordHashes.put(id, hash);
        salts.put(id, salt);
        portfolios.put(id, new Portfolio(user));

        return user;
    }

    public User authenticate(String email, String password) {
        Integer id = emailToId.get(email.toLowerCase());
        if (id == null) return null;
        String salt = salts.get(id);
        String hash = hashPassword(password, salt);
        if (!hash.equals(passwordHashes.get(id))) return null;
        return users.get(id);
    }

    public User getUser(int id) { return users.get(id); }
    public Portfolio getPortfolio(int userId) { return portfolios.get(userId); }
    public String getEmail(int userId) { return emails.get(userId); }
    public boolean emailExists(String email) { return emailToId.containsKey(email.toLowerCase()); }

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
}
