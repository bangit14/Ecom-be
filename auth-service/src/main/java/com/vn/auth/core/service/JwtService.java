package com.vn.auth.core.service;

import com.vn.auth.core.enums.TokenType;
import com.vn.auth.core.security.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String generateAccessToken(CustomUserDetails userDetails);

    String generateRefreshToken(CustomUserDetails userDetails);

    CustomUserDetails extractUserDetailsFromToken(String token);

    String extractSubject(String token, TokenType tokenType);

    String extractUsername(String token, TokenType tokenType);

    String extractJti(String token);

    long getRemainingExpirationTime(String token);

    boolean isTokenValid(String token, TokenType tokenType, UserDetails userDetails);

    boolean isTokenExpired(String token, TokenType tokenType);
}
