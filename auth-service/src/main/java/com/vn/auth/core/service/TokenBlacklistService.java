package com.vn.auth.core.service;

public interface TokenBlacklistService {
    void blacklist(String jti, long ttlMillis);
    boolean isBlacklisted(String jti);
}
