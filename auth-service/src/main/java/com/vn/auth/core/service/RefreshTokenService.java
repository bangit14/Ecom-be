package com.vn.auth.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j(topic = "Refresh-Token-Service")
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refresh_token:";

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private final StringRedisTemplate redisTemplate;
    private final ConcurrentHashMap<String, String> inMemoryFallback = new ConcurrentHashMap<>();

    public RefreshTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveOrUpdate(UUID userId, String refreshToken) {
        if (userId == null) return;
        String key = KEY_PREFIX + userId;
        try {
            redisTemplate.delete(key);
            redisTemplate.opsForValue().set(key, refreshToken, refreshExpiration, TimeUnit.SECONDS);
            log.debug("Saved refresh token in Redis for user: {}, expires in {}s", userId, refreshExpiration);
        } catch (Exception e) {
            log.warn("Redis unavailable, saving refresh token in memory fallback: {}", e.getMessage());
            inMemoryFallback.put(userId.toString(), refreshToken);
        }
    }

    public boolean isValid(UUID userId, String refreshToken) {
        if (userId == null) return false;
        String key = KEY_PREFIX + userId;
        try {
            String storedToken = redisTemplate.opsForValue().get(key);
            if (storedToken != null) {
                return storedToken.equals(refreshToken);
            }
        } catch (Exception e) {
            log.warn("Redis unavailable, checking in memory fallback: {}", e.getMessage());
        }
        String memoryToken = inMemoryFallback.get(userId.toString());
        return memoryToken != null && memoryToken.equals(refreshToken);
    }

    public void revoke(UUID userId) {
        if (userId == null) return;
        String key = KEY_PREFIX + userId;
        inMemoryFallback.remove(userId.toString());
        try {
            Boolean deleted = redisTemplate.delete(key);
            log.debug("Revoked refresh token for user: {}, deleted: {}", userId, deleted);
        } catch (Exception e) {
            log.warn("Redis unavailable when revoking token: {}", e.getMessage());
        }
    }

    public void revokeAll(UUID userId) {
        if (userId == null) return;
        revoke(userId);
        try {
            String pattern = KEY_PREFIX + userId + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Revoked all refresh tokens for user: {}", userId);
            }
        } catch (Exception e) {
            log.warn("Redis unavailable when revoking all tokens: {}", e.getMessage());
        }
    }
}
