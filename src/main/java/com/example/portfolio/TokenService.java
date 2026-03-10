package com.example.portfolio;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenService {
    private final Map<String, Integer> tokenToUserId = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public String createToken(int userId) {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);
        tokenToUserId.put(token, userId);
        return token;
    }

    public Integer getUserIdFromToken(String token) {
        if (token == null) return null;
        return tokenToUserId.get(token);
    }

    public void invalidateToken(String token) {
        if (token != null) tokenToUserId.remove(token);
    }
}
