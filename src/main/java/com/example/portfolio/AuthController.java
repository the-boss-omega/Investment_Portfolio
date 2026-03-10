package com.example.portfolio;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserStore userStore;
    private final TokenService tokenService;

    public AuthController(UserStore userStore, TokenService tokenService) {
        this.userStore = userStore;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");

        if (username == null || username.isBlank() || email == null || email.isBlank()
                || password == null || password.length() < 4) {
            return ResponseEntity.badRequest().body(Map.of("error",
                    "Username, email, and password (min 4 chars) are required"));
        }

        if (userStore.emailExists(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }

        User user = userStore.register(username, email, password);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Registration failed"));
        }

        String token = tokenService.createToken(user.getId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("email", email);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
        }

        User user = userStore.authenticate(email, password);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }

        String token = tokenService.createToken(user.getId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("email", userStore.getEmail(user.getId()));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        Integer userId = tokenService.getUserIdFromToken(authHeader.substring(7));
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
        User user = userStore.getUser(userId);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not found"));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("email", userStore.getEmail(userId));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            tokenService.invalidateToken(authHeader.substring(7));
        }
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }
}
