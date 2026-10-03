package com.vn.auth.core.service.impl;

import com.vn.auth.core.enums.TokenType;
import com.vn.auth.core.exception.AppException;
import com.vn.auth.core.exception.ErrorCode;
import com.vn.auth.core.security.CustomUserDetails;
import com.vn.auth.core.service.JwtService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.access-secret}")
    private String accessSecret;

    @Value("${jwt.refresh-secret}")
    private String refreshSecret;

    @Value("${jwt.access-expiration}")
    private Long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Value("${jwt.issuer:ecommerce-auth-service}")
    private String issuer;

    private static final String USER_ID_CLAIM = "userId";
    private static final String STORE_ID_CLAIM = "storeId";
    private static final String ROLE_CLAIM = "role";
    private static final String TYPE_CLAIM = "type";
    private static final String FULL_NAME_CLAIM = "fullName";
    private static final String EMAIL_CLAIM = "email";
    private static final String ROLES_CLAIM = "roles";
    private static final String TOKEN_TYPE_CLAIM = "typ";

    private SecretKey getAccessKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
    }

    private SecretKey getRefreshKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
    }

    @Override
    public String generateAccessToken(CustomUserDetails userDetails) {
        log.debug("Generating access token for userId: {}", userDetails.getId());

        Map<String, Object> claims = new HashMap<>();
        claims.put(USER_ID_CLAIM, userDetails.getId() != null ? userDetails.getId().toString() : null);
        claims.put(STORE_ID_CLAIM, userDetails.getStoreId() != null ? userDetails.getStoreId().toString() : null);
        claims.put(ROLE_CLAIM, userDetails.getRole());
        claims.put(TYPE_CLAIM, userDetails.getType());
        claims.put(FULL_NAME_CLAIM, userDetails.getFullName());
        claims.put(EMAIL_CLAIM, userDetails.getEmail());
        claims.put(ROLES_CLAIM, userDetails.getRoles());
        claims.put(TOKEN_TYPE_CLAIM, "access");

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userDetails.getUsername())
                .claims(claims)
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(getAccessKey(), Jwts.SIG.HS512)
                .compact();
    }

    @Override
    public String generateRefreshToken(CustomUserDetails userDetails) {
        return generateRefreshToken(userDetails.getId() != null ? userDetails.getId().toString() : userDetails.getUsername());
    }

    private String generateRefreshToken(String subject) {
        log.debug("Generating refresh token for user: {}", subject);
        long expirationMs = refreshExpiration > 10000000L ? refreshExpiration : refreshExpiration * 1000L;

        return Jwts.builder()
                .subject(subject)
                .claim(TOKEN_TYPE_CLAIM, "refresh")
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getRefreshKey(), Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public CustomUserDetails extractUserDetailsFromToken(String token) {
        Claims claims = extractAllClaims(token, TokenType.ACCESS_TOKEN);

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get(ROLES_CLAIM, List.class);

        Object userIdObj = claims.get(USER_ID_CLAIM);
        UUID userId = null;
        if (userIdObj != null) {
            try {
                userId = UUID.fromString(String.valueOf(userIdObj));
            } catch (Exception e) {
                log.warn("Failed to parse userId as UUID: {}", userIdObj);
            }
        }

        Object storeIdObj = claims.get(STORE_ID_CLAIM);
        UUID storeId = null;
        if (storeIdObj != null) {
            try {
                storeId = UUID.fromString(String.valueOf(storeIdObj));
            } catch (Exception ignored) {}
        }

        String role = claims.get(ROLE_CLAIM, String.class);
        String type = claims.get(TYPE_CLAIM, String.class);
        String fullName = claims.get(FULL_NAME_CLAIM, String.class);
        String email = claims.get(EMAIL_CLAIM, String.class);

        return CustomUserDetails.builder()
                .id(userId)
                .storeId(storeId)
                .role(role)
                .type(type)
                .username(claims.getSubject())
                .email(email)
                .fullName(fullName)
                .roles(roles != null ? roles : (role != null ? List.of(role) : List.of()))
                .active(true)
                .build();
    }

    @Override
    public String extractUsername(String token, TokenType tokenType) {
        return extractClaim(token, tokenType, Claims::getSubject);
    }

    @Override
    public String extractSubject(String token, TokenType tokenType) {
        return extractClaim(token, tokenType, Claims::getSubject);
    }

    @Override
    public String extractJti(String token) {
        return extractClaim(token, TokenType.ACCESS_TOKEN, Claims::getId);
    }

    @Override
    public long getRemainingExpirationTime(String token) {
        try {
            Date expiration = extractClaim(token, TokenType.ACCESS_TOKEN, Claims::getExpiration);
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(remaining, 0L);
        } catch (Exception e) {
            return 0L;
        }
    }

    @Override
    public boolean isTokenValid(String token, TokenType tokenType, UserDetails userDetails) {
        try {
            final String username = extractUsername(token, tokenType);
            final String tokenTypeClaim = extractClaim(token, tokenType,
                    claims -> claims.get(TOKEN_TYPE_CLAIM, String.class));

            return username.equals(userDetails.getUsername())
                    && tokenTypeClaim != null
                    && tokenTypeClaim.equals(mapTokenTypeClaim(tokenType))
                    && !isTokenExpired(token, tokenType);

        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isTokenExpired(String token, TokenType tokenType) {
        try {
            Date expiration = extractClaim(token, tokenType, Claims::getExpiration);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    private <T> T extractClaim(String token, TokenType tokenType, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, tokenType);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token, TokenType tokenType) {
        SecretKey key = getSigningKey(tokenType);

        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    private SecretKey getSigningKey(TokenType tokenType) {
        return switch (tokenType) {
            case ACCESS_TOKEN -> getAccessKey();
            case REFRESH_TOKEN -> getRefreshKey();
            default -> throw new AppException(ErrorCode.TOKEN_TYPE_NOT_SUPPORTED);
        };
    }

    private String mapTokenTypeClaim(TokenType tokenType) {
        return switch (tokenType) {
            case ACCESS_TOKEN -> "access";
            case REFRESH_TOKEN -> "refresh";
            default -> throw new AppException(ErrorCode.TOKEN_TYPE_NOT_SUPPORTED);
        };
    }
}
