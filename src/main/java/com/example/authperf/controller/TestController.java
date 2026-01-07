package com.example.authperf.controller;

import com.example.authperf.model.User;
import com.example.authperf.repository.UserRepository;
import com.example.authperf.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestController {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtil jwtUtil;

    // Helper to get raw token
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Missing or Invalid Authorization Header");
    }

    // 0. GENERATE TOKENS (For testing convenience)
    @GetMapping("/generate-tokens")
    public ResponseEntity<?> generateTokens(@RequestParam Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        String minimal = jwtUtil.generateMinimalToken(userId);
        String full = jwtUtil.generateFullToken(userId, user.getUsername(), user.getEmail(), user.getRole());

        return ResponseEntity.ok(Map.of("minimal", minimal, "full", full));
    }

    // 1. DATABASE STRATEGY
    @GetMapping("/db")
    public ResponseEntity<?> testDb(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        Long userId = jwtUtil.extractUserId(token);

        // HIT DB
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok("Hello " + user.getUsername() + " [DB]");
    }

    // 2. REDIS STRATEGY
    @GetMapping("/redis")
    public ResponseEntity<?> testRedis(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        Long userId = jwtUtil.extractUserId(token);
        String key = "user:" + userId;

        // HIT REDIS
        User user = (User) redisTemplate.opsForValue().get(key);

        if (user == null) {
            // Cache Miss -> Hit DB
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            // Write to Redis (TTL 10 mins)
            redisTemplate.opsForValue().set(key, user, 10, TimeUnit.MINUTES);
        }

        return ResponseEntity.ok("Hello " + user.getUsername() + " [REDIS]");
    }

    // 3. STATELESS STRATEGY
    @GetMapping("/stateless")
    public ResponseEntity<?> testStateless(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);

        // NO DB, NO REDIS. JUST PARSE.
        Claims claims = jwtUtil.extractAllClaims(token);
        String username = claims.get("username", String.class);
        String role = claims.get("role", String.class);

        return ResponseEntity.ok("Hello " + username + " [STATELESS] Role: " + role);
    }
}
