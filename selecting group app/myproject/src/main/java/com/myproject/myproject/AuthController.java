package com.myproject.myproject;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final Map<String, String> users = new ConcurrentHashMap<>();

    public AuthController() {
        users.put("admin", hash("password"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session) {
        String cleanUsername = username.trim().toLowerCase(Locale.ROOT);
        String storedHash = users.get(cleanUsername);
        if (storedHash == null || !storedHash.equals(hash(password))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid username or password."));
        }

        session.setAttribute("username", cleanUsername);
        return ResponseEntity.ok(Map.of("username", cleanUsername));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session) {
        String cleanUsername = username.trim().toLowerCase(Locale.ROOT);
        if (cleanUsername.length() < 3) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Username must be at least 3 characters."));
        }
        if (password.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Password must be at least 6 characters."));
        }
        if (users.putIfAbsent(cleanUsername, hash(password)) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Username is already taken."));
        }

        session.setAttribute("username", cleanUsername);
        return ResponseEntity.ok(Map.of("username", cleanUsername));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(HttpSession session) {
        Object username = session.getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Not logged in."));
        }
        return ResponseEntity.ok(Map.of("username", username.toString()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logged out."));
    }

    private String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable.", exception);
        }
    }
}
