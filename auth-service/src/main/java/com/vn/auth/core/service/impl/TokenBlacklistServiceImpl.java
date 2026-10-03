package com.vn.auth.core.service.impl;

import com.vn.auth.core.service.TokenBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j(topic = "Token-Blacklist")
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String KEY_PREFIX = "blacklist:jti:";

    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void blacklist(String jti, long ttlMillis) {
        if (ttlMillis <= 0) {
            log.debug("Token already expired, skip blacklisting jti={}", jti);
            return;
        }
        try {
            String key = KEY_PREFIX + jti;
            redisTemplate.opsForValue().set(key, "1", ttlMillis, TimeUnit.MILLISECONDS);
            log.debug("Blacklisted jti={}, ttl={}ms", jti, ttlMillis);
        } catch (Exception e) {
            log.warn("Could not blacklist jti in Redis (Redis may be offline): {}", e.getMessage());
        }
    }

    @Override
    public boolean isBlacklisted(String jti) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + jti));
        } catch (Exception e) {
            log.warn("Could not check blacklist in Redis: {}", e.getMessage());
            return false;
        }
    }
}
